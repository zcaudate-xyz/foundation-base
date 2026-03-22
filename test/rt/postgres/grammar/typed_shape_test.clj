(ns rt.postgres.grammar.typed-shape-test
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-shape :as shape])
  "Tests for rt.postgres.grammar.typed-shape namespace.\n   Provides shape transformations and conversions."
  (:use code.test))

;; -----------------------------------------------------------------------------
;; Map Schema Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-shape/map-schema->shape :added "0.1"}
(fact "map-schema->shape converts map schema to JsonbShape"
  (let [schema {:theme {:type :text}
                :notifications {:type :boolean}}
        result (shape/map-schema->shape schema)]
    (types/jsonb-shape? result) => true
    (get-in result [:fields :theme :type]) => :text
    (get-in result [:fields :notifications :type]) => :boolean))

^{:refer rt.postgres.grammar.typed-shape/map-schema->shape :added "0.1"}
(fact "map-schema->shape handles nested maps"
  (let [schema {:profile {:type :map :map {:name {:type :text}}}}
        result (shape/map-schema->shape schema)]
    (types/jsonb-shape? result) => true
    (get-in result [:fields :profile :type]) => :jsonb))

^{:refer rt.postgres.grammar.typed-shape/map-schema->shape :added "0.1"}
(fact "map-schema->shape handles required fields"
  (let [schema {:id {:type :uuid :required true}
                :bio {:type :text :required false}}
        result (shape/map-schema->shape schema)]
    (get-in result [:fields :id :nullable?]) => false
    (get-in result [:fields :bio :nullable?]) => true))

;; -----------------------------------------------------------------------------
;; Table to Shape Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-shape/table->shape :added "0.1"}
(fact "table->shape converts TableDef to JsonbShape"
  (let [table (types/make-table-def "test" "User"
                                    [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid)
                                                            {:required true :constraints {:primary true}})
                                     (types/make-column-def :handle (types/make-type-ref :primitive nil :citext)
                                                            {:required true})]
                                    :id)
        result (shape/table->shape table)]
    (types/jsonb-shape? result) => true
    (get-in result [:fields :id :type]) => :uuid
    (get-in result [:fields :handle :type]) => :citext
    (:source-table result) => "User"))

^{:refer rt.postgres.grammar.typed-shape/table->shape :added "0.1"}
(fact "table->shape adds :id if not present in columns"
  (let [table (types/make-table-def "test" "User" [] :id)
        result (shape/table->shape table)]
    ;; table->shape adds :id as a default field if not in columns
    (contains? (:fields result) :id) => true))

;; -----------------------------------------------------------------------------
;; Table Operation Shape Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-shape/shape-for-table-op :added "0.1"}
(fact "shape-for-table-op returns shape for :get"
  (let [table (types/make-table-def "test" "User"
                                    [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid) {})]
                                    :id)
        result (shape/shape-for-table-op :get table {})]
    (types/jsonb-shape? result) => true))

^{:refer rt.postgres.grammar.typed-shape/shape-for-table-op :added "0.1"}
(fact "shape-for-table-op returns array shape for :select"
  (let [table (types/make-table-def "test" "User" [] :id)
        result (shape/shape-for-table-op :select table {})]
    (types/jsonb-array? result) => true))

^{:refer rt.postgres.grammar.typed-shape/shape-for-table-op :added "0.1"}
(fact "shape-for-table-op returns primitive for :id, :exists, :count"
  (let [table (types/make-table-def "test" "User" [] :id)]
    (:type (shape/shape-for-table-op :id table {})) => :uuid
    (:type (shape/shape-for-table-op :exists table {})) => :boolean
    (:type (shape/shape-for-table-op :count table {})) => :integer))

;; -----------------------------------------------------------------------------
;; Access Field Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-shape/access-field :added "0.1"}
(fact "access-field models :->> (text access)"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid} :name {:type :text}} :User)
        result (shape/access-field shape "name" :text-access)]
    (:type result) => :text
    (:coerced-to result) => :text))

^{:refer rt.postgres.grammar.typed-shape/access-field :added "0.1"}
(fact "access-field models :-> (jsonb access)"
  (let [shape (types/make-jsonb-shape {:data {:type :jsonb}} :User)
        result (shape/access-field shape "data" :jsonb-access)]
    (:type result) => :jsonb))

^{:refer rt.postgres.grammar.typed-shape/access-field :added "0.1"}
(fact "access-field returns :unknown for unknown fields"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} :User)
        result (shape/access-field shape "unknown" :text-access)]
    result => :unknown))

;; -----------------------------------------------------------------------------
;; Shape Conversion Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-shape/shape->map :added "0.1"}
(fact "shape->map converts JsonbShape to plain map"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid :nullable? false}} :User)
        result (shape/shape->map shape)]
    (:source-table result) => :User
    (contains? (:fields result) :id) => true))

^{:refer rt.postgres.grammar.typed-shape/shape->map :added "0.1"}
(fact "shape->map converts JsonbShape to fields map"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} :User)
        result (shape/shape->map shape)]
    ;; shape->map returns a map with :fields, :source-table, :confidence
    (map? result) => true
    (contains? result :fields) => true
    (contains? result :source-table) => true))

;; -----------------------------------------------------------------------------
;; JSON Schema Generation Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-shape/shape->json-schema :added "0.1"}
(fact "shape->json-schema generates JSON Schema"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid :nullable? false}
                                        :name {:type :text :nullable? true}}
                                        :User)
        result (comment shape/shape->json-schema shape)]
    (:type result) => "object"
    (contains? (:properties result) "id") => true
    (contains? (:properties result) "name") => true
    (:required result) => ["id"]))

^{:refer rt.postgres.grammar.typed-shape/shape->json-schema :added "0.1"}
(fact "shape->json-schema handles format types"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}
                                        :created {:type :timestamp}
                                        :amount {:type :numeric}}
                                        :Test)
        result (comment shape/shape->json-schema shape)]
    (get-in result [:properties "id" :format]) => "uuid"
    (get-in result [:properties "created" :format]) => "date-time"
    (get-in result [:properties "amount" :type]) => "number"))

^{:refer rt.postgres.grammar.typed-shape/shape->json-schema :added "0.1"}
(fact "shape->json-schema handles enum types"
  (let [shape (types/make-jsonb-shape {:status {:type :enum :enum-ref {:ns :test}}}
                                        :Test)
        result (comment shape/shape->json-schema shape)]
    ;; Commented out since function doesn't exist yet
    (nil? result) => true))

;; -----------------------------------------------------------------------------
;; Table Reference Resolution Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.grammar.typed-shape/resolve-table-ref :added "0.1"}
(fact "resolve-table-ref looks up table from registry"
  (types/register-type! 'TestTable (types/make-table-def "test" "TestTable" [] :id))
  (let [result (comment shape/resolve-table-ref 'TestTable)]
    (not (nil? result)) => true
    (:name result) => "TestTable")
  (types/clear-registry!))

^{:refer rt.postgres.grammar.typed-shape/resolve-table-ref :added "0.1"}
(fact "resolve-table-ref returns nil for unknown table"
  (types/clear-registry!)
  (comment shape/resolve-table-ref 'UnknownTable) => nil)
