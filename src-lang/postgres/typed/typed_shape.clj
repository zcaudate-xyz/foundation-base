(ns postgres.typed.typed-shape
  (:require [postgres.typed.typed-common :as types]))

;; ─────────────────────────────────────────────────────────────────────────────
;; Column Type Mapping (internal use only)
;; ─────────────────────────────────────────────────────────────────────────────

(def ^:private column->field-type
     (merge
      (into {} (map (fn [k] [k {:type k}]) (keys types/+type-formats+)))
      {:map {:type :jsonb}
       :ref {:type :uuid}
       :array {:type :array :items {:type :jsonb}}}))

(declare map-schema->shape)

(defn map-schema-entry->field-type
       "Converts a single map schema entry to a field descriptor.
   Handles nested map schemas recursively."
       [entry-key entry-val]
       (let [_ (types/validate-jsonb-metadata! entry-val)
             entry-type (get entry-val :type :unknown)
             required? (boolean (get entry-val :required false))
             base-type (get column->field-type entry-type {:type entry-type})
             nested-map-schema (get entry-val :map)
             nested-items-schema (get entry-val :items)
             shape-marker (types/inferred-jsonb-shape entry-type
                                                      (:shape entry-val)
                                                      nested-map-schema)
             with-nested-shape (if (and (types/jsonb-column-type? entry-type)
                                        nested-map-schema)
                                   (assoc base-type :shape (map-schema->shape nested-map-schema))
                                   base-type)
             with-nested-items (if (and (= :array shape-marker)
                                        (map? nested-items-schema))
                                 (assoc with-nested-shape
                                        :items
                                        (map-schema-entry->field-type
                                         :item
                                         nested-items-schema))
                                 with-nested-shape)
             ;; :shape is reserved for a nested JsonbShape in field
             ;; descriptors; keep the explicit column marker separately.
             with-shape-marker (if shape-marker
                                 (assoc with-nested-items :jsonb-shape shape-marker)
                                 with-nested-items)]
            (assoc with-shape-marker
                   :nullable? (not required?)
                   :source (str entry-key))))

(defn map-schema->shape
      "Converts a map schema definition to a JsonbShape."
      [map-schema]
      (when (map? map-schema)
            (let [fields (into {}
                               (map (fn [[k v]]
                                        [(keyword (name k))
                                         (map-schema-entry->field-type k v)]))
                               map-schema)]
                 (types/make-jsonb-shape fields))))

(defn resolve-column-type
       "Resolves a ColumnDef's type to a field descriptor.
   Handles explicit JSONB shapes and nested map schemas for JSONB columns."
       [col]
       (let [tr (:type col)
             raw-type (cond
                        (types/type-ref? tr)
                        (when (= :primitive (:kind tr)) (:name tr))

                        (keyword? tr)
                        tr

                        :else
                        nil)
             base (cond
                   (types/type-ref? tr)
                   (case (:kind tr)
                         :primitive (get column->field-type (:name tr) {:type (:name tr)})
                         :enum {:type :enum :enum-ref (or (:enum-ref col) {:ns (:ns tr)})}
                         :ref {:type :uuid}
                         {:type :jsonb})

                   (keyword? (:type col))
                   (get column->field-type (:type col) {:type (:type col)})

                   :else {:type :unknown})

             map-schema (:map-schema col)
             items-schema (:items-schema col)
             shape-marker (types/inferred-jsonb-shape raw-type
                                                      (:shape col)
                                                      map-schema)
             is-map-type? (or (= :map raw-type)
                              (= :map (:type base))
                              (= :map shape-marker))

             item-type (when (and (= :array shape-marker)
                                  items-schema)
                         (map-schema-entry->field-type :item items-schema))
             col-type (cond-> base
                        shape-marker (assoc :jsonb-shape shape-marker)
                        (and is-map-type? map-schema)
                        (assoc :shape (map-schema->shape map-schema))
                        item-type
                        (assoc :items item-type))]
            (assoc col-type :nullable? (not (:required col)) :source (str (:name col)))))

(defn- variant-for
  [table-def class-table field]
  (when (and class-table (seq (:variants table-def)))
    (let [matches (filter #(and (= class-table (:class-table %))
                                (= field (:field %)))
                          (:variants table-def))]
      (when (> (count matches) 1)
        (throw (ex-info "Multiple JSONB variants match the same table field."
                        {:type ::duplicate-variant
                         :table (:name table-def)
                         :class-table class-table
                         :field field
                         :variants (vec matches)})))
      (first matches))))

(defn- variant-column
  [table-def class-table col]
  (if-let [variant (variant-for table-def class-table (:name col))]
    (do
      (when-not (types/jsonb-column-type? (:type col))
        (throw (ex-info "JSONB variants require a JSONB-backed base column."
                        {:type ::incompatible-variant
                         :table (:name table-def)
                         :field (:name col)
                         :class-table class-table})))
      (let [attrs (types/normalize-jsonb-variant (:attrs variant))
            semantic-type (or (:type attrs) :jsonb)]
        (-> col
            ;; A variant replaces the semantic contract while preserving the
            ;; physical column constraints from the base ColumnDef.
            (assoc :type (types/make-type-ref :primitive nil semantic-type)
                   :shape (:shape attrs)
                   :map-schema (:map attrs)
                   :items-schema (:items attrs)))))
    col))

;; ─────────────────────────────────────────────────────────────────────────────
;; CRITIQUE FIX #1: Single Table to Shape Conversion
;; This is THE ONLY function that converts TableDef -> JsonbShape.
;; ─────────────────────────────────────────────────────────────────────────────

(defn table->shape
      "Converts a TableDef to a JsonbShape. SINGLE SOURCE OF TRUTH.
   Guarantees:
   - Ref fields get -id suffix (e.g., :org -> :org-id)
   - Primary keys are always non-nullable."
      ([table-def]
       (table->shape table-def nil))
      ([table-def class-table]
      {:pre [(types/table-def? table-def)]}
      (let [cols (:columns table-def)
            col-names (set (map :name cols))
            pks (let [pk (:primary-key table-def)]
                     (if (vector? pk) (set pk) #{pk}))
            explicit-fields (into {}
                                  (map (fn [col]
                                           (let [col (variant-column table-def class-table col)
                                                 is-ref? (= :ref (get-in col [:type :kind]))
                                                 col-name (keyword (str (name (:name col)) (when is-ref? "-id")))
                                                 is-pk? (contains? pks (:name col))
                                                 field-type (assoc (resolve-column-type col)
                                                                   :is-ref? is-ref?
                                                                   :nullable? (if is-pk? false (not (:required col)))
                                                                   :source (str (:name table-def) "." (:name col)))]
                                                [col-name field-type]))
                                       cols))
            ;; ONLY add id if it's missing from the explicit list and we want it as a default
            standard-fields (cond-> {}
                                    (not (contains? col-names :id))
                                    (assoc :id {:type :uuid :nullable? (not (contains? pks :id)) :source (str (:name table-def) ".id")}))]
           (types/make-jsonb-shape (merge standard-fields explicit-fields) (:name table-def) :high false))))

;; ─────────────────────────────────────────────────────────────────────────────
;; Shape Operations
;; ─────────────────────────────────────────────────────────────────────────────

(defn shape-for-table-op
      "Returns the appropriate type/shape for a table operation."
      [op table-def opts]
      (let [opts (if (map? opts) opts {})
            class-table (or (:class-table opts)
                            (get-in opts [:set :class-table])
                            (get-in opts [:where :class-table]))
            table-shape (table->shape table-def class-table)]
        (case op
            (:insert :get :update :delete :upsert) table-shape
            :select (types/make-jsonb-array table-shape)
            :id {:type :uuid :source (str (:name table-def) ".id")}
            :exists {:type :boolean}
            :count {:type :integer}
            :get-field (let [field-name (:returning opts)]
                            (get-in table-shape [:fields (keyword field-name)]
                                    {:type :unknown :source (str (:name table-def) "." field-name)}))
            nil)))

(defn access-field
      "Extracts a field type from a shape. Used by analyzer for :-> and :->> ops."
      [shape field-name accessor-type]
      (let [k (keyword field-name)
            field-info (get-in shape [:fields k])]
           (or (:type field-info)
               (case accessor-type :-> :jsonb :->> :text :unknown))))

(defn shape->map
      "Converts a shape to a plain map (for debugging/API)."
      [shape]
      {:fields (types/flatten-shape shape)
       :source-table (:source-table shape)
       :confidence (:confidence shape)})
