(ns code.migrate.engine
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
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

(defn add-require-alias
  "adds a target alias to an existing namespace dependency"
  {:added "4.1"}
  [form source-namespace alias]
  (let [[head name & clauses] form
        clauses (map (fn [clause]
                       (if (and (seq? clause)
                                (= :require (first clause)))
                         (cons :require
                               (map (fn [entry]
                                      (if (and (vector? entry)
                                               (= source-namespace (first entry))
                                               (not (some #{:as} entry)))
                                        (into entry [:as alias])
                                        entry))
                                    (rest clause)))
                         clause))
                     clauses)]
    (apply list head name clauses)))

(defn rewrite-test-override-symbols
  "qualifies test calls hidden by native Foundation overrides"
  {:added "4.1"}
  [root target applied]
  (if (and (= :test (:unit/kind target))
           (contains? (set (:target/rules target))
                      :foundation/qualified-overrides))
    (let [overrides (set (:target/overrides target))
          alias     (:target/test-alias target)]
      (query/modify
       root
       [symbol?]
       (fn [location]
         (let [value (nav/value location)]
           (if (contains? overrides value)
             (do (swap! applied conj :foundation/qualified-overrides)
                 (nav/replace location
                              (symbol (name alias) (name value))))
             location)))))
    root))

(defn rewrite-single-arity-defn
  "normalizes one wrapped Clojure defn arity to native defn syntax"
  {:added "4.1"}
  [form]
  (apply list (concat (butlast form) (last form))))

(defn rewrite-single-arity-defns
  "normalizes only defn forms containing exactly one wrapped arity"
  {:added "4.1"}
  [root target applied]
  (if (contains? (set (:target/rules target))
                 :clojure/single-arity-defn)
    (query/modify
     root
     [seq?]
     (fn [location]
       (let [form    (nav/value location)
             arities (filter #(and (seq? %)
                                    (vector? (first %)))
                             (drop 2 form))]
         (if (and (= 'defn (first form))
                  (= 1 (count arities))
                  (= (last form) (first arities)))
           (do (swap! applied conj :clojure/single-arity-defn)
               (nav/replace location
                            (rewrite-single-arity-defn form)))
           location))))
    root))

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

(defn rewrite-struct-merge
  "associates map entries into a native struct without degrading its type"
  {:added "4.1"}
  [form]
  (let [[_ target source] form]
    (list 'reduce
          (list 'fn ['output 'entry]
                (list 'assoc
                      'output
                      (list 'key 'entry)
                      (list 'val 'entry)))
          target
          source)))

(defn rewrite-native-result
  "rewrites Foundation Wrapped calls to the native Result wrapper surface"
  {:added "4.1"}
  [form]
  (case (first form)
    h/wrapped
    (list 'result :success (second form))

    h/wrapped?
    (cons 'result? (rest form))

    form))

(defn rewrite-character-whitespace
  "rewrites JVM character whitespace checks to the native String surface"
  {:added "4.1"}
  [form]
  (list 'String/blank? (list 'str (second form))))

(defn rewrite-string-starts-with
  "rewrites JVM String.startsWith calls to the native String surface"
  {:added "4.1"}
  [form]
  (cons 'String/starts-with? (rest form)))

(defn rewrite-character-literal
  "rewrites ambiguous Clojure delimiter characters to portable native expressions"
  {:added "4.1"}
  [character]
  (case (int character)
    34 (list 'first (list 'pr-str ""))
    92 (list 'first
             (list 'pr-str
                   (list 'first (list 'pr-str ""))))
    character))

(defn rewrite-nil-set
  "rewrites a Clojure set containing nil to native runtime construction"
  {:added "4.1"}
  [values]
  (list 'set (vec (sort-by pr-str values))))

(defn rewrite-ratio-literal
  "rewrites a Clojure ratio literal to native numeric division"
  {:added "4.1"}
  [value]
  (list '/ (numerator value) (denominator value)))

(defn rewrite-token-checks
  "replaces Clojure host numeric categories with the native Hara taxonomy"
  {:added "4.1"}
  [form]
  (list 'def
        (second form)
        {:nil 'nil?
         :boolean 'boolean?
         :number 'number?
         :keyword 'keyword?
         :symbol 'symbol?
         :string 'string?
         :char 'char?}))

(defn escaped-character-string?
  "checks for a tab or quote escape amplified by the Foundation block reader"
  {:added "4.1"}
  [value]
  (and (string? value)
       (or (re-matches #"\\+t" value)
           (re-matches #"\\+\"" value))))

(defn rewrite-escaped-character-first
  "normalizes escaped one-character strings produced by the Foundation reader"
  {:added "4.1"}
  [form]
  (let [value (second form)]
    (cond (re-matches #"\\+t" value)
          \tab

          (re-matches #"\\+\"" value)
          (rewrite-character-literal \")

          :else form)))

(defn rewrite-nil-membership
  "preserves Clojure membership for a set whose nil member is significant"
  {:added "4.1"}
  [form]
  (list 'or
        (list 'nil? (nth form 2))
        (list 'has? (second form) (nth form 2))))

(defn rewrite-check-tag
  "rewrites map sequence destructuring to native reduce-kv traversal"
  {:added "4.1"}
  [form]
  (let [body '(reduce-kv
               (fn [out tag check]
                 (if out
                   out
                   (if (check input) tag nil)))
               nil
               checks)]
    (apply list (concat (butlast form) [body]))))

(defn rewrite-zero-arity-thread-steps
  "normalizes empty Clojure thread calls to native callable steps"
  {:added "4.1"}
  [form]
  (apply list
         (first form)
         (second form)
         (map (fn [step]
                (if (and (seq? step) (= 1 (count step)))
                  (first step)
                  step))
              (drop 2 form))))

(defn rewrite-iterator-first-pipeline
  "moves a lazy first-search pipeline onto a consumptive native iterator"
  {:added "4.1"}
  [form]
  (let [[_ iterate-form & steps] form
        [_ move seed]          iterate-form
        normalized             (map (fn [step]
                                      (if (and (seq? step)
                                               (= 1 (count step)))
                                        (first step)
                                        step))
                                    steps)]
    (list 'call
          seed
          (apply list
                 'comp
                 (concat (reverse normalized)
                         ['iter (list 'partial 'iterate move)])))))

(defn iterator-first-pipeline?
  "checks for the bounded lazy search shape requiring iterator consumption"
  {:added "4.1"}
  [form]
  (let [[thread iterate-form & steps] form
        step-head (fn [step]
                    (if (seq? step) (first step) step))]
    (and (= '->> thread)
         (seq? iterate-form)
         (= 'iterate (first iterate-form))
         (= 3 (count iterate-form))
         (= ['drop 'take-while 'filter 'first]
            (mapv step-head steps)))))

(defn rewrite-target-forms
  "applies explicitly enabled structural adaptations for one target"
  {:added "4.1"}
  [root target applied]
  (let [rules (set (:target/rules target))]
    (query/modify
     root
     [seq?]
     (fn [location]
       (let [original (nav/value location)
             form     (if (contains? rules :clojure/character-literal)
                        (walk/postwalk
                         (fn [value]
                           (cond
                             (char? value)
                             (let [rewritten (rewrite-character-literal value)]
                               (when (not= value rewritten)
                                 (swap! applied conj :clojure/character-literal))
                               rewritten)

                             (and (contains? rules :clojure/nil-set)
                                  (set? value)
                                  (contains? value nil))
                             (do (swap! applied conj :clojure/nil-set)
                                 (rewrite-nil-set value))

                             (and (contains? rules :clojure/ratio-literal)
                                  (ratio? value))
                             (do (swap! applied conj :clojure/ratio-literal)
                                 (rewrite-ratio-literal value))

                             :else value))
                         original)
                        original)]
         (cond
           (and (contains? rules :clojure/map-entry-traversal)
                (= 'defn (first form))
                (= 'tag (second form)))
           (do (swap! applied conj :clojure/map-entry-traversal)
               (nav/replace location (rewrite-check-tag form)))

           (and (contains? rules :clojure/nil-membership)
                (= 'contains? (first form))
                (= '*boundaries* (second form)))
           (do (swap! applied conj :clojure/nil-membership)
               (nav/replace location (rewrite-nil-membership form)))

           (and (contains? rules :clojure/escaped-character-first)
                (= 'first (first form))
                (escaped-character-string? (second form)))
           (do (swap! applied conj :clojure/escaped-character-first)
               (nav/replace location
                            (rewrite-escaped-character-first form)))

           (and (contains? rules :foundation/native-number-taxonomy)
                (= 'def (first form))
                (= '*token-checks* (second form)))
           (do (swap! applied conj :foundation/native-number-taxonomy)
               (nav/replace location (rewrite-token-checks form)))

           (and (or (contains? rules :clojure/source-namespace-overrides)
                    (contains? rules :clojure/test-namespace-overrides))
                (= 'ns (first form)))
           (do (swap! applied conj (if (= :test (:unit/kind target))
                                     :clojure/test-namespace-overrides
                                     :clojure/source-namespace-overrides))
               (let [rewritten (rewrite-ns-form form
                                                (:target/overrides target))
                     rewritten (if (and (= :test (:unit/kind target))
                                        (:target/test-alias target))
                                 (add-require-alias
                                  rewritten
                                  (:target/source-namespace target)
                                  (:target/test-alias target))
                                 rewritten)]
                 (nav/replace location rewritten)))

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

           (and (contains? rules :foundation/struct-merge)
                (= 'merge (first form))
                (= 'zip (second form))
                (= 3 (count form)))
           (do (swap! applied conj :foundation/struct-merge)
               (nav/replace location (rewrite-struct-merge form)))

           (and (contains? rules :foundation/native-result)
                (#{'h/wrapped 'h/wrapped?} (first form)))
           (do (swap! applied conj :foundation/native-result)
               (nav/replace location (rewrite-native-result form)))

           (and (contains? rules :clojure/character-whitespace)
                (= 'Character/isWhitespace (first form)))
           (do (swap! applied conj :clojure/character-whitespace)
               (nav/replace location (rewrite-character-whitespace form)))

           (and (contains? rules :clojure/string-starts-with)
                (= '.startsWith (first form)))
           (do (swap! applied conj :clojure/string-starts-with)
               (nav/replace location (rewrite-string-starts-with form)))

           (and (contains? rules :clojure/iterator-first-pipeline)
                (iterator-first-pipeline? form))
           (do (swap! applied conj :clojure/iterator-first-pipeline)
               (nav/replace location
                            (rewrite-iterator-first-pipeline form)))

           (and (contains? rules :clojure/thread-zero-arity-step)
                (#{'-> '->>} (first form))
                (some #(and (seq? %) (= 1 (count %)))
                      (drop 2 form)))
           (do (swap! applied conj :clojure/thread-zero-arity-step)
               (nav/replace location
                            (rewrite-zero-arity-thread-steps form)))

           (not= original form)
           (nav/replace location form)

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

(defn rewrite-anonymous-function
  "converts a reader function value into a deterministic native fn form"
  {:added "4.1"}
  [form]
  (let [state (reduce (fn [{:keys [renames parameters index rest?] :as state}
                           parameter]
                        (cond
                          (= '& parameter)
                          (assoc state
                                 :parameters (conj parameters parameter)
                                 :rest? true)

                          :else
                          (let [replacement (if rest?
                                              'migration-arguments
                                              (symbol
                                               (str "migration-argument-"
                                                    index)))]
                            {:renames (assoc renames parameter replacement)
                             :parameters (conj parameters replacement)
                             :index (if rest? index (inc index))
                             :rest? rest?})))
                      {:renames {}
                       :parameters []
                       :index 0
                       :rest? false}
                      (second form))
        rewrite (fn [value]
                  (if (and (symbol? value)
                           (contains? (:renames state) value))
                    (get (:renames state) value)
                    value))]
    (apply list
           'fn
           (:parameters state)
           (map #(walk/postwalk rewrite %) (nnext form)))))

(defn rewrite-dependencies
  "rewrites exact and qualified dependency symbols through code.query"
  {:added "4.1"}
  [root migration-catalog applied]
  (let [routes (dependency-routes migration-catalog)
        reader-route (get routes 'fn*)
        root (if reader-route
               (query/modify
                root
                [seq?]
                (fn [location]
                  (let [form (nav/value location)]
                    (if (= 'fn* (first form))
                      (do (swap! applied conj (:rule/id reader-route))
                          (nav/replace location
                                       (rewrite-anonymous-function form)))
                      location))))
               root)]
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

(def +native-class-names+
  #{"String"})

(defn native-class-symbol?
  "checks for a qualified symbol owned by a known std.native class"
  {:added "4.1"}
  [value]
  (and (symbol? value)
       (contains? +native-class-names+ (namespace value))))

(defn host-symbol?
  "checks for a JVM interop symbol requiring an explicit adaptation"
  {:added "4.1"}
  [value]
  (boolean
   (and (symbol? value)
        (not (native-class-symbol? value))
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
        override-result   (if target
                            (rewrite-test-override-symbols initial target applied)
                            initial)
        arity-result      (if target
                            (rewrite-single-arity-defns override-result
                                                        target
                                                        applied)
                            override-result)
        target-result     (if target
                            (rewrite-target-forms arity-result target applied)
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
    (when-let [target (first (filter #(= (:source/path unit) (get % path-key))
                                     (:migration/targets migration-catalog)))]
      (let [pathway (:unit/kind unit)
            rule-key (if (= :test pathway)
                       :target/test-rules
                       :target/source-rules)]
        (assoc target
               :unit/kind pathway
               :target/rules (get target rule-key []))))))

(defn migrate-unit
  "migrates a normalized source or test unit"
  {:added "4.1"}
  [unit migration-catalog]
  (let [target (target-for-unit unit migration-catalog)]
    (merge (select-keys unit
                        [:unit/kind :source/path :target/path])
           (migrate-source (:source/string unit)
                           migration-catalog
                           target))))
