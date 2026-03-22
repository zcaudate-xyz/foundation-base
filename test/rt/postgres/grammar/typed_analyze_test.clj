(ns rt.postgres.grammar.typed-analyze-test
  (:use code.test)
  (:require [rt.postgres.grammar.typed-parse :as parse]
            [rt.postgres.grammar.typed-analyze :as analyze]
            [clojure.string :as str]))

(defn- get-scratch-fn
  [sym]
  (let [analysis (-> 'rt.postgres.script.test.scratch-v2
                     parse/analyze-namespace
                     parse/register-types!)]
    (some #(when (= (name sym) (:name %)) %)
          (:functions analysis))))

^{:refer rt.postgres.grammar.typed-analyze/detect-operations :added "4.1"}
(fact "detect-operations finds postgres write operations in a function body"
  (let [fn-def (get-scratch-fn 'insert-entry)
        ops    (analyze/detect-operations fn-def)]
    ops => [{:symbol "pg/g:insert"
             :op :insert
             :returns :table-instance
             :linked true
             :table "Entry"}]))

^{:refer rt.postgres.grammar.typed-analyze/infer-report :added "4.1"}
(fact "infer-report returns JSON-friendly plain data"
  (let [report (analyze/infer-report (get-scratch-fn 'insert-entry))]
    (get-in report [:function :name]) => "insert-entry"
    (get-in report [:analysis :mutating]) => true
    (get-in report [:analysis :source-tables]) => ["Entry"]
    (get-in report [:analysis :return :kind]) => "shaped"))

^{:refer rt.postgres.grammar.typed-analyze/report-json :added "4.1"}
(fact "report-json serializes a report"
  (let [output (analyze/report-json
                (analyze/infer-report (get-scratch-fn 'insert-entry))
                true)]
    (string? output) => true
    (str/includes? output "\"function\"") => true
    (str/includes? output "\"insert-entry\"") => true))


^{:refer rt.postgres.grammar.typed-analyze/analyze-expr :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-analyze/infer-return-type :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-analyze/reset-cache! :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-analyze/cached-infer :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.typed-analyze/json-safe :added "4.1"}
(fact "TODO")