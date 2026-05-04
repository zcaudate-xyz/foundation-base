(ns hara.runtime.postgres.base.compile.json-openapi-test
  (:require [hara.runtime.postgres.base.compile.json-openapi :as compile.openapi]
            [hara.runtime.postgres.base.typed.typed-common :as types]
            [hara.runtime.postgres.base.typed.typed-parse :as parse]
            [hara.runtime.postgres.base.typed.typed-common-test]
            [hara.runtime.postgres.test.scratch-v2])
  (:use code.test))

^{:refer hara.runtime.postgres.base.compile.json-openapi/field->openapi :added "4.1"}
(fact "converts field info to OpenAPI schema"
  (let [field-info {:type :uuid :nullable? false}]
    (compile.openapi/field->openapi field-info) => {:type "string" :format "uuid"})

  (let [field-info {:type :text :nullable? true}]
    (compile.openapi/field->openapi field-info) => {:type "string"})

  (let [field-info {:type :boolean}]
    (compile.openapi/field->openapi field-info) => {:type "boolean"})

  (let [field-info {:type :array :items {:type :text}}]
    (:type (compile.openapi/field->openapi field-info)) => "array")

  (let [field-info {:is-ref? true}]
    (compile.openapi/field->openapi field-info) => {:type "string" :format "uuid"}))

^{:refer hara.runtime.postgres.base.compile.json-openapi/shape->openapi :added "0.1"}
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

^{:refer hara.runtime.postgres.base.compile.json-openapi/arg->openapi :added "4.1"}
(fact "converts function argument to OpenAPI parameter"
  (types/clear-registry!)
  (let [arg {:name 'm :type :text}
        fn-def {:inputs [arg]}]
    (compile.openapi/arg->openapi arg fn-def) => ["m" {:type "string"}])

  (let [arg {:name 'i-count :type :integer}
        fn-def {:inputs [arg]}]
    (compile.openapi/arg->openapi arg fn-def) => ["count" {:type "integer"}])

  (let [arg {:name 'm :type :jsonb}
        fn-def {:inputs [arg]}]
    (compile.openapi/arg->openapi arg fn-def) => vector?))

^{:refer hara.runtime.postgres.base.compile.json-openapi/fn->openapi :added "0.1"}
(fact "fn->openapi retains track args in request body"
  (let [form '(defn.pg ^{:%% :sql :- Task}
                insert-task
                "inserts a task"
                [:text i-name :jsonb o-op]
                (let [o-out (pg/t:insert Task {:name i-name} {:track o-op})]
                  (return o-op)))
        fn-def (parse/parse-defn form "test.ns" nil)
        openapi (compile.openapi/fn->openapi fn-def)
        request-schema (get-in openapi [:requestBody :content "application/json" :schema])]
    (contains? (:properties request-schema) "o_op") => true))

^{:refer hara.runtime.postgres.base.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi response schemas are valid"
  (let [spec (compile.openapi/generate-openapi 'rt.postgres.test.scratch-v2 (constantly true))
        paths (:paths spec)]
    (doseq [[_path methods] paths
            [_method op] methods]
      (contains? op :responses) => true)))