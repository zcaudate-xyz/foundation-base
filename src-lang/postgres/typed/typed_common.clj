(ns postgres.typed.typed-common
  (:require [clojure.string :as str]
            [std.string.case :as case]))

;; ─────────────────────────────────────────────────────────────────────────────
;; CRITIQUE FIX #2: Centralized Type Format Registry
;; One map for ALL type mappings - add a new type here, works everywhere
;; ─────────────────────────────────────────────────────────────────────────────

(def +type-formats+
  {:uuid      {:openapi {:type "string" :format "uuid"}
               :jschema {:type "string" :format "uuid"}
               :ts "string"}
   :text      {:openapi {:type "string"}
               :jschema {:type "string"}
               :ts "string"}
   :citext    {:openapi {:type "string"}
               :jschema {:type "string"}
               :ts "string"}
   :boolean   {:openapi {:type "boolean"}
               :jschema {:type "boolean"}
               :ts "boolean"}
   :integer   {:openapi {:type "integer"}
               :jschema {:type "integer"}
               :ts "number"}
   :bigint    {:openapi {:type "integer" :format "int64"}
               :jschema {:type "integer" :format "int64"}
               :ts "number"}
   :smallint  {:openapi {:type "integer"}
               :jschema {:type "integer"}
               :ts "number"}
   :numeric   {:openapi {:type "number"}
               :jschema {:type "number"}
               :ts "number"}
   :float     {:openapi {:type "number" :format "float"}
               :jschema {:type "number"}
               :ts "number"}
   :double    {:openapi {:type "number" :format "double"}
               :jschema {:type "number"}
               :ts "number"}
   :timestamp {:openapi {:type "string" :format "date-time"}
               :jschema {:type "string" :format "date-time"}
               :ts "string"}
   :date      {:openapi {:type "string" :format "date"}
               :jschema {:type "string" :format "date"}
               :ts "string"}
   :time      {:openapi {:type "string" :format "time"}
               :jschema {:type "string" :format "time"}
               :ts "string"}
   :jsonb     {:openapi {:type "object"}
               :jschema {:type "object"}
               :ts "Record<string, any>"}
   :bytea     {:openapi {:type "string" :format "binary"}
               :jschema {:type "string" :format "binary"}
               :ts "string"}
   :void      {:openapi {:type "object"}
               :jschema {:type "object"}
               :ts "void"}
   :keyword   {:openapi {:type "string"}
               :jschema {:type "string"}
               :ts "string"}})

;; ─────────────────────────────────────────────────────────────────────────────
;; CRITIQUE FIX #3: Single Key Normalization
;; All generators use this - no more scattered (str/replace #"-" "_")
;; ─────────────────────────────────────────────────────────────────────────────

(defn normalize-key
  "Converts kebab-case keyword to snake_case string.
   Single source of truth for all output generation."
  [k]
  (case/snake-case (name k)))

(defn emitted-key
  "Preserves literal string keys while normalizing keywords/symbols for output."
  [k]
  (cond
    (string? k) k
    (keyword? k) (normalize-key k)
    (symbol? k) (normalize-key k)
    :else (str k)))

(defn typescript-key
  "Formats a JSON object key for TypeScript property emission."
  [k]
  (let [key-str (emitted-key k)]
    (if (re-matches #"[A-Za-z_$][A-Za-z0-9_$]*" key-str)
      key-str
      (pr-str key-str))))

;; ─────────────────────────────────────────────────────────────────────────────
;; Constants
;; ─────────────────────────────────────────────────────────────────────────────

(def PRIMITIVE-TYPES
  "Set of primitive PostgreSQL types"
  (set (keys +type-formats+)))

(def TYPE-KINDS
  #{:primitive :enum :table :ref :array :composite
    :jsonb-shape :jsonb-merge :jsonb-array :union :unknown})

(def CONFIDENCE-LEVELS #{:high :medium :low})

(def JSONB-VARIANT-TYPES
  "Semantic types allowed in a class-table JSONB variant contract."
  #{nil :jsonb :map :array})

(def JSONB-VARIANT-PHYSICAL-KEYS
  "deftype.pg attributes which a variant must not replace."
  #{:sql :ref :primary :required :unique :default :scope
    :partition-by :generated})

(def JSONB-SHAPES
  "Explicit shapes supported by JSONB column metadata."
  #{:map :array :opaque})

(def JSONB-TYPES
  "Column type aliases which are emitted as PostgreSQL jsonb."
  #{:jsonb :map :array :image})

;; ─────────────────────────────────────────────────────────────────────────────
;; Core Records
;; ─────────────────────────────────────────────────────────────────────────────

(defrecord TypeRef [kind ns name constraints])
(defrecord EnumDef [ns name values dbschema])
(defrecord ColumnDef [name type required default constraints enum-ref
                      scope foreign map-schema items-schema ref-info])
(defrecord TableDef [ns name columns primary-key addons entity-meta dbschema])
(defrecord VariantDef [type class-table field attrs source])
(defrecord FnDef [ns name inputs output body-meta dbschema])
(defrecord FnArg [name type modifiers role])

(defrecord JsonbShape
  [fields           ; Map of field-key -> type-info
   source-table     ; Keyword: table this shape came from
   confidence       ; :high/:medium/:low
   nullable?])      ; Boolean

(defrecord JsonbPath [segments root-var])
(defrecord JsonbMerge [left right])
(defrecord JsonbArray [element-type])
(defrecord TypeUnion [types])

(defrecord JsonbInference
  [return-shape intermediate-vars operations-detected])

(defrecord BindingContext
  [bindings jsonb-shapes jsonb-paths parent])

;; ─────────────────────────────────────────────────────────────────────────────
;; Predicates
;; ─────────────────────────────────────────────────────────────────────────────

(defn type-ref? [x] (instance? TypeRef x))
(defn table-def? [x] (instance? TableDef x))
(defn enum-def? [x] (instance? EnumDef x))
(defn fn-def? [x] (instance? FnDef x))
(defn variant-def? [x] (instance? VariantDef x))
(defn jsonb-shape? [x] (instance? JsonbShape x))
(defn jsonb-path? [x] (instance? JsonbPath x))
(defn jsonb-merge? [x] (instance? JsonbMerge x))
(defn jsonb-array? [x] (instance? JsonbArray x))
(defn type-union? [x] (instance? TypeUnion x))
(defn binding-context? [x] (instance? BindingContext x))

(defn primitive? [t] (contains? PRIMITIVE-TYPES t))
(defn table? [t] (or (and (type-ref? t) (= :table (:kind t))) (table-def? t)))
(defn enum? [t] (or (and (type-ref? t) (= :enum (:kind t))) (enum-def? t)))
(defn ref? [t] (and (type-ref? t) (= :ref (:kind t))))

(defn jsonb-column-type?
  "Returns true when a type or primitive TypeRef is emitted as PostgreSQL jsonb."
  [t]
  (let [t (if (and (type-ref? t)
                   (= :primitive (:kind t)))
            (:name t)
            t)]
    (contains? JSONB-TYPES t)))

(defn jsonb-shape-marker?
  "Returns true for an explicit JSONB shape marker."
  [shape]
  (contains? JSONB-SHAPES shape))

(defn inferred-jsonb-shape
  "Infers the shape implied by a JSONB alias or a nested map schema.

   Plain :jsonb remains unclassified unless its metadata supplies a shape or
   nested :map schema. The :map and :array aliases retain their historical
   JSONB emission while carrying their useful semantic shape downstream."
  [type shape map-schema]
  (or shape
      (when map-schema :map)
      (case (if (and (type-ref? type)
                     (= :primitive (:kind type)))
              (:name type)
              type)
        :map :map
        :array :array
        nil)))

(declare validate-jsonb-metadata!)

(defn- invalid-jsonb-metadata
  [message data]
  (throw (ex-info message (merge {:type ::invalid-jsonb-metadata} data))))

(defn- valid-jsonb-schema-key?
  [k]
  (or (keyword? k)
      (symbol? k)
      (string? k)))

(defn validate-jsonb-metadata!
  "Validates JSONB shape metadata on a column or nested map entry.

   The function returns the original metadata map so it can be used in
   parser pipelines. It deliberately accepts plain :jsonb without a marker,
   preserving the existing open-ended JSONB form."
  [opts]
  (when-not (map? opts)
    (invalid-jsonb-metadata
     "JSONB metadata must be a map."
     {:options opts}))
  (let [{:keys [type shape]} opts
        map-present? (contains? opts :map)
        map-schema   (:map opts)
        items-present? (contains? opts :items)
        items-schema (:items opts)
        implied      (case type
                       :map :map
                       :array :array
                       nil)]
    (when (and (contains? opts :shape)
               (not (jsonb-shape-marker? shape)))
      (invalid-jsonb-metadata
       "Invalid JSONB shape. Expected :map, :array, or :opaque."
       {:shape shape :options opts}))
    (when (and (contains? opts :shape)
               (not (jsonb-column-type? type)))
      (invalid-jsonb-metadata
       "JSONB shape metadata requires a JSONB column type."
       {:shape shape :type type :options opts}))
    (when (and implied
               (contains? opts :shape)
               (not= implied shape))
      (invalid-jsonb-metadata
       "JSONB shape conflicts with the column type alias."
       {:shape shape :type type :expected implied :options opts}))
    (when map-present?
      (when-not (jsonb-column-type? type)
        (invalid-jsonb-metadata
         "Nested :map metadata requires a JSONB column type."
         {:type type :options opts}))
      (when-not (map? map-schema)
        (invalid-jsonb-metadata
         "Nested :map metadata must be a map of key schemas."
         {:map map-schema :options opts}))
      (when (and (contains? opts :shape)
                 (not= :map shape))
        (invalid-jsonb-metadata
         "Nested :map metadata requires the :map JSONB shape."
         {:shape shape :options opts}))
      (when (and implied (not= :map implied))
        (invalid-jsonb-metadata
         "Nested :map metadata conflicts with the column type alias."
         {:type type :options opts}))
      (doseq [[key entry] map-schema]
        (when-not (valid-jsonb-schema-key? key)
          (invalid-jsonb-metadata
           "JSONB map schema keys must be keywords, symbols, or strings."
           {:key key :options opts}))
        (when-not (map? entry)
          (invalid-jsonb-metadata
           "JSONB map schema entries must be metadata maps."
           {:key key :entry entry :options opts}))
        (validate-jsonb-metadata! entry)))
    (when items-present?
      (when-not (jsonb-column-type? type)
        (invalid-jsonb-metadata
         "Array item metadata requires a JSONB column type."
         {:type type :options opts}))
      (when-not (or (= :array shape)
                    (= :array implied))
        (invalid-jsonb-metadata
         "Array item metadata requires the :array JSONB shape."
         {:shape shape :type type :options opts}))
      (when map-present?
        (invalid-jsonb-metadata
         "Array item metadata cannot be combined with nested :map metadata."
         {:options opts}))
      (when-not (map? items-schema)
        (invalid-jsonb-metadata
         "Array item metadata must be a metadata map."
         {:items items-schema :options opts}))
      (validate-jsonb-metadata! items-schema))
    opts))

(defn normalize-jsonb-variant
  "Normalizes optional JSONB contract markers for a class-table variant.

   Variants describe the semantic payload stored in a JSONB column.  Unlike a
   normal column declaration, a variant may omit :type when :shape, :map, or
   :items establishes the JSONB contract.  In that case :jsonb is used only
   as the semantic base for validation; it does not change the physical
   column type."
  [attrs]
  (when-not (map? attrs)
    (throw (ex-info "JSONB variant metadata must be a map."
                    {:type ::invalid-jsonb-variant
                     :attrs attrs})))
  (let [type (:type attrs)
        needs-jsonb? (and (nil? type)
                          (or (contains? attrs :shape)
                              (contains? attrs :map)
                              (contains? attrs :items)))
        normalized (cond-> attrs
                     (and needs-jsonb?
                          (or (contains? attrs :shape)
                              (contains? attrs :map)
                              (contains? attrs :items)))
                     (assoc :type :jsonb)

                     (and (contains? attrs :items)
                          (not (contains? attrs :shape)))
                     (assoc :shape :array))]
    (when-not (contains? JSONB-VARIANT-TYPES (:type normalized))
      (throw (ex-info "JSONB variants must use :jsonb, :map, or :array."
                      {:type ::invalid-jsonb-variant
                       :attrs attrs
                       :variant-type (:type normalized)})))
    (when-let [key (some #(when (contains? normalized %) %) JSONB-VARIANT-PHYSICAL-KEYS)]
      (throw (ex-info "JSONB variants cannot declare physical column attributes."
                      {:type ::invalid-jsonb-variant
                       :attrs attrs
                       :key key})))
    (when (contains? normalized :reason)
      (when-not (and (string? (:reason normalized))
                     (seq (:reason normalized)))
        (throw (ex-info "JSONB variant :reason must be a non-empty string."
                        {:type ::invalid-jsonb-variant
                         :attrs attrs}))))
    (validate-jsonb-metadata! normalized)
    normalized))

;; ─────────────────────────────────────────────────────────────────────────────
;; Type Registry
;; ─────────────────────────────────────────────────────────────────────────────

(defonce ^:dynamic *type-registry* (atom {}))
(defonce ^:dynamic *variant-registry* (atom {}))

(declare variants-for-type attach-variants-to-table)

(defn valid-key?
  "Validates that a key is a namespaced symbol (e.g., 'ns/name)."
  [key]
  (boolean
   (and (symbol? key)
        (namespace key)
        (seq (namespace key))
        (seq (name key)))))

(defn register-type!
  "Registers a type definition. Requires a namespaced symbol key."
  [key type-def]
  (when-not (valid-key? key)
    (throw (ex-info "Invalid registry key. Must be a namespaced symbol like 'ns/name"
                    {:key key
                     :type (type key)
                     :help "Use (symbol \"namespace\" \"name\") to construct valid keys"})))
  (let [variants (when (table-def? type-def)
                   (vec (distinct (concat (:variants type-def)
                                          (variants-for-type key)))))
        type-def (if (seq variants)
                   (attach-variants-to-table type-def variants)
                   type-def)]
    (swap! *type-registry* assoc key type-def)))

(defn get-type [key]
  (get @*type-registry* key))

(defn clear-registry! []
  (reset! *type-registry* {})
  (reset! *variant-registry* {}))

;; ─────────────────────────────────────────────────────────────────────────────
;; App Typed Payload
;; ─────────────────────────────────────────────────────────────────────────────

(defn type-key
  "Stable key for a typed definition. Uses a namespaced symbol when possible."
  [type-def]
  (let [{:keys [ns name]} type-def]
    (if ns
      (symbol ns name)
      (symbol name))))

(defn variant-key
  "Returns the stable dispatch key for a VariantDef."
  [variant]
  [(:type variant) (:class-table variant) (:field variant)])

(defn variants-for-type
  "Returns registered variants for a qualified table type."
  [type]
  (->> @*variant-registry*
       (filter (fn [[key _]]
                 (= type (first key))))
       (mapcat (fn [[_ variants]] variants))
       vec))

(defn empty-typed
  "Empty app-level typed payload."
  []
  {:tables {}
   :enums {}
   :functions {}})

(defn add-typed
  "Adds a typed declaration to an app-level typed payload."
  [typed type-def]
  (cond
    (table-def? type-def)
    (assoc-in typed [:tables (type-key type-def)] type-def)

    (enum-def? type-def)
    (assoc-in typed [:enums (type-key type-def)] type-def)

    (fn-def? type-def)
    (assoc-in typed [:functions (type-key type-def)] type-def)

    (variant-def? type-def)
    (update-in typed [:variants (variant-key type-def)] (fnil conj []) type-def)

    :else typed))

(defn- variant-values
  [variants]
  (cond
    (map? variants) (mapcat #(if (sequential? %)
                               %
                               [%])
                            (vals variants))
    (sequential? variants) variants
    :else []))

(defn- table-column
  [table field]
  (some #(when (= field (:name %)) %)
        (:columns table)))

(defn- validate-table-variants
  [table variants]
  (let [duplicate-groups (->> variants
                              (group-by variant-key)
                              (filter (fn [[_ entries]]
                                        (> (count entries) 1))))]
    (when (seq duplicate-groups)
      (throw (ex-info "Multiple JSONB variants match the same table field."
                      {:type ::duplicate-variant
                       :table (type-key table)
                       :variants (mapv second duplicate-groups)})))
    (doseq [variant variants]
      (let [column (table-column table (:field variant))
            attrs (normalize-jsonb-variant (:attrs variant))
            base-type (when column
                        (let [type (:type column)]
                          (if (and (type-ref? type)
                                   (= :primitive (:kind type)))
                            (:name type)
                            type)))
            base-shape (case base-type
                         :map :map
                         :array :array
                         nil)
            variant-shape (inferred-jsonb-shape (:type attrs)
                                                (:shape attrs)
                                                (:map attrs))]
        (when-not column
          (throw (ex-info "JSONB variant references an undeclared table field."
                          {:type ::orphan-variant
                           :table (type-key table)
                           :field (:field variant)
                           :variant variant})))
        (when-not (jsonb-column-type? (:type column))
          (throw (ex-info "JSONB variants require a JSONB-backed base column."
                          {:type ::incompatible-variant
                           :table (type-key table)
                           :field (:field variant)
                           :base-type base-type
                           :variant variant})))
        (when (and base-shape
                   (not= base-shape variant-shape))
          (throw (ex-info "JSONB variant shape conflicts with the base column."
                          {:type ::incompatible-variant
                           :table (type-key table)
                           :field (:field variant)
                           :base-shape base-shape
                           :variant-shape variant-shape
                           :variant variant}))))))
  table)

(defn attach-variants-to-table
  "Attaches and validates variants for one qualified TableDef."
  [table variants]
  (let [variants (vec variants)]
    (if (seq variants)
      (assoc (validate-table-variants table variants)
             :variants variants)
      table)))

(defn attach-variants-to-tables
  "Attaches parsed variant declarations to their qualified TableDefs.

   Variant declarations can be parsed before or after their base table because
   they are retained in a separate typed section.  Attaching them after all
   declarations have been reduced keeps distributed declarations load-order
   independent while leaving the flat type registry compatible."
  [typed]
  (let [variants (vec (variant-values (:variants typed)))]
    (if (seq variants)
      (update typed :tables
              (fn [tables]
                (reduce-kv
                 (fn [acc table-key table]
                   (let [table-variants (->> variants
                                              (filter #(= table-key (:type %)))
                                              vec)]
                     (if (seq table-variants)
                       (assoc acc table-key
                              (attach-variants-to-table table table-variants))
                       acc)))
                 tables
                 tables)))
      typed)))

(defn register-variant!
  "Registers a variant and updates an already-registered base TableDef."
  [variant]
  (when-not (variant-def? variant)
    (throw (ex-info "Only VariantDef values can be registered as JSONB variants."
                    {:type ::invalid-variant
                     :variant variant})))
  (normalize-jsonb-variant (:attrs variant))
  (let [key (variant-key variant)]
    (swap! *variant-registry*
           update
           key
           (fnil (fn [variants]
                   (vec (distinct (conj variants variant))))
                 []))
    (when-let [table (get @*type-registry* (:type variant))]
      (swap! *type-registry*
             assoc
             (:type variant)
             (attach-variants-to-table
              table
              (variants-for-type (:type variant))))))
  variant)

(defn analysis->typed
  "Converts parsed analysis into app typed maps.

   The optional :variants section is retained separately and also attached to
   matching TableDefs for class-table-aware shape inference."
  [analysis]
  (attach-variants-to-tables
   (reduce add-typed
           (empty-typed)
           (concat (:tables analysis)
                   (:enums analysis)
                   (:functions analysis)
                   (:variants analysis)))))

(defn merge-typed
  "Merges app-level typed payloads."
  [& typed-maps]
  (let [merged (reduce (fn [acc m]
                         (-> acc
                             (update :tables merge (:tables m))
                             (update :enums merge (:enums m))
                             (update :functions merge (:functions m))
                             (cond-> (seq (:variants m))
                               (update :variants
                                       (fn [variants]
                                         (merge-with
                                          (fn [left right]
                                            (into (vec left) right))
                                          variants
                                          (:variants m)))))))
                       (empty-typed)
                       typed-maps)]
    (attach-variants-to-tables merged)))

;; ─────────────────────────────────────────────────────────────────────────────
;; Constructors
;; ─────────────────────────────────────────────────────────────────────────────

(defn make-type-ref
  ([kind] (make-type-ref kind nil nil))
  ([kind ns name] (make-type-ref kind ns name nil))
  ([kind ns name constraints] (->TypeRef kind ns name constraints)))

(defn make-enum-def [ns name values dbschema]
  (->EnumDef ns name (set values) dbschema))

(defn make-column-def
  ([name type] (make-column-def name type {}))
  ([name type opts] (map->ColumnDef (assoc opts :name name :type type))))

(defn make-table-def
  ([ns name columns primary-key] (make-table-def ns name columns primary-key nil nil nil))
  ([ns name columns primary-key addons entity-meta dbschema]
   (->TableDef ns name columns primary-key addons entity-meta dbschema)))

(defn make-variant-def
  ([type class-table field attrs]
   (make-variant-def type class-table field attrs nil))
  ([type class-table field attrs source]
   (->VariantDef type class-table field attrs source)))

(defn make-fn-def [ns name inputs output body-meta dbschema]
  (->FnDef ns name inputs output body-meta dbschema))

(defn make-jsonb-shape
  ([fields] (make-jsonb-shape fields nil :medium false))
  ([fields source-table] (make-jsonb-shape fields source-table :medium false))
  ([fields source-table confidence] (make-jsonb-shape fields source-table confidence false))
  ([fields source-table confidence nullable?]
   (->JsonbShape fields source-table confidence nullable?)))

(defn make-jsonb-merge [left right] (->JsonbMerge left right))
(defn make-jsonb-array [element-type] (->JsonbArray element-type))
(defn make-type-union [types] (->TypeUnion (vec (distinct types))))
(defn make-jsonb-path ([segments] (make-jsonb-path segments nil)) ([segments root-var] (->JsonbPath segments root-var)))
(defn make-jsonb-inference [return-shape intermediate-vars operations-detected]
  (->JsonbInference return-shape intermediate-vars operations-detected))
(defn make-context
  ([] (->BindingContext {} {} {} nil))
  ([bindings] (->BindingContext bindings {} {} nil))
  ([bindings shapes] (->BindingContext bindings shapes {} nil))
  ([bindings shapes paths] (->BindingContext bindings shapes paths nil)))

;; ─────────────────────────────────────────────────────────────────────────────
;; Context Operations
;; ─────────────────────────────────────────────────────────────────────────────

(defn push-scope [ctx] (->BindingContext {} {} {} ctx))
(defn pop-scope [ctx] (:parent ctx))

(defn add-binding [ctx var-name type-info & {:keys [shape path]}]
  (cond-> ctx
    true (update :bindings assoc var-name type-info)
    shape (update :jsonb-shapes assoc var-name shape)
    path (update :jsonb-paths assoc var-name path)))

(defn lookup-binding [ctx var-name]
  (or (get-in ctx [:bindings var-name])
      (when-let [parent (:parent ctx)] (lookup-binding parent var-name))))

(declare shape-at-path empty-jsonb-shape)

(defn get-var-shape [ctx var-name]
  (or (get-in ctx [:jsonb-shapes var-name])
      (when-let [path (get-in ctx [:jsonb-paths var-name])]
        (when-not (and (= var-name (:root-var path))
                       (empty? (:segments path)))
          (when-let [root-shape (get-var-shape ctx (:root-var path))]
            (shape-at-path root-shape (:segments path)))))
      (when-let [parent (:parent ctx)] (get-var-shape parent var-name))))

(defn set-var-shape [ctx var-name shape]
  (update ctx :jsonb-shapes assoc var-name shape))

(defn get-var-path [ctx var-name]
  (or (get-in ctx [:jsonb-paths var-name])
      (when-let [parent (:parent ctx)] (get-var-path parent var-name))))

(defn set-var-path [ctx var-name path]
  (update ctx :jsonb-paths assoc var-name path))

;; ─────────────────────────────────────────────────────────────────────────────
;; Shape Operations
;; ─────────────────────────────────────────────────────────────────────────────

(defn empty-jsonb-shape [] (->JsonbShape {} nil :low false))

(defn shape-at-path
  "Returns the nested JsonbShape at the provided path, if known."
  [shape segments]
  (cond
    (nil? shape)
    nil

    (empty? segments)
    shape

    :else
    (let [field-info (get-in shape [:fields (first segments)])
          nested-shape (:shape field-info)
          remaining (next segments)]
      (cond
        (and (empty? remaining)
             (jsonb-shape? nested-shape))
        nested-shape

        (and (empty? remaining)
             (= :jsonb (:type field-info)))
        (or nested-shape
            (empty-jsonb-shape))

        (jsonb-shape? nested-shape)
        (shape-at-path nested-shape remaining)

        :else
        nil))))

(defn add-key [shape key type-info]
  {:pre [(jsonb-shape? shape)]}
  (update shape :fields assoc key type-info))

(defn get-key-type [shape key]
  {:pre [(jsonb-shape? shape)]}
  (get-in shape [:fields key]))

(defn has-key? [shape key]
  {:pre [(jsonb-shape? shape)]}
  (contains? (:fields shape) key))

;; ─────────────────────────────────────────────────────────────────────────────
;; CRITIQUE FIX #4: Shape merge - used by both analyze and shape
;; Single implementation, no duplication
;; ─────────────────────────────────────────────────────────────────────────────

(defn merge-shapes
  "Merges two JSONB shapes. When keys conflict, prefers more specific type.
   CRITIQUE FIX: This is THE ONE implementation used everywhere.
   analyze.clj uses this - no local duplication."
  [shape1 shape2]
  {:pre [(or (nil? shape1) (jsonb-shape? shape1))
         (or (nil? shape2) (jsonb-shape? shape2))]}
  (cond
    (nil? shape1) shape2
    (nil? shape2) shape1
    :else
    (let [merged-fields (merge-with
                         (fn [t1 t2]
                           (cond
                             (= t1 t2) t1
                             (and (map? t1)
                                  (map? t2)
                                  (= (:type t1) (:type t2))
                                  (:type t1))
                             (merge t1 t2)
                             (or (= t1 :unknown) (= t1 :jsonb)) t2
                             (or (= t2 :unknown) (= t2 :jsonb)) t1
                             :else (make-type-union [t1 t2])))
                         (:fields shape1)
                         (:fields shape2))
          source1 (:source-table shape1)
          source2 (:source-table shape2)
          source-table (cond
                         (= source1 source2) source1
                         (nil? source1) source2
                         (nil? source2) source1
                         :else nil)]
      (->JsonbShape merged-fields source-table :medium
                    (or (:nullable? shape1) (:nullable? shape2))))))

(defn flatten-shape
  "Flattens a JsonbMerge tree into a single map of fields."
  [shape]
  (cond
    (jsonb-shape? shape) (:fields shape)
    (jsonb-merge? shape) (merge (flatten-shape (:left shape))
                                (flatten-shape (:right shape)))
    (map? shape) (if (:fields shape) (:fields shape) shape)
    :else {}))

;; ─────────────────────────────────────────────────────────────────────────────
;; Pretty Printing
;; ─────────────────────────────────────────────────────────────────────────────

(defmethod print-method TypeRef [t ^java.io.Writer w]
  (.write w (str "#Type[" (name (:kind t)) (when (:name t) (str ":" (:name t))) "]")))

(defmethod print-method EnumDef [e ^java.io.Writer w]
  (.write w (str "#Enum[" (:name e) ":" (count (:values e)) "]")))

(defmethod print-method TableDef [t ^java.io.Writer w]
  (.write w (str "#Table[" (:name t) ":" (count (:columns t)) "cols]")))

(defmethod print-method FnDef [f ^java.io.Writer w]
  (.write w (str "#Fn[" (:name f) ":" (count (:inputs f)) "args]")))

(defmethod print-method JsonbShape [s ^java.io.Writer w]
  (.write w (str "#Shape[" (:source-table s) ":" (count (:fields s)) "fields]")))

(defmethod print-method JsonbMerge [m ^java.io.Writer w]
  (.write w (str "#Merge[" (print-str (:left m)) " || " (print-str (:right m)) "]")))

(defmethod print-method JsonbArray [a ^java.io.Writer w]
  (.write w (str "#Array[" (print-str (:element-type a)) "]")))

(defmethod print-method TypeUnion [u ^java.io.Writer w]
  (.write w (str "#Union[" (count (:types u)) " types]")))
