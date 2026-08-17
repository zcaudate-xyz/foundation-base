(ns code.migrate.test
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [code.framework.test.fact :as fact]
            [code.migrate.engine :as engine]
            [code.query :as query]
            [code.test.compile :as compile]
            [std.block :as block]
            [std.block.layout.estimate :as estimate]
            [std.block.navigate :as nav]))

(defn test-ns-form
  "returns the namespace form from a Foundation test source"
  {:added "4.1"}
  [source]
  (let [root (nav/parse-root source)
        location (query/$* root '[(ns | _ & _)]
                            {:return :zipper :first true})]
    (some-> location nav/up nav/value)))

(defn code-test-entry?
  [entry]
  (or (= 'code.test entry)
      (and (vector? entry) (= 'code.test (first entry)))))

(defn native-test-ns
  "removes the Foundation runner and adds native checker dependencies"
  {:added "4.1"}
  [form]
  (let [[head name & clauses] form
        clauses (keep (fn [clause]
                        (if (and (seq? clause)
                                 (#{:use :require} (first clause)))
                          (let [entries (remove code-test-entry? (rest clause))]
                            (when (seq entries)
                              (cons (first clause) entries)))
                          clause))
                      clauses)
        required '([std.codec.base64 :as base64]
                   [code.test.base.process :as process]
                   [code.test.checker.common :as checker]
                   [code.test.checker.collection :as collection])
        require-clause (first (filter #(and (seq? %)
                                            (= :require (first %)))
                                      clauses))
        clauses (remove #(= % require-clause) clauses)
        require-clause (apply list :require
                              (concat (rest require-clause) required))]
    (apply list head name (cons require-clause clauses))))

(defn fact-records
  "compiles each Foundation fact while retaining its title and metadata"
  {:added "4.1"}
  [source]
  (let [root (nav/parse-root source)]
    (->> (fact/top-level-fact-navs root)
         (keep fact/gather-fact)
         (mapv (fn [{:keys [sexp intro] :as gathered}]
                 (let [body (rest sexp)
                       body (if (string? (first body)) (rest body) body)]
                   {:title intro
                    :meta (select-keys gathered [:refer :added :id :class])
                   :operations (vec (compile/rewrite-top-level body))}))))))

(defn normalize-root-form
  "normalizes portable scalar forms before source-tree migration"
  {:added "4.1"}
  [form target]
  (let [rules (set (:target/rules target))]
    (cond
      (and (contains? rules :clojure/character-literal) (char? form))
      (engine/rewrite-character-literal form)

      (and (contains? rules :clojure/nil-set)
           (set? form)
           (contains? form nil))
      (engine/rewrite-nil-set form)

      (and (contains? rules :clojure/ratio-literal) (ratio? form))
      (engine/rewrite-ratio-literal form)

      :else form)))

(defn rewrite-block-test-compat
  ([form]
   (rewrite-block-test-compat form nil))
  ([form target]
   (let [navigate? (= :migration/std-block-navigate (:target/id target))
         form (if navigate?
                (walk/postwalk
                 (fn [node]
                   (cond
                     (and (seq? node)
                          (= 'quote (first node))
                          (seq? (second node))
                          (#{'clojure.core/defn 'std.foundation/defn}
                           (first (second node))))
                     (list
                      'quote
                      (walk/postwalk
                       (fn [value]
                         (cond
                           (= 'clojure.core/defn value)
                           'std.foundation/defn

                           (= 'clojure.core/if-let value)
                           'std.foundation/if-let

                           (= :right value)
                           '(std.foundation/keyword "right")

                           (= 'std.lib.zip/get value)
                           'zip/get

                           :else value))
                       (second node)))

                     (and (seq? node)
                          (= 'str (first node))
                          (= 2 (count node)))
                     (list 'display-navigator (second node))

                     (and (seq? node)
                          (= '-> (first node))
                          (or (= 'str (last node))
                              (= '(str) (last node))))
                     (concat (butlast node) (list 'display-navigator))

                     :else node))
                 form)
                form)]
   (walk/postwalk
    (fn [node]
      (cond
       (and target
            (seq? node)
            (= 'quote (first node))
            (seq? (second node))
            (= 'quote (first (second node)))
            (symbol? (second (second node)))
            (= (str (:target/test-namespace target))
               (namespace (second (second node)))))
       (list 'quote
             (list 'syntax-quote
                   (symbol (name (second (second node))))))

       (and (seq? node)
            (= 'slurp (first node))
            (= 2 (count node))
            (string? (second node)))
       (let [content (slurp (second node))
             encoded (.encodeToString (java.util.Base64/getEncoder)
                                      (.getBytes content "UTF-8"))]
         (list 'str/decode-utf8 (list 'base64/decode encoded)))

       (= :long node)
       :number

       (= :collection node)
       :container

       (and target
            (keyword? node)
            (nil? (namespace node))
            (str/starts-with? (name node) ":"))
       (keyword (str (:target/test-namespace target))
                (subs (name node) 1))

       (and (seq? node)
            (= 'construct/token (first node))
            (= 2 (count node))
            (ratio? (second node)))
       (let [value (second node)]
         (list 'construct/token-from-string
               (str value)
               (list '/ (double (numerator value))
                     (double (denominator value)))))

       (and (seq? node)
            (= '->> (first node))
            (= 3 (count node))
            (seq? (last node))
            (= '(apply str) (last node)))
       (list 'apply 'str
             (list 'map 'std.block.base/block-representation (second node)))

       (and (seq? node)
            (= '->> (first node))
            (seq? (last node))
            (= '(map str) (last node)))
       (concat (butlast node)
               (list '(map std.block.base/block-representation)))

       (and (seq? node)
            (= '->> (first node))
            (= 3 (count node))
            (= 'str (last node)))
       (list 'std.block.base/block-representation (second node))

       (and (seq? node)
            (= '-> (first node))
            (= '(str) (last node)))
       (concat (butlast node)
               (list '(std.block.base/block-representation)))

       (and (seq? node)
            (= 'mapv (first node))
            (= 'str (second node))
            (= 3 (count node)))
       (list 'mapv 'std.block.base/block-representation (nth node 2))

       (and (seq? node)
            (= 'map (first node))
            (= 'str (second node))
            (= 3 (count node)))
       (list 'map 'std.block.base/block-representation (nth node 2))

       (and (seq? node)
            (= 'catch (first node))
            (= 'clojure.lang.ExceptionInfo (second node)))
       (cons 'catch (cons 'Throwable (nnext node)))

       (and (seq? node)
            (= 'apply (first node))
            (= 'str (second node))
            (= 3 (count node)))
       (list 'apply 'str
             (list 'map 'std.block.base/block-representation (nth node 2)))

       (and (seq? node)
            (= 'str (first node))
            (= 2 (count node)))
       (list 'std.block.base/block-representation (second node))

       :else node))
    form))))

(defn migrate-form
  [form migration-catalog target]
  (-> (if (contains? (set (:target/rules target))
                     :foundation/block-test-compat)
        (rewrite-block-test-compat form target)
        form)
      (normalize-root-form target)
      pr-str
      (engine/migrate-source migration-catalog target)
      :output
      read-string))

(defn checker-form
  "maps Foundation matcher shorthand to native checker constructors"
  {:added "4.1"}
  [expected]
  (cond
    (= 'var? expected)
    '(fn [actual] (= :std.native.Var (type actual)))

    (and (seq? expected) (= 'contains (first expected)))
    (cons 'collection/contains (rest expected))

    (and (seq? expected) (= 'throws (first expected)))
    (cons 'checker/throws (rest expected))

    :else expected))

(defn compile-test-record
  "migrates one Foundation fact without losing its function boundary"
  {:added "4.1"}
  [{:keys [title meta operations]} migration-catalog target]
  (let [case-count (count (filter #(= :test-equal (:type %)) operations))
        state
        (reduce
         (fn [state operation]
           (case (:type operation)
             :form
             (if (= '=> (:form operation))
               state
               (update state :setup conj
                       (migrate-form (:form operation)
                                     migration-catalog target)))

             :test-equal
             (let [index    (inc (count (:cases state)))
                   actual   (migrate-form (get-in operation [:input :form])
                                          migration-catalog target)
                   expected (migrate-form (get-in operation [:output :form])
                                          migration-catalog target)]
               (update state :cases conj
                       {:name (cond
                                (str/blank? title) (str "case " index)
                                (= 1 case-count) title
                                :else (str title " #" index))
                        :test (list 'fn [] actual)
                        :expected (checker-form expected)}))

             (update state :diagnostics conj
                     {:type :migration/unsupported-test-operation
                      :operation (:type operation)})))
         {:setup [] :cases [] :diagnostics []}
         operations)]
    (assoc state :meta meta)))

(defn test-record-form
  "returns one native Test/run form and its owning setup"
  {:added "4.1"}
  [{:keys [setup cases]}]
  (let [run-form (list 'Test/run cases 'migration-test-check)]
    (if (seq setup)
      (apply list 'do (concat setup [run-form]))
      run-form)))

(defn layout-test-form
  "lays out one generated test form at the migration readability width"
  {:added "4.1"}
  [form]
  (binding [estimate/*readable-len* 80]
    (block/layout form)))

(defn test-metadata-string
  "formats deterministic refer-first reader metadata"
  {:added "4.1"}
  [metadata]
  (let [entries (keep (fn [key]
                        (when (contains? metadata key)
                          [key (get metadata key)]))
                      [:refer :added :id :class])
        strings (mapv (fn [[key value]]
                        (str (pr-str key)
                             " "
                             (block/string (layout-test-form value))))
                      entries)
        single (str "^{" (str/join " " strings) "}")]
    (if (<= (count single) 80)
      single
      (str "^{" (first strings)
           "\n  " (str/join "\n  " (rest strings)) "}"))))

(defn test-record-string
  "formats one metadata-delimited native test block"
  {:added "4.1"}
  [record]
  (str (test-metadata-string (:meta record))
       "\n"
       (block/string (layout-test-form (test-record-form record)))))

(defn emit-test-run
  "emits setup forms and executable Test/run cases from compiled facts"
  {:added "4.1"}
  [source migration-catalog target]
  (let [operation-target (assoc target
                                :unit/kind :test
                                :target/rules
                                (vec (distinct
                                      (concat (:target/source-rules target)
                                              (:target/test-rules target)))))
        migrated-ns (migrate-form (test-ns-form source)
                                  migration-catalog target)
        records (mapv #(compile-test-record % migration-catalog operation-target)
                      (fact-records source))
        state {:setup (vec (mapcat :setup records))
               :cases (vec (mapcat :cases records))
               :diagnostics (vec (mapcat :diagnostics records))}
        check-form '(defn migration-test-check
                      [test expected]
                      (let [result (select-keys (process/check test expected)
                                                [:pass :status :error])]
                        (if (not (:pass result))
                          (println :failure
                                   (assoc result
                                          :actual (try
                                                    (test)
                                                    (catch Throwable error
                                                      {:exception (str error)}))
                                          :expected expected)))
                        result))]
    (assoc state
           :output (str (block/string
                         (layout-test-form (native-test-ns migrated-ns)))
                        "\n\n"
                        (block/string (layout-test-form check-form))
                        "\n\n"
                        (str/join "\n\n" (map test-record-string records))
                        "\n"))))

(defn migrate-unit
  "migrates a test unit into native Test/run source"
  {:added "4.1"}
  [unit migration-catalog]
  (when-not (= :test (:unit/kind unit))
    (throw (ex-info "Test migration requires a :test unit"
                    {:unit/kind (:unit/kind unit)})))
  (let [target (engine/target-for-unit unit migration-catalog)
        emitted (emit-test-run (:source/string unit)
                               migration-catalog target)
        output (:output emitted)]
    (merge (select-keys unit [:unit/kind :source/path :target/path])
           {:input (:source/string unit)
            :source/checksum (engine/sha256 (:source/string unit))
            :output output
            :output/checksum (engine/sha256 output)
            :applied [:foundation/code-test-to-native-test]
            :diagnostics (:diagnostics emitted)
            :operations (+ (count (:setup emitted)) (count (:cases emitted)))
            :assertions (count (:cases emitted))
            :changed (not= (:source/string unit) output)})))
