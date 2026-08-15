(ns code.migrate.probe
  (:require [clojure.string :as str]
            [code.framework.test.fact :as fact]
            [code.migrate.engine :as engine]
            [code.migrate.verify :as verify]
            [code.query :as query]
            [code.test.compile :as compile]
            [std.block.navigate :as nav]))

(defn test-ns-form
  "returns the namespace form from generated test source"
  {:added "4.1"}
  [source]
  (let [root  (nav/parse-root source)
        nsloc (query/$* root '[(ns | _ & _)]
                          {:return :zipper :first true})]
    (some-> nsloc nav/up nav/value)))

(defn code-test-entry?
  "checks for a code.test :use or :require entry"
  {:added "4.1"}
  [entry]
  (or (= 'code.test entry)
      (and (vector? entry)
           (= 'code.test (first entry)))))

(defn probe-ns-form
  "removes code.test only from the temporary verification namespace"
  {:added "4.1"}
  [form]
  (let [[head nsp & body] form
        body (keep (fn [clause]
                     (if (and (seq? clause)
                              (#{:use :require} (first clause)))
                       (let [entries (remove code-test-entry? (rest clause))]
                         (when (seq entries)
                           (cons (first clause) entries)))
                       clause))
                   body)]
    (apply list head nsp body)))

(defn fact-operations
  "compiles Foundation fact forms into their existing operation model"
  {:added "4.1"}
  [source]
  (let [root (nav/parse-root source)]
    (->> (fact/top-level-fact-navs root)
         (keep fact/gather-fact)
         (mapcat (fn [{:keys [sexp]}]
                   (let [body (rest sexp)
                         body (if (string? (first body))
                                (rest body)
                                body)]
                     (compile/rewrite-top-level body))))
         vec)))

(defn migrate-operation-form
  "migrates one compiled fact form through the source target"
  {:added "4.1"}
  [form migration-catalog target]
  (if (and migration-catalog target)
    (-> form
        pr-str
        (engine/migrate-source migration-catalog target)
        :output
        read-string)
    form))

(defn migrate-operation
  "migrates the input and expected forms of one compiled fact operation"
  {:added "4.1"}
  [operation migration-catalog target]
  (reduce (fn [result path]
            (if (get-in result path)
              (update-in result path migrate-operation-form
                         migration-catalog target)
              result))
          operation
          [[:input :form] [:output :form]]))

(defn assertion-form
  "lowers one supported Foundation test operation to native Hara"
  {:added "4.1"}
  [operation index]
  (case (:type operation)
    :form
    (:form operation)

    :test-equal
    (let [actual   (get-in operation [:input :form])
          expected (get-in operation [:output :form])]
      (cond
        (= 'var? expected)
        (list 'if
              (list '= :std.native.Var (list 'type actual))
              true
              (list 'throw
                    (list 'ex-info
                          "migration assertion failed: expected var"
                          {:index index})))

        (and (seq? expected) (= 'throws (first expected)))
        (list 'let
              ['threw (list 'try
                            (list 'do actual false)
                            (list 'catch 'Throwable 'error true))]
              (list 'if
                    'threw
                    true
                    (list 'throw
                          (list 'ex-info
                                "migration assertion failed: expected throw"
                                {:index index}))))

        (and (seq? expected) (= 'contains (first expected)))
        (list 'let
              ['actual actual
               'expected (second expected)]
              (list 'if
                    (list 'every?
                          (list 'fn ['entry]
                                (list '= (list 'get 'actual (list 'key 'entry))
                                      (list 'val 'entry)))
                          'expected)
                    true
                    (list 'throw
                          (list 'ex-info
                                "migration assertion failed: expected contained values"
                                {:index index}))))

        :else
        (list 'let
              ['actual actual
               'expected expected]
              (list 'if
                    (list '= 'actual 'expected)
                    true
                    (list 'throw
                          (list 'ex-info
                                "migration assertion failed"
                                {:assertion/index index
                                 :actual (list 'quote actual)
                                 :expected (list 'quote expected)}))))))

    nil))

(defn probe-program
  "creates a self-contained native assertion program for a migrated pair"
  {:added "4.1"}
  ([source test]
   (probe-program source test test))
  ([source test fact-source]
   (probe-program source test fact-source nil nil))
  ([source test fact-source migration-catalog target]
   (let [operations  (->> (fact-operations fact-source)
                          (mapv #(migrate-operation % migration-catalog target)))
         unsupported (->> operations
                          (remove #(#{:form :test-equal} (:type %)))
                          vec)
         forms       (keep-indexed (fn [index operation]
                                     (assertion-form operation index))
                                   operations)
         program     (str source
                          "\n"
                          (pr-str (probe-ns-form (test-ns-form test)))
                          "\n"
                          (str/join "\n" (map pr-str forms))
                          "\n:migration/tests-passed\n")]
     {:program program
      :operations (count operations)
      :assertions (count (filter #(= :test-equal (:type %)) operations))
      :diagnostics (mapv (fn [operation]
                           {:type :migration/unsupported-test-operation
                            :operation (:type operation)})
                         unsupported)})))

(defn verify-pair
  "verifies a migrated pair without loading the incomplete native test stack"
  {:added "4.1"}
  [pair options]
  (let [migration-catalog (:migration/catalog options)
        target (when migration-catalog
                 (engine/target-for-unit (:test pair) migration-catalog))
        probe  (probe-program (:output (:source pair))
                              (:output (:test pair))
                              (:input (:test pair))
                              migration-catalog
                              target)
        result (verify/verify-source (:program probe) options)]
    (merge probe
           {:verification result
            :passed (and (empty? (:diagnostics probe))
                         (:passed result))})))
