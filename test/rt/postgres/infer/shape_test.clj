(ns rt.postgres.infer.shape-test
  "Tests for rt.postgres.infer.shape namespace.
   Provides shape transformations and conversions."
  (:use code.test)
  (:require [rt.postgres.infer.shape :as shape]
            [rt.postgres.infer.types :as types]))

;; -----------------------------------------------------------------------------
;; Map Schema Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.shape/map-schema->shape :added "0.1"}
(fact "map-schema->shape converts map schema to JsonbShape"
  (let [schema {:theme {:type :text}
                :notifications {:type :boolean}}
        result (shape/map-schema->shape schema)]
    (types/jsonb-shape? result) => true
    (get-in result [:fields :theme :type]) => :text
    (get-in result [:fields :notifications :type]) => :boolean))

^{:refer rt.postgres.infer.shape/map-schema->shape :added "0.1"}
(fact "map-schema->shape handles nested maps"
  (let [schema {:profile {:type :map :map {:name {:type :text}}}}
        result (shape/map-schema->shape schema)]
    (types/jsonb-shape? result) => true
    (get-in result [:fields :profile :type]) => :jsonb))

^{:refer rt.postgres.infer.shape/map-schema->shape :added "0.1"}
(fact "map-schema->shape handles required fields"
  (let [schema {:id {:type :uuid :required true}
                :bio {:type :text :required false}}
        result (shape/map-schema->shape schema)]
    (get-in result [:fields :id :nullable?]) => false
    (get-in result [:fields :bio :nullable?]) => true))

;; -----------------------------------------------------------------------------
;; Table to Shape Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.shape/table->shape :added "0.1"}
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

^{:refer rt.postgres.infer.shape/table->shape :added "0.1"}
(fact "table->shape includes standard tracking fields"
  (let [table (types/make-table-def "test" "User" [] :id)
        result (shape/table->shape table)]
    (contains? (:fields result) :id) => true
    (contains? (:fields result) :time-created) => true
    (contains? (:fields result) :time-updated) => true
    (contains? (:fields result) :op-created) => true
    (contains? (:fields result) :op-updated) => true))

;; -----------------------------------------------------------------------------
;; Table Operation Shape Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.shape/shape-for-table-op :added "0.1"}
(fact "shape-for-table-op returns shape for :get"
  (let [table (types/make-table-def "test" "User"
                                    [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid) {})]
                                    :id)
        result (shape/shape-for-table-op :get table {})]
    (types/jsonb-shape? result) => true))

^{:refer rt.postgres.infer.shape/shape-for-table-op :added "0.1"}
(fact "shape-for-table-op returns array shape for :select"
  (let [table (types/make-table-def "test" "User" [] :id)
        result (shape/shape-for-table-op :select table {})]
    (types/jsonb-array? result) => true))

^{:refer rt.postgres.infer.shape/shape-for-table-op :added "0.1"}
(fact "shape-for-table-op returns primitive for :id, :exists, :count"
  (let [table (types/make-table-def "test" "User" [] :id)]
    (:type (shape/shape-for-table-op :id table {})) => :uuid
    (:type (shape/shape-for-table-op :exists table {})) => :boolean
    (:type (shape/shape-for-table-op :count table {})) => :integer))

;; -----------------------------------------------------------------------------
;; Access Field Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.shape/access-field :added "0.1"}
(fact "access-field models :->> (text access)"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid} :name {:type :text}} :User)
        result (shape/access-field shape "name" :text-access)]
    (:type result) => :text
    (:coerced-to result) => :text))

^{:refer rt.postgres.infer.shape/access-field :added "0.1"}
(fact "access-field models :-> (jsonb access)"
  (let [shape (types/make-jsonb-shape {:data {:type :jsonb}} :User)
        result (shape/access-field shape "data" :jsonb-access)]
    (:type result) => :jsonb))

^{:refer rt.postgres.infer.shape/access-field :added "0.1"}
(fact "access-field handles unknown fields"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} :User)
        result (shape/access-field shape "unknown" :text-access)]
    (:type result) => :text
    (:nullable? result) => true))

;; -----------------------------------------------------------------------------
;; Shape Conversion Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.shape/shape->map :added "0.1"}
(fact "shape->map converts JsonbShape to plain map"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid :nullable? false}} :User)
        result (shape/shape->map shape)]
    (:source result) => :User
    (contains? (:fields result) :id) => true))

^{:refer rt.postgres.infer.shape/shape->map :added "0.1"}
(fact "shape->map handles JsonbMerge"
  (let [shape1 (types/make-jsonb-shape {:id {:type :uuid}} :User)
        shape2 (types/make-jsonb-shape {:name {:type :text}} :Profile)
        merged (types/make-jsonb-merge shape1 shape2)
        result (shape/shape->map merged)]
    (contains? result :merge) => true))

^{:refer rt.postgres.infer.shape/shape->map :added "0.1"}
(fact "shape->map handles JsonbArray"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} :User)
        arr (types/make-jsonb-array shape)
        result (shape/shape->map arr)]
    (contains? result :array) => true))

;; -----------------------------------------------------------------------------
;; JSON Schema Generation Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.shape/shape->json-schema :added "0.1"}
(fact "shape->json-schema generates JSON Schema"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid :nullable? false}
                                        :name {:type :text :nullable? true}}
                                        :User)
        result (shape/shape->json-schema shape)]
    (:type result) => "object"
    (contains? (:properties result) "id") => true
    (contains? (:properties result) "name") => true
    (:required result) => ["id"]))

^{:refer rt.postgres.infer.shape/shape->json-schema :added "0.1"}
(fact "shape->json-schema handles format types"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}
                                        :created {:type :timestamp}
                                        :amount {:type :numeric}}
                                        :Test)
        result (shape/shape->json-schema shape)]
    (get-in result [:properties "id" :format]) => "uuid"
    (get-in result [:properties "created" :format]) => "date-time"
    (get-in result [:properties "amount" :type]) => "number"))

^{:refer rt.postgres.infer.shape/shape->json-schema :added "0.1"}
(fact "shape->json-schema handles enum types"
  (let [shape (types/make-jsonb-shape {:status {:type :enum :enum-ref {:ns :test}}}
                                        :Test)
        result (shape/shape->json-schema shape)]
    (get-in result [:properties "status" :type]) => "string"
    (clojure.string/includes? (get-in result [:properties "status" :description]) "enum") => true))

;; -----------------------------------------------------------------------------
;; Table Reference Resolution Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.shape/resolve-table-ref :added "0.1"}
(fact "resolve-table-ref looks up table from registry"
  (types/register-type! 'TestTable (types/make-table-def "test" "TestTable" [] :id))
  (let [result (shape/resolve-table-ref 'TestTable)]
    (not (nil? result)) => true
    (:name result) => "TestTable")
  (types/clear-registry!))

^{:refer rt.postgres.infer.shape/resolve-table-ref :added "0.1"}
(fact "resolve-table-ref returns nil for unknown table"
  (types/clear-registry!)
  (shape/resolve-table-ref 'UnknownTable) => nil)
