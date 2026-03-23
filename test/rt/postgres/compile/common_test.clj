(ns rt.postgres.compile.common-test
  (:require [rt.postgres.compile.common :as compile.common]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-parse :as parse])
  (:use code.test))

;; -----------------------------------------------------------------------------
;; Shared Compile Helpers
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.compile.common/infer-jsonb-arg-shape :added "0.1"}
(fact "infer-jsonb-arg-shape traces jsonb args to target table shapes"
  ;; Clear and setup
  (types/clear-registry!)
  (let [task-columns [(types/make-column-def :id
                                             (types/make-type-ref :primitive nil :uuid)
                                             {:required true :primary true})
                      (types/make-column-def :status
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})
                      (types/make-column-def :name
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})]
        task-table (types/make-table-def "test.ns" "Task" task-columns :id)]
    (types/register-type! 'test.ns/Task task-table)
    ;; Parse function
    (let [form '(defn.pg ^{:%% :sql :- Task}
                  insert-task-raw
                  "inserts a task"
                  [:jsonb m :jsonb o-op]
                  (let [o-out (pg/t:insert Task m {:track o-op})]
                    (return o-out)))
          fn-def (parse/parse-defn form "test.ns" nil)
          _ (types/register-type! (symbol "test.ns" "insert-task-raw") fn-def)
          inferred (compile.common/infer-jsonb-arg-shape 'm fn-def)]
      (:source-table inferred) => "Task"
      (-> inferred :fields keys set) => #{:id :status :name})))

^{:refer rt.postgres.compile.common/infer-jsonb-arg-shape :added "4.1"}
(fact "infer-jsonb-arg-shape follows derived payload vars through helper calls"
  (types/clear-registry!)
  (let [task-columns [(types/make-column-def :id
                                             (types/make-type-ref :primitive nil :uuid)
                                             {:required true :primary true})
                      (types/make-column-def :status
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})
                      (types/make-column-def :name
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})]
        task-table (types/make-table-def "test.ns" "Task" task-columns :id)
        prep-form '(defn.pg
                     prepare-task
                     "prepares a task payload"
                     [:jsonb m]
                     (return (merge m {:status "pending"})))
        insert-form '(defn.pg ^{:%% :sql :- Task}
                       insert-task
                       "inserts a prepared task"
                       [:jsonb m]
                       (let [v-prep (prepare-task m)
                             v-input (merge v-prep {:name "demo"})]
                         (pg/t:insert Task v-input)))
        prep-def (parse/parse-defn prep-form "test.ns" nil)
        insert-def (parse/parse-defn insert-form "test.ns" nil)]
    (types/register-type! 'test.ns/Task task-table)
    (types/register-type! 'test.ns/prepare-task prep-def)
    (types/register-type! 'test.ns/insert-task insert-def)
    (let [inferred (compile.common/infer-jsonb-arg-shape 'm insert-def)]
      (:source-table inferred) => "Task"
      (-> inferred :fields keys set) => #{:id :status :name})))


^{:refer rt.postgres.compile.common/def-name :added "4.1"}
(fact "def-name returns the display name for typed definitions"
  (compile.common/def-name (types/make-table-def "demo" "User" [] :id))
  => "User"

  (compile.common/def-name nil)
  => nil)

^{:refer rt.postgres.compile.common/unique-defs :added "4.1"}
(fact "unique-defs keeps one definition per display name"
  (let [defs [(types/make-table-def "demo" "User" [] :id)
              (types/make-table-def "other" "User" [] :id)
              (types/make-table-def "demo" "Task" [] :id)]
        out  (compile.common/unique-defs defs)]
    (count out) => 2
    (set (map compile.common/def-name out)) => #{"User" "Task"}))

^{:refer rt.postgres.compile.common/select-shape-columns :added "4.1"}
(fact "select-shape-columns restricts a shape to the requested columns"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}
                                       :name {:type :text}
                                       :active {:type :boolean}}
                                      :User)
        out   (compile.common/select-shape-columns shape [:name 'active])]
    (set (keys (:fields out))) => #{:name :active}
    (:source-table out) => :User)

  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} :User)]
    (compile.common/select-shape-columns shape nil) => shape))

^{:refer rt.postgres.compile.common/infer-jsonb-arg-access-shape :added "4.1"}
(fact "infer-jsonb-arg-access-shape infers shape from jsonb field access patterns"
  (let [form '(defn.pg
                prepare-topic
                "prepares a topic payload"
                [:jsonb m]
                (let [(:uuid v-organisation-id) (pg/field-id m "organisation")
                      #{(:text v-code)
                        (:text v-format)
                        (:citext v-currency-id)} m]
                  (return
                   (|| {:publish "none"}
                       m
                       {:code-full v-code
                        :organisation-id v-organisation-id
                        :format v-format
                        :currency-id v-currency-id}))))
        fn-def (parse/parse-defn form "test.ns" nil)
        shape  (compile.common/infer-jsonb-arg-access-shape 'm fn-def)]
    (-> shape :fields keys set) => #{:organisation :code :format :currency-id}
    (get-in shape [:fields :organisation :type]) => :uuid
    (get-in shape [:fields :currency-id :type]) => :citext))

^{:refer rt.postgres.compile.common/resolve-table-def :added "4.1"}
(fact "resolve-table-def resolves tables by qualified or display name"
  (types/clear-registry!)
  (let [table (types/make-table-def "demo" "User" [] :id)]
    (types/register-type! 'demo/User table)
    (:name (compile.common/resolve-table-def 'demo/User)) => "User"
    (:name (compile.common/resolve-table-def 'User)) => "User"
    (:name (compile.common/resolve-table-def :User)) => "User"
    (compile.common/resolve-table-def nil) => nil))

^{:refer rt.postgres.compile.common/resolve-type :added "4.1"}
(fact "resolve-type formats primitive, enum, and unknown types"
  (compile.common/resolve-type :uuid :openapi)
  => {:type "string" :format "uuid"}

  (compile.common/resolve-type (types/make-type-ref :primitive nil :boolean) :openapi)
  => {:type "boolean"}

  (compile.common/resolve-type {:type :enum :enum-ref {:ns 'demo/Status}} :jschema)
  => {:$ref "#/definitions/Status"}

  (compile.common/resolve-type {:type :mystery} :ts)
  => "string")
