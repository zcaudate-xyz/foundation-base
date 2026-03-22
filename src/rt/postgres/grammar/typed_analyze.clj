(ns rt.postgres.grammar.typed-analyze
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-shape :as shape]
            [std.json :as json])
  "AST analysis and type inference.\n   \n   CRITIQUE FIX #4: Uses shape/merge-shapes - no local duplication of merge logic.")

;; ─────────────────────────────────────────────────────────────────────────────
;; PG Operation Signatures
;; ─────────────────────────────────────────────────────────────────────────────

(def +pg-operations+
  "Maps pg DSL operations to their return-type descriptors"
  {;; Table CRUD
   'pg/t:insert {:op :insert :returns :table-instance}
   'pg/t:get {:op :get :returns :table-instance}
   'pg/t:get-field {:op :get-field :returns :field}
   'pg/t:select {:op :select :returns :array}
   'pg/t:update {:op :update :returns :table-instance}
   'pg/t:delete {:op :delete :returns :table-instance}
   'pg/t:upsert {:op :upsert :returns :table-instance}
   'pg/t:id {:op :id :returns :uuid}
   'pg/t:exists {:op :exists :returns :boolean}
   'pg/t:count {:op :count :returns :integer}

   ;; Graph operations
   'pg/g:insert {:op :insert :returns :table-instance :linked? true}
   'pg/g:get {:op :get :returns :table-instance :linked? true}
   'pg/g:select {:op :select :returns :array :linked? true}
   'pg/g:update {:op :update :returns :table-instance :linked? true}
   'pg/g:delete {:op :delete :returns :table-instance :linked? true}
   'pg/g:id {:op :id :returns :uuid}
   'pg/g:exists {:op :exists :returns :boolean}
   'pg/g:count {:op :count :returns :integer}})

;; ─────────────────────────────────────────────────────────────────────────────
;; Table Resolution
;; ─────────────────────────────────────────────────────────────────────────────

(defn- resolve-table [table-expr]
  (cond
    ;; Handle quoted symbols like '-/AccessRequest
    (and (seq? table-expr)
         (= 'quote (first table-expr)))
    (resolve-table (second table-expr))

    (symbol? table-expr)
    (let [table-name (name table-expr)
          table-def (or (types/get-type table-expr)
                        (types/get-type (symbol table-name))
                        (types/get-type (keyword table-name))
                        ;; Search registry for any table with matching name
                        (first (filter (fn [t]
                                         (and (types/table-def? t)
                                              (= table-name (name (:name t)))))
                                       (vals @types/*type-registry*))))]
      (when (types/table-def? table-def) table-def))

    (keyword? table-expr)
    (let [table-def (or (types/get-type (symbol (name table-expr)))
                        (first (filter (fn [t]
                                         (and (types/table-def? t)
                                              (= (name table-expr) (name (:name t)))))
                                       (vals @types/*type-registry*))))]
      (when (types/table-def? table-def) table-def))

    :else nil))

;; ─────────────────────────────────────────────────────────────────────────────
;; Expression Analysis
;; CRITIQUE FIX #4: Delegates to shape/merge-shapes - no local merge logic
;; ─────────────────────────────────────────────────────────────────────────────

(declare analyze-expr cached-infer)

(defn- merge-analyzed-shapes
  [analyzed]
  (let [shapes (keep (fn [v]
                       (let [shape (or (:shape v) v)]
                         (when (types/jsonb-shape? shape) shape)))
                     analyzed)]
    (when (seq shapes)
      (reduce types/merge-shapes (types/empty-jsonb-shape) shapes))))

(defn- analyze-table-op [op-sym args ctx]
  (let [op-info (get +pg-operations+ op-sym)
        table-expr (first args)
        table-def (resolve-table table-expr)]
    (if (and op-info table-def)
      (let [shape (shape/shape-for-table-op (:op op-info) table-def {})]
        (case (:returns op-info)
          :table-instance {:kind :shaped :shape shape :table (:name table-def)}
          :array (if (types/jsonb-array? shape)
                   {:kind :array :element-type (:element-type shape) :table (:name table-def)}
                   {:kind :array :element-type shape :table (:name table-def)})
          :uuid {:kind :primitive :type :uuid}
          :boolean {:kind :primitive :type :boolean}
          :integer {:kind :primitive :type :integer}
          {:kind :unknown}))
      {:kind :unknown :op op-sym})))

(defn- analyze-jsonb-merge
  "CRITIQUE FIX #4: Uses shape/merge-shapes - no local merge logic."
  [args ctx]
  (let [left (analyze-expr (first args) ctx)
        right (analyze-expr (second args) ctx)
        left-shape (or (:shape left) left)
        right-shape (or (:shape right) right)]
    {:kind :shaped
     ;; DELEGATES to shape/merge-shapes - single source of truth
     :shape (types/merge-shapes
             (if (types/jsonb-shape? left-shape) left-shape (types/empty-jsonb-shape))
             (if (types/jsonb-shape? right-shape) right-shape (types/empty-jsonb-shape)))
     :op :merge}))

(defn- analyze-jsonb-access
  "Analyzes :-> (jsonb) and :->> (text) operators."
  [accessor args ctx]
  (let [source (analyze-expr (first args) ctx)
        field-name (second args)]
    {:kind :field-access
     :field field-name
     :type (if (= accessor :->) :jsonb :text)}))

(defn- analyze-let [bindings body ctx]
  (let [new-ctx (reduce (fn [c [bind-name bind-val]]
                          (if (symbol? bind-name)
                            (let [val-type (analyze-expr bind-val c)]
                              (if (= :shaped (:kind val-type))
                                (types/add-binding c bind-name :jsonb :shape (:shape val-type))
                                (types/add-binding c bind-name val-type)))
                            c))
                        ctx (partition 2 bindings))]
    (analyze-expr (last body) new-ctx)))

(defn- analyze-control-flow [form ctx]
  (let [branches (case (first form)
                   (if cond) (rest form)
                   (when) [(nth form 1 nil) nil]
                   [])]
    (let [branch-types (map #(analyze-expr % ctx) (remove nil? branches))]
      (if (apply = (map :kind branch-types))
        (first branch-types)
        {:kind :union :types branch-types}))))

(defn analyze-expr
  "Analyzes an expression and returns type information.
   CRITIQUE FIX #4: For JSONB merge (||), delegates to shape/merge-shapes."
  [expr ctx]
  (cond
    ;; Literals
    (nil? expr) {:kind :literal :type :nil}
    (boolean? expr) {:kind :literal :type :boolean :value expr}
    (string? expr) {:kind :literal :type :text :value expr}
    (number? expr) {:kind :literal :type (if (integer? expr) :integer :numeric) :value expr}
    (keyword? expr) {:kind :literal :type :keyword :value expr}

    ;; Map literal
    (map? expr)
    {:kind :shaped
     :shape (types/make-jsonb-shape
             (into {} (map (fn [[k v]] [(keyword (name k)) (analyze-expr v ctx)]) expr))
             nil :high false)}

    ;; Set literal used as merged JSONB/object return
    (set? expr)
    (if-let [merged (merge-analyzed-shapes (map #(analyze-expr % ctx) expr))]
      {:kind :shaped
       :shape merged
       :op :set-merge}
      {:kind :unknown :expr expr})

    ;; Symbol lookup
    (symbol? expr)
    (or (types/get-var-shape ctx expr)
        (when-let [binding (types/lookup-binding ctx expr)]
          binding)
        {:kind :symbol :name expr})

    ;; List/Call
    (seq? expr)
    (let [op (first expr)
          op-name (name op)
          args (rest expr)]
      (cond
        ;; Table operations
        (contains? +pg-operations+ op)
        (analyze-table-op op args ctx)

        ;; JSONB merge - CRITIQUE FIX #4: delegates to shape/merge-shapes
        (= "||" op-name)
        (analyze-jsonb-merge args ctx)

        ;; JSONB access
        (= ":->" op-name)
        (analyze-jsonb-access :-> args ctx)

        (= ":->>" op-name)
        (analyze-jsonb-access :->> args ctx)

        ;; Return
        (= "return" op-name)
        (analyze-expr (first args) ctx)

        ;; Let binding
        (= "let" op-name)
        (analyze-let (first args) (rest args) ctx)

        ;; Control flow
        (#{'if 'when 'cond} op)
        (analyze-control-flow expr ctx)

        ;; Threading macros ->> and ->
        (#{'->> '->} op)
        (let [[first-arg & rest-forms] args
              expanded (reduce (fn [acc form]
                                 (if (seq? form)
                                   (if (= '->> op)
                                     ;; ->> threads as last argument
                                     (concat form [acc])
                                     ;; -> threads as first argument
                                     (list* (first form) acc (rest form)))
                                   (list form acc)))
                               first-arg
                               rest-forms)]
          (analyze-expr expanded ctx))

        ;; Type cast
        (keyword? op)
        {:kind :cast :type op :expr (analyze-expr (first args) ctx)}

        ;; Built-in SQL functions
        (= "coalesce" op-name)
        (analyze-expr (first args) ctx)

        ;; Function call tracing
        (symbol? op)
        (let [;; Resolve alias to full namespace if possible
              aliases (:aliases ctx {})
              op-ns (namespace op)
              resolved-op (if op-ns
                            (let [alias-sym (symbol op-ns)]
                              (if-let [full-ns (get aliases alias-sym)]
                                (symbol (str full-ns) op-name)
                                op))
                            op)
              fn-def (or (types/get-type resolved-op)
                         (types/get-type op)
                         (types/get-type (symbol op-name))
                         (first (filter (fn [f] (and (types/fn-def? f) (= op-name (:name f))))
                                        (vals @types/*type-registry*))))]
          (if fn-def
            (cached-infer fn-def)
            {:kind :unknown :op resolved-op}))

        ;; Unknown
        :else {:kind :unknown :op op}))

    :else {:kind :unknown :expr expr}))

;; ─────────────────────────────────────────────────────────────────────────────
;; Function Return Type Inference
;; ─────────────────────────────────────────────────────────────────────────────

(defn infer-return-type
  "Infers the return type of a function definition."
  [fn-def]
  (let [body (get-in fn-def [:body-meta :raw-body])
        aliases (get-in fn-def [:body-meta :aliases] {})
        ctx (types/make-context (into {} (map (fn [arg] [(:name arg) (:type arg)])) (:inputs fn-def)))
        ctx (assoc ctx :aliases aliases)]
    (when (seq body)
      (analyze-expr (last body) ctx))))

;; ─────────────────────────────────────────────────────────────────────────────
;; Cache Management
;; ─────────────────────────────────────────────────────────────────────────────

(def ^:dynamic *infer-cache* (atom {}))
(def ^:dynamic *visiting* #{})

(defn reset-cache! []
  (reset! *infer-cache* {}))

(defn cached-infer [fn-def]
  (let [key (symbol (or (:ns fn-def) "") (:name fn-def))]
    (cond
      (get @*infer-cache* key)
      (get @*infer-cache* key)

      (contains? *visiting* key)
      {:kind :unknown :recursion key}

      :else
      (binding [*visiting* (conj *visiting* key)]
        (let [result (infer-return-type fn-def)]
          (swap! *infer-cache* assoc key result)
          result)))))

;; ─────────────────────────────────────────────────────────────────────────────
;; JSON-Friendly Reporting
;; ─────────────────────────────────────────────────────────────────────────────

(defn- normalize-table-name
  [table-expr]
  (cond
    (and (seq? table-expr)
         (= 'quote (first table-expr)))
    (normalize-table-name (second table-expr))

    (symbol? table-expr)
    (name table-expr)

    (keyword? table-expr)
    (name table-expr)

    :else nil))

(defn detect-operations
  "Returns a JSON-friendly vector of postgres operations detected in a FnDef body."
  [fn-def]
  (let [body (get-in fn-def [:body-meta :raw-body])
        found (volatile! [])]
    (walk/postwalk
     (fn [form]
       (when (seq? form)
         (let [op-sym (first form)]
           (when-let [{:keys [op returns linked?]} (get +pg-operations+ op-sym)]
             (let [table-name (normalize-table-name (second form))]
               (vswap! found conj
                       (cond-> {:symbol (str op-sym)
                                :op op
                                :returns returns}
                         linked? (assoc :linked true)
                         table-name (assoc :table table-name)))))))
       form)
     body)
    (->> @found
         distinct
         vec)))

(defn json-safe
  "Converts infer output to JSON-safe plain data."
  [x]
  (cond
    (nil? x)
    nil

    (symbol? x)
    (str x)

    (keyword? x)
    (name x)

    (set? x)
    (->> x
         (map json-safe)
         (sort-by pr-str)
         vec)

    (map? x)
    (into (sorted-map)
          (map (fn [[k v]]
                 [(cond
                    (symbol? k) (str k)
                    :else k)
                  (json-safe v)]))
          x)

    (sequential? x)
    (mapv json-safe x)

    :else
    x))

(defn- inferred->report
  [inferred]
  (cond
    (nil? inferred)
    nil

    (types/jsonb-shape? inferred)
    {:kind "shaped"
     :shape (json-safe inferred)}

    (types/jsonb-array? inferred)
    {:kind "array"
     :element-type (json-safe (:element-type inferred))}

    (types/type-union? inferred)
    {:kind "union"
     :types (json-safe (:types inferred))}

    :else
    (cond-> {:kind (json-safe (:kind inferred))}
      (:shape inferred) (assoc :shape (json-safe (:shape inferred)))
      (:table inferred) (assoc :table (json-safe (:table inferred)))
      (:type inferred) (assoc :type (json-safe (:type inferred)))
      (:field inferred) (assoc :field (json-safe (:field inferred)))
      (:element-type inferred) (assoc :element-type (json-safe (:element-type inferred)))
      (:types inferred) (assoc :types (json-safe (:types inferred)))
      (:op inferred) (assoc :op (json-safe (:op inferred)))
      (:expr inferred) (assoc :expr (json-safe (:expr inferred)))
      (:recursion inferred) (assoc :recursion (json-safe (:recursion inferred))))))

(defn infer-report
  "Returns a JSON-friendly analysis report for a parsed FnDef."
  [fn-def]
  (let [inferred   (cached-infer fn-def)
        operations (detect-operations fn-def)
        source-tables (->> (concat
                            (keep :table operations)
                            [(or (:table inferred)
                                 (some-> inferred :shape :source-table name))])
                           (remove nil?)
                           distinct
                           vec)
        mutating?  (boolean (some (comp #{:insert :update :delete :upsert} :op)
                                  operations))]
    {:function {:ns (:ns fn-def)
                :name (:name fn-def)
                :dbschema (:dbschema fn-def)}
     :declared {:inputs (json-safe (:inputs fn-def))
                :output (json-safe (:output fn-def))
                :docstring (get-in fn-def [:body-meta :docstring])
                :expose (json-safe (get-in fn-def [:body-meta :expose]))}
     :analysis {:mutating mutating?
                :source-tables source-tables
                :operations-detected (json-safe operations)
                :return (inferred->report inferred)}}))

(defn report-json
  "Serializes an infer report to JSON."
  ([report]
   (json/write report))
  ([report pretty?]
   (if pretty?
     (json/write-pp report)
     (json/write report))))
