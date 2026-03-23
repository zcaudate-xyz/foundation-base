(ns rt.postgres.compile.json-openapi-test
  (:require [rt.postgres.compile.json-openapi :as compile.openapi]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-parse :as parse]
            [rt.postgres.grammar.typed-common-test]
            [rt.postgres.script.test.scratch-v2])
  (:use code.test))

^{:refer rt.postgres.compile.json-openapi/shape->openapi :added "0.1"}
(fact "shape->openapi converts JsonbShape to OpenAPI schema"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid :nullable? false}
                                       :name {:type :text :nullable? true}}
                                      :User)
        result (compile.openapi/shape->openapi shape)]
    (:type result) => "object"
    (contains? (:properties result) "id") => true
    (contains? (:properties result) "name") => true
    (:required result) => ["id"]))

^{:refer rt.postgres.compile.json-openapi/shape->openapi :added "0.1"}
(fact "shape->openapi handles various primitive types"
  (let [shape (types/make-jsonb-shape {:s {:type :text}
                                       :n {:type :integer}
                                       :f {:type :numeric}
                                       :b {:type :boolean}
                                       :j {:type :jsonb}}
                                      :Test)
        result (compile.openapi/shape->openapi shape)]
    (get-in result [:properties "s" :type]) => "string"
    (get-in result [:properties "n" :type]) => "integer"
    (get-in result [:properties "f" :type]) => "number"
    (get-in result [:properties "b" :type]) => "boolean"
    (get-in result [:properties "j" :type]) => "object"))

^{:refer rt.postgres.compile.json-openapi/shape->openapi :added "0.1"}
(fact "shape->openapi preserves raw string keys"
  (let [shape (types/make-jsonb-shape {"db/sync" {:type :jsonb
                                                  :shape (types/make-jsonb-shape {"UserProfile" {:type :array
                                                                                                 :items {:type :jsonb}}}
                                                                                 nil :high false)}}
                                      nil :high false)
        result (compile.openapi/shape->openapi shape)]
    (contains? (:properties result) "db/sync") => true
    (contains? (get-in result [:properties "db/sync" :properties]) "UserProfile") => true
    (contains? (:properties result) "db_sync") => false))

^{:refer rt.postgres.compile.json-openapi/fn->openapi :added "0.1"}
(fact "fn->openapi generates OpenAPI operation from FnDef"
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
    (let [form '(defn.pg ^{:%% :sql :- Task}
                  insert-task-raw
                  "inserts a task"
                  [:jsonb m :jsonb o-op]
                  (let [o-out (pg/t:insert Task m {:track o-op})]
                    (return o-out)))
          fn-def (parse/parse-defn form "test.ns" nil)
          _ (types/register-type! (symbol "test.ns" "insert-task-raw") fn-def)
          openapi (compile.openapi/fn->openapi fn-def)
          request-schema (get-in openapi [:requestBody :content "application/json" :schema])
          m-schema (get-in request-schema [:properties "m"])]
      (:type m-schema) => "object"
      (contains? (:properties m-schema) "id") => true
      (contains? (:properties m-schema) "status") => true
      (contains? (:properties m-schema) "name") => true)))

^{:refer rt.postgres.compile.json-openapi/fn->openapi :added "0.1"}
(fact "fn->openapi uses output field for response schema - TABLE REF"
  (let [form '(defn.pg ^{:%% :sql :- Entry}
                insert-entry
                "inserts an entry"
                [:text i-name :jsonb i-tags]
                (pg/t:insert Entry {:name i-name :tags i-tags}))
        fn-def (parse/parse-defn form "test.ns" nil)
        openapi (compile.openapi/fn->openapi fn-def)
        response-schema (get-in openapi [:responses "200" :content "application/json" :schema])]
    response-schema => {:$ref "#/components/schemas/Entry"}))

^{:refer rt.postgres.compile.json-openapi/fn->openapi :added "0.1"}
(fact "fn->openapi filters out o-op from request body"
  (let [form '(defn.pg ^{:%% :sql :- Task}
                insert-task
                "inserts a task"
                [:text i-name :jsonb o-op]
                (let [o-out (pg/t:insert Task {:name i-name} {:track o-op})]
                  (return o-op)))
        fn-def (parse/parse-defn form "test.ns" nil)
        openapi (compile.openapi/fn->openapi fn-def)
        request-schema (get-in openapi [:requestBody :content "application/json" :schema])]
    (contains? (:properties request-schema) "op") => false))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi creates full OpenAPI spec from namespace"
  (let [spec (compile.openapi/generate-openapi 'rt.postgres.grammar.typed-common-test (constantly true))]
    (contains? spec :openapi) => true
    (contains? spec :paths) => true
    (contains? spec :components) => true))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi generates correct OpenAPI spec for rt.postgres.script.test.scratch-v2"
  (let [spec (compile.openapi/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        paths (:paths spec)]
    (map? paths) => true
    (not (empty? paths)) => true))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi generates valid OpenAPI structure"
  (let [spec (compile.openapi/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))]
    (contains? spec :openapi) => true
    (contains? spec :info) => true
    (contains? spec :paths) => true
    (contains? spec :components) => true
    (contains? (:components spec) :schemas) => true
    (map? (:paths spec)) => true))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi includes schemas in components"
  (let [spec (compile.openapi/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        schemas (get-in spec [:components :schemas])]
    (map? schemas) => true))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi filters paths based on predicate"
  (let [spec (compile.openapi/generate-openapi
              'rt.postgres.script.test.scratch-v2
              #(clojure.string/starts-with? % "/rpc/insert"))
        paths (keys (:paths spec))]
    (every? #(clojure.string/starts-with? % "/rpc/insert") paths) => true))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi response schemas are valid"
  (let [spec (compile.openapi/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        paths (:paths spec)]
    (doseq [[_path methods] paths
            [_method op] methods]
      (contains? op :responses) => true)))
