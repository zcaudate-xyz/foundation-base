(ns rt.postgres.typed-test
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-parse :as parse]
            [rt.postgres.script.test.scratch-v2 :as scratch]
            [rt.postgres.typed :as typed])
  (:use code.test))

^{:refer rt.postgres.typed/clear-registry! :added "4.1"}
(fact "clear-registry! empties the type registry"
  (typed/clear-registry!)
  (typed/register-type! 'test/Type (types/make-type-ref :primitive nil :test))
  (some? (typed/get-type 'test/Type)) => true
  (typed/clear-registry!)
  (typed/get-type 'test/Type) => nil)

^{:refer rt.postgres.typed/register-type! :added "4.1"}
(fact "register-type! adds a type to the registry"
  (typed/clear-registry!)
  (let [type-ref (types/make-type-ref :primitive nil :uuid)]
    (typed/register-type! 'test/Uuid type-ref)
    (typed/get-type 'test/Uuid) => type-ref))

^{:refer rt.postgres.typed/get-type :added "4.1"}
(fact "get-type retrieves a registered type"
  (typed/clear-registry!)
  (typed/get-type 'nonexistent/Type) => nil
  (let [type-ref (types/make-type-ref :primitive nil :text)]
    (typed/register-type! 'test/Text type-ref)
    (typed/get-type 'test/Text) => type-ref))

^{:refer rt.postgres.typed/analyze-file :added "4.1"}
(fact "analyze-file returns structure with tables, enums, and functions"
  (let [result (typed/analyze-file "src/rt/postgres/grammar/typed_common.clj")]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.typed/analyze-namespace :added "4.1"}
(fact "analyze-namespace analyzes a namespace and returns type definitions"
  (let [result (typed/analyze-namespace 'rt.postgres.grammar.typed-common)]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.typed/analyze-and-register! :added "4.1"}
(fact "analyze-and-register! analyzes and registers types from a namespace"
  (typed/clear-registry!)
  (let [result (typed/analyze-and-register! 'rt.postgres.grammar.typed-common)]
    ;; Result is the analysis map with tables, enums, functions
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.typed/make-function-report :added "4.1"}
(fact "make-function-report returns a function-level infer report"
  (let [report (typed/make-function-report 'rt.postgres.script.test.scratch-v2
                                           'insert-entry)]
    (get-in report [:function :name]) => "insert-entry"
    (get-in report [:analysis :mutating]) => true
    (get-in report [:analysis :source-tables]) => ["Entry"]))

^{:refer rt.postgres.typed/get-function-report :added "4.1"}
(fact "get-function-report lazily retrieves and caches infer reports for registered functions"
  (typed/clear-registry!)
  (let [analysis (-> 'rt.postgres.script.test.scratch-v2
                     parse/analyze-namespace
                     parse/register-types!)
        fn-def   (some #(when (= "insert-entry" (:name %)) %)
                       (:functions analysis))]
    (typed/register-type! 'rt.postgres.script.test.scratch-v2/insert-entry fn-def)
    (let [report (typed/get-function-report 'rt.postgres.script.test.scratch-v2/insert-entry)]
      (get-in report [:function :name]) => "insert-entry"
      (get-in report [:analysis :mutating]) => true)))

^{:refer rt.postgres.typed/inferred->shape :added "4.1"}
(fact "inferred->shape unwraps shaped inference results"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} "Entry")]
    (typed/inferred->shape shape) => shape
    (typed/inferred->shape {:kind :shaped :shape shape}) => shape
    (typed/inferred->shape {:kind :primitive}) => nil))

^{:refer rt.postgres.typed/format-shape :added "4.1"}
(fact "format-shape dispatches by output format"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} "Entry")]
    (typed/format-shape shape :shape) => shape
    (typed/format-shape shape :openapi) => {:type "object"
                                            :properties {"id" {:type "string"
                                                                :format "uuid"}}
                                            :required ["id"]}))

^{:refer rt.postgres.typed/get-app-function-report :added "4.1"}
(fact "get-app-function-report runs infer reporting against an app typed payload"
  (let [typed-payload (types/analysis->typed
                       (parse/analyze-namespace 'rt.postgres.script.test.scratch-v2))]
    (with-redefs [rt.postgres.grammar.common-application/app-typed (fn [_] typed-payload)]
      (let [report (typed/get-app-function-report "demo"
                                                  'rt.postgres.script.test.scratch-v2/insert-entry)]
        (get-in report [:function :name]) => "insert-entry"
        (get-in report [:analysis :mutating]) => true))))

^{:refer rt.postgres.typed/get-function-report-json :added "4.1"}
(fact "get-function-report-json serializes reports for registered functions"
  (typed/clear-registry!)
  (-> 'rt.postgres.script.test.scratch-v2
      parse/analyze-namespace
      parse/register-types!)
  (let [output (typed/get-function-report-json 'rt.postgres.script.test.scratch-v2/insert-entry
                                               true)]
    (string? output) => true
    (clojure.string/includes? output "\"insert-entry\"") => true))

^{:refer rt.postgres.typed/get-app-function-report-json :added "4.1"}
(fact "get-app-function-report-json serializes app-scoped reports"
  (let [typed-payload (types/analysis->typed
                       (parse/analyze-namespace 'rt.postgres.script.test.scratch-v2))]
    (with-redefs [rt.postgres.grammar.common-application/app-typed (fn [_] typed-payload)]
      (let [output (typed/get-app-function-report-json "demo"
                                                       'rt.postgres.script.test.scratch-v2/insert-entry
                                                       true)]
        (string? output) => true
        (clojure.string/includes? output "\"insert-entry\"") => true))))

^{:refer rt.postgres.typed/get-function-def :added "4.1"}
(fact "get-function-def returns a registered function definition"
  (typed/clear-registry!)
  (let [fn-def (types/make-fn-def "test" "get-user" [] [:jsonb] {} nil)]
    (typed/register-type! 'test/get-user fn-def)
    (typed/get-function-def 'test/get-user) => fn-def))

^{:refer rt.postgres.typed/fn-ref->fn-sym :added "4.1"}
(fact "fn-ref->fn-sym delegates to the resolve layer"
  (typed/fn-ref->fn-sym #'clojure.string/blank?) => 'clojure.string/blank?)

^{:refer rt.postgres.typed/resolve-function-def :added "4.1"}
(fact "resolve-function-def returns registered function defs"
  (typed/clear-registry!)
  (let [fn-def (types/make-fn-def "demo" "inner" [] [:jsonb] {} nil)]
    (typed/register-type! 'demo/inner fn-def)
    (typed/resolve-function-def 'demo/inner) => fn-def))

^{:refer rt.postgres.typed/get-app-function-def :added "4.1"}
(fact "get-app-function-def reads function defs directly from an app typed payload"
  (let [fn-def (types/make-fn-def "test.ns" "insert-task" [] :jsonb {} nil)
        typed-payload {:tables {}
                       :enums {}
                       :functions {'test.ns/insert-task fn-def}}]
    (with-redefs [rt.postgres.grammar.common-application/app-typed (fn [_] typed-payload)]
      (typed/get-app-function-def "demo" 'test.ns/insert-task) => fn-def)))

^{:refer rt.postgres.typed/with-app-typed-registry :added "4.1"}
(fact "with-app-typed-registry temporarily merges app types into the registry"
  (typed/clear-registry!)
  (let [table (types/make-table-def "demo" "Entry" [] :id)]
    (with-redefs [rt.postgres.grammar.common-application/app-typed
                  (fn [_] {:tables {'demo/Entry table}
                           :enums {}
                           :functions {}})]
      (let [seen (typed/with-app-typed-registry
                  "demo"
                  (fn [_]
                    (contains? @types/*type-registry* 'demo/Entry)))]
        seen => true))
    (contains? @types/*type-registry* 'demo/Entry) => false))

^{:refer rt.postgres.typed/get-function-input-shape :added "4.1"}
(fact "get-function-input-shape projects update columns and keeps helper-chain narrowing intact"
  (typed/clear-registry!)
  (let [user-columns [(types/make-column-def :id
                                             (types/make-type-ref :primitive nil :uuid)
                                             {:required true :primary true})
                      (types/make-column-def :type
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})
                      (types/make-column-def :handle
                                             (types/make-type-ref :primitive nil :text)
                                             {})
                      (types/make-column-def :color
                                             (types/make-type-ref :primitive nil :text)
                                             {})
                      (types/make-column-def :is-active
                                             (types/make-type-ref :primitive nil :boolean)
                                             {})
                      (types/make-column-def :is-official
                                             (types/make-type-ref :primitive nil :boolean)
                                             {})
                      (types/make-column-def :is-onboarded
                                             (types/make-type-ref :primitive nil :boolean)
                                             {})
                      (types/make-column-def :is-super
                                             (types/make-type-ref :primitive nil :boolean)
                                             {})
                      (types/make-column-def :first-name
                                             (types/make-type-ref :primitive nil :text)
                                             {})
                      (types/make-column-def :last-name
                                             (types/make-type-ref :primitive nil :text)
                                             {})
                      (types/make-column-def :country-code
                                             (types/make-type-ref :primitive nil :citext)
                                             {})
                      (types/make-column-def :location
                                             (types/make-type-ref :primitive nil :jsonb)
                                             {})
                      (types/make-column-def :bio
                                             (types/make-type-ref :primitive nil :text)
                                             {})
                      (types/make-column-def :picture
                                             (types/make-type-ref :primitive nil :text)
                                             {})
                      (types/make-column-def :detail
                                             (types/make-type-ref :primitive nil :jsonb)
                                             {})]
        user-table (types/make-table-def "test.ns" "User" user-columns :id)
        update-form '(defn.pg ^{:%% :sql :- User}
                       update-user-raw
                       [:uuid i-user-id
                        :jsonb m
                        :jsonb o-op]
                       (pg/t:update User
                         {:set m
                          :where {:id i-user-id}
                          :track o-op
                          :columns [:type
                                    :handle
                                    :color
                                    :is-active
                                    :is-official
                                    :is-onboarded
                                    :is-super
                                    :first-name
                                    :last-name
                                    :country-code
                                    :location
                                    :bio
                                    :picture
                                    :detail]
                          :single true
                          :coalesce true}))
        set-form '(defn.pg set-user
                    [:uuid i-user-id
                     :jsonb m
                     :jsonb o-op]
                    (return
                     (update-user-raw i-user-id m o-op)))
        public-form '(defn.pg user-set-public
                       [:uuid i-user-id
                        :jsonb m
                        :jsonb o-op]
                       (let [v-m (fu/js-select m (js ["type"
                                                      "color"
                                                      "is_active"
                                                      "is_onboarded"
                                                      "first_name"
                                                      "last_name"
                                                      "country_code"
                                                      "location"
                                                      "bio"
                                                      "picture"
                                                      "detail"]))]
                         (return
                          (set-user i-user-id v-m o-op))))
        js-select-def (types/make-fn-def "test.ns"
                                         "js-select"
                                         [(types/->FnArg 'm :jsonb [:jsonb] :payload)
                                          (types/->FnArg 'keys :jsonb [:jsonb] :payload)]
                                         :jsonb
                                         {} nil)
        update-def (parse/parse-defn update-form "test.ns" nil)
        set-def (parse/parse-defn set-form "test.ns" nil)
        public-def (parse/parse-defn public-form "test.ns" nil)
        full-fields #{:type :handle :color :is-active :is-official :is-onboarded
                      :is-super :first-name :last-name :country-code :location
                      :bio :picture :detail}
        public-fields #{:type :color :is-active :is-onboarded :first-name
                        :last-name :country-code :location :bio :picture :detail}]
    (types/register-type! 'test.ns/User user-table)
    (types/register-type! 'test.ns/js-select js-select-def)
    (types/register-type! 'test.ns/update-user-raw update-def)
    (types/register-type! 'test.ns/set-user set-def)
    (types/register-type! 'test.ns/user-set-public public-def)

    (let [update-shape (typed/get-function-input-shape 'test.ns/update-user-raw 'm)
          set-shape (typed/get-function-input-shape 'test.ns/set-user 'm)
          public-shape (typed/get-function-input-shape 'test.ns/user-set-public 'm)
          public-type (typed/Type public-def)]
      (types/jsonb-shape? update-shape) => true
      (types/jsonb-shape? set-shape) => true
      (types/jsonb-shape? public-shape) => true
      (some #(= :track (:role %)) (get-in public-type [:input :args])) => true
      (get-in public-type [:input :schemas :o-op :role]) => :track
      (nil? (get-in public-type [:input :schemas :o-op :shape])) => true

      (set (keys (:fields update-shape))) => full-fields
      (set (keys (:fields set-shape))) => full-fields
      (set (keys (:fields public-shape))) => public-fields

      (nil? (get-in public-shape [:fields :handle])) => true
      (nil? (get-in public-shape [:fields :is-official])) => true
      (nil? (get-in public-shape [:fields :is-super])) => true

      (get-in public-shape [:fields :type :type]) => :text
      (get-in public-shape [:fields :location :type]) => :jsonb
      (get-in public-shape [:fields :bio :type]) => :text)))

^{:refer rt.postgres.typed/get-app-function-input-shape :added "4.1"}
(fact "get-app-function-input-shape infers jsonb input shape from an app typed payload"
  (typed/clear-registry!)
  (let [task-columns [(types/make-column-def :id
                                             (types/make-type-ref :primitive nil :uuid)
                                             {:required true :primary true})
                      (types/make-column-def :status
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})]
        task-table (types/make-table-def "test.ns" "Task" task-columns :id)
        form '(defn.pg ^{:%% :sql :- Task}
                insert-task-raw
                "inserts a task"
                [:jsonb m]
                (pg/t:insert Task m))
        fn-def (parse/parse-defn form "test.ns" nil)
        typed-payload {:tables {'test.ns/Task task-table}
                       :enums {}
                       :functions {'test.ns/insert-task-raw fn-def}}]
    (with-redefs [rt.postgres.grammar.common-application/app-typed (fn [_] typed-payload)]
      (let [shape (typed/get-app-function-input-shape "demo" 'test.ns/insert-task-raw 'm)]
        (types/jsonb-shape? shape) => true
        (-> shape :fields keys set) => #{:id :status}))))

^{:refer rt.postgres.typed/get-function-input-schema :added "4.1"}
(fact "get-function-input-schema formats inferred input shapes"
  (typed/clear-registry!)
  (let [task-columns [(types/make-column-def :id
                                             (types/make-type-ref :primitive nil :uuid)
                                             {:required true :primary true})
                      (types/make-column-def :status
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})]
        task-table (types/make-table-def "test.ns" "Task" task-columns :id)
        form '(defn.pg ^{:%% :sql :- Task}
                insert-task-raw
                "inserts a task"
                [:jsonb m]
                (pg/t:insert Task m))
        fn-def (parse/parse-defn form "test.ns" nil)]
    (typed/register-type! 'test.ns/Task task-table)
    (typed/register-type! 'test.ns/insert-task-raw fn-def)
    (get-in (typed/get-function-input-schema 'test.ns/insert-task-raw 'm :openapi)
            [:properties "id" :format]) => "uuid"
    (get-in (typed/get-function-input-schema 'test.ns/insert-task-raw 'm :json-schema)
            [:properties "status" :type]) => "string"
    (string? (typed/get-function-input-schema 'test.ns/insert-task-raw 'm :typescript)) => true))

^{:refer rt.postgres.typed/get-app-function-input-schema :added "4.1"}
(fact "get-app-function-input-schema formats inferred app input shapes"
  (let [task-columns [(types/make-column-def :id
                                             (types/make-type-ref :primitive nil :uuid)
                                             {:required true :primary true})
                      (types/make-column-def :status
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})]
        task-table (types/make-table-def "test.ns" "Task" task-columns :id)
        fn-def (types/make-fn-def "test.ns"
                                  "insert-task-raw"
                                  [(types/->FnArg 'm :jsonb [:jsonb] :payload)]
                                  :jsonb
                                  {:api/meta {:table 'test.ns/Task}}
                                  nil)
        typed-payload {:tables {'test.ns/Task task-table}
                       :enums {}
                       :functions {'test.ns/insert-task-raw fn-def}}]
    (with-redefs [rt.postgres.grammar.common-application/app-typed (fn [_] typed-payload)]
      (get-in (typed/get-app-function-input-schema "demo"
                                                   'test.ns/insert-task-raw
                                                   'm
                                                   :openapi)
              [:properties "id" :format])
      => "uuid"

      (get-in (typed/get-app-function-input-schema "demo"
                                                   'test.ns/insert-task-raw
                                                   'm
                                                   :json-schema)
              [:properties "status" :type])
      => "string"

      (string? (typed/get-app-function-input-schema "demo"
                                                    'test.ns/insert-task-raw
                                                    'm
                                                    :typescript))
      => true)))

^{:refer rt.postgres.typed/get-function-output-shape :added "4.1"}
(fact "get-function-output-shape preserves nested jsonb refinement from plain child bindings"
  (typed/clear-registry!)
  (let [form '(defn.pg
                prepare-account
                "prepares an account payload"
                [:jsonb i-created]
                (let [#{o-profile
                        v-account
                        v-security} i-created
                      #{(:text v-password-salt)} v-security]
                  (return
                   (|| i-created
                       {:profile o-profile
                        :password-salt v-password-salt}))))
        fn-def (parse/parse-defn form "test.ns" nil)]
    (typed/register-type! 'test.ns/prepare-account fn-def)
    (let [shape (typed/get-function-output-shape 'test.ns/prepare-account)]
      (types/jsonb-shape? shape) => true
      (get-in shape [:fields :security :shape :fields :password-salt :type]) => :text
      (get-in shape [:fields :profile :type]) => :jsonb)))

^{:refer rt.postgres.typed/get-app-function-output-shape :added "4.1"}
(fact "get-app-function-output-shape infers output shapes from an app typed payload"
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
        typed-payload {:tables {}
                       :enums {}
                       :functions {'test.ns/prepare-topic fn-def}}]
    (with-redefs [rt.postgres.grammar.common-application/app-typed (fn [_] typed-payload)]
      (let [shape (typed/get-app-function-output-shape "demo" 'test.ns/prepare-topic)]
        (types/jsonb-shape? shape) => true
        (-> shape :fields keys set) => #{:publish
                                         :organisation
                                         :organisation_id
                                         :code
                                         :code-full
                                         :organisation-id}))))

^{:refer rt.postgres.typed/get-function-output-schema :added "4.1"}
(fact "get-function-output-schema formats inferred output shapes"
  (typed/clear-registry!)
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
        fn-def (parse/parse-defn form "test.ns" nil)]
    (typed/register-type! 'test.ns/prepare-topic fn-def)
    (get-in (typed/get-function-output-schema 'test.ns/prepare-topic :openapi)
            [:properties "organisation_id" :format]) => "uuid"
    (get-in (typed/get-function-output-schema 'test.ns/prepare-topic :json-schema)
            [:properties "publish" :type]) => "string"
    (string? (typed/get-function-output-schema 'test.ns/prepare-topic :typescript)) => true))

^{:refer rt.postgres.typed/get-app-function-output-schema :added "4.1"}
(fact "get-app-function-output-schema formats inferred app output shapes"
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
        typed-payload {:tables {}
                       :enums {}
                       :functions {'test.ns/prepare-topic fn-def}}]
    (with-redefs [rt.postgres.grammar.common-application/app-typed (fn [_] typed-payload)]
      (get-in (typed/get-app-function-output-schema "demo"
                                                    'test.ns/prepare-topic
                                                    :openapi)
              [:properties "organisation_id" :format])
      => "uuid"

      (get-in (typed/get-app-function-output-schema "demo"
                                                    'test.ns/prepare-topic
                                                    :json-schema)
              [:properties "publish" :type])
      => "string"

      (string? (typed/get-app-function-output-schema "demo"
                                                     'test.ns/prepare-topic
                                                     :typescript))
      => true)))

^{:refer rt.postgres.typed/report-json :added "4.1"}
(fact "report-json serializes reports"
  (let [report (typed/make-function-report 'rt.postgres.script.test.scratch-v2
                                           'insert-entry)
        output (typed/report-json report true)]
    (string? output) => true
    (clojure.string/includes? output "\"insert-entry\"") => true))

^{:refer rt.postgres.typed/make-function-json :added "4.1"}
(fact "make-function-json returns serialized analysis for a function"
  (let [output (typed/make-function-json 'rt.postgres.script.test.scratch-v2
                                         'insert-entry
                                         true)]
    (string? output) => true
    (clojure.string/includes? output "\"function\"") => true
    (clojure.string/includes? output "\"Entry\"") => true))

^{:refer rt.postgres.typed/make-openapi :added "4.1"}
(fact "make-openapi generates OpenAPI spec for a namespace"
  (let [spec (typed/make-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))]
    (contains? spec :openapi) => true
    (contains? spec :info) => true
    (contains? spec :paths) => true
    (contains? spec :components) => true))

^{:refer rt.postgres.typed/make-json-schema :added "4.1"}
(fact "make-json-schema generates JSON Schema definitions"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'rt.postgres.script.test.scratch-v2)
  (let [schema (typed/make-json-schema)]
    (map? schema) => true))

^{:refer rt.postgres.typed/make-typescript :added "4.1"}
(fact "make-typescript generates TypeScript definitions"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'rt.postgres.script.test.scratch-v2)
  (let [ts (typed/make-typescript)]
    (string? ts) => true
    (clojure.string/includes? ts "interface") => true))

^{:refer rt.postgres.typed/get-table-shape :added "4.1"}
(fact "get-table-shape returns shape for a registered table"
  (typed/clear-registry!)
  (let [table (types/make-table-def "test" "User"
                                    [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid) {:required true})]
                                    :id)]
    (typed/register-type! 'test/User table)
    (let [shape (typed/get-table-shape 'test/User)]
      (types/jsonb-shape? shape) => true
      (contains? (:fields shape) :id) => true)))

^{:refer rt.postgres.typed/list-tables :added "4.1"}
(fact "list-tables returns all registered table definitions"
  (typed/clear-registry!)
  (let [table (types/make-table-def "test" "User" [] :id)]
    (typed/register-type! 'test/User table)
    (let [tables (typed/list-tables)]
      (count tables) => 1
      (:name (first tables)) => "User")))

^{:refer rt.postgres.typed/list-functions :added "4.1"}
(fact "list-functions returns all registered function definitions"
  (typed/clear-registry!)
  (let [fn-def (types/make-fn-def "test" "get-user" [] [:jsonb] {} nil)]
    (typed/register-type! 'test/get-user fn-def)
    (let [fns (typed/list-functions)]
      (count fns) => 1
      (:name (first fns)) => "get-user")))

^{:refer rt.postgres.typed/list-enums :added "4.1"}
(fact "list-enums returns all registered enum definitions"
  (typed/clear-registry!)
  (let [enum (types/make-enum-def "test" "Status" #{:active :inactive} nil)]
    (typed/register-type! 'test/Status enum)
    (let [enums (typed/list-enums)]
      (count enums) => 1
      (:name (first enums)) => "Status")))

^{:refer rt.postgres.typed/load-runtime-tables :added "4.1"}
(fact "load-runtime-tables loads tables from runtime format"
  (let [tables-map {:User [:id {:type :uuid :primary true}
                           :name {:type :text}]
                    :Org [:id {:type :uuid :primary true}
                          :handle {:type :citext}]}
        loaded (typed/load-runtime-tables tables-map)]
    (count loaded) => 2
    (contains? loaded :User) => true
    (contains? loaded :Org) => true))

^{:refer rt.postgres.typed/register-runtime-tables! :added "4.1"}
(fact "register-runtime-tables! registers runtime tables in the registry"
  (typed/clear-registry!)
  (let [tables-map {:TestTable [:id {:type :uuid :primary true}]}
        loaded (typed/load-runtime-tables tables-map)]
    (typed/register-runtime-tables! loaded)
    (some? (typed/get-type :TestTable)) => true))

^{:refer rt.postgres.typed/app-name-from-static :added "4.1"}
(fact "app-name-from-static normalizes app metadata"
  (typed/app-name-from-static ["demo"]) => "demo"
  (typed/app-name-from-static "demo") => "demo")

^{:refer rt.postgres.typed/fn-ref->app-name :added "4.1"}
(fact "fn-ref->app-name reads application names from function defs"
  (typed/fn-ref->app-name
   nil
   {:body-meta {:static/application ["demo"]}})
  => "demo")

^{:refer rt.postgres.typed/arg-type :added "4.1"}
(fact "arg-type returns the normalized type of an argument"
  (typed/arg-type {:type :uuid}) => :uuid
  (typed/arg-type {:type {:name :text}}) => :text
  (typed/arg-type {:type nil}) => nil)

^{:refer rt.postgres.typed/arg-type-name :added "4.1"}
(fact "arg-type-name returns the string name of the argument type"
  (typed/arg-type-name {:type :uuid}) => "uuid")

^{:refer rt.postgres.typed/track-arg-role? :added "4.1"}
(fact "track-arg-role? recognizes tracked jsonb arguments"
  (let [fn-def (types/make-fn-def
                "demo"
                "track-entry"
                [{:name 'o-op :type :jsonb :role :payload}]
                [:jsonb]
                {:raw-body '((pg/t:insert -/Entry {:track o-op}))}
                nil)]
    (typed/track-arg-role? fn-def 'o-op) => true))

^{:refer rt.postgres.typed/enrich-function-arg-roles :added "4.1"}
(fact "enrich-function-arg-roles annotates jsonb inputs with roles"
  (let [fn-def (types/make-fn-def
                "demo"
                "track-entry"
                [{:name 'o-op :type :jsonb :role :payload}
                 {:name 'x :type :text :role :payload}]
                [:jsonb]
                {:raw-body '((pg/t:insert -/Entry {:track o-op}))}
                nil)]
    (map :role (:inputs (typed/enrich-function-arg-roles fn-def)))
    => [:track :payload]))

^{:refer rt.postgres.typed/jsonb-arg? :added "4.1"}
(fact "jsonb-arg? checks the argument type and modifiers"
  (typed/jsonb-arg? {:type :jsonb}) => true
  (typed/jsonb-arg? {:type :text}) => nil
  (typed/jsonb-arg? {:type :text :modifiers #{:jsonb}}) => :jsonb)

^{:refer rt.postgres.typed/format-primitive :added "4.1"}
(fact "format-primitive maps primitive types to schema fragments"
  (typed/format-primitive :shape :uuid) => {:type :uuid}
  (typed/format-primitive :openapi :uuid) => {:type "string" :format "uuid"}
  (typed/format-primitive :typescript :text) => "string")

^{:refer rt.postgres.typed/build-input-schemas :added "4.1"}
(fact "build-input-schemas formats inputs and inferred jsonb shapes"
  (let [fn-def (types/make-fn-def
                "demo"
                "select-entry"
                [{:name 'm :type :jsonb :role :payload}
                 {:name 'name :type :text :role :payload}]
                [:jsonb]
                {:raw-body '((js-select m (js ["id"])))}
                nil)]
    (contains? (typed/build-input-schemas fn-def :shape) :m) => true
    (get-in (typed/build-input-schemas fn-def :shape) [:m :shape :fields :id :type])
    => :jsonb
    (get-in (typed/build-input-schemas fn-def :shape) [:name :schema]) => {:type :text}))

^{:refer rt.postgres.typed/build-output-schema :added "4.1"}
(fact "build-output-schema wraps the inferred output shape and schema"
  (let [analysis (-> 'rt.postgres.script.test.scratch-v2
                     parse/analyze-namespace
                     parse/register-types!)
        fn-def (some #(when (= "insert-entry" (:name %)) %) (:functions analysis))
        out (typed/build-output-schema fn-def :shape)]
    (types/jsonb-shape? (:shape out)) => true
    (contains? (:shape out) :fields) => true
    (contains? (:schema out) :fields) => true))

^{:refer rt.postgres.typed/Type :added "4.1"}
(fact "Type uses app typed registry when available"
  (types/clear-registry!)
  (let [table (types/make-table-def "test" "Foo"
                                    [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid))
                                     (types/make-column-def :extra (types/make-type-ref :primitive nil :text))]
                                    :id)
        form '(defn.pg ^{:static/application ["demo"]}
                insert-foo
                [:uuid i-id :text i-extra :jsonb o-op]
                (pg/t:insert -/Foo {:id i-id :extra i-extra} {:track o-op}))
        fn-def (parse/parse-defn form "test.ns" nil)]
    (types/register-type! 'test.ns/insert-foo fn-def)
    (with-redefs [rt.postgres.grammar.common-application/app
                  (fn [_] {:dummy true})
                  rt.postgres.grammar.common-application/app-typed
                  (fn [_] {:tables {'test/Foo table}
                           :enums {}
                           :functions {}})]
      (contains? (get-in (typed/Type 'test.ns/insert-foo)
                         [:output :shape :fields])
                 :extra)
      => true)))
