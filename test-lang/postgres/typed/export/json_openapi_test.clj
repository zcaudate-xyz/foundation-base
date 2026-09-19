(ns postgres.typed.export.json-openapi-test
  (:require [postgres.typed.export.json-openapi :as compile.openapi]
            [postgres.typed.typed-analyze :as analyze]
            [postgres.typed.typed-common :as types]
            [postgres.typed.typed-parse :as parse]
            [postgres.typed.typed-shape :as typed-shape]
            [postgres.typed.typed-common-test]
            [postgres.sample.scratch-v2]
            [std.json :as json])
  (:use code.test))

^{:refer postgres.typed.export.json-openapi/field->openapi :added "4.1"}
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

^{:refer postgres.typed.export.json-openapi/shape->openapi :added "0.1"}
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

(fact "shape->openapi preserves nested source field order"
  (let [shape (typed-shape/map-schema->shape
               (array-map
                :zeta {:type :text :required true}
                :alpha {:type :text :required true}
                :nested {:type :map
                         :map (array-map
                               :second {:type :integer}
                               :first {:type :boolean})}))
        result (compile.openapi/shape->openapi shape)]
    (vec (keys (:properties result))) => ["zeta" "alpha" "nested"]
    (vec (:required result)) => ["zeta" "alpha"]
    (vec (keys (get-in result [:properties "nested" :properties])))
    => ["second" "first"]
    (let [serialized (json/write-pp result)]
      (< (.indexOf serialized "\"zeta\"")
         (.indexOf serialized "\"alpha\"")))
    => true))

^{:refer postgres.typed.export.json-openapi/arg->openapi :added "4.1"}
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

^{:refer postgres.typed.export.json-openapi/fn->openapi :added "0.1"}
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

(fact "fn->openapi preserves function input order"
  (let [fn-def (types/make-fn-def
                "test.ns"
                "ordered_inputs"
                [(types/->FnArg 'i-zeta :text nil :payload)
                 (types/->FnArg 'i-alpha :text nil :payload)]
                :text
                {}
                nil)]
    (with-redefs [analyze/cached-infer
                  (constantly {:kind :primitive :type :text})]
      (let [request-schema (get-in (compile.openapi/fn->openapi fn-def)
                                   [:requestBody :content "application/json" :schema])]
        (vec (keys (:properties request-schema)))
        => ["zeta" "alpha"]))))

(fact "generate-openapi uses explicit source order and lexical fallback"
  (binding [types/*type-registry* (atom {})]
    (let [first-table (types/make-table-def
                       "test.ns"
                       "FirstTable"
                       [(types/make-column-def :first-field :text)]
                       :id)
          second-table (types/make-table-def
                        "test.ns"
                        "SecondTable"
                        [(types/make-column-def :second-field :text)]
                        :id)
          first-fn (types/make-fn-def "test.ns" "first-fn" [] :text {} nil)
          second-fn (types/make-fn-def "test.ns" "second-fn" [] :text {} nil)]
      (types/register-type! 'test.ns/FirstTable first-table)
      (types/register-type! 'test.ns/SecondTable second-table)
      (types/register-type! 'test.ns/first-fn first-fn)
      (types/register-type! 'test.ns/second-fn second-fn)
      (with-redefs [compile.openapi/fn->openapi (constantly {})]
        (let [ordered (compile.openapi/generate-openapi
                       "test.ns"
                       (constantly true)
                       {:function-order ['second_fn 'first_fn]
                        :schema-order ['SecondTable 'FirstTable]})
              fallback (compile.openapi/generate-openapi
                        "test.ns"
                        (constantly true))]
          (vec (keys (:paths ordered)))
          => ["/rpc/second_fn" "/rpc/first_fn"]
          (vec (keys (get-in ordered [:components :schemas])))
          => ["SecondTable" "FirstTable"]
          (vec (keys (:paths fallback)))
          => ["/rpc/first_fn" "/rpc/second_fn"]
          (vec (keys (get-in fallback [:components :schemas])))
          => ["FirstTable" "SecondTable"])))))

^{:refer postgres.typed.export.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi response schemas are valid"
  (let [spec (compile.openapi/generate-openapi 'postgres.sample.scratch-v2 (constantly true))
        paths (:paths spec)]
    (doseq [[_path methods] paths
            [_method op] methods]
      (contains? op :responses) => true)))
