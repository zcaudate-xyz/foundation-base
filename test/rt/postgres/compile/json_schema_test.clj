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
