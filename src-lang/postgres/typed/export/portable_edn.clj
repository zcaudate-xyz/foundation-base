(ns postgres.typed.export.portable-edn
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [postgres.typed.typed-common :as types]))

(def ^:private +edn-format+ :postgres.typed/context)
(def ^:private +edn-version+ 1)
(def ^:private +edn-format-key+ :postgres.typed/format)
(def ^:private +edn-version-key+ :postgres.typed/version)
(def ^:private +edn-context-key+ :postgres.typed/context)
(def ^:private +edn-record-key+ :postgres.typed/record)
(def ^:private +edn-data-key+ :postgres.typed/data)
(def ^:private +edn-meta-key+ :postgres.typed/meta)
(def ^:private +edn-stream-format+ :postgres.typed/context-stream)
(def ^:private +edn-stream-metadata-key+ :postgres.typed/metadata)
(def ^:private +edn-stream-entry-counts-key+ :postgres.typed/entry-counts)
(def ^:private +edn-stream-entry-tag+ :postgres.typed/entry)
(def ^:private +edn-stream-entry-paths+
  [[:registry]
   [:typed :enums]
   [:typed :functions]
   [:typed :tables]])

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
    (or (some (fn [codec]
                (when (= (:class-name codec)
                         (.getName (class value)))
                  codec))
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

   Use `export-edn-string` and `import-edn-string` when persisting a context so
   entries remain separately diffable and the encoded metadata is preserved.
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

(defn- canonical-edn-value
  [value]
  (let [compare-values (fn [left right]
                         (compare (pr-str left) (pr-str right)))
        metadata (when (meta value)
                   (canonical-edn-value (meta value)))]
    (cond
      (map? value)
      (with-meta
        (into (sorted-map-by compare-values)
              (map (fn [[key item]]
                     [(canonical-edn-value key)
                      (canonical-edn-value item)]))
              value)
        metadata)

      (vector? value)
      (with-meta (mapv canonical-edn-value value) metadata)

      (set? value)
      (with-meta
        (into (sorted-set-by compare-values)
              (map canonical-edn-value)
              value)
        metadata)

      (seq? value)
      (with-meta (apply list (map canonical-edn-value value)) metadata)

      :else value)))

(defn- snapshot-entry-path
  [path]
  (into [+edn-context-key+] path))

(defn- empty-snapshot-entries
  [snapshot]
  (reduce (fn [out path]
            (let [snapshot-path (snapshot-entry-path path)
                  entries (get-in out snapshot-path)]
              (if (map? entries)
                (assoc-in out snapshot-path
                          (with-meta (empty entries) (meta entries)))
                out)))
          snapshot
          +edn-stream-entry-paths+))

(defn- stream-entry-forms
  [snapshot]
  (for [path +edn-stream-entry-paths+
        :let [entries (get-in snapshot (snapshot-entry-path path))]
        :when (map? entries)
        [key value] (sort-by (comp pr-str first) entries)]
    [+edn-stream-entry-tag+ {:path path :key key} value]))

(defn- stream-entry-counts
  [snapshot]
  (into {}
        (for [path +edn-stream-entry-paths+
              :let [entries (get-in snapshot (snapshot-entry-path path))]
              :when (map? entries)]
          [path (count entries)])))

(defn- print-edn-list
  [value]
  (when (and *print-meta* (meta value))
    (print "^")
    (pr (meta value))
    (print " "))
  (pprint/pprint-logical-block :prefix "(" :suffix ")"
    (loop [items (seq value)]
      (when items
        (pprint/write-out (first items))
        (when-let [remaining (next items)]
          (.write ^java.io.Writer *out* " ")
          (pprint/pprint-newline :linear)
          (recur remaining))))))

(defn- edn-pprint-dispatch
  [value]
  (if (seq? value)
    (print-edn-list value)
    (pprint/simple-dispatch value)))

(defn- print-edn-form
  [form]
  (binding [pprint/*print-pprint-dispatch* edn-pprint-dispatch
            pprint/*print-right-margin* 200
            *print-namespace-maps* false
            *print-meta* true]
    (with-out-str (pprint/pprint (canonical-edn-value form)))))

(defn export-edn-string
  "Serializes a typed context as a stable EDN stream.

   The first form is the portable snapshot metadata with registry, enum,
   function, and table entry maps emptied. Each following form has the shape
   `[:postgres.typed/entry {:path path :key key} value]`. Pass the stream to
   `import-edn-string` to reconstruct the original typed context. Use
   `export-edn` when a single snapshot data structure is needed instead."
  [ctx]
  (let [snapshot (export-edn ctx)
        header {+edn-format-key+ +edn-stream-format+
                +edn-version-key+ +edn-version+
                +edn-stream-metadata-key+ (empty-snapshot-entries snapshot)
                +edn-stream-entry-counts-key+ (stream-entry-counts snapshot)}
        forms (cons header (stream-entry-forms snapshot))]
    (string/join "\n" (map print-edn-form forms))))

(defn- import-edn-stream
  [header read-form eof]
  (let [version (get header +edn-version-key+)
        snapshot (get header +edn-stream-metadata-key+)
        expected-counts (get header +edn-stream-entry-counts-key+)]
    (when-not (= 1 version)
      (edn-error "Unsupported postgres.typed EDN stream version"
                 {:type :postgres.typed/unsupported-edn-stream-version
                  :version version}))
    (when-not (map? snapshot)
      (edn-error "A postgres.typed EDN stream must contain metadata"
                 {:type :postgres.typed/invalid-edn-stream-metadata}))
    (when-not (map? expected-counts)
      (edn-error "A postgres.typed EDN stream must declare its entry counts"
                 {:type :postgres.typed/invalid-edn-stream-metadata}))
    (let [target-paths
          (reduce (fn [out path]
                    (let [entries (get-in snapshot (snapshot-entry-path path))]
                      (if (map? entries)
                        (if (empty? entries)
                          (conj out path)
                          (edn-error "EDN stream metadata contains entry data"
                                     {:type :postgres.typed/invalid-edn-stream-metadata
                                      :path path}))
                        out)))
                  #{}
                  +edn-stream-entry-paths+)]
      (when-not (= target-paths (set (keys expected-counts)))
        (edn-error "EDN stream entry counts do not match its metadata"
                   {:type :postgres.typed/invalid-edn-stream-metadata
                    :paths target-paths
                    :entry-count-paths (set (keys expected-counts))}))
      (when-not (every? (fn [[path count]]
                          (and (contains? target-paths path)
                               (integer? count)
                               (<= 0 count)))
                        expected-counts)
        (edn-error "EDN stream entry counts must be nonnegative integers"
                   {:type :postgres.typed/invalid-edn-stream-metadata
                    :entry-counts expected-counts}))
      (loop [snapshot snapshot
             seen #{}
             actual-counts (zipmap target-paths (repeat 0))]
        (let [form (read-form)]
          (if (identical? eof form)
            (if (= expected-counts actual-counts)
              (import-edn snapshot)
              (edn-error "The postgres.typed EDN stream is incomplete"
                         {:type :postgres.typed/incomplete-edn-stream
                          :expected expected-counts
                          :actual actual-counts}))
            (let [[tag entry-header value] (when (vector? form) form)
                  path (:path entry-header)
                  key (:key entry-header)
                  identity [path key]]
              (when-not (and (vector? form)
                             (= 3 (count form))
                             (= +edn-stream-entry-tag+ tag)
                             (map? entry-header)
                             (contains? entry-header :path)
                             (contains? entry-header :key)
                             (contains? target-paths path))
                (edn-error "Invalid postgres.typed EDN stream entry"
                           {:type :postgres.typed/invalid-edn-stream-entry
                            :entry form}))
              (when (contains? seen identity)
                (edn-error "Duplicate postgres.typed EDN stream entry"
                           {:type :postgres.typed/duplicate-edn-stream-entry
                            :path path
                            :key key}))
              (recur (assoc-in snapshot
                               (conj (snapshot-entry-path path) key)
                               value)
                     (conj seen identity)
                     (update actual-counts path inc)))))))))

(defn import-edn-string
  "Reads an EDN stream from `export-edn-string` into a typed context.

   Also accepts the previous single-form snapshot representation for migration
   of existing resources."
  [source]
  (when-not (string? source)
    (edn-error "A postgres.typed EDN stream must be a string"
               {:type :postgres.typed/invalid-edn-stream}))
  (with-open [reader (java.io.PushbackReader. (java.io.StringReader. source))]
    (let [eof (Object.)
          read-core-form #(binding [*read-eval* false]
                            (read {:eof eof} reader))
          first-form (read-core-form)]
      (when (identical? eof first-form)
        (edn-error "A postgres.typed EDN stream cannot be empty"
                   {:type :postgres.typed/invalid-edn-stream}))
      (if (and (map? first-form)
               (= +edn-stream-format+ (get first-form +edn-format-key+)))
        (import-edn-stream first-form
                           #(edn/read {:eof eof} reader)
                           eof)
        (let [ctx (import-edn first-form)]
          (when-not (identical? eof (read-core-form))
            (edn-error "A legacy postgres.typed snapshot must contain one EDN form"
                       {:type :postgres.typed/invalid-edn-stream}))
          ctx)))))
