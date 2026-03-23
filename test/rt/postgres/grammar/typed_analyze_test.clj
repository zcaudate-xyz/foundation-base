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

^{:refer rt.postgres.grammar.typed-analyze/analyze-expr :added "4.1"}
(fact "analyze-expr infers pg/field-id as uuid field access"
  (let [ctx (types/make-context {'m :jsonb}
                                {'m (types/make-jsonb-shape
                                     {:organisation {:type :uuid
                                                     :nullable? true}})}
                                {'m (types/make-jsonb-path [] 'm)})]
    (analyze/analyze-expr '(pg/field-id m "organisation") ctx)
    => {:kind :field-access
        :field "organisation"
        :type :uuid}))

^{:refer rt.postgres.grammar.typed-analyze/analyze-expr :added "4.1"}
(fact "analyze-expr preserves vector literals as arrays of shaped elements"
  (let [ctx (types/make-context {'v-profile :jsonb}
                                {'v-profile (types/make-jsonb-shape
                                             {:id {:type :uuid}
                                              :language {:type :text}}
                                             "UserProfile")}
                                {})]
    (select-keys (analyze/analyze-expr '[v-profile] ctx)
                 [:kind :element-type])
    => {:kind :array
        :element-type (types/make-jsonb-shape
                       {:id {:type :uuid}
                        :language {:type :text}}
                       "UserProfile")}))

^{:refer rt.postgres.grammar.typed-analyze/register-call-analyzer! :added "4.1"}
(fact "call analyzers can specialize a resolved function call"
  (types/clear-registry!)
  (analyze/reset-cache!)
  (let [fn-form '(defn.pg annotate
                   [:jsonb i-message]
                   (return i-message))
        fn-def  (parse/parse-defn fn-form "test.plugin" nil)
        _       (types/register-type! 'test.plugin/annotate fn-def)
        _       (analyze/register-call-analyzer!
                 'test.plugin/annotate
                 (fn [{:keys [ctx args]}]
                   (let [base (analyze/analyze-expr (first args) ctx)
                         base-shape (or (:shape base)
                                        (types/empty-jsonb-shape))]
                     {:kind :shaped
                      :shape (types/merge-shapes
                              base-shape
                              (types/make-jsonb-shape
                               {:plugin-added {:type :text}}
                               nil :high false))})))
        result  (analyze/analyze-expr
                 '(test.plugin/annotate {:hello "world"})
                 (types/make-context))]
    (get-in result [:shape :fields :hello :type]) => :text
    (get-in result [:shape :fields :plugin-added :type]) => :text))

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

^{:refer rt.postgres.grammar.typed-analyze/infer-return-type :added "4.1"}
(fact "infer-return-type uses jsonb paths from plain child destructuring"
  (types/clear-registry!)
  (let [form '(defn.pg
                prepare-account
                [:jsonb i-created]
                (let [#{o-profile
                        v-account
                        v-security} i-created
                      #{(:text v-password-salt)} v-security]
                  (return
                   (|| i-created
                       {:password-salt v-password-salt
                        :profile o-profile}))))
        fn-def (parse/parse-defn form "test.ns" nil)]
    (types/register-type! 'test.ns/prepare-account fn-def)
    (let [result (analyze/infer-return-type fn-def)]
      (get-in result [:shape :fields :security :shape :fields :password-salt :type]) => :text
      (get-in result [:shape :fields :profile :type]) => :jsonb)))

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
