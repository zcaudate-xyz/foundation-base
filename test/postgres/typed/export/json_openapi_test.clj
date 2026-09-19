(ns postgres.typed.export.json-openapi-test
  (:use code.test)
  (:require [postgres.typed.export.json-openapi :as openapi]
            [postgres.typed.typed-common :as types]
            [postgres.typed.typed-infer :as infer]
  [postgres.typed.typed-shape :as shape]
  [std.json :as json]))

^{:refer postgres.typed.export.json-openapi/shape->openapi :added "4.1"}
(fact "preserves table column order in properties and required"
  (let [table (types/make-table-def
               'fixture
               'OrderedTable
               [(types/make-column-def :zeta :text {:required true})
                (types/make-column-def :alpha :uuid {:required true})
                (types/make-column-def :middle :boolean)]
               :zeta)
        schema (openapi/shape->openapi (shape/table->shape table))
        properties (:properties schema)]
    [(vec (.keySet ^java.util.Map properties))
     (:required schema)]
    => [["zeta" "alpha" "middle"]
        ["zeta" "alpha"]]

    (let [serialized (json/write properties)]
      (< (.indexOf serialized "\"zeta\"")
         (.indexOf serialized "\"alpha\"")
         (.indexOf serialized "\"middle\""))
      => true))

  (let [selected (-> (shape/table->shape table)
                     (infer/select-shape-columns [:middle :zeta]))
        schema (openapi/shape->openapi selected)]
    (vec (.keySet ^java.util.Map (:properties schema)))
    => ["zeta" "middle"]))
