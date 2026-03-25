(ns rt.postgres.compile.json-schema-test
  (:require [rt.postgres.compile.json-schema :as compile.json-schema]
            [rt.postgres.grammar.typed-common :as types])
  (:use code.test))

^{:refer rt.postgres.compile.json-schema/shape->json-schema :added "0.1"}
(fact "shape->json-schema generates JSON Schema from shape"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}
                                       :name {:type :text}}
                                      :User)
        result (compile.json-schema/shape->json-schema shape)]
    (:type result) => "object"
    (contains? (:properties result) "id") => true
    (contains? (:properties result) "name") => true))

^{:refer rt.postgres.compile.json-schema/shape->json-schema :added "0.1"}
(fact "shape->json-schema preserves raw string keys"
  (let [shape (types/make-jsonb-shape {"db/remove" {:type :jsonb
                                                    :shape (types/make-jsonb-shape {"UserEmail" {:type :array
                                                                                                 :items {:type :jsonb}}}
                                                                                   nil :high false)}}
                                      nil :high false)
        result (compile.json-schema/shape->json-schema shape)]
    (contains? (:properties result) "db/remove") => true
    (contains? (get-in result [:properties "db/remove" :properties]) "UserEmail") => true
    (contains? (:properties result) "db_remove") => false))

^{:refer rt.postgres.compile.json-schema/generate-json-schema :added "0.1"}
(fact "generate-json-schema creates JSON Schema for all types"
  (let [schemas (compile.json-schema/generate-json-schema)]
    (map? schemas) => true))


^{:refer rt.postgres.compile.json-schema/field->json-schema :added "4.1"}
(fact "converts field info to JSON Schema"
  (let [field-info {:type :uuid :nullable? false}]
    (compile.json-schema/field->json-schema field-info) => {:type "string" :format "uuid"})

  (let [field-info {:type :text :nullable? true}]
    (compile.json-schema/field->json-schema field-info) => {:type "string"})

  (let [field-info {:type :boolean}]
    (compile.json-schema/field->json-schema field-info) => {:type "boolean"})

  (let [field-info {:type :array :items {:type :text}}]
    (:type (compile.json-schema/field->json-schema field-info)) => "array")

  (let [field-info {:is-ref? true}]
    (compile.json-schema/field->json-schema field-info) => {:type "string" :format "uuid"})

  (let [field-info {:type :jsonb :shape (types/make-jsonb-shape {:id {:type :uuid}} :Test)}]
    (get-in (compile.json-schema/field->json-schema field-info) [:properties "id" :type]) => "string"))

^{:refer rt.postgres.compile.json-schema/resolve-type :added "4.1"}
(fact "resolve-type maps primitive, enum, and fallback schema types"
  (let [uuid-ref (types/make-type-ref :primitive nil :uuid)]
    (compile.json-schema/resolve-type uuid-ref :jschema)
    => {:type "string" :format "uuid"})

  (compile.json-schema/resolve-type
   {:type :enum
    :enum-ref {:ns 'Status}}
   :jschema)
  => {:$ref "#/definitions/Status"}

  (compile.json-schema/resolve-type :unknown :jschema)
  => {:type "string"})
