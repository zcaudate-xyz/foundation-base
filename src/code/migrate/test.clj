(ns code.migrate.test
  (:require [clojure.string :as str]
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
                    :meta (cond-> (select-keys gathered [:refer :added :id])
                            (symbol? (:refer gathered))
                            (assoc :refer (list 'quote (:refer gathered))))
                    :operations (vec (compile/rewrite-top-level body))}))))))

(defn migrate-form
  [form migration-catalog target]
  (-> form
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
        records (fact-records source)
        state (reduce
               (fn [state {:keys [title meta operations]}]
                 (reduce
                  (fn [state operation]
                    (case (:type operation)
                      :form
                      (update state :setup conj
                              (migrate-form (:form operation)
                                            migration-catalog operation-target))

                      :test-equal
                      (let [index (inc (count (:cases state)))
                            actual (migrate-form (get-in operation [:input :form])
                                                 migration-catalog operation-target)
                            expected (migrate-form (get-in operation [:output :form])
                                                   migration-catalog operation-target)]
                        (update state :cases conj
                                {:name (if (str/blank? title)
                                         (str "case " index)
                                         (str title " #" index))
                                 :meta meta
                                 :test (list 'fn [] actual)
                                 :expected (checker-form expected)}))

                      (update state :diagnostics conj
                              {:type :migration/unsupported-test-operation
                               :operation (:type operation)})))
                  state
                  operations))
               {:setup [] :cases [] :diagnostics []}
               records)
        form (list 'Test/run
                   (:cases state)
                   'process/check)]
    (assoc state
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
           {:input (:source/string unit)
            :source/checksum (engine/sha256 (:source/string unit))
            :output output
            :output/checksum (engine/sha256 output)
            :applied [:foundation/code-test-to-native-test]
            :diagnostics (:diagnostics emitted)
            :operations (+ (count (:setup emitted)) (count (:cases emitted)))
            :assertions (count (:cases emitted))
            :changed (not= (:source/string unit) output)})))
