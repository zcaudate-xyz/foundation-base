(ns rt.postgres.compile.common
    "Unified schema compilation for JSON/OpenAPI/TypeScript targets.
   
   CRITIQUE FIX #3: Single generation file with format dispatch.
   Uses types/+type-formats+ for all type mappings.
   Uses types/normalize-key for all key conversion."
    (:require [rt.postgres.grammar.typed-common :as types]
              [rt.postgres.grammar.typed-shape :as shape]
              [rt.postgres.grammar.typed-analyze :as analyze]
              [clojure.string :as str]
              [clojure.walk :as walk]))

(declare shape->openapi shape->jschema shape->ts-interface resolve-table-def infer-jsonb-arg-shape*)

(defn- def-name
  [x]
  (some-> x :name name))

;; ─────────────────────────────────────────────────────────────────────────────
;; Type Resolution
;; Uses centralized +type-formats+ registry
;; ─────────────────────────────────────────────────────────────────────────────

(defn- find-table-op-in-body
       "Searches body for pg/t:insert or pg/g:insert calls with arg-name.
   Also traces through function calls and let bindings.
   Returns the table symbol if found, nil otherwise."
       [body arg-name]
       (let [found (atom nil)]
            (walk/postwalk
             (fn [form]
                 (when (seq? form)
                       (let [op (first form)]
                            (cond
             ;; Direct table insert: (pg/t:insert Table arg ...)
                             (and (#{'pg/t:insert 'pg/g:insert 'pg/t:update 'pg/g:update} op)
                                  (= arg-name (first (rest (rest form)))))
                             (reset! found (second form))

             ;; Let binding: (let [v-arg arg-name] ...)
             ;; Trace arg-name to v-arg and search in let body
                             (and (= 'let op)
                                  (sequential? (second form)))
                             (let [bindings (partition 2 (second form))
                                   new-vars (keep (fn [[v k]] (when (= k arg-name) v)) bindings)]
                                  (doseq [v new-vars]
                                         (when-let [table-sym (find-table-op-in-body (drop 2 form) v)]
                                                   (reset! found table-sym))))

             ;; Function call with arg: (-/other-fn ... arg-name ...)
                             (and (symbol? op)
                                  (some #(= arg-name %) (rest form)))
                             (let [arg-pos (first (keep-indexed (fn [idx itm] (when (= arg-name itm) idx)) (rest form)))
                                   op-name (name op)
                                   fn-def (or (types/get-type op)
                                              (types/get-type (symbol op-name))
                                              (first (filter (fn [f] (and (types/fn-def? f) (= op-name (:name f))))
                                                             (vals @types/*type-registry*))))]
                                  (when (types/fn-def? fn-def)
                                        (when-let [target-arg (nth (:inputs fn-def) arg-pos nil)]
                                                  (when (= :jsonb (:type target-arg))
                                                        (when-let [table-sym (find-table-op-in-body
                                                                              (get-in fn-def [:body-meta :raw-body])
                                                                              (:name target-arg))]
                                                                  (reset! found table-sym)))))))))
                 form)
             body)
            @found))

(defn- apply-columns-filter
       [base-shape cols]
       (if (and (map? base-shape) (:properties base-shape) (seq cols))
         (let [snake-cols (set (map types/normalize-key cols))]
           (update base-shape :properties (fn [props] (select-keys props snake-cols))))
         base-shape))

(defn- resolve-called-fn
       [op aliases]
       (let [op-name (name op)
             op-str (str op)
             resolved-op (if (str/includes? op-str "/")
                           (let [[alias-part fn-part] (str/split op-str #"/")
                                 alias-sym (symbol alias-part)]
                             (if-let [full-ns (get aliases alias-sym)]
                               (symbol (str full-ns "/" fn-part))
                               op))
                           op)]
         (or (types/get-type resolved-op)
             (types/get-type op)
             (types/get-type (symbol op-name))
             (first (filter (fn [f]
                              (and (types/fn-def? f)
                                   (= op-name (:name f))))
                            (vals @types/*type-registry*))))))

(defn- infer-jsonb-arg-shape
       "Infers shape for a :jsonb argument when used with table operations.
   Returns shape->openapi result or nil if can't infer."
       [arg-name fn-def]
       (infer-jsonb-arg-shape* arg-name fn-def #{}))

(defn- infer-jsonb-arg-shape*
       [arg-name fn-def visited]
       (let [fn-key (symbol (or (:ns fn-def) "") (:name fn-def))
             meta-table (get-in fn-def [:body-meta :api/meta :table])
             meta-cols (get-in fn-def [:body-meta :api/meta :columns])
             aliases (get-in fn-def [:body-meta :aliases] {})
             body (get-in fn-def [:body-meta :raw-body])]
         (or
          (when-let [table-def (resolve-table-def meta-table)]
            (apply-columns-filter
             (shape->openapi (shape/table->shape table-def))
             meta-cols))

          (when-let [table-sym (and body (find-table-op-in-body body arg-name))]
            (let [table-def (or (types/get-type table-sym)
                                (types/get-type (symbol (name table-sym)))
                                (types/get-type (symbol (str "-/" (name table-sym))))
                                (first (filter #(= (name table-sym) (:name %))
                                               (vals @types/*type-registry*))))]
              (when (types/table-def? table-def)
                (shape->openapi (shape/table->shape table-def)))))

          (when (and body (not (contains? visited fn-key)))
            (let [forms (tree-seq coll? seq body)]
              (some
               (fn [form]
                 (when (seq? form)
                   (let [op (first form)
                         args (vec (rest form))
                         arg-pos (first (keep-indexed (fn [idx itm]
                                                        (when (= arg-name itm) idx))
                                                      args))]
                     (when arg-pos
                       (when-let [called-fn (resolve-called-fn op aliases)]
                         (when-let [target-arg (nth (:inputs called-fn) arg-pos nil)]
                           (when (= :jsonb (:type target-arg))
                             (infer-jsonb-arg-shape* (:name target-arg)
                                                     called-fn
                                                     (conj visited fn-key)))))))))
               forms))))))

(defn- resolve-table-def
       "Resolves a table symbol from the type registry."
       [table-sym]
       (when table-sym
             (or (types/get-type table-sym)
                 (types/get-type (symbol (name table-sym)))
                 (types/get-type (keyword (name table-sym)))
                 (first (filter (fn [t]
                                    (and (types/table-def? t)
                                         (= (name table-sym) (:name t))))
                                (vals @types/*type-registry*))))))

(defn- resolve-type
       "Resolves any type to the target format using +type-formats+."
       [t target]
       (let [base-type (cond
                        (keyword? t) t
                        (types/type-ref? t) (if (= :primitive (:kind t)) (:name t) (:kind t))
                        (map? t) (let [inner-type (:type t)]
                                      (cond
                                       (keyword? inner-type) inner-type
                                       (types/type-ref? inner-type) (if (= :primitive (:kind inner-type)) (:name inner-type) (:kind inner-type))
                                       :else (or (:kind t) :unknown)))
                        :else :unknown)]
            (or (get-in types/+type-formats+ [base-type target])
                (case base-type
                      :enum (if (and (map? t) (:enum-ref t))
                                (case target
                                      :openapi {:$ref (str "#/components/schemas/" (name (get-in t [:enum-ref :ns])))}
                                      :jschema {:$ref (str "#/definitions/" (name (get-in t [:enum-ref :ns])))}
                                      :ts (name (get-in t [:enum-ref :ns])))
                                (get-in types/+type-formats+ [:text target]))
          ;; Fallback
                      (case target
                            :openapi {:type "string"}
                            :jschema {:type "string"}
                            :ts "string")))))

;; ─────────────────────────────────────────────────────────────────────────────
;; OpenAPI Generation
;; ─────────────────────────────────────────────────────────────────────────────

(defn- field->openapi
       "Converts a field descriptor to OpenAPI schema."
       [field-info]
       (let [t (:type field-info)]
            (cond
      ;; Ref field -> UUID
             (:is-ref? field-info)
             {:type "string" :format "uuid"}

      ;; Nested shape
             (:shape field-info)
             (shape->openapi (:shape field-info))

      ;; Array with items
             (= :array t)
             {:type "array" :items (or (field->openapi (:items field-info)) {:type "object"})}

      ;; Standard type
             :else (resolve-type field-info :openapi))))

(defn shape->openapi
      "Converts a JsonbShape to OpenAPI schema object."
      [shape]
      (let [fields (:fields shape)
            properties (into (sorted-map)
                             (map (fn [[k v]] [(types/normalize-key k) (field->openapi v)]))
                             fields)
            required (mapv types/normalize-key
                           (filter #(not (:nullable? (get fields %))) (keys fields)))]
           (cond-> {:type "object" :properties properties}
                   (seq required) (assoc :required required))))

(defn- arg->openapi
       "Converts a function argument to OpenAPI parameter schema.
   For :jsonb types that map to table inputs, uses the table's shape."
       [arg fn-def]
       (let [param-name (types/normalize-key (str/replace (name (:name arg)) #"^i-" ""))
             arg-type (:type arg)
             meta-table (get-in fn-def [:body-meta :api/meta :table])
             meta-cols (get-in fn-def [:body-meta :api/meta :columns])]
            [param-name
             (cond
       ;; :jsonb arg used with table insert - infer from body
              (and (= :jsonb arg-type)
                   (types/fn-def? fn-def))
              (let [base-shape (or (when-let [table-def (resolve-table-def meta-table)]
                                     (shape->openapi (shape/table->shape table-def)))
                                   (infer-jsonb-arg-shape (:name arg) fn-def)
                                   (resolve-type arg-type :openapi))]
                   (apply-columns-filter base-shape meta-cols))

       ;; Standard type resolution
              :else (resolve-type arg-type :openapi))]))

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

        ;; Infer response schema
            inferred (analyze/cached-infer fn-def)
            output (:output fn-def)

        ;; Determine table name for $ref
            table-name (cond
                        meta-table (name meta-table)

                        ;; Inferred is {:kind :shaped, :shape #JsonbShape...}
                        ;; Extract shape and check if it's a JsonbShape
                        (= :shaped (:kind inferred))
                        (if-let [shape (:shape inferred)]
                          (when (types/jsonb-shape? shape)
                            (when-let [source (:source-table shape)]
                              (name source)))
                          nil)

                        ;; Inferred is a raw JsonbShape (from let bindings returning shapes)
                        (types/jsonb-shape? inferred)
                        (when-let [source (:source-table inferred)]
                          (name source))

                        (and (= :shaped (:kind inferred)) (:table inferred))
                        (name (:table inferred))

                        (symbol? output) (name output)
                        :else nil)

            response-schema (cond
                             table-name
                             {:$ref (str "#/components/schemas/" table-name)}

                             ;; Handle inferred array response
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

                             ;; Inline traced shape when it isn't tied to a single table
                             (= :shaped (:kind inferred))
                             (if-let [shape (:shape inferred)]
                               (when (types/jsonb-shape? shape)
                                 (shape->openapi shape))
                               {:type "object"})

                             (types/jsonb-shape? inferred)
                             (shape->openapi inferred)

                          ;; Vector of primitives like [:uuid]
                             (and (vector? output) (seq output)
                                  (get-in types/+type-formats+ [(first output) :openapi]))
                             (get-in types/+type-formats+ [(first output) :openapi])

                          ;; Default fallback
                             :else {:type "object"})
            
            ;; Supabase/PostgREST headers
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

(defn- unique-defs
       "Filters registry values to unique definitions by name."
       [defs]
       (->> defs
            (group-by def-name)
            (map (fn [[_ v]] (first v)))))

(defn generate-openapi
      "Generates complete OpenAPI 3.0 spec."
      [root-ns fn-filter]
      (let [all-vals (vals @types/*type-registry*)
            fns (->> all-vals
                     (filter types/fn-def?)
                     (filter fn-filter)
                     (unique-defs))
            tables (->> all-vals
                        (filter types/table-def?)
                        (unique-defs))
            enums (->> all-vals
                       (filter types/enum-def?)
                       (unique-defs))]
           {:openapi "3.0.3"
            :info {:title (str root-ns " API") :version "0.1.0"}
            :paths (into (sorted-map)
                         (map (fn [f] [(str "/rpc/" (types/normalize-key (:name f))) {"post" (fn->openapi f)}]))
                         fns)
            :components
            {:schemas (into (sorted-map)
                            (concat
                             (map (fn [t] [(def-name t) (shape->openapi (shape/table->shape t))]) tables)
                             (map (fn [e] [(def-name e) {:type "string" :enum (mapv name (:values e))}]) enums)))}
            :security [{"bearerAuth" []}]
            :securityDefinitions {"bearerAuth" {:type "http" :scheme "bearer" :bearerFormat "JWT"}}}))

;; ─────────────────────────────────────────────────────────────────────────────
;; JSON Schema Generation
;; ─────────────────────────────────────────────────────────────────────────────

(defn- field->jschema
       "Converts a field descriptor to JSON Schema."
       [field-info]
       (let [t (:type field-info)]
            (cond
             (:is-ref? field-info)
             {:type "string" :format "uuid"}

             (:shape field-info)
             (shape->jschema (:shape field-info))

             (= :array t)
             {:type "array" :items (or (field->jschema (:items field-info)) {:type "object"})}

             :else (resolve-type field-info :jschema))))

(defn shape->jschema
      "Converts a JsonbShape to JSON Schema."
      [shape]
      (let [fields (:fields shape)
            properties (into (sorted-map)
                             (map (fn [[k v]] [(types/normalize-key k) (field->jschema v)]))
                             fields)
            required (mapv types/normalize-key
                           (filter #(not (:nullable? (get fields %))) (keys fields)))]
           (cond-> {:type "object" :properties properties}
                   (seq required) (assoc :required required)
                   :always (assoc :additionalProperties false))))

(defn generate-jschema
      "Generates JSON Schema for tables and enums."
      []
      (let [all (unique-defs (vals @types/*type-registry*))
            tables (filter types/table-def? all)
            enums (filter types/enum-def? all)]
           {:$schema "http://json-schema.org/draft-07/schema#"
            :definitions (into (sorted-map)
                               (concat
                                (map (fn [t] [(def-name t) (shape->jschema (shape/table->shape t))]) tables)
                                (map (fn [e] [(def-name e) {:type "string" :enum (mapv name (:values e))}]) enums)))}))

;; ─────────────────────────────────────────────────────────────────────────────
;; TypeScript Generation
;; ─────────────────────────────────────────────────────────────────────────────

(defn- type->ts
       "Converts a type descriptor to TypeScript type string."
       [field-info]
       (let [t (:type field-info)]
            (cond
             (:is-ref? field-info)
             "string"

             (:shape field-info)
             (shape->ts-interface (:shape field-info) nil)

             (= :array t)
             (str (type->ts (:items field-info)) "[]")

             (:enum-ref field-info)
             (name (get-in field-info [:enum-ref :ns]))

             :else (resolve-type field-info :ts))))

(defn- field->ts
       "Converts a field to TypeScript property declaration."
       [[k v]]
       (let [ts-type (type->ts v)
             optional (if (:nullable? v) "?" "")]
            (str "  " (types/normalize-key k) optional ": " ts-type ";")))

(defn shape->ts-interface
      "Converts a JsonbShape to TypeScript interface or inline object."
      ([shape name]
       (let [fields (:fields shape)
             field-strs (map field->ts (sort-by key fields))]
            (if name
                (str "export interface " name " {\n"
                     (str/join "\n" field-strs)
                     "\n}")
                (str "{ " (str/join " " (map #(str/replace % #"^  " "") field-strs)) " }"))))
      ([shape]
       (shape->ts-interface shape nil)))

(defn generate-typescript
      "Generates TypeScript interfaces for all tables and enums."
      []
      (let [all (unique-defs (vals @types/*type-registry*))
            tables (filter types/table-def? all)
            enums (filter types/enum-def? all)
            enum-types (map (fn [e]
                                (str "export type " (def-name e) " = "
                                     (str/join " | " (sort (map #(str "\"" (name %) "\"") (:values e)))) ";"))
                            enums)
            table-interfaces (map (fn [t] (shape->ts-interface (shape/table->shape t) (def-name t))) tables)]
           (str/join "\n\n" (concat enum-types table-interfaces))))

;; ─────────────────────────────────────────────────────────────────────────────
;; Public API
;; ─────────────────────────────────────────────────────────────────────────────

(defn emit
      "Generates output in the specified format.
   Formats: :openapi, :jschema, :typescript"
      [format & args]
      (case format
            :openapi (apply generate-openapi args)
            :jschema (generate-jschema)
            :typescript (generate-typescript)
            (throw (ex-info "Unknown format" {:format format :available [:openapi :jschema :typescript]}))))
