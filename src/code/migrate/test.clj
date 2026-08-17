(ns code.migrate.test
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [code.framework.test.fact :as fact]
            [code.migrate.engine :as engine]
            [code.query :as query]
            [code.test.compile :as compile]
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
        required '([code.test.base.process :as process]
                   [code.test.checker.common :as checker]
                   [code.test.checker.collection :as collection])
        require-clause (first (filter #(and (seq? %)
                                            (= :require (first %)))
                                      clauses))
        clauses (remove #(= % require-clause) clauses)
        require-clause (apply list :require
                              (concat (rest require-clause) required))]
    (apply list head name (cons require-clause clauses))))

(defn emitted-fact-meta
  "retains all source fact metadata while quoting symbolic references"
  {:added "4.1"}
  [gathered]
  (let [metadata (apply dissoc gathered
                        [:form :sexp :line :test :intro :ns :var])]
    (cond-> metadata
      (symbol? (:refer metadata))
      (assoc :refer (list 'quote (:refer metadata))))))

(defn fact-records
  "compiles each Foundation fact with stable identity and ordering"
  {:added "4.1"}
  [source]
  (let [root (nav/parse-root source)]
    (->> (fact/top-level-fact-navs root)
         (keep fact/gather-fact)
         (map-indexed
          (fn [index {:keys [sexp intro id refer] :as gathered}]
            (let [body (rest sexp)
                  body (if (string? (first body)) (rest body) body)
                  ordinal (inc index)]
              {:title intro
               :meta (emitted-fact-meta gathered)
               :fact/id (or id refer (symbol (str "fact-" ordinal)))
               :fact/ordinal ordinal
               :operations (vec (compile/rewrite-top-level body))})))
         vec)))

(defn zero-arity-thread-form?
  [form]
  (and (seq? form)
       (#{'-> '->>} (first form))
       (some #(and (seq? %) (= 1 (count %)))
             (drop 2 form))))

(defn rewrite-test-form
  "applies only adaptations explicitly owned by the test pathway"
  {:added "4.1"}
  [form target applied]
  (if (contains? (set (:target/rules target))
                 :clojure.test/thread-zero-arity-step)
    (walk/postwalk
     (fn [value]
       (if (zero-arity-thread-form? value)
         (do (swap! applied conj :clojure.test/thread-zero-arity-step)
             (engine/rewrite-zero-arity-thread-steps value))
         value))
     form)
    form))

(defn merge-migration-evidence!
  [evidence result]
  (swap! evidence update :applied into (:applied result))
  (swap! evidence update :diagnostics into (:diagnostics result))
  result)

(defn migrate-form
  "migrates one test form through test-owned rules only"
  {:added "4.1"}
  ([form migration-catalog target]
   (migrate-form form migration-catalog target
                 (atom {:applied [] :diagnostics []})))
  ([form migration-catalog target evidence]
   (let [applied (atom [])
         form (rewrite-test-form form target applied)
         result (-> (engine/migrate-source (pr-str form)
                                           migration-catalog
                                           target)
                    (update :applied #(vec (concat @applied %)))
                    (merge-migration-evidence! evidence))]
     (read-string (:output result)))))

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

(defn fact-id-string
  [value]
  (cond
    (keyword? value) (subs (str value) 1)
    (symbol? value) (str value)
    :else (pr-str value)))

(defn operation-id
  "assigns a stable id from fact identity and source ordinals"
  {:added "4.1"}
  [record operation-ordinal assertion-ordinal]
  (str (fact-id-string (:fact/id record))
       "#fact-" (:fact/ordinal record)
       "/operation-" operation-ordinal
       (when assertion-ordinal
         (str "/assertion-" assertion-ordinal))))

(defn case-name
  [title index]
  (if (str/blank? title)
    (str "case " index)
    title))

(defn emit-test-run
  "emits a complete Test/run namespace and one-to-one operation manifest"
  {:added "4.1"}
  [source migration-catalog target]
  (let [evidence (atom {:applied [] :diagnostics []})
        migrated-ns (migrate-form (test-ns-form source)
                                  migration-catalog target evidence)
        records (fact-records source)
        state
        (reduce
         (fn [state record]
           (let [assertion-ordinal (atom 0)]
             (reduce-kv
              (fn [state operation-index operation]
                (let [operation-ordinal (inc operation-index)
                      source-order (inc (:next-order state))
                      assertion-order (when (= :test-equal (:type operation))
                                        (swap! assertion-ordinal inc))
                      id (operation-id record
                                       operation-ordinal
                                       assertion-order)
                      correspondence
                      {:operation/id id
                       :fact/id (:fact/id record)
                       :fact/ordinal (:fact/ordinal record)
                       :operation/ordinal operation-ordinal
                       :assertion/ordinal assertion-order
                       :source/order source-order
                       :source/type (:type operation)}]
                  (case (:type operation)
                    :form
                    (let [emitted-index (count (:setup state))
                          emitted (migrate-form (:form operation)
                                                migration-catalog
                                                target
                                                evidence)]
                      (-> state
                          (update :setup conj emitted)
                          (update :operation-correspondence conj
                                  (assoc correspondence
                                         :emitted/order source-order
                                         :emitted/path [:setup emitted-index]))
                          (assoc :next-order source-order)))

                    :test-equal
                    (let [case-index (count (:cases state))
                          actual (migrate-form (get-in operation [:input :form])
                                               migration-catalog
                                               target
                                               evidence)
                          expected (migrate-form (get-in operation [:output :form])
                                                 migration-catalog
                                                 target
                                                 evidence)]
                      (-> state
                          (update :cases conj
                                  {:operation/id id
                                   :name (case-name (:title record)
                                                    (inc case-index))
                                   :meta (:meta record)
                                   :test (list 'fn [] actual)
                                   :expected (checker-form expected)})
                          (update :operation-correspondence conj
                                  (assoc correspondence
                                         :emitted/order source-order
                                         :emitted/path [:cases case-index]))
                          (assoc :next-order source-order)))

                    (-> state
                        (update :diagnostics conj
                                {:type :migration/unsupported-test-operation
                                 :operation/id id
                                 :operation (:type operation)})
                        (update :operation-correspondence conj
                                (assoc correspondence
                                       :emitted/order nil
                                       :emitted/path nil))
                        (assoc :next-order source-order)))))
              state
              (:operations record))))
         {:setup []
          :cases []
          :diagnostics []
          :operation-correspondence []
          :next-order 0}
         records)
        emitted-count (+ (count (:setup state)) (count (:cases state)))
        count-diagnostics
        (when-not (= (:next-order state) emitted-count)
          [{:type :migration/test-operation-count
            :source/operations (:next-order state)
            :emitted/operations emitted-count}])
        diagnostics (vec (concat (:diagnostics @evidence)
                                 (:diagnostics state)
                                 count-diagnostics))
        form (list 'Test/run (:cases state) 'process/check)]
    (assoc state
           :applied (vec (distinct
                          (concat (:applied @evidence)
                                  [:foundation/code-test-to-native-test])))
           :diagnostics diagnostics
           :output (str (pr-str (native-test-ns migrated-ns)) "\n\n"
                        (str/join "\n" (map pr-str (:setup state)))
                        (when (seq (:setup state)) "\n\n")
                        (pr-str form) "\n"))))

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
           {:target/path (or (:target/path unit)
                             (:target/output-path target))
            :input (:source/string unit)
            :source/checksum (engine/sha256 (:source/string unit))
            :output output
            :output/checksum (engine/sha256 output)
            :applied (:applied emitted)
            :diagnostics (:diagnostics emitted)
            :operations (+ (count (:setup emitted)) (count (:cases emitted)))
            :assertions (count (:cases emitted))
            :operation-correspondence (:operation-correspondence emitted)
            :changed (not= (:source/string unit) output)})))