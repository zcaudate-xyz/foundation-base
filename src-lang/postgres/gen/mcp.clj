(ns postgres.gen.mcp
  "Extracts opt-in MCP tool descriptors from PostgreSQL definitions. This
   namespace produces data only; executable handlers are attached by xt.mcp.node."
  (:require [hara.lang :as l]
            [postgres.gen.bind-macro :as bind]
            [postgres.gen.template-code :as template]))

(defn pg-type-schema
  "maps a bound PostgreSQL type to the portable MCP JSON Schema subset"
  {:added "4.1"}
  [type]
  (cond
    (contains? #{"smallint" "integer" "bigint" "int2" "int4" "int8"} type)
    {:type "integer"}

    (contains? #{"decimal" "numeric" "real" "double precision" "float4" "float8"} type)
    {:type "number"}

    (contains? #{"boolean" "bool"} type) {:type "boolean"}
    (contains? #{"json" "jsonb"} type) {:type "object"}
    (= "uuid" type) {:type "string" :format "uuid"}
    (= "date" type) {:type "string" :format "date"}
    (contains? #{"timestamp" "timestamptz" "timestamp with time zone"
                 "timestamp without time zone"} type)
    {:type "string" :format "date-time"}
    (= "bytea" type) {:type "string" :contentEncoding "base64"}
    :else {:type "string"}))

(defn input-schema
  "derives an object schema while preserving PostgreSQL argument names exactly"
  {:added "4.1"}
  [input]
  {:type "object"
   :properties (into (sorted-map)
                     (map (fn [{:keys [symbol type]}]
                            [symbol (pg-type-schema type)]))
                     input)
   :required (mapv :symbol input)
   :additional_properties false})

(defn output-schema
  "derives a conservative result schema from a PostgreSQL return type"
  {:added "4.1"}
  [return]
  (pg-type-schema return))

(defn tool-descriptor
  "extracts one opt-in descriptor from a PostgreSQL pointer"
  {:added "4.1"}
  [ptr]
  (let [entry (bind/bind-entry ptr)
        metadata (get-in entry [:api/meta :mcp])]
    (when metadata
      (let [{:keys [input return]} (bind/bind-function ptr)
            descriptor (merge {:input_schema (input-schema input)
                               :output_schema (output-schema return)}
                              metadata)]
        (when-not (and (string? (:name descriptor))
                       (seq (:name descriptor)))
          (throw (ex-info "MCP metadata requires a non-empty :name"
                          {:entry (:id entry) :mcp metadata})))
        (when-not (and (string? (:description descriptor))
                       (seq (:description descriptor)))
          (throw (ex-info "MCP metadata requires a non-empty :description"
                          {:entry (:id entry) :mcp metadata})))
        descriptor))))

(defn tool-entries
  "extracts deterministic, uniquely named MCP descriptors from namespaces"
  {:added "4.1"}
  [source-namespaces]
  (let [entries (->> source-namespaces
                     (mapcat (fn [ns-sym]
                               (require ns-sym)
                               (keep (fn [[_ sym]]
                                       (when-let [descriptor
                                                  (tool-descriptor @(resolve sym))]
                                         [sym descriptor]))
                                     (bind/list-all ns-sym))))
                     (sort-by (juxt (comp :name second) (comp str first)))
                     vec)
        duplicates (->> entries
                        (group-by (comp :name second))
                        (keep (fn [[name matches]]
                                (when (< 1 (count matches))
                                  {:name name
                                   :sources (mapv (comp str first) matches)})))
                        seq)]
    (when duplicates
      (throw (ex-info "Duplicate generated MCP tool names"
                      {:duplicates duplicates})))
    entries))

(defn render-module
  "renders descriptors as an xt.mcp data module"
  {:added "4.1"}
  [target-ns source-namespaces]
  (template/render-module target-ns :mcp (tool-entries source-namespaces)))
