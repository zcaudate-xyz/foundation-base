(ns postgres.typed.export.json-openapi
  (:require [clojure.string :as str]
            [postgres.typed.typed-analyze :as analyze]
            [postgres.typed.typed-common :as types]
            [postgres.typed.typed-infer :as typed-infer]
            [postgres.typed.typed-shape :as shape]
            [postgres.typed.export.json-schema :as json-schema]))

(declare shape->openapi)

(defn- def-name
  [x]
  (some-> x :name name))

(defn- unique-defs
  [defs]
  (second
   (reduce (fn [[seen output] definition]
             (let [name (def-name definition)]
               (if (contains? seen name)
                 [seen output]
                 [(conj seen name) (conj output definition)])))
           [#{} []]
           defs)))

(defn- ordered-map
  [entries]
  (let [output (java.util.LinkedHashMap.)]
    (doseq [[key value] entries]
      (.put output key value))
    output))

(defn- map-like?
  [value]
  (or (map? value)
      (instance? java.util.Map value)))

(defn- ordered-value
  [value]
  (cond
    (map-like? value)
    (ordered-map
     (map (fn [[key nested-value]]
            [key (ordered-value nested-value)])
          (if (instance? java.util.LinkedHashMap value)
            value
            (sort-by (comp str key) value))))

    (sequential? value)
    (mapv ordered-value value)

    :else
    value))

(defn- order-name
  [value]
  (let [value (if (map-like? value)
                (or (:id value) (:name value))
                value)]
    (cond
      (nil? value) nil
      (string? value) value
      (keyword? value) (name value)
      (symbol? value) (name value)
      :else (str value))))

(defn- definition-order-keys
  [definition]
  (let [name (def-name definition)]
    (distinct [name
               (when name (types/normalize-key name))])))

(defn- ordered-defs
  [defs source-order registration-order]
  (let [defs (vec (unique-defs defs))
        by-name (reduce (fn [output definition]
                          (reduce (fn [output key]
                                    (if key
                                      (assoc output key definition)
                                      output))
                                  output
                                  (definition-order-keys definition)))
                        {}
                        defs)
        preferred-names (->> (concat source-order registration-order)
                             (map order-name)
                             (remove nil?)
                             distinct)
        preferred (keep by-name preferred-names)
        preferred-set (set preferred)
        remaining (remove preferred-set defs)]
    (concat preferred
            (sort-by (juxt (comp str def-name)
                           (comp str :ns))
                     remaining))))

(defn- ordered-field-keys
  [fields field-order]
  (let [ordered (->> field-order
                     (filter #(contains? fields %))
                     distinct)
        remaining (sort-by str (remove (set ordered) (keys fields)))]
    (concat ordered remaining)))

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

      :else (json-schema/resolve-type field-info :openapi))))

(defn shape->openapi
  "Converts a JsonbShape to OpenAPI schema object."
  [shape]
  (let [fields (:fields shape)
        field-keys (ordered-field-keys fields (:field-order shape))
        properties (ordered-map
                    (map (fn [k]
                           [(types/emitted-key k)
                            (field->openapi (get fields k))])
                         field-keys))
        required (mapv types/emitted-key
                       (filter #(not (:nullable? (get fields %))) field-keys))]
    (ordered-map
     (cond-> [[:type "object"]
              [:properties properties]]
       (seq required) (conj [:required required])))))

(defn arg->openapi
  "Converts a function argument to OpenAPI parameter schema.
   For :jsonb types that map to table inputs, uses the table's shape."
  [arg fn-def]
  (let [param-name (types/normalize-key (str/replace (name (:name arg)) #"^i-" ""))
        arg-type (:type arg)
        arg-role (:role arg)
        meta-table (get-in fn-def [:body-meta :api/meta :table])
        meta-cols (get-in fn-def [:body-meta :api/meta :columns])]
    [param-name
     (cond
       (= :track arg-role)
       (json-schema/resolve-type arg-type :openapi)

       (and (= :jsonb arg-type)
            (types/fn-def? fn-def))
       (let [base-shape (or (when-let [table-def (typed-infer/resolve-table-def meta-table)]
                              (typed-infer/select-shape-columns
                               (shape/table->shape table-def)
                               meta-cols))
                            (typed-infer/infer-jsonb-arg-shape (:name arg) fn-def))]
         (if base-shape
           (shape->openapi base-shape)
           (json-schema/resolve-type arg-type :openapi)))

       :else (json-schema/resolve-type arg-type :openapi))]))

(defn fn->openapi
  "Converts a FnDef to OpenAPI operation."
  [fn-def]
  (let [fn-name (:name fn-def)
        expose (get-in fn-def [:body-meta :expose])
        meta-table (get-in fn-def [:body-meta :api/meta :table])
        inputs (:inputs fn-def)
        request-body (when (seq inputs)
                       (ordered-map
                        [[:content
                          (ordered-map
                           [["application/json"
                             (ordered-map
                              [[:schema
                                (ordered-map
                                 [[:type "object"]
                                  [:properties
                                   (ordered-map
                                    (map #(arg->openapi % fn-def) inputs))]])]])]])]]))
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
                                                           :description "Database schema for the request body"}))
        responses (ordered-map
                   (list
                    (list "200"
                          (ordered-map
                           [[:description "Successful response"]
                            [:content
                             (ordered-map
                              (list
                               (list "application/json"
                                     (ordered-map
                                      [[:schema (ordered-value response-schema)]]))))]]))
                    (list "400" (ordered-map [[:description "Bad request"]]))
                    (list "401" (ordered-map [[:description "Unauthorized"]]))
                    (list "500" (ordered-map [[:description "Internal server error"]]))))]
    (ordered-map
     [[:operationId (types/normalize-key fn-name)]
      [:tags [(or schema-name (:ns fn-def) "default")]]
      [:summary (get-in fn-def [:body-meta :docstring])]
      [:security (when (and expose (not= :sb/query expose))
                   (case expose
                     :sb/auth [{"bearerAuth" []}]
                     :sb/super [{"bearerAuth" ["super"]}]
                     []))]
      [:parameters parameters]
      [:requestBody request-body]
      [:responses responses]])))

(defn generate-openapi
  "Generates complete OpenAPI 3.0 spec."
  ([root-ns fn-filter]
   (generate-openapi root-ns fn-filter {}))
  ([root-ns fn-filter {:keys [function-order schema-order
                              registration-order]}]
   (let [all-vals (vals @types/*type-registry*)
         fns (ordered-defs
              (filter (every-pred types/fn-def? fn-filter) all-vals)
              function-order
              (get registration-order :functions))
         schemas (ordered-defs
                  (filter #(or (types/table-def? %)
                               (types/enum-def? %))
                          all-vals)
                  schema-order
                  (get registration-order :schemas))
         path-entries (map (fn [f]
                             [(str "/rpc/" (types/normalize-key (:name f)))
                              (ordered-map [["post" (fn->openapi f)]])])
                           fns)
         schema-entries (map (fn [definition]
                               [(def-name definition)
                                (if (types/table-def? definition)
                                  (shape->openapi (shape/table->shape definition))
                                  (ordered-map
                                   [[:type "string"]
                                    [:enum (mapv name (:values definition))]]))])
                             schemas)]
     (ordered-value
      (ordered-map
       [[:openapi "3.0.3"]
        [:info (ordered-map [[:title (str root-ns " API")]
                             [:version "0.1.0"]])]
        [:paths (ordered-map path-entries)]
        [:components
         (ordered-map
          [[:schemas (ordered-map schema-entries)]])]
        [:security [{"bearerAuth" []}]]
        [:securityDefinitions
         (ordered-map
          [["bearerAuth"
            (ordered-map [[:type "http"]
                          [:scheme "bearer"]
                          [:bearerFormat "JWT"]])]])]])))))
