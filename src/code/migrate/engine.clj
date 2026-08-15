(ns code.migrate.engine
  (:require [clojure.string :as str]
            [code.migrate.catalog :as catalog]
            [code.query :as query]
            [std.block.navigate :as nav])
  (:import (java.security MessageDigest)))

(defn sha256
  "returns the lowercase SHA-256 digest for a migration input"
  {:added "4.1"}
  [value]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256")
                        (.getBytes (str value) "UTF-8"))]
    (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))

(defn dependency-routes
  "compiles safe namespace replacement rules from a migration catalog"
  {:added "4.1"}
  [migration-catalog]
  (->> (:migration/rules migration-catalog)
       (filter #(and (= :dependency (:rule/kind %))
                     (#{:replace-namespace :replace-symbol}
                      (get-in % [:rule/rewrite :op]))))
       (map (fn [rule]
              [(:rule/match rule)
               {:target (or (get-in rule [:rule/rewrite :namespace])
                            (get-in rule [:rule/rewrite :symbol]))
                :rule/id (:rule/id rule)}]))
       (into {})))

(defn rewrite-ns-form
  "rewrites one Clojure namespace form using target-owned overrides"
  {:added "4.1"}
  [form overrides]
  (let [[head name & clauses] form
        clauses (remove #(and (seq? %)
                              (#{:refer-clojure :config} (first %)))
                        clauses)]
    (apply list head name
           (concat clauses
                   [(list :config {:override (vec overrides)})]))))

(defn rewrite-function-recur
  "rewrites function and loop recur into deterministic named calls"
  {:added "4.1"}
  ([form function-name]
   (rewrite-function-recur form function-name (atom 0)))
  ([form recur-target counter]
   (cond
     (seq? form)
     (let [head (first form)]
       (with-meta
         (cond
           (= 'recur head)
           (apply list recur-target
                  (map #(rewrite-function-recur % recur-target counter)
                       (rest form)))

           (and (= 'catch head) (= 3 (count form)))
           (apply list
                  (concat (map #(rewrite-function-recur % recur-target counter)
                               form)
                          [nil]))

           (= 'loop head)
           (let [bindings  (partition 2 (second form))
                 loop-name (symbol (str (name recur-target)
                                        "--loop-"
                                        (swap! counter inc)))
                 parameters (mapv first bindings)
                 initial    (map #(rewrite-function-recur (second %)
                                                          recur-target
                                                          counter)
                                 bindings)
                 body       (map #(rewrite-function-recur % loop-name counter)
                                 (nnext form))
                 definition (apply list loop-name parameters body)]
             (list 'letfn [definition]
                   (apply list loop-name initial)))

           :else
           (apply list
                  (map #(rewrite-function-recur % recur-target counter)
                       form)))
         (meta form)))

     (vector? form)
     (with-meta
       (mapv #(rewrite-function-recur % recur-target counter) form)
       (meta form))

     (map? form)
     (with-meta
       (into (empty form)
             (map (fn [[key value]]
                    [(rewrite-function-recur key recur-target counter)
                     (rewrite-function-recur value recur-target counter)])
                  form))
       (meta form))

     :else form)))

(defn rewrite-foundation-call
  "ports std.lib.foundation/call without extending the native foundation"
  {:added "4.1"}
  ([form]
   (rewrite-foundation-call form false))
  ([form threaded?]
   (if (and (not threaded?) (= 2 (count form)))
     (second form)
     (let [argument-count (if threaded?
                            (- (count form) 2)
                            (- (count form) 3))
           arguments  (mapv #(symbol (str "migration-argument-" %))
                            (range argument-count))
          parameters (into ['migration-object 'migration-function]
                           arguments)
          invocation (apply list
                            'migration-function
                            'migration-object
                            arguments)
          helper     (list 'fn
                           parameters
                           (list 'if
                                 (list 'nil? 'migration-function)
                                 'migration-object
                                 invocation))]
       (apply list helper (rest form))))))

(defn rewrite-target-forms
  "applies explicitly enabled structural adaptations for one target"
  {:added "4.1"}
  [root target applied]
  (let [rules (set (:target/rules target))]
    (query/modify
     root
     [seq?]
     (fn [location]
       (let [form (nav/value location)]
         (cond
           (and (contains? rules :hara/namespace-overrides)
                (= 'ns (first form)))
           (do (swap! applied conj :hara/namespace-overrides)
               (nav/replace location
                            (rewrite-ns-form form (:target/overrides target))))

           (and (contains? rules :foundation/defrecord-native-struct)
                (= 'defrecord (first form)))
           (do (swap! applied conj :foundation/defrecord-native-struct)
               (nav/replace location
                            (list 'defstruct
                                  (second form)
                                  (get-in target
                                          [:target/struct-fields (second form)]
                                          (nth form 2)))))

           (and (contains? rules :foundation/drop-host-printer)
                (= 'defmethod (first form))
                (= 'print-method (second form)))
           (do (swap! applied conj :foundation/drop-host-printer)
               (nav/delete location))

           (and (contains? rules :clojure/empty-catch-body)
                (= 'catch (first form))
                (= 3 (count form)))
           (do (swap! applied conj :clojure/empty-catch-body)
               (nav/replace location (apply list (concat form [nil]))))

           (and (contains? rules :clojure/named-recur)
                (= 'defn (first form))
                (some #(and (seq? %) (= 'recur (first %)))
                      (tree-seq coll? seq form)))
           (do (swap! applied conj :clojure/named-recur)
               (nav/replace location
                            (rewrite-function-recur form (second form))))

           (and (contains? rules :foundation/exact-port-call)
                (= 'h/call (first form)))
           (do (swap! applied conj :foundation/exact-port-call)
               (let [parent      (some-> location nav/up nav/value)
                     threaded?   (and (seq? parent)
                                      (= '-> (first parent)))]
                 (nav/replace location
                              (rewrite-foundation-call form threaded?))))

           :else
           location))))))

(defn rewrite-symbol
  "returns a replacement symbol and its rule id when a route applies"
  {:added "4.1"}
  [routes value]
  (if (symbol? value)
    (let [value-ns   (namespace value)
          value-name (name value)]
      (cond (contains? routes value)
            {:value (:target (get routes value))
             :rule/id (:rule/id (get routes value))}

            (and value-ns
                 (contains? routes (symbol value-ns)))
            (let [route (get routes (symbol value-ns))]
              {:value (symbol (str (:target route)) value-name)
               :rule/id (:rule/id route)})

            :else
            {:value value}))
    {:value value}))

(defn rewrite-dependencies
  "rewrites exact and qualified dependency symbols through code.query"
  {:added "4.1"}
  [root migration-catalog applied]
  (let [routes (dependency-routes migration-catalog)]
    (query/modify
     root
     [symbol?]
     (fn [location]
       (let [before (nav/value location)
             {:keys [value rule/id]} (rewrite-symbol routes before)]
         (if (= before value)
           location
           (do (swap! applied conj id)
               (nav/replace location value))))))))

(defn rewrite-refer-metadata
  "quotes symbolic :refer values required by native Hara test metadata"
  {:added "4.1"}
  [root applied]
  (loop [cursor root]
    (if-let [refer-location (nav/find-next-token cursor :refer)]
      (let [value-location (nav/right-token refer-location)
            value          (some-> value-location nav/value)
            metadata?      (= :map (some-> refer-location nav/up nav/tag))]
        (if (and metadata? (symbol? value))
          (let [updated (nav/replace value-location (list 'quote value))]
            (swap! applied conj :clojure/quoted-refer-metadata)
            (recur updated))
          (recur refer-location)))
      (-> cursor nav/root-string nav/parse-root))))

(defn host-symbol?
  "checks for a JVM interop symbol requiring an explicit adaptation"
  {:added "4.1"}
  [value]
  (boolean
   (and (symbol? value)
        (not (contains? #{"." ".." "..."} (name value)))
        (or (str/starts-with? (name value) ".")
            (re-find #"^[A-Z][A-Za-z0-9.]*[/.]" (str value))))))

(defn diagnostics
  "returns deterministic unresolved host-interop diagnostics"
  {:added "4.1"}
  [root]
  (->> (query/select root [symbol?])
       (map nav/value)
       (filter host-symbol?)
       distinct
       (sort-by str)
       (mapv (fn [value]
               {:type :migration/host-interop
                :symbol value
                :safety :manual}))))

(defn migrate-source
  "migrates one source string and returns reproducible evidence"
  {:added "4.1"}
  ([source migration-catalog]
   (migrate-source source migration-catalog nil))
  ([source migration-catalog target]
   (let [applied           (atom [])
        initial           (nav/parse-root source)
        target-result     (if target
                            (rewrite-target-forms initial target applied)
                            initial)
        dependency-result (rewrite-dependencies target-result
                                                migration-catalog applied)
        final             (rewrite-refer-metadata dependency-result applied)
        output            (nav/root-string final)]
     {:input source
      :source/checksum (sha256 source)
      :output/checksum (sha256 output)
      :output output
      :applied (vec (distinct @applied))
      :diagnostics (diagnostics final)
      :changed (not= source output)})))

(defn target-for-unit
  "returns the catalog target owning a normalized migration unit"
  {:added "4.1"}
  [unit migration-catalog]
  (let [path-key (if (= :test (:unit/kind unit))
                   :target/test-path
                   :target/source-path)]
    (first (filter #(= (:source/path unit) (get % path-key))
                   (:migration/targets migration-catalog)))))

(defn migrate-unit
  "migrates a normalized source or test unit"
  {:added "4.1"}
  [unit migration-catalog]
  (merge (select-keys unit
                      [:unit/kind :source/path :target/path])
         (migrate-source (:source/string unit)
                         migration-catalog
                         (target-for-unit unit migration-catalog))))
