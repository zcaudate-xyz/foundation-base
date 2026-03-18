(ns rt.postgres.infer.types
  "Unified type definitions for rt.postgres.infer.
   
   Addresses critique issues:
   1. NO table-instance-shape - use shape/table->shape everywhere
   2. Centralized +type-formats+ - single source for type mappings
   3. Single normalize-key - shared kebab->snake conversion"
  (:require [clojure.string :as str]
            [std.string.case :as case]))

;; ─────────────────────────────────────────────────────────────────────────────
;; CRITIQUE FIX #2: Centralized Type Format Registry
;; One map for ALL type mappings - add a new type here, works everywhere
;; ─────────────────────────────────────────────────────────────────────────────

(def +type-formats+
  "Central mapping of PostgreSQL types to all output formats.
   :openapi - OpenAPI 3.0 schema
   :jschema - JSON Schema
   :ts      - TypeScript type"
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

;; ─────────────────────────────────────────────────────────────────────────────
;; Core Records
;; ─────────────────────────────────────────────────────────────────────────────

(defrecord TypeRef [kind ns name constraints])
(defrecord EnumDef [ns name values])
(defrecord ColumnDef [name type required default constraints enum-ref
                      scope foreign map-schema ref-info])
(defrecord TableDef [ns name columns primary-key addons entity-meta])
(defrecord FnDef [ns name inputs output body-meta])
(defrecord FnArg [name type modifiers])

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
(defn jsonb-shape? [x] (instance? JsonbShape x))
(defn jsonb-merge? [x] (instance? JsonbMerge x))
(defn jsonb-array? [x] (instance? JsonbArray x))
(defn type-union? [x] (instance? TypeUnion x))
(defn binding-context? [x] (instance? BindingContext x))

(defn primitive? [t] (contains? PRIMITIVE-TYPES t))
(defn table? [t] (or (and (type-ref? t) (= :table (:kind t))) (table-def? t)))
(defn enum? [t] (or (and (type-ref? t) (= :enum (:kind t))) (enum-def? t)))
(defn ref? [t] (and (type-ref? t) (= :ref (:kind t))))

;; ─────────────────────────────────────────────────────────────────────────────
;; Type Registry
;; ─────────────────────────────────────────────────────────────────────────────

(defonce ^:dynamic *type-registry* (atom {}))

(defn register-type! [key type-def]
  (swap! *type-registry* assoc key type-def))

(defn get-type [key]
  (get @*type-registry* key))

(defn clear-registry! []
  (reset! *type-registry* {}))

;; ─────────────────────────────────────────────────────────────────────────────
;; Constructors
;; ─────────────────────────────────────────────────────────────────────────────

(defn make-type-ref
  ([kind] (make-type-ref kind nil nil))
  ([kind ns name] (make-type-ref kind ns name nil))
  ([kind ns name constraints] (->TypeRef kind ns name constraints)))

(defn make-enum-def [ns name values]
  (->EnumDef ns name (set values)))

(defn make-column-def
  ([name type] (make-column-def name type {}))
  ([name type opts] (map->ColumnDef (assoc opts :name name :type type))))

(defn make-table-def
  ([ns name columns primary-key] (make-table-def ns name columns primary-key nil nil))
  ([ns name columns primary-key addons entity-meta]
   (->TableDef ns name columns primary-key addons entity-meta)))

(defn make-fn-def [ns name inputs output body-meta]
  (->FnDef ns name inputs output body-meta))

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

(defn add-binding [ctx var-name type-info & {:keys [shape]}]
  (cond-> ctx
    true (update :bindings assoc var-name type-info)
    shape (update :jsonb-shapes assoc var-name shape)))

(defn lookup-binding [ctx var-name]
  (or (get-in ctx [:bindings var-name])
      (when-let [parent (:parent ctx)] (lookup-binding parent var-name))))

(defn get-var-shape [ctx var-name]
  (or (get-in ctx [:jsonb-shapes var-name])
      (when-let [parent (:parent ctx)] (get-var-shape parent var-name))))

(defn set-var-shape [ctx var-name shape]
  (update ctx :jsonb-shapes assoc var-name shape))

;; ─────────────────────────────────────────────────────────────────────────────
;; Shape Operations
;; ─────────────────────────────────────────────────────────────────────────────

(defn empty-jsonb-shape [] (->JsonbShape {} nil :low false))

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
                             (or (= t1 :unknown) (= t1 :jsonb)) t2
                             (or (= t2 :unknown) (= t2 :jsonb)) t1
                             :else (make-type-union [t1 t2])))
                         (:fields shape1)
                         (:fields shape2))
          source-table (or (:source-table shape1) (:source-table shape2))]
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
