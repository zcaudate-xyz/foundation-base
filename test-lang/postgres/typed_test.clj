(ns postgres.typed-test
  (:require [clojure.string :as str]
            [postgres.typed.typed-common :as types]
            [postgres.typed.typed-parse :as parse]
            [postgres.typed :as typed])
  (:use code.test))

^{:refer postgres.typed/load-file :added "4.1"}
(fact "creates a postgres typed context from a source file"
  (let [ctx (typed/load-file "src-lang/postgres/sample/scratch_v2.clj")]
    [(= :postgres (:domain ctx))
     (map? (:registry ctx))
     (pos? (count (typed/entries ctx)))])
  => [true true true])

^{:refer postgres.typed/load-ns :added "4.1"}
(fact "creates a postgres typed context from a namespace"
  (let [ctx (typed/load-ns 'postgres.sample.scratch-v2)]
    [(:domain ctx)
     (some? (typed/entry ctx 'postgres.sample.scratch-v2/insert-entry))])
  => [:postgres true])

^{:refer postgres.typed/load-app :added "4.1"}
(fact "creates a postgres typed context from an app typed payload"
  (let [typed-payload (types/analysis->typed
                       (parse/analyze-namespace 'postgres.sample.scratch-v2))]
    (with-redefs [hara.runtime.postgres.base.application/app-typed (fn [_] typed-payload)]
      (let [ctx (typed/load-app "demo")]
        [(:app-name ctx)
         (some? (typed/function-def ctx 'postgres.sample.scratch-v2/insert-entry))]))
    => ["demo" true]))

^{:refer postgres.typed/function-report :added "4.1"}
(fact "returns function-level infer reports from a context"
  (let [ctx (typed/load-ns 'postgres.sample.scratch-v2)
        report (typed/function-report ctx 'postgres.sample.scratch-v2/insert-entry)]
    [(get-in report [:function :name])
     (get-in report [:analysis :mutating])
     (get-in report [:analysis :source-tables])])
  => ["insert-entry" true ["Entry"]])

^{:refer postgres.typed/report-json :added "4.1"}
(fact "serializes context reports"
  (let [ctx (typed/load-ns 'postgres.sample.scratch-v2)
        output (typed/report-json
                (typed/function-report ctx 'postgres.sample.scratch-v2/insert-entry)
                true)]
    [(string? output)
     (str/includes? output "\"insert-entry\"")])
  => [true true])

^{:refer postgres.typed/function-def :added "4.1"}
(fact "resolves function definitions from a postgres typed context"
  (:name (typed/function-def (typed/load-ns 'postgres.sample.scratch-v2)
                             'postgres.sample.scratch-v2/insert-entry))
  => "insert-entry")

^{:refer postgres.typed/function-contract :added "4.1"}
(fact "function-contract includes grouped nested operations"
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
                                      "test.rpc")
        ctx (typed/load-registry {'test/User user-table
                                  'test.core/update-user-raw update-fn
                                  'test.core/create-user create-fn
                                  'test.core/set-user set-user-fn
                                  'test.rpc/user-set-handle wrapper-fn})
        report (typed/function-contract ctx wrapper-fn)]
    [(get-in report [:analysis :mutating])
     (get-in report [:analysis :operations-by-type "update" 0 :table])
     (get-in report [:analysis :operations-by-type "insert" 0 :table])])
  => [true "User" "User"])

^{:refer postgres.typed/input-shape :added "4.1"}
(fact "input-shape projects update columns and keeps helper-chain narrowing intact"
  (let [user-columns [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid) {:required true :primary true})
                      (types/make-column-def :type (types/make-type-ref :primitive nil :text) {:required true})
                      (types/make-column-def :handle (types/make-type-ref :primitive nil :text) {})
                      (types/make-column-def :color (types/make-type-ref :primitive nil :text) {})
                      (types/make-column-def :is-active (types/make-type-ref :primitive nil :boolean) {})
                      (types/make-column-def :is-official (types/make-type-ref :primitive nil :boolean) {})
                      (types/make-column-def :is-onboarded (types/make-type-ref :primitive nil :boolean) {})
                      (types/make-column-def :is-super (types/make-type-ref :primitive nil :boolean) {})
                      (types/make-column-def :first-name (types/make-type-ref :primitive nil :text) {})
                      (types/make-column-def :last-name (types/make-type-ref :primitive nil :text) {})
                      (types/make-column-def :country-code (types/make-type-ref :primitive nil :citext) {})
                      (types/make-column-def :location (types/make-type-ref :primitive nil :jsonb) {})
                      (types/make-column-def :bio (types/make-type-ref :primitive nil :text) {})
                      (types/make-column-def :picture (types/make-type-ref :primitive nil :text) {})
                      (types/make-column-def :detail (types/make-type-ref :primitive nil :jsonb) {})]
        user-table (types/make-table-def "test.ns" "User" user-columns :id)
        update-def (parse/parse-defn '(defn.pg ^{:%% :sql :- User}
                                        update-user-raw
                                        [:uuid i-user-id :jsonb m :jsonb o-op]
                                        (pg/t:update User
                                          {:set m
                                           :where {:id i-user-id}
                                           :track o-op
                                           :columns [:type :handle :color :is-active :is-official
                                                     :is-onboarded :is-super :first-name :last-name
                                                     :country-code :location :bio :picture :detail]
                                           :single true
                                           :coalesce true}))
                                      "test.ns" nil)
        set-def (parse/parse-defn '(defn.pg set-user
                                     [:uuid i-user-id :jsonb m :jsonb o-op]
                                     (return (update-user-raw i-user-id m o-op)))
                                   "test.ns" nil)
        public-def (parse/parse-defn '(defn.pg user-set-public
                                        [:uuid i-user-id :jsonb m :jsonb o-op]
                                        (let [v-m (fu/js-select m (js ["type" "color" "is_active"
                                                                       "is_onboarded" "first_name"
                                                                       "last_name" "country_code"
                                                                       "location" "bio" "picture" "detail"]))]
                                          (return (set-user i-user-id v-m o-op))))
                                      "test.ns" nil)
        js-select-def (types/make-fn-def "test.ns" "js-select"
                                         [(types/->FnArg 'm :jsonb [:jsonb] :payload)
                                          (types/->FnArg 'keys :jsonb [:jsonb] :payload)]
                                         :jsonb {} nil)
        ctx (typed/load-registry {'test.ns/User user-table
                                  'test.ns/js-select js-select-def
                                  'test.ns/update-user-raw update-def
                                  'test.ns/set-user set-def
                                  'test.ns/user-set-public public-def})
        update-shape (typed/input-shape ctx 'test.ns/update-user-raw 'm)
        set-shape (typed/input-shape ctx 'test.ns/set-user 'm)
        public-shape (typed/input-shape ctx 'test.ns/user-set-public 'm)
        public-type (typed/function-contract ctx public-def)]
    [(set (keys (:fields update-shape)))
     (set (keys (:fields set-shape)))
     (set (keys (:fields public-shape)))
     (get-in public-type [:input :schemas :o-op :role])
     (nil? (get-in public-shape [:fields :handle]))])
  => [#{:type :handle :color :is-active :is-official :is-onboarded :is-super
        :first-name :last-name :country-code :location :bio :picture :detail}
      #{:type :handle :color :is-active :is-official :is-onboarded :is-super
        :first-name :last-name :country-code :location :bio :picture :detail}
      #{:type :color :is-active :is-onboarded :first-name :last-name :country-code
        :location :bio :picture :detail}
      :track
      true])

^{:refer postgres.typed/input-schema :added "4.1"}
(fact "formats inferred input shapes from namespace and app contexts"
  (let [ctx (typed/load-ns 'postgres.sample.scratch-v2)
        typed-payload (types/analysis->typed
                       (parse/analyze-namespace 'postgres.sample.scratch-v2))]
    [(get-in (typed/input-schema ctx 'postgres.sample.scratch-v2/insert-task-raw 'm :openapi)
             [:properties "id" :format])
     (with-redefs [hara.runtime.postgres.base.application/app-typed (fn [_] typed-payload)]
       (get-in (typed/input-schema (typed/load-app "demo")
                                   'postgres.sample.scratch-v2/insert-task-raw
                                   'm
                                   :json-schema)
               [:properties "id" :format]))])
  => ["uuid" "uuid"])

^{:refer postgres.typed/output-shape :added "4.1"}
(fact "infers output shapes from namespace and app contexts"
  (let [form '(defn.pg
                prepare-topic
                "prepares a topic payload"
                [:jsonb m]
                (let [(:uuid v-organisation-id) (pg/field-id m "organisation")
                      #{(:text v-code)} m]
                  (return
                   (|| {:publish "none"}
                       m
                       {:code-full v-code
                        :organisation-id v-organisation-id}))))
        fn-def (parse/parse-defn form "test.ns" nil)
        ctx (typed/load-registry {'test.ns/prepare-topic fn-def})
        typed-payload {:tables {} :enums {} :functions {'test.ns/prepare-topic fn-def}}]
    [(-> (typed/output-shape ctx 'test.ns/prepare-topic) :fields keys set)
     (with-redefs [hara.runtime.postgres.base.application/app-typed (fn [_] typed-payload)]
       (-> (typed/output-shape (typed/load-app "demo") 'test.ns/prepare-topic)
           :fields
           keys
           set))])
  => [#{:publish :organisation :organisation_id :code :code-full :organisation-id}
      #{:publish :organisation :organisation_id :code :code-full :organisation-id}])

^{:refer postgres.typed/output-schema :added "4.1"}
(fact "formats inferred output schemas"
  (let [ctx (typed/load-ns 'postgres.sample.scratch-v2)]
    [(get-in (typed/output-schema ctx 'postgres.sample.scratch-v2/insert-task-raw :openapi)
             [:properties "name" :type])
     (string? (typed/output-schema ctx 'postgres.sample.scratch-v2/insert-task-raw :typescript))])
  => ["string" true])

^{:refer postgres.typed/export-openapi :added "4.1"}
(fact "exports schemas from a postgres context"
  (let [ctx (typed/load-ns 'postgres.sample.scratch-v2)
        openapi (typed/export-openapi ctx (constantly true))
        json-schema (typed/export-json-schema ctx)
        ts (typed/export-typescript ctx)]
    [(contains? openapi :openapi)
     (map? json-schema)
     (str/includes? ts "interface")])
  => [true true true])


^{:refer postgres.typed/inferred->shape :added "4.1"}
(fact "unwraps shaped inference values"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} "Entry")]
    [(typed/inferred->shape shape)
     (typed/inferred->shape {:kind :shaped :shape shape})
     (typed/inferred->shape {:kind :primitive})])
  => [(types/make-jsonb-shape {:id {:type :uuid}} "Entry")
      (types/make-jsonb-shape {:id {:type :uuid}} "Entry")
      nil])

^{:refer postgres.typed/format-shape :added "4.1"}
(fact "formats jsonb shapes for supported schema targets"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} "Entry")]
    [(typed/format-shape shape :shape)
     (get-in (typed/format-shape shape :openapi) [:properties "id" :format])
     (get-in (typed/format-shape shape :json-schema) [:properties "id" :format])
     (string? (typed/format-shape shape :typescript))])
  => [(types/make-jsonb-shape {:id {:type :uuid}} "Entry") "uuid" "uuid" true])

^{:refer postgres.typed/registry->typed :added "4.1"}
(fact "groups flat registry entries into a typed payload"
  (let [table (types/make-table-def "demo" "Entry" [] :id)
        fn-def (types/make-fn-def "demo" "insert-entry" [] [:jsonb] {} nil)
        typed-payload (typed/registry->typed {'demo/Entry table
                                              'demo/insert-entry fn-def})]
    [(contains? (:tables typed-payload) 'demo/Entry)
     (contains? (:functions typed-payload) 'demo/insert-entry)])
  => [true true])

^{:refer postgres.typed/typed->registry :added "4.1"}
(fact "flattens typed payload sections into one registry"
  (let [table (types/make-table-def "demo" "Entry" [] :id)
        fn-def (types/make-fn-def "demo" "insert-entry" [] [:jsonb] {} nil)]
    (set (keys (typed/typed->registry {:tables {'demo/Entry table}
                                       :enums {}
                                       :functions {'demo/insert-entry fn-def}}))))
  => '#{demo/Entry demo/insert-entry})

^{:refer postgres.typed/load-analysis :added "4.1"}
(fact "creates a context from parsed postgres analysis"
  (let [analysis (parse/analyze-namespace 'postgres.sample.scratch-v2)
        ctx (typed/load-analysis analysis)]
    [(:domain ctx)
     (= analysis (:analysis ctx))
     (some? (typed/entry ctx 'postgres.sample.scratch-v2/insert-entry))])
  => [:postgres true true])

^{:refer postgres.typed/load-registry :added "4.1"}
(fact "creates a context from an explicit registry"
  (let [fn-def (types/make-fn-def "demo" "get-entry" [] [:jsonb] {} nil)
        ctx (typed/load-registry {'demo/get-entry fn-def})]
    [(:domain ctx)
     (= fn-def (typed/entry ctx 'demo/get-entry))])
  => [:postgres true])

^{:refer postgres.typed/with-context-registry :added "4.1"}
(fact "temporarily installs the context registry for inference internals"
  (let [fn-def (types/make-fn-def "demo" "get-entry" [] [:jsonb] {} nil)
        ctx (typed/load-registry {'demo/get-entry fn-def})]
    [(contains? @types/*type-registry* 'demo/get-entry)
     (typed/with-context-registry ctx #(contains? @types/*type-registry* 'demo/get-entry))
     (contains? @types/*type-registry* 'demo/get-entry)])
  => [false true false])

^{:refer postgres.typed/entries :added "4.1"}
(fact "returns all declarations from a postgres context"
  (let [ctx (typed/load-ns 'postgres.sample.scratch-v2)]
    (pos? (count (typed/entries ctx))))
  => true)

^{:refer postgres.typed/entry :added "4.1"}
(fact "resolves namespaced entries from a postgres context"
  (let [ctx (typed/load-ns 'postgres.sample.scratch-v2)]
    (:name (typed/entry ctx 'postgres.sample.scratch-v2/insert-entry)))
  => "insert-entry")

^{:refer postgres.typed/missing-function! :added "4.1"}
(fact "throws typed missing-function errors"
  (try
    (typed/missing-function! 'demo/missing)
    (catch clojure.lang.ExceptionInfo e
      (ex-data e)))
  => {:type :typed/missing-function
      :fn 'demo/missing})

^{:refer postgres.typed/missing-argument! :added "4.1"}
(fact "throws typed missing-argument errors"
  (try
    (typed/missing-argument! 'demo/get-entry 'm)
    (catch clojure.lang.ExceptionInfo e
      (ex-data e)))
  => {:type :typed/missing-argument
      :fn 'demo/get-entry
      :arg 'm})

^{:refer postgres.typed/function-input :added "4.1"}
(fact "returns all inputs or one named input from a context"
  (let [ctx (typed/load-ns 'postgres.sample.scratch-v2)]
    [(mapv :name (typed/function-input ctx 'postgres.sample.scratch-v2/insert-entry))
     (:name (typed/function-input ctx 'postgres.sample.scratch-v2/insert-entry 'i-tags))])
  => ['[i-name i-tags o-op] 'i-tags])

^{:refer postgres.typed/function-output :added "4.1"}
(fact "returns the declared function output from a context"
  (typed/function-output (typed/load-ns 'postgres.sample.scratch-v2)
                         'postgres.sample.scratch-v2/insert-entry)
  => '[:jsonb])

^{:refer postgres.typed/export-json-schema :added "4.1"}
(fact "exports JSON Schema from a postgres context"
  (map? (typed/export-json-schema (typed/load-ns 'postgres.sample.scratch-v2)))
  => true)

^{:refer postgres.typed/export-typescript :added "4.1"}
(fact "exports TypeScript declarations from a postgres context"
  (str/includes? (typed/export-typescript (typed/load-ns 'postgres.sample.scratch-v2))
                 "interface")
  => true)
