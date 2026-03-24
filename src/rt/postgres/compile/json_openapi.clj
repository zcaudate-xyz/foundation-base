(ns rt.postgres.compile.json-openapi
  (:require [clojure.string :as str]
            [rt.postgres.compile.common :as compile.common]
            [rt.postgres.grammar.typed-analyze :as analyze]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-shape :as shape]))

(declare shape->openapi)

(defn field->openapi
  "Converts a field descriptor to OpenAPI schema."
  [field-info]
  (let [t (:type field-info)]
    (cond
      (:is-ref? field-info)
      {:type "string" :format "uuid"}

      (:shape field-info)
      (shape->openapi (:shape field-info))

      (= :array t)
      {:type "array" :items (or (field->openapi (:items field-info)) {:type "object"})}

      :else (compile.common/resolve-type field-info :openapi))))

(defn shape->openapi
  "Converts a JsonbShape to OpenAPI schema object."
  [shape]
  (let [fields (:fields shape)
        properties (into (sorted-map)
                         (map (fn [[k v]] [(types/emitted-key k) (field->openapi v)]))
                         fields)
        required (mapv types/emitted-key
                       (filter #(not (:nullable? (get fields %))) (keys fields)))]
    (cond-> {:type "object" :properties properties}
      (seq required) (assoc :required required))))

(defn arg->openapi
  "Converts a function argument to OpenAPI parameter schema.
   For :jsonb types that map to table inputs, uses the table's shape."
  [arg fn-def]
  (let [param-name (types/normalize-key (str/replace (name (:name arg)) #"^i-" ""))
        arg-type (:type arg)
        meta-table (get-in fn-def [:body-meta :api/meta :table])
        meta-cols (get-in fn-def [:body-meta :api/meta :columns])]
    [param-name
     (cond
       (and (= :jsonb arg-type)
            (types/fn-def? fn-def))
       (let [base-shape (or (when-let [table-def (compile.common/resolve-table-def meta-table)]
                              (compile.common/select-shape-columns
                               (shape/table->shape table-def)
                               meta-cols))
                            (compile.common/infer-jsonb-arg-shape (:name arg) fn-def))]
         (if base-shape
           (shape->openapi base-shape)
           (compile.common/resolve-type arg-type :openapi)))

       :else (compile.common/resolve-type arg-type :openapi))]))

(defn fn->openapi
  "Converts a FnDef to OpenAPI operation."
  [fn-def]
  (let [fn-name (:name fn-def)
        expose (get-in fn-def [:body-meta :expose])
        meta-table (get-in fn-def [:body-meta :api/meta :table])
        inputs (remove #(= 'o-op (:name %)) (:inputs fn-def))
        request-body (when (seq inputs)
                       {:content {"application/json"
                                  {:schema {:type "object"
                                            :properties (into (sorted-map) (map #(arg->openapi % fn-def) inputs))}}}})
        inferred (analyze/cached-infer fn-def)
        output (:output fn-def)
        table-name (cond
                     meta-table (name meta-table)
                     (= :shaped (:kind inferred))
                     (if-let [shape (:shape inferred)]
                       (when (types/jsonb-shape? shape)
                         (some-> shape :source-table name))
                       nil)
                     (types/jsonb-shape? inferred)
                     (some-> inferred :source-table name)
                     (and (= :shaped (:kind inferred)) (:table inferred))
                     (name (:table inferred))
                     (symbol? output) (name output)
                     :else nil)
        response-schema (cond
                          table-name
                          {:$ref (str "#/components/schemas/" table-name)}

                          (= :array (:kind inferred))
                          (let [elem (:element-type inferred)]
                            {:type "array"
                             :items (cond
                                      (and (:table inferred) (not (types/jsonb-shape? elem)))
                                      {:$ref (str "#/components/schemas/" (name (:table inferred)))}

                                      (types/jsonb-shape? elem)
                                      (if-let [source (:source-table elem)]
                                        {:$ref (str "#/components/schemas/" (name source))}
                                        (shape->openapi elem))

                                      (keyword? elem)
                                      (get-in types/+type-formats+ [elem :openapi])

                                      :else {:type "object"})})

                          (= :shaped (:kind inferred))
                          (if-let [shape (:shape inferred)]
                            (when (types/jsonb-shape? shape)
                              (shape->openapi shape))
                            {:type "object"})

                          (types/jsonb-shape? inferred)
                          (shape->openapi inferred)

                          (and (vector? output) (seq output)
                               (get-in types/+type-formats+ [(first output) :openapi]))
                          (get-in types/+type-formats+ [(first output) :openapi])

                          :else {:type "object"})
        schema-name (:dbschema fn-def)
        parameters (cond-> []
                     true (conj {:name "apikey"
                                 :in "header"
                                 :required true
                                 :schema {:type "string"}
                                 :description "Supabase API key"})
                     schema-name (conj {:name "Accept-Profile"
                                        :in "header"
                                        :required false
                                        :schema {:type "string" :default schema-name}
                                        :description "Database schema for the response"})
                     (and schema-name request-body) (conj {:name "Content-Profile"
                                                           :in "header"
                                                           :required false
                                                           :schema {:type "string" :default schema-name}
                                                           :description "Database schema for the request body"}))]
    {:operationId (types/normalize-key fn-name)
     :tags [(or schema-name (:ns fn-def) "default")]
     :summary (get-in fn-def [:body-meta :docstring])
     :security (when (and expose (not= :sb/query expose))
                 (case expose
                   :sb/auth [{"bearerAuth" []}]
                   :sb/super [{"bearerAuth" ["super"]}]
                   []))
     :parameters parameters
     :requestBody request-body
     :responses {"200" {:description "Successful response" :content {"application/json" {:schema response-schema}}}
                 "400" {:description "Bad request"}
                 "401" {:description "Unauthorized"}
                 "500" {:description "Internal server error"}}}))

(defn generate-openapi
  "Generates complete OpenAPI 3.0 spec."
  [root-ns fn-filter]
  (let [all-vals (vals @types/*type-registry*)
        fns (->> all-vals
                 (filter types/fn-def?)
                 (filter fn-filter)
                 (compile.common/unique-defs))
        tables (->> all-vals
                    (filter types/table-def?)
                    (compile.common/unique-defs))
        enums (->> all-vals
                   (filter types/enum-def?)
                   (compile.common/unique-defs))]
    {:openapi "3.0.3"
     :info {:title (str root-ns " API") :version "0.1.0"}
     :paths (into (sorted-map)
                  (map (fn [f]
                         [(str "/rpc/" (types/normalize-key (:name f)))
                          {"post" (fn->openapi f)}]))
                  fns)
     :components
     {:schemas (into (sorted-map)
                      (concat
                       (map (fn [t] [(compile.common/def-name t) (shape->openapi (shape/table->shape t))]) tables)
                       (map (fn [e] [(compile.common/def-name e) {:type "string" :enum (mapv name (:values e))}]) enums)))}
     :security [{"bearerAuth" []}]
     :securityDefinitions {"bearerAuth" {:type "http" :scheme "bearer" :bearerFormat "JWT"}}}))
