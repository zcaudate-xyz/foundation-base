(ns postgres.typed.typed-shape-test
  (:require [postgres.typed.typed-common :as types]
            [postgres.typed.typed-shape :as shape])
  (:use code.test))

^{:refer postgres.typed.typed-shape/map-schema-entry->field-type :added "4.1"}
(fact "map-schema-entry->field-type converts map schema entries to field descriptors"
  ;; Basic type
  (shape/map-schema-entry->field-type :id {:type :uuid})
  => (contains {:type :uuid})

  ;; Required field
  (shape/map-schema-entry->field-type :id {:type :uuid :required true})
  => (contains {:type :uuid :nullable? false})

  ;; Optional field
  (shape/map-schema-entry->field-type :bio {:type :text :required false})
  => (contains {:type :text :nullable? true})

  ;; Nested map schema
  (let [result (shape/map-schema-entry->field-type :profile {:type :map :map {:name {:type :text}}})]
    (:type result) => :jsonb
    (some? (:shape result)) => true)

  ;; Unknown type
  (shape/map-schema-entry->field-type :data {:type :unknown})
  => (contains {:type :unknown}))

^{:refer postgres.typed.typed-shape/map-schema->shape :added "0.1"}
(fact "map-schema->shape handles required fields"
  (let [schema {:id {:type :uuid :required true}
                :bio {:type :text :required false}}
        result (shape/map-schema->shape schema)]
    (get-in result [:fields :id :nullable?]) => false
    (get-in result [:fields :bio :nullable?]) => true))

^{:refer postgres.typed.typed-shape/resolve-column-type :added "4.1"}
(fact "resolve-column-type resolves ColumnDef types to field descriptors"
  ;; Primitive type via TypeRef
  (let [col (types/make-column-def :id (types/make-type-ref :primitive nil :uuid)
                                   {:required true})]
    (shape/resolve-column-type col)
    => (contains {:type :uuid :nullable? false}))

  ;; Enum type
  (let [col (types/make-column-def :status (types/make-type-ref :enum :core "Status")
                                   {:required false :enum-ref {:ns :core}})]
    (shape/resolve-column-type col)
    => (contains {:type :enum}))

  ;; Ref type
  (let [col (types/make-column-def :org-id (types/make-type-ref :ref nil "Organisation")
                                   {:required true})]
    (shape/resolve-column-type col)
    => (contains {:type :uuid :nullable? false}))

  ;; Map type with nested schema
  (let [col (types/make-column-def :settings (types/make-type-ref :primitive nil :map)
                                   {:map-schema {:theme {:type :text}}})]
    (let [result (shape/resolve-column-type col)]
      (:type result) => :jsonb
      (:jsonb-shape result) => :map
      (some? (:shape result)) => true)))

(fact "resolve-column-type retains explicit JSONB shapes"
  (let [map-col (types/make-column-def
                 :settings
                 (types/make-type-ref :primitive nil :jsonb)
                 {:shape :map
                  :map-schema {:theme {:type :text}}})
        inferred-map-col (types/make-column-def
                          :preferences
                          (types/make-type-ref :primitive nil :jsonb)
                          {:map-schema {:locale {:type :text}}})
        array-col (types/make-column-def
                   :tags
                   (types/make-type-ref :primitive nil :jsonb)
                   {:shape :array})
        array-items-col (types/make-column-def
                         :events
                         (types/make-type-ref :primitive nil :jsonb)
                         {:shape :array
                          :items-schema {:type :map
                                         :map {:name {:type :text}}}})
        opaque-col (types/make-column-def
                    :detail
                    (types/make-type-ref :primitive nil :jsonb)
                    {:shape :opaque})]
    (let [map-result (shape/resolve-column-type map-col)
          inferred-map-result (shape/resolve-column-type inferred-map-col)
          array-result (shape/resolve-column-type array-col)
    array-items-result (shape/resolve-column-type array-items-col)
    opaque-result (shape/resolve-column-type opaque-col)]
      (:type map-result) => :jsonb
      (:jsonb-shape map-result) => :map
      (get-in map-result [:shape :fields :theme :type]) => :text
      (:jsonb-shape inferred-map-result) => :map
      (get-in inferred-map-result [:shape :fields :locale :type]) => :text
      (:type array-result) => :jsonb
      (:jsonb-shape array-result) => :array
      (:type array-items-result) => :jsonb
      (:jsonb-shape array-items-result) => :array
      (get-in array-items-result [:items :shape :fields :name :type])
      => :text
      (:type opaque-result) => :jsonb
      (:jsonb-shape opaque-result) => :opaque)))

(fact "map-schema-entry->field-type retains nested JSONB shapes"
  (let [result (shape/map-schema-entry->field-type
                :items
                {:type :jsonb
                 :shape :array})]
    (:type result) => :jsonb
    (:jsonb-shape result) => :array))

^{:refer postgres.typed.typed-shape/table->shape :added "0.1"}
(fact "table->shape adds :id if not present in columns"
  (let [table (types/make-table-def "test" "User" [] :id)
        result (shape/table->shape table)]
    ;; table->shape adds :id as a default field if not in columns
    (contains? (:fields result) :id) => true))

^{:refer postgres.typed.typed-shape/shape-for-table-op :added "0.1"}
(fact "shape-for-table-op returns primitive for :id, :exists, :count"
  (let [table (types/make-table-def "test" "User" [] :id)]
    (:type (shape/shape-for-table-op :id table {})) => :uuid
    (:type (shape/shape-for-table-op :exists table {})) => :boolean
    (:type (shape/shape-for-table-op :count table {})) => :integer))

^{:refer postgres.typed.typed-shape/access-field :added "0.1"}
(fact "access-field returns :unknown for unknown fields"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} :User)
        result (shape/access-field shape "unknown" :text-access)]
    result => :unknown))

^{:refer postgres.typed.typed-shape/shape->map :added "0.1"}
(fact "shape->map converts JsonbShape to fields map"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}} :User)
        result (shape/shape->map shape)]
    ;; shape->map returns a map with :fields, :source-table, :confidence
    (map? result) => true
    (contains? result :fields) => true
    (contains? result :source-table) => true))