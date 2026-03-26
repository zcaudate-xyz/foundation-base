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

^{:refer rt.postgres.grammar.typed-analyze/detect-operations :added "4.1"}
(fact "detect-operations follows nested function calls"
  (types/clear-registry!)
  (let [user-table (types/make-table-def "test" "User"
                                         [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid)
                                                                 {:required true :primary true})
                                          (types/make-column-def :handle (types/make-type-ref :primitive nil :text)
                                                                 {:required true})]
                                         :id)
        update-fn (types/make-fn-def "test.core" "update-user-raw"
                                     [(types/->FnArg 'i-user-id :uuid nil :payload)
                                      (types/->FnArg 'm :jsonb nil :payload)
                                      (types/->FnArg 'o-op :jsonb nil :track)]
                                     [:jsonb]
                                     {:raw-body '[(pg/t:update User
                                                              {:set m
                                                               :where {:id i-user-id}
                                                               :single true})]}
                                     "test")
        create-fn (types/make-fn-def "test.core" "create-user"
                                     [(types/->FnArg 'i-user-id :uuid nil :payload)
                                      (types/->FnArg 'm :jsonb nil :payload)
                                      (types/->FnArg 'o-op :jsonb nil :track)]
                                     [:jsonb]
                                     {:raw-body '[(pg/t:insert User
                                                              {:id i-user-id
                                                               :handle (:->> m "handle")}
                                                              {:track o-op})]}
                                     "test")
        set-user-fn (types/make-fn-def "test.core" "set-user"
                                       [(types/->FnArg 'i-user-id :uuid nil :payload)
                                        (types/->FnArg 'm :jsonb nil :payload)
                                        (types/->FnArg 'o-op :jsonb nil :track)]
                                       [:jsonb]
                                       {:raw-body '[(let [o-user (test.core/update-user-raw i-user-id m o-op)
                                                          _      (when [o-user :is-null]
                                                                   (return (test.core/create-user i-user-id m o-op)))]
                                                      (return o-user))]}
                                       "test")
        wrapper-fn (types/make-fn-def "test.rpc" "user-set-handle"
                                      [(types/->FnArg 'handle :text nil :payload)]
                                      [:jsonb]
                                      {:raw-body '[(test.core/set-user auth-user-id {:handle handle} auth-op)]}
                                      "test.rpc")]
    (types/register-type! 'test/User user-table)
    (types/register-type! 'test.core/update-user-raw update-fn)
    (types/register-type! 'test.core/create-user create-fn)
    (types/register-type! 'test.core/set-user set-user-fn)
    (types/register-type! 'test.rpc/user-set-handle wrapper-fn)
    (let [ops (analyze/detect-operations wrapper-fn)]
      (count ops) => 2
      (set (map :op ops)) => #{:update :insert}
      (set (map :table ops)) => #{"User"})))

^{:refer rt.postgres.grammar.typed-analyze/infer-report :added "4.1"}
(fact "infer-report returns JSON-friendly plain data"
  (let [report (analyze/infer-report (get-scratch-fn 'insert-entry))]
    (get-in report [:function :name]) => "insert-entry"
    (get-in report [:analysis :mutating]) => true
    (get-in report [:analysis :source-tables]) => ["Entry"]
    (get-in report [:analysis :return :kind]) => "shaped"))

^{:refer rt.postgres.grammar.typed-analyze/infer-report :added "4.1"}
(fact "infer-report follows alias-qualified same-name functions without false recursion"
  (types/clear-registry!)
  (let [user-table (types/make-table-def "test" "User"
                                         [(types/make-column-def :id
                                                                 (types/make-type-ref :primitive nil :uuid)
                                                                 {:required true :primary true})
                                          (types/make-column-def :handle
                                                                 (types/make-type-ref :primitive nil :text)
                                                                 {})]
                                         :id)
        core-fn (types/make-fn-def "test.core" "user-set-handle"
                                   [(types/->FnArg 'i-user-id :uuid nil :payload)
                                    (types/->FnArg 'i-handle :text nil :payload)
                                    (types/->FnArg 'o-op :jsonb nil :track)]
                                   [:jsonb]
                                   {:raw-body '[(pg/t:update User
                                                            {:set {:handle i-handle}
                                                             :where {:id i-user-id}
                                                             :single true})]}
                                   "test")
        rpc-fn (types/make-fn-def "test.rpc" "user-set-handle"
                                  [(types/->FnArg 'handle :text nil :payload)]
                                  [:jsonb]
                                  {:raw-body '[(fuc/user-set-handle auth-user-id handle auth-op)]
                                   :aliases {'fuc 'test.core}}
                                  "test.rpc")]
    (types/register-type! 'test/User user-table)
    (types/register-type! 'test.core/user-set-handle core-fn)
    (types/register-type! 'test.rpc/user-set-handle rpc-fn)
    (let [report (analyze/infer-report rpc-fn)]
      (get-in report [:analysis :return :kind]) => "shaped"
      (get-in report [:analysis :return :table]) => "User")))

^{:refer rt.postgres.grammar.typed-analyze/infer-report :added "4.1"}
(fact "infer-report groups nested operations by type"
  (types/clear-registry!)
  (let [user-table (types/make-table-def "test" "User"
                                         [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid)
                                                                 {:required true :primary true})
                                          (types/make-column-def :handle (types/make-type-ref :primitive nil :text)
                                                                 {:required true})]
                                         :id)
        update-fn (types/make-fn-def "test.core" "update-user-raw"
                                     [(types/->FnArg 'i-user-id :uuid nil :payload)
                                      (types/->FnArg 'm :jsonb nil :payload)
                                      (types/->FnArg 'o-op :jsonb nil :track)]
                                     [:jsonb]
                                     {:raw-body '[(pg/t:update User
                                                              {:set m
                                                               :where {:id i-user-id}
                                                               :single true})]}
                                     "test")
        create-fn (types/make-fn-def "test.core" "create-user"
                                     [(types/->FnArg 'i-user-id :uuid nil :payload)
                                      (types/->FnArg 'm :jsonb nil :payload)
                                      (types/->FnArg 'o-op :jsonb nil :track)]
                                     [:jsonb]
                                     {:raw-body '[(pg/t:insert User
                                                              {:id i-user-id
                                                               :handle (:->> m "handle")}
                                                              {:track o-op})]}
                                     "test")
        set-user-fn (types/make-fn-def "test.core" "set-user"
                                       [(types/->FnArg 'i-user-id :uuid nil :payload)
                                        (types/->FnArg 'm :jsonb nil :payload)
                                        (types/->FnArg 'o-op :jsonb nil :track)]
                                       [:jsonb]
                                       {:raw-body '[(let [o-user (test.core/update-user-raw i-user-id m o-op)
                                                          _      (when [o-user :is-null]
                                                                   (return (test.core/create-user i-user-id m o-op)))]
                                                      (return o-user))]}
                                       "test")
        wrapper-fn (types/make-fn-def "test.rpc" "user-set-handle"
                                      [(types/->FnArg 'handle :text nil :payload)]
                                      [:jsonb]
                                      {:raw-body '[(test.core/set-user auth-user-id {:handle handle} auth-op)]}
                                      "test.rpc")]
    (types/register-type! 'test/User user-table)
    (types/register-type! 'test.core/update-user-raw update-fn)
    (types/register-type! 'test.core/create-user create-fn)
    (types/register-type! 'test.core/set-user set-user-fn)
    (types/register-type! 'test.rpc/user-set-handle wrapper-fn)
    (let [report (analyze/infer-report wrapper-fn)]
      (get-in report [:analysis :mutating]) => true
      (get-in report [:analysis :operations-by-type "update" 0 :table]) => "User"
      (get-in report [:analysis :operations-by-type "insert" 0 :table]) => "User")))

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

^{:refer rt.postgres.grammar.typed-analyze/analyze-expr :added "4.1"}
(fact "analyze-expr turns all-symbol sets into keyed objects"
  (let [u1-shape (types/make-jsonb-shape {:id {:type :uuid}} "User")
        u2-shape (types/make-jsonb-shape {:name {:type :text}} "User")
        ctx (types/make-context {'v-u1 :jsonb
                                 'v-u2 :jsonb}
                                {'v-u1 u1-shape
                                 'v-u2 u2-shape}
                                {})
        result (analyze/analyze-expr '#{v-u1 v-u2} ctx)]
    (:kind result) => :shaped
    (:op result) => :set-object
    (get-in result [:shape :fields :u1 :shape :fields :id :type]) => :uuid
    (get-in result [:shape :fields :u2 :shape :fields :name :type]) => :text))

^{:refer rt.postgres.grammar.typed-analyze/analyze-expr :added "4.1"}
(fact "analyze-expr merges duplicate derived keys from symbol sets"
  (let [u1-a-shape (types/make-jsonb-shape {:id {:type :uuid}} "User")
        u1-b-shape (types/make-jsonb-shape {:name {:type :text}} "User")
        ctx (types/make-context {'v-u1 :jsonb
                                 'o-u1 :jsonb}
                                {'v-u1 u1-a-shape
                                 'o-u1 u1-b-shape}
                                {})
        result (analyze/analyze-expr '#{v-u1 o-u1} ctx)]
    (get-in result [:shape :fields :u1 :shape :fields :id :type]) => :uuid
    (get-in result [:shape :fields :u1 :shape :fields :name :type]) => :text))

^{:refer rt.postgres.grammar.typed-analyze/analyze-expr :added "4.1"}
(fact "analyze-expr keeps merge behavior for mixed sets"
  (let [u1-shape (types/make-jsonb-shape {:id {:type :uuid}} "User")
        ctx (types/make-context {'v-u1 :jsonb}
                                {'v-u1 u1-shape}
                                {})
        result (analyze/analyze-expr '#{v-u1 {:extra "x"}} ctx)]
    (:op result) => :set-merge
    (get-in result [:shape :fields :extra :type]) => :text
    (get-in result [:shape :fields :u1]) => nil))

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

^{:refer rt.postgres.grammar.typed-analyze/infer-return-type :added "4.1"}
(fact "infer-return-type auto-registers referenced function namespaces"
  (types/clear-registry!)
  (let [form '(defn.pg
                call-insert
                [:text i-name :jsonb i-tags :jsonb o-op]
                (rt.postgres.script.test.scratch-v2/insert-entry i-name i-tags o-op))
        fn-def (parse/parse-defn form "test.ns" nil)]
    (types/register-type! 'test.ns/call-insert fn-def)
    (let [result (analyze/infer-return-type fn-def)]
      (types/jsonb-shape? result) => true
      (:source-table result) => "Entry")))


^{:refer rt.postgres.grammar.typed-analyze/resolve-table :added "4.1"}
(fact "resolve-table finds table definitions from symbols and keywords"
  (types/clear-registry!)
  (let [table-def (types/make-table-def "test.ns" "User" [] :id)]
    (types/register-type! 'test.ns/User table-def)

    ;; Resolves symbol directly
    (:name (analyze/resolve-table 'test.ns/User)) => "User"

    ;; Resolves quoted symbol
    (:name (analyze/resolve-table ''test.ns/User)) => "User"

    ;; Resolves keyword
    (:name (analyze/resolve-table :User)) => "User"

    ;; Returns nil for unknown table
    (analyze/resolve-table 'UnknownTable) => nil))

^{:refer rt.postgres.grammar.typed-analyze/analyzed->shape :added "4.1"}
(fact "analyzed->shape extracts JsonbShape from analyzed values"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} "User")]
    ;; From shaped result
    (analyze/analyzed->shape {:kind :shaped :shape shape}) => shape

    ;; From jsonb-shape directly
    (analyze/analyzed->shape shape) => shape

    ;; Returns nil for non-shaped values
    (analyze/analyzed->shape {:kind :primitive :type :uuid}) => nil))

^{:refer rt.postgres.grammar.typed-analyze/analyzed->field-info :added "4.1"}
(fact "analyzed->field-info extracts field info from analyzed values"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} "User")]
    ;; From shaped result
    (analyze/analyzed->field-info {:kind :shaped :shape shape})
    => {:type :jsonb :shape shape}

    ;; From primitive
    (analyze/analyzed->field-info {:kind :primitive :type :uuid})
    => {:type :uuid}

    ;; From field-access
    (analyze/analyzed->field-info {:kind :field-access :field "name" :type :text})
    => {:type :text}

    ;; From array
    (analyze/analyzed->field-info {:kind :array :element-type {:kind :primitive :type :uuid}})
    => {:type :array :items {:type :uuid}}))

^{:refer rt.postgres.grammar.typed-analyze/value->field-info :added "4.1"}
(fact "value->field-info is an alias for analyzed->field-info"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}})]
    (analyze/value->field-info {:kind :shaped :shape shape})
    => {:type :jsonb :shape shape}))

^{:refer rt.postgres.grammar.typed-analyze/merge-analyzed-shapes :added "4.1"}
(fact "merge-analyzed-shapes combines multiple analyzed values into one shape"
  (let [shape1 (types/make-jsonb-shape {:id {:type :uuid}})
        shape2 (types/make-jsonb-shape {:name {:type :text}})]
    ;; Merges shapes from analyzed values
    (let [merged (analyze/merge-analyzed-shapes [{:kind :shaped :shape shape1}
                                                 {:kind :shaped :shape shape2}])]
      (types/jsonb-shape? merged) => true
      (contains? (:fields merged) :id) => true
      (contains? (:fields merged) :name) => true)

    ;; Returns nil for empty input
    (analyze/merge-analyzed-shapes []) => nil

    ;; Returns nil when no shapes found
    (analyze/merge-analyzed-shapes [{:kind :primitive :type :uuid}]) => nil))

^{:refer rt.postgres.grammar.typed-analyze/merge-array-element-types :added "4.1"}
(fact "merge-array-element-types combines array element types"
  ;; Merges two shapes
  (let [shape1 (types/make-jsonb-shape {:id {:type :uuid}})
        shape2 (types/make-jsonb-shape {:name {:type :text}})
        merged (analyze/merge-array-element-types
                {:kind :shaped :shape shape1}
                {:kind :shaped :shape shape2})]
    (types/jsonb-shape? (:shape merged)) => true)

  ;; Handles nil left
  (analyze/merge-array-element-types nil {:kind :primitive :type :uuid})
  => {:kind :primitive :type :uuid}

  ;; Handles nil right
  (analyze/merge-array-element-types {:kind :primitive :type :uuid} nil)
  => {:kind :primitive :type :uuid}

  ;; Returns union for incompatible types
  (let [result (analyze/merge-array-element-types
                {:kind :primitive :type :uuid}
                {:kind :primitive :type :text})]
    (:kind result) => :union))

^{:refer rt.postgres.grammar.typed-analyze/literal-map-key :added "4.1"}
(fact "literal-map-key normalizes keys for map literals"
  ;; String keys preserved
  (analyze/literal-map-key "literal-key") => "literal-key"

  ;; Keyword keys converted
  (analyze/literal-map-key :foo-bar) => :foo-bar
  (analyze/literal-map-key :my-ns/foo) => "my-ns/foo"

  ;; Symbol keys converted to name
  (analyze/literal-map-key 'my-symbol) => "my-symbol"

  ;; Other values passed through
  (analyze/literal-map-key 42) => 42)

^{:refer rt.postgres.grammar.typed-analyze/resolve-called-fn :added "4.1"}
(fact "resolve-called-fn resolves function references with aliases"
  (types/clear-registry!)
  (let [fn-def (types/make-fn-def "test.ns" "my-fn" [] :jsonb {} nil)]
    (types/register-type! 'test.ns/my-fn fn-def)

    ;; Resolves fully qualified symbol
    (let [[resolved fn-result] (analyze/resolve-called-fn 'test.ns/my-fn {})]
      resolved => 'test.ns/my-fn
      (:name fn-result) => "my-fn")

    ;; Resolves with alias
    (let [[resolved fn-result] (analyze/resolve-called-fn 'alias/my-fn {'alias "test.ns"})]
      resolved => (symbol "test.ns" "my-fn"))

    ;; Returns nil for unknown function
    (let [[resolved fn-result] (analyze/resolve-called-fn 'unknown/fn {})]
      fn-result => nil)))

^{:refer rt.postgres.grammar.typed-analyze/analyze-table-op :added "4.1"}
(fact "analyze-table-op returns appropriate shape for table operations"
  (types/clear-registry!)
  (let [table-def (types/make-table-def "test.ns" "Entry"
                                        [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid)
                                                                {:required true :constraints {:primary true}})
                                         (types/make-column-def :name (types/make-type-ref :primitive nil :text)
                                                                {:required true})]
                                        :id)]
    (types/register-type! 'test.ns/Entry table-def)
    (let [ctx (types/make-context)]
      ;; Insert returns shaped result
      (:kind (analyze/analyze-table-op 'pg/t:insert ['test.ns/Entry] ctx)) => :shaped

      ;; Select returns array
      (:kind (analyze/analyze-table-op 'pg/t:select ['test.ns/Entry] ctx)) => :array

      ;; Id returns uuid
      (:kind (analyze/analyze-table-op 'pg/t:id ['test.ns/Entry] ctx)) => :primitive
      (:type (analyze/analyze-table-op 'pg/t:id ['test.ns/Entry] ctx)) => :uuid

      ;; Exists returns boolean
      (:type (analyze/analyze-table-op 'pg/t:exists ['test.ns/Entry] ctx)) => :boolean

      ;; Count returns integer
      (:type (analyze/analyze-table-op 'pg/t:count ['test.ns/Entry] ctx)) => :integer)))

^{:refer rt.postgres.grammar.typed-analyze/analyze-jsonb-merge :added "4.1"}
(fact "analyze-jsonb-merge combines shapes from merge arguments"
  (let [ctx (types/make-context)]
    ;; Merges map literals
    (let [result (analyze/analyze-jsonb-merge [{:id 1} {:name "test"}] ctx)]
      (:kind result) => :shaped
      (types/jsonb-shape? (:shape result)) => true)

    ;; Empty merge still returns shaped
    (let [result (analyze/analyze-jsonb-merge [] ctx)]
      (:kind result) => :shaped
      (:op result) => :merge)))

^{:refer rt.postgres.grammar.typed-analyze/analyze-jsonb-access :added "4.1"}
(fact "analyze-jsonb-access analyzes JSONB field access operators"
  (let [ctx (types/make-context {'m :jsonb}
                                {'m (types/make-jsonb-shape {:data {:type :jsonb}})}
                                {})]
    ;; :-> returns jsonb
    (let [result (analyze/analyze-jsonb-access :-> '(m "data") ctx)]
      (:kind result) => :field-access
      (:type result) => :jsonb)

    ;; :->> returns text
    (let [result (analyze/analyze-jsonb-access :->> '(m "name") ctx)]
      (:type result) => :text)))

^{:refer rt.postgres.grammar.typed-analyze/analyze-jsonb-accessor-expr :added "4.1"}
(fact "analyze-jsonb-accessor-expr analyzes JSONB accessor expressions"
  (let [shape (types/make-jsonb-shape {:organisation {:type :uuid}})
        ctx (types/make-context {'m :jsonb}
                                {'m shape}
                                {'m (types/make-jsonb-path [] 'm)})]
    ;; Analyzes pg/field-id access
    (let [result (analyze/analyze-jsonb-accessor-expr '(pg/field-id m "organisation") ctx)]
      (or (nil? result)
          (= (:kind result) :field-access)) => true)))

^{:refer rt.postgres.grammar.typed-analyze/analyze-let :added "4.1"}
(fact "analyze-let analyzes let bindings and body"
  
  (let [ctx (types/make-context)
        result (analyze/analyze-let '[x 1] '[(return x)] ctx)]
     result)
  => {:kind :literal, :type :integer, :value 1}

  ;; JSONB binding with destructuring - nil when no descriptor match
  (let [ctx-with-jsonb (types/make-context {'m :jsonb}
                                           {'m (types/make-jsonb-shape {:id {:type :uuid}})}
                                           {'m (types/make-jsonb-path [] 'm)})
        result (analyze/analyze-let '[n (:-> m "id")] '[(return n)] ctx-with-jsonb)]
    result)
  => :jsonb)

^{:refer rt.postgres.grammar.typed-analyze/analyze-control-flow :added "4.1"}
(fact "analyze-control-flow analyzes conditional expressions"
  (let [ctx (types/make-context)]
    ;; If with different map shapes returns union
    (let [result (analyze/analyze-control-flow '(if true {:a 1} {:b 2}) ctx)]
      ;; Maps with different keys produce union kind
      (:kind result) => :union)

    ;; If with different types returns union
    (let [result (analyze/analyze-control-flow '(if true 1 "two") ctx)]
      (or (= (:kind result) :union)
          (= (:kind result) :literal)) => true)

    ;; When with single branch - returns literal for map
    (let [result (analyze/analyze-control-flow '(when true {:x 1}) ctx)]
      (:kind result) => :literal)))

^{:refer rt.postgres.grammar.typed-analyze/normalize-table-name :added "4.1"}
(fact "normalize-table-name extracts table name from expressions"
  ;; Symbol
  (analyze/normalize-table-name 'User) => "User"

  ;; Keyword
  (analyze/normalize-table-name :Entry) => "Entry"

  ;; Quoted symbol
  (analyze/normalize-table-name ''MyTable) => "MyTable"

  ;; Nil for invalid
  (analyze/normalize-table-name 123) => nil)

^{:refer rt.postgres.grammar.typed-analyze/inferred->report :added "4.1"}
(fact "inferred->report converts inferred types to JSON-friendly format"
  ;; JsonbShape
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} "User")
        result (analyze/inferred->report shape)]
    (:kind result) => "shaped"
    (contains? result :shape) => true)

  ;; JsonbArray
  (let [arr (types/make-jsonb-array (types/make-jsonb-shape {:id {:type :uuid}}))
        result (analyze/inferred->report arr)]
    (:kind result) => "array")

  ;; TypeUnion
  (let [union (types/make-type-union [:uuid :text])
        result (analyze/inferred->report union)]
    (:kind result) => "union")

  ;; Regular map
  (let [result (analyze/inferred->report {:kind :primitive :type :uuid})]
    (:kind result) => "primitive"
    (:type result) => "uuid")

  ;; Nil
  (analyze/inferred->report nil) => nil)


^{:refer rt.postgres.grammar.typed-analyze/projected-js-select-shape :added "4.1"}
(fact "projected-js-select-shape projects fields from the analyzed source"
  (let [shape (types/make-jsonb-shape
               {:id {:type :uuid :nullable? false}
                :name {:type :text :nullable? true}}
               "User")
        ctx (types/make-context {'m :jsonb}
                                {'m shape}
                                {'m (types/make-jsonb-path [] 'm)})]
    (types/jsonb-shape? (analyze/projected-js-select-shape ctx 'm '(js ["id" "name"]))) => true
    (get-in (analyze/projected-js-select-shape ctx 'm '(js ["id" "name"]))
            [:fields :id :type]) => :uuid
    (get-in (analyze/projected-js-select-shape ctx 'm '(js ["id" "name"]))
            [:fields :name :type]) => :text))


^{:refer rt.postgres.grammar.typed-analyze/analyze-symbol-set :added "4.1"}
(fact "analyze-symbol-set turns all-symbol sets into keyed jsonb shapes"
  (let [u1-shape (types/make-jsonb-shape {:id {:type :uuid}} "User")
        u2-shape (types/make-jsonb-shape {:name {:type :text}} "User")
        ctx (types/make-context {'v-u1 :jsonb
                                 'o-u2 :jsonb}
                                {'v-u1 u1-shape
                                 'o-u2 u2-shape}
                                {})
        result (analyze/analyze-symbol-set '#{v-u1 o-u2} ctx)]
    (types/jsonb-shape? result) => true
    (get-in result [:fields :u1 :shape :fields :id :type]) => :uuid
    (get-in result [:fields :u2 :shape :fields :name :type]) => :text))
