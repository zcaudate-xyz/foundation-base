(ns code.migrate.probe
  (:require [clojure.string :as str]
            [code.migrate.verify :as verify]
            [code.query :as query]
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
  "removes native code.test dependencies from the lowered program"
  {:added "4.1"}
  [form]
  (let [[head nsp & body] form
        body (keep (fn [clause]
                     (if (and (seq? clause)
                              (#{:use :require} (first clause)))
                       (let [entries (remove
                                      (fn [entry]
                                        (and (vector? entry)
                                             (contains?
                                              #{'code.test.base.process
                                                'code.test.checker.common
                                                'code.test.checker.collection}
                                              (first entry))))
                                      (rest clause))]
                         (when (seq entries)
                           (cons (first clause) entries)))
                       clause))
                   body)]
    (apply list head nsp body)))

(defn top-level-forms
  "returns top-level forms from emitted Hara source in byte order"
  {:added "4.1"}
  [source]
  (loop [cursor (nav/down (nav/parse-root source))
         output []]
    (if (nil? cursor)
      output
      (recur (nav/right cursor)
             (cond-> output
               (nav/expression? cursor)
               (conj (nav/value cursor)))))))

(defn test-run-form?
  [form]
  (and (seq? form) (= 'Test/run (first form))))

(defn emitted-test-plan
  "reads setup and assertion cases exclusively from the emitted test artifact"
  {:added "4.1"}
  [source]
  (let [forms (vec (top-level-forms source))
        ns-index (first (keep-indexed
                         (fn [index form]
                           (when (and (seq? form) (= 'ns (first form))) index))
                         forms))
        run-index (first (keep-indexed
                          (fn [index form]
                            (when (test-run-form? form) index))
                          forms))
        namespace (when ns-index (nth forms ns-index))
        run-form (when run-index (nth forms run-index))
        setup (if (and ns-index run-index (< ns-index run-index))
                (subvec forms (inc ns-index) run-index)
                [])
        cases (if (vector? (second run-form)) (second run-form) [])
        diagnostics (cond-> []
                      (nil? namespace)
                      (conj {:type :migration/emitted-test-namespace-missing})

                      (nil? run-form)
                      (conj {:type :migration/emitted-test-run-missing})

                      (and run-form (not (vector? (second run-form))))
                      (conj {:type :migration/emitted-test-cases-invalid}))]
    {:namespace namespace
     :setup setup
     :cases cases
     :checker (nth run-form 2 nil)
     :diagnostics diagnostics}))

(defn function-body
  [form]
  (when (and (seq? form) (= 'fn (first form)))
    (let [body (nnext form)]
      (cond
        (empty? body) nil
        (= 1 (count body)) (first body)
        :else (cons 'do body)))))

(defn failure-form
  [message index case]
  (list 'throw
        (list 'ex-info
              message
              {:assertion/index index
               :operation/id (:operation/id case)})))

(defn emitted-assertion-form
  "lowers one emitted Test/run case without consulting Foundation source"
  {:added "4.1"}
  [case index]
  (let [actual (function-body (:test case))
        expected (:expected case)]
    (cond
      (nil? actual)
      nil

      (and (seq? expected) (= 'checker/throws (first expected)))
      (list 'let
            ['threw (list 'try
                          (list 'do actual false)
                          (list 'catch 'Throwable 'error true))]
            (list 'if
                  'threw
                  true
                  (failure-form
                   "migration assertion failed: expected throw"
                   index case)))

      (and (seq? expected) (= 'collection/contains (first expected)))
      (let [checks (mapv (fn [[key value]]
                           (list '= (list 'std.foundation/get 'actual key) value))
                         (second expected))
            condition (cond
                        (empty? checks) true
                        (= 1 (count checks)) (first checks)
                        :else (apply list 'and checks))]
        (list 'let
              ['actual actual]
              (list 'if
                    condition
                    true
                    (failure-form
                     "migration assertion failed: expected contained values"
                     index case))))

      (and (seq? expected) (= 'fn (first expected)))
      (list 'let
            ['actual actual]
            (list 'if
                  (list expected 'actual)
                  true
                  (failure-form
                   "migration assertion failed: predicate expectation"
                   index case)))

      :else
      (list 'let
            ['actual actual
             'expected expected]
            (list 'if
                  (list '= 'actual 'expected)
                  true
                  (failure-form
                   "migration assertion failed"
                   index case))))))

(defn probe-program
  "lowers the emitted test artifact into an isolated assertion program"
  {:added "4.1"}
  [source test]
  (let [plan (emitted-test-plan test)
        assertions (keep-indexed emitted-assertion-form (:cases plan))
        unsupported (- (count (:cases plan)) (count assertions))
        diagnostics (cond-> (vec (:diagnostics plan))
                      (pos? unsupported)
                      (conj {:type :migration/emitted-test-case-unsupported
                             :count unsupported}))
        program (str source
                     "\n"
                     (pr-str (probe-ns-form (:namespace plan)))
                     "\n"
                     (str/join "\n" (map pr-str (:setup plan)))
                     (when (seq (:setup plan)) "\n")
                     (str/join "\n" (map pr-str assertions))
                     "\n:migration/tests-passed\n")]
    {:program program
     :operations (+ (count (:setup plan)) (count (:cases plan)))
     :assertions (count (:cases plan))
     :diagnostics diagnostics}))

(defn native-test-program
  [source test]
  (str source
       "\n"
       test
       "\n(if (every? Test/passed? (Test/run []))\n"
       "  :migration/tests-passed\n"
       "  (throw (ex-info \"migrated tests failed\" {})))\n"))

(defn verify-pair
  "verifies emitted source and tests in two independent fresh processes"
  {:added "4.1"}
  [pair options]
  (let [source (:output (:source pair))
        test (:output (:test pair))
        pair-diagnostics (vec (concat (:diagnostics (:source pair))
                                      (:diagnostics (:test pair))))
        lowered (probe-program source test)
        lowered-result (verify/verify-source (:program lowered) options)
        lowered (assoc lowered
                       :verification lowered-result
                       :passed (and (empty? (:diagnostics lowered))
                                    (:passed lowered-result)))
        native-program (native-test-program source test)
        native-result (verify/verify-source native-program options)
        native {:program native-program
                :verification native-result
                :passed (:passed native-result)}
        diagnostics (vec (concat pair-diagnostics
                                 (:diagnostics lowered)))]
    {:operations (:operations lowered)
     :assertions (:assertions lowered)
     :diagnostics diagnostics
     :lowered lowered
     :native native
     :passed (and (empty? diagnostics)
                  (:passed lowered)
                  (:passed native))}))