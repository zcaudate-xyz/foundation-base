(ns postgres.typed.export.portable-edn
  (:require [clojure.edn :as edn]
            [postgres.typed.typed-common :as types]))

(def ^:private +edn-format+ :postgres.typed/context)
(def ^:private +edn-version+ 1)
(def ^:private +edn-format-key+ :postgres.typed/format)
(def ^:private +edn-version-key+ :postgres.typed/version)
(def ^:private +edn-context-key+ :postgres.typed/context)
(def ^:private +edn-record-key+ :postgres.typed/record)
(def ^:private +edn-data-key+ :postgres.typed/data)
(def ^:private +edn-meta-key+ :postgres.typed/meta)

(def ^:private +edn-record-codecs+
  [{:class-name "postgres.typed.typed_common.TypeRef"
    :tag :type-ref
    :constructor types/map->TypeRef}
   {:class-name "postgres.typed.typed_common.EnumDef"
    :tag :enum-def
    :constructor types/map->EnumDef}
   {:class-name "postgres.typed.typed_common.ColumnDef"
    :tag :column-def
    :constructor types/map->ColumnDef}
   {:class-name "postgres.typed.typed_common.TableDef"
    :tag :table-def
    :constructor types/map->TableDef}
   {:class-name "postgres.typed.typed_common.VariantDef"
    :tag :variant-def
    :constructor types/map->VariantDef}
   {:class-name "postgres.typed.typed_common.FnDef"
    :tag :fn-def
    :constructor types/map->FnDef}
   {:class-name "postgres.typed.typed_common.FnArg"
    :tag :fn-arg
    :constructor types/map->FnArg}
   {:class-name "postgres.typed.typed_common.JsonbShape"
    :tag :jsonb-shape
    :constructor types/map->JsonbShape}
   {:class-name "postgres.typed.typed_common.JsonbPath"
    :tag :jsonb-path
    :constructor types/map->JsonbPath}
   {:class-name "postgres.typed.typed_common.JsonbMerge"
    :tag :jsonb-merge
    :constructor types/map->JsonbMerge}
   {:class-name "postgres.typed.typed_common.JsonbArray"
    :tag :jsonb-array
    :constructor types/map->JsonbArray}
   {:class-name "postgres.typed.typed_common.TypeUnion"
    :tag :type-union
    :constructor types/map->TypeUnion}
   {:class-name "postgres.typed.typed_common.JsonbInference"
    :tag :jsonb-inference
    :constructor types/map->JsonbInference}
   {:class-name "postgres.typed.typed_common.BindingContext"
    :tag :binding-context
    :constructor types/map->BindingContext}])

(def ^:private +edn-record-codecs-by-tag+
  (into {} (map (juxt :tag identity) +edn-record-codecs+)))

(declare encode-edn-value decode-edn-value)

(defn- edn-error
  [message data]
  (throw (ex-info message data)))

(defn- attach-encoded-meta
  [value encoded path]
  (if-let [metadata (meta value)]
    (with-meta encoded
      (encode-edn-value metadata (conj path :metadata)))
    encoded))

(defn- attach-decoded-meta
  [value decoded path]
  (if-let [metadata (meta value)]
    (with-meta decoded
      (decode-edn-value metadata (conj path :metadata)))
    decoded))

(defn- record-codec
  [value]
  (when (instance? clojure.lang.IRecord value)
    (or (some #(when (= (:class-name %)
                        (.getName (class value)))
              %)
              +edn-record-codecs+)
        (edn-error "Unsupported postgres.typed record"
                   {:type :postgres.typed/unsupported-record
                    :class (.getName (class value))}))))

(defn- edn-scalar?
  [value]
  (or (nil? value)
      (string? value)
      (char? value)
      (boolean? value)
      (number? value)
      (keyword? value)
      (symbol? value)
      (instance? java.util.Date value)
      (instance? java.util.UUID value)
      (instance? java.util.regex.Pattern value)))

(defn- encode-edn-value
  [value path]
  (let [codec (record-codec value)]
    (cond
      codec
      (let [data (encode-edn-value (into {} value)
                                   (conj path +edn-data-key+))
            encoded (cond-> {+edn-record-key+ (:tag codec)
                             +edn-data-key+ data}
                      (some? (meta value))
                      (assoc +edn-meta-key+
                             (encode-edn-value (meta value)
                                               (conj path +edn-meta-key+))))]
        encoded)

      (edn-scalar? value)
      value

      (map? value)
      (attach-encoded-meta
       value
       (into {}
             (map (fn [[key item]]
                    [(encode-edn-value key (conj path :key))
                     (encode-edn-value item (conj path key))])
                  value))
       path)

      (vector? value)
      (attach-encoded-meta value
                           (mapv #(encode-edn-value % path) value)
                           path)

      (set? value)
      (attach-encoded-meta value
                           (set (map #(encode-edn-value % path) value))
                           path)

      (seq? value)
      (attach-encoded-meta value
                           (apply list (map #(encode-edn-value % path) value))
                           path)

      :else
      (edn-error "Value cannot be represented in a postgres.typed EDN snapshot"
                 {:type :postgres.typed/non-serializable-value
                  :path path
                  :class (some-> value class .getName)}))))

(defn- decode-edn-map
  [value path]
  (if (contains? value +edn-record-key+)
    (let [tag (get value +edn-record-key+)
          codec (get +edn-record-codecs-by-tag+ tag)
          data (get value +edn-data-key+)]
      (when-not codec
        (edn-error "Unknown postgres.typed record tag"
                   {:type :postgres.typed/unknown-record-tag
                    :path path
                    :tag tag}))
      (when-not (map? data)
        (edn-error "Postgres.typed record data must be a map"
                   {:type :postgres.typed/invalid-record-data
                    :path (conj path +edn-data-key+)
                    :tag tag}))
      (let [record ((:constructor codec)
                    (decode-edn-value data
                                     (conj path +edn-data-key+)))]
        (if (contains? value +edn-meta-key+)
          (let [metadata (decode-edn-value
                          (get value +edn-meta-key+)
                          (conj path +edn-meta-key+))]
            (when-not (map? metadata)
              (edn-error "Postgres.typed record metadata must be a map"
                         {:type :postgres.typed/invalid-record-meta
                          :path (conj path +edn-meta-key+)
                          :tag tag}))
            (with-meta record metadata))
          record)))
    (attach-decoded-meta
     value
     (into {}
           (map (fn [[key item]]
                  [(decode-edn-value key (conj path :key))
                   (decode-edn-value item (conj path key))])
                value))
     path)))

(defn- decode-edn-value
  [value path]
  (cond
    (map? value)
    (decode-edn-map value path)

    (vector? value)
    (attach-decoded-meta
     value
     (mapv #(decode-edn-value % path) value)
     path)

    (set? value)
    (attach-decoded-meta
     value
     (set (map #(decode-edn-value % path) value))
     path)

    (seq? value)
    (attach-decoded-meta
     value
     (apply list (map #(decode-edn-value % path) value))
     path)

    (edn-scalar? value)
    value

    :else
    (edn-error "Value cannot be represented in a postgres.typed context"
               {:type :postgres.typed/non-serializable-value
                :path path
                :class (some-> value class .getName)})))

(defn export-edn
  "Returns a versioned, plain EDN data structure for a postgres typed context.

   Pretty print the result with `clojure.pprint/pprint` or serialize it with
   `pr-str`. Read serialized text with `clojure.edn/read-string` before passing
   it to `import-edn`.
   Function filters are runtime values and therefore must be supplied again to
   APIs such as `export-openapi` after importing."
  [ctx]
  (when-not (map? ctx)
    (edn-error "A postgres.typed context must be a map"
               {:type :postgres.typed/invalid-context
                :value ctx}))
  (when (some? (:function-filter ctx))
    (edn-error "A postgres.typed context with a function filter cannot be exported"
               {:type :postgres.typed/non-serializable-value
                :path [:function-filter]
                :class (some-> (:function-filter ctx) class .getName)}))
  {+edn-format-key+ +edn-format+
   +edn-version-key+ +edn-version+
   +edn-context-key+ (encode-edn-value ctx [:context])})

(defn import-edn
  "Returns a postgres typed context from a snapshot data structure."
  [snapshot]
  (when-not (map? snapshot)
    (edn-error "A postgres.typed EDN snapshot must contain a map"
               {:type :postgres.typed/invalid-edn
                :value snapshot}))
  (when-not (= +edn-format+ (get snapshot +edn-format-key+))
    (edn-error "Unknown postgres.typed EDN snapshot format"
               {:type :postgres.typed/invalid-edn-format
                :format (get snapshot +edn-format-key+)}))
  (when-not (= +edn-version+ (get snapshot +edn-version-key+))
    (edn-error "Unsupported postgres.typed EDN snapshot version"
               {:type :postgres.typed/unsupported-edn-version
                :version (get snapshot +edn-version-key+)}))
  (let [ctx (decode-edn-value (get snapshot +edn-context-key+)
                              [:context])]
    (when-not (map? ctx)
      (edn-error "The postgres.typed EDN context must be a map"
                 {:type :postgres.typed/invalid-context}))
    ctx))
