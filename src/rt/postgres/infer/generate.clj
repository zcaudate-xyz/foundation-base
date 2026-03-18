(ns rt.postgres.infer.generate
    "Unified schema generation for all output formats.
   
   CRITIQUE FIX #3: Single generation file with format dispatch.
   Uses types/+type-formats+ for all type mappings.
   Uses types/normalize-key for all key conversion."
    (:require [rt.postgres.infer.types :as types]
              [rt.postgres.infer.shape :as shape]
              [rt.postgres.infer.analyze :as analyze]
              [clojure.string :as str]
              [clojure.walk :as walk]))

(declare shape->openapi shape->jschema shape->ts-interface)

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

(defn- infer-jsonb-arg-shape
       "Infers shape for a :jsonb argument when used with table operations.
   Returns shape->openapi result or nil if can't infer."
       [arg-name fn-def]
       (when-let [body (get-in fn-def [:body-meta :raw-body])]
                 (when-let [table-sym (find-table-op-in-body body arg-name)]
      ;; Try multiple lookup strategies for the table
                           (let [table-def (or (types/get-type table-sym)
                                               (types/get-type (symbol (name table-sym)))
                          ;; Try with dash prefix
                                               (types/get-type (symbol (str "-/" (name table-sym))))
                          ;; Try lookup in registry by string name
                                               (first (filter #(= (name table-sym) (:name %))
                                                              (vals @types/*type-registry*))))]
                                (when (types/table-def? table-def)
                                      (shape->openapi (shape/table->shape table-def)))))))

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
             meta-cols (get-in fn-def [:body-meta :api/meta :columns])]
            [param-name
             (cond
       ;; :jsonb arg used with table insert - infer from body
              (and (= :jsonb arg-type)
                   (types/fn-def? fn-def))
              (let [base-shape (or (infer-jsonb-arg-shape (:name arg) fn-def)
                                   (resolve-type arg-type :openapi))]
                   (if (and (map? base-shape) (:properties base-shape) (seq meta-cols))
                       (let [snake-cols (set (map types/normalize-key meta-cols))]
                            (update base-shape :properties (fn [props] (select-keys props snake-cols))))
                       base-shape))

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
                        (and (types/jsonb-shape? inferred) (:source-table inferred))
                        (name (:source-table inferred))

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

                          ;; Vector of primitives like [:uuid]
                             (and (vector? output) (seq output)
                                  (get-in types/+type-formats+ [(first output) :openapi]))
                             (get-in types/+type-formats+ [(first output) :openapi])

                          ;; Default fallback
                             :else {:type "object"})]
           {:operationId (types/normalize-key fn-name)
            :tags [(or (:ns fn-def) "default")]
            :summary (get-in fn-def [:body-meta :docstring])
            :security (when (and expose (not= :sb/query expose))
                            (case expose
                                  :sb/auth [{"bearerAuth" []}]
                                  :sb/super [{"bearerAuth" ["super"]}]
                                  []))
            :requestBody request-body
            :responses {"200" {:description "Successful response" :content {"application/json" {:schema response-schema}}}
                        "400" {:description "Bad request"}
                        "401" {:description "Unauthorized"}
                        "500" {:description "Internal server error"}}}))

(defn- unique-defs
       "Filters registry values to unique definitions by ns/name."
       [defs]
       (->> defs
            (group-by (fn [d] [(:ns d) (:name d)]))
            (map (fn [[_ v]] (first v)))))

(defn generate-openapi
      "Generates complete OpenAPI 3.0 spec."
      [root-ns fn-filter]
      (let [all (unique-defs (vals @types/*type-registry*))
            fns (filter #(and (types/fn-def? %) (fn-filter %)) all)
            tables (filter types/table-def? all)
            enums (filter types/enum-def? all)]
           {:openapi "3.0.3"
            :info {:title (str root-ns " API") :version "0.1.0"}
            :paths (into (sorted-map)
                         (map (fn [f] [(str "/rpc/" (types/normalize-key (:name f))) {"post" (fn->openapi f)}]))
                         fns)
            :components
            {:schemas (into (sorted-map)
                            (concat
                             (map (fn [t] [(:name t) (shape->openapi (shape/table->shape t))]) tables)
                             (map (fn [e] [(:name e) {:type "string" :enum (mapv name (:values e))}]) enums)))}
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
                                (map (fn [t] [(:name t) (shape->jschema (shape/table->shape t))]) tables)
                                (map (fn [e] [(:name e) {:type "string" :enum (mapv name (:values e))}]) enums)))}))

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
                                (str "export type " (:name e) " = "
                                     (str/join " | " (sort (map #(str "\"" (name %) "\"") (:values e)))) ";"))
                            enums)
            table-interfaces (map (fn [t] (shape->ts-interface (shape/table->shape t) (:name t))) tables)]
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
