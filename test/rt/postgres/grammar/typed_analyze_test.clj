(ns rt.postgres.grammar.typed-analyze-test
  (:require [clojure.string :as str]
            [rt.postgres.grammar.typed-analyze :as analyze]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-parse :as parse])
  (:use code.test))

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
(fact "analyze-expr infers map literals and table operations"
  (types/clear-registry!)
  (-> 'rt.postgres.script.test.scratch-v2
      parse/analyze-namespace
      parse/register-types!)
  (let [ctx (types/make-context)]
    (get-in (analyze/analyze-expr {:id 1 :name "x"} ctx)
            [:shape :fields :id :type])
    => :integer

    (select-keys (analyze/analyze-expr '(pg/g:insert -/Entry {:name i-name :tags i-tags} {:track o-op})
                                       ctx)
                 [:kind :table])
    => {:kind :shaped
        :table "Entry"}))

^{:refer rt.postgres.grammar.typed-analyze/infer-return-type :added "4.1"}
(fact "infer-return-type analyzes the last form in the function body"
  (types/clear-registry!)
  (let [analysis (-> 'rt.postgres.script.test.scratch-v2
                     parse/analyze-namespace
                     parse/register-types!)
        fn-def (some #(when (= "insert-entry" (:name %)) %)
                     (:functions analysis))]
    (select-keys (analyze/infer-return-type fn-def)
                 [:source-table :confidence]))
  => {:source-table "Entry"
      :confidence :high})

^{:refer rt.postgres.grammar.typed-analyze/reset-cache! :added "4.1"}
(fact "reset-cache! clears the infer cache"
  (reset! analyze/*infer-cache* {'demo/test {:kind :primitive}})
  (analyze/reset-cache!)
  @analyze/*infer-cache*
  => {})

^{:refer rt.postgres.grammar.typed-analyze/cached-infer :added "4.1"}
(fact "cached-infer memoizes function inference by namespace and name"
  (types/clear-registry!)
  (let [analysis (-> 'rt.postgres.script.test.scratch-v2
                     parse/analyze-namespace
                     parse/register-types!)
        fn-def (some #(when (= "insert-entry" (:name %)) %)
                     (:functions analysis))]
    (analyze/reset-cache!)
    (analyze/cached-infer fn-def)
    (keys @analyze/*infer-cache*))
  => ['rt.postgres.script.test.scratch-v2/insert-entry])

^{:refer rt.postgres.grammar.typed-analyze/json-safe :added "4.1"}
(fact "json-safe normalizes nested keywords, symbols, and sets"
  (analyze/json-safe {:a :kw
                      :b 'sym
                      :c #{2 1}
                      :d {'inner :v}})
  => {:a "kw"
      :b "sym"
      :c [1 2]
      :d {"inner" "v"}})
