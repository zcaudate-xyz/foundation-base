(ns postgres.typed
  (:refer-clojure :exclude [load-file])
  (:require [postgres.typed.export.json-openapi :as compile.json-openapi]
            [postgres.typed.export.json-schema :as compile.json-schema]
            [postgres.typed.export.ts-schema :as compile.ts-schema]
            [hara.runtime.postgres.base.application :as app]
            [postgres.typed.typed-analyze :as analyze]
            [postgres.typed.typed-common :as types]
            [postgres.typed.typed-infer :as typed-infer]
            [postgres.typed.typed-resolve :as typed-resolve]
            [postgres.typed.typed-parse :as parse]))
(declare enrich-function-arg-roles input-shape output-shape)
;; ─────────────────────────────────────────────────────────────────────────────
;; Shape Formatting Helpers
;; ─────────────────────────────────────────────────────────────────────────────


(defn inferred->shape
  [inferred]
  (cond
    (types/jsonb-shape? inferred)
    inferred

    (and (= :shaped (:kind inferred))
         (types/jsonb-shape? (:shape inferred)))
    (:shape inferred)

    :else nil))

(defn format-shape
  [shape format]
  (case format
    :shape shape
    :openapi (compile.json-openapi/shape->openapi shape)
    :json-schema (compile.json-schema/shape->json-schema shape)
    :typescript (compile.ts-schema/shape->ts-interface shape)
    (throw (ex-info "Unknown schema format"
                    {:format format
                     :available [:shape :openapi :json-schema :typescript]}))))

(defn report-json
  "Serializes an infer report to JSON."
  ([report]
   (analyze/report-json report))
  ([report pretty?]
   (analyze/report-json report pretty?)))

;; Contract Helpers

;; ─────────────────────────────────────────────────────────────────────────────


(defn- arg-type
  [arg]
  (let [t (:type arg)]
    (cond
      (keyword? t) t
      (and (map? t) (keyword? (:name t))) (:name t)
      :else nil)))

(defn- arg-type-name
  [arg]
  (when-let [t (arg-type arg)]
    (name t)))

(defn- track-arg-role?
  [fn-def arg-name]
  (let [body (get-in fn-def [:body-meta :raw-body])
        aliases (get-in fn-def [:body-meta :aliases] {})
        fn-key (symbol (or (:ns fn-def) "") (:name fn-def))]
    (letfn [(scan-form [form tracked visited]
              (cond
                (seq? form)
                (let [op (first form)
                      args (vec (rest form))
                      params (nth form 2 nil)]
                  (cond
                    (and (map? params)
                         (typed-infer/form-uses-track-param? params tracked))
                    true

                    (and (= 'let op)
                         (sequential? (second form)))
                    (loop [bindings (partition 2 (second form))
                           tracked tracked]
                      (if-let [[binding expr] (first bindings)]
                        (or (scan-form expr tracked visited)
                            (recur (next bindings)
                                   (cond-> tracked
                                     (and (symbol? binding)
                                          (typed-infer/form-uses-tracked? expr tracked))
                                     (conj binding))))
                        (scan-forms (drop 2 form) tracked visited)))

                    (symbol? op)
                    (let [arg-pos (first (keep-indexed (fn [idx itm]
                                                         (when (contains? tracked itm) idx))
                                                       args))
                          [_ called-fn] (analyze/resolve-called-fn op aliases)
                          called-key (when called-fn
                                       (symbol (or (:ns called-fn) "") (:name called-fn)))]
                      (or (when (and (some? arg-pos)
                                     called-fn
                                     (not (contains? visited called-key)))
                            (when-let [target-arg (nth (:inputs called-fn) arg-pos nil)]
                              (when (= :jsonb (:type target-arg))
                                (scan-form (get-in called-fn [:body-meta :raw-body])
                                           #{(:name target-arg)}
                                           (conj visited called-key)))))
                          (scan-forms form tracked visited)))

                    :else
                    (scan-forms form tracked visited)))

                (coll? form)
                (scan-forms form tracked visited)

                :else
                false))
            (scan-forms [forms tracked visited]
              (some #(scan-form % tracked visited) forms))]
      (boolean (and body (scan-form body #{arg-name} #{fn-key}))))))

(defn- enrich-function-arg-roles
  [fn-def]
  (if (types/fn-def? fn-def)
    (update fn-def :inputs
            (fn [inputs]
              (mapv (fn [arg]
                      (if (and (= :jsonb (arg-type arg))
                               (track-arg-role? fn-def (:name arg)))
                        (assoc arg :role :track)
                        (assoc arg :role (or (:role arg) :payload))))
                    inputs)))
    fn-def))

(defn- jsonb-arg?
  [arg]
  (or (= :jsonb (arg-type arg))
      (some #{:jsonb} (:modifiers arg))))

(defn- resolve-type
  [t target]
  (let [base-type (cond
                    (keyword? t) t
                    (types/type-ref? t) (if (= :primitive (:kind t)) (:name t) (:kind t))
                    (map? t) (let [inner-type (:type t)]
                               (cond
                                 (keyword? inner-type) inner-type
                                 (types/type-ref? inner-type) (if (= :primitive (:kind inner-type))
                                                                 (:name inner-type)
                                                                 (:kind inner-type))
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
          (case target
            :openapi {:type "string"}
            :jschema {:type "string"}
            :ts "string")))))

(defn- format-primitive
  [format t]
  (case format
    :shape {:type t}
    :openapi (resolve-type t :openapi)
    :json-schema (resolve-type t :jschema)
    :typescript (resolve-type t :ts)))

(defn- build-input-schemas
  [ctx fn-def format]
  (into {}
        (keep (fn [arg]
                (let [arg-name (:name arg)
                      t (arg-type arg)]
                  (cond
                    (= :track (:role arg))
                    [(keyword arg-name)
                     {:type :jsonb
                      :role :track
                      :schema (format-primitive format :jsonb)}]

                    (jsonb-arg? arg)
                    (when-let [shape (input-shape ctx fn-def arg-name)]
                      [(keyword arg-name)
                       {:shape shape
                        :schema (format-shape shape format)}])

                    t
                    [(keyword arg-name)
                     {:type t
                      :schema (format-primitive format t)}])))
              (:inputs fn-def))))

(defn- build-output-schema
  [ctx fn-def format]
  (when-let [output-shape (output-shape ctx fn-def)]
    {:shape output-shape
     :schema (format-shape output-shape format)}))


;; ─────────────────────────────────────────────────────────────────────────────
;; Context API
;; ─────────────────────────────────────────────────────────────────────────────

(defn registry->typed
  "Builds a typed payload from a flat registry map."
  [registry]
  (reduce types/add-typed
          (types/empty-typed)
          (vals registry)))

(defn typed->registry
  "Flattens an app typed payload into the registry shape expected by inference."
  [typed]
  (merge (:tables typed)
         (:enums typed)
         (:functions typed)))

(defn load-analysis
  "Creates a postgres typed context from parsed analysis."
  [analysis]
  (let [typed (types/analysis->typed analysis)]
    {:domain :postgres
     :analysis analysis
     :typed typed
     :registry (typed->registry typed)}))

(defn load-file
  "Creates a postgres typed context from a source file."
  [file-path]
  (load-analysis (parse/analyze-file file-path)))

(defn load-ns
  "Creates a postgres typed context from a namespace."
  [ns-sym]
  (load-analysis (parse/analyze-namespace ns-sym)))

(defn load-app
  "Creates a postgres typed context from an app typed payload."
  [app-name]
  (let [typed (app/app-typed app-name)]
    {:domain :postgres
     :app-name app-name
     :typed typed
     :registry (typed->registry typed)}))

(defn load-registry
  "Creates a postgres typed context from a registry map, defaulting to the current registry."
  ([]
   (load-registry @types/*type-registry*))
  ([registry]
   {:domain :postgres
    :typed (registry->typed registry)
    :registry registry}))

(defn with-context-registry
  "Runs `f` with the context registry visible to existing inference internals."
  [ctx f]
  (let [current @types/*type-registry*]
    (try
      (reset! types/*type-registry* (:registry ctx))
      (f)
      (finally
        (reset! types/*type-registry* current)))))

(defn entries
  "Returns all typed declarations in a postgres context."
  [ctx]
  (vals (:registry ctx)))

(defn entry
  "Returns a typed declaration from a postgres context."
  [ctx sym]
  (let [sym (typed-resolve/canonical-fn-sym sym)]
    (or (get-in ctx [:registry sym])
        (get-in ctx [:registry (symbol (name sym))])
        (get-in ctx [:registry (keyword (name sym))]))))

(defn missing-function!
  [fn-ref]
  (throw (ex-info "Typed function not found"
                  {:type :typed/missing-function
                   :fn fn-ref})))

(defn missing-argument!
  [fn-ref arg-sym]
  (throw (ex-info "Typed function argument not found"
                  {:type :typed/missing-argument
                   :fn fn-ref
                   :arg arg-sym})))

(defn function-def
  "Resolves and enriches a function definition from a postgres context."
  [ctx fn-ref]
  (or (with-context-registry
        ctx
        #(some-> (or (when (types/fn-def? fn-ref)
                       fn-ref)
                     (typed-resolve/resolve-function-def fn-ref)
                     (when-let [fn-sym (typed-resolve/fn-ref->fn-sym fn-ref)]
                       (entry ctx fn-sym)))
                 enrich-function-arg-roles))
      (missing-function! fn-ref)))

(defn function-report
  "Returns the postgres inference report for a function in a context."
  [ctx fn-ref]
  (with-context-registry
    ctx
    #(analyze/infer-report (function-def ctx fn-ref))))

(defn function-input
  "Returns all inputs, or a single input by argument symbol."
  ([ctx fn-ref]
   (:inputs (function-def ctx fn-ref)))
  ([ctx fn-ref arg-sym]
   (or (some (fn [arg]
               (when (= arg-sym (:name arg))
                 arg))
             (:inputs (function-def ctx fn-ref)))
       (missing-argument! fn-ref arg-sym))))

(defn function-output
  "Returns the declared postgres function output."
  [ctx fn-ref]
  (:output (function-def ctx fn-ref)))

(defn input-shape
  "Infers a JsonbShape for a postgres function input. Returns nil when not inferable."
  [ctx fn-ref arg-sym]
  (let [fn-def (function-def ctx fn-ref)
        arg (function-input ctx fn-ref arg-sym)]
    (with-context-registry
      ctx
      #(typed-infer/infer-jsonb-arg-shape arg-sym fn-def (:role arg)))))

(defn input-schema
  "Formats an inferred postgres JSONB input shape."
  ([ctx fn-ref arg-sym]
   (input-schema ctx fn-ref arg-sym :shape))
  ([ctx fn-ref arg-sym format]
   (some-> (input-shape ctx fn-ref arg-sym)
           (format-shape format))))

(defn output-shape
  "Infers a JsonbShape for a postgres function output. Returns nil when not inferable."
  [ctx fn-ref]
  (let [fn-def (function-def ctx fn-ref)]
    (with-context-registry
      ctx
      #(some-> fn-def analyze/cached-infer inferred->shape))))

(defn output-schema
  "Formats an inferred postgres output shape."
  ([ctx fn-ref]
   (output-schema ctx fn-ref :shape))
  ([ctx fn-ref format]
   (some-> (output-shape ctx fn-ref)
           (format-shape format))))

(defn function-contract
  "Returns the function report plus formatted input and output schemas."
  ([ctx fn-ref]
   (function-contract ctx fn-ref {:format :shape}))
  ([ctx fn-ref opts]
   (let [format (get opts :format :shape)
         fn-def (function-def ctx fn-ref)]
     (with-context-registry
       ctx
       #(let [report (analyze/infer-report fn-def)
              input-schemas (build-input-schemas ctx fn-def format)
              output-schema (build-output-schema ctx fn-def format)]
          (-> report
              (assoc :input {:args (mapv (fn [arg]
                                           {:name (:name arg)
                                            :type (arg-type-name arg)
                                            :modifiers (:modifiers arg)
                                            :role (:role arg)})
                                         (:inputs fn-def))
                             :schemas input-schemas})
              (assoc :output output-schema)))))))

(defn export-openapi
  "Generates OpenAPI from a postgres typed context."
  ([ctx]
   (export-openapi ctx (constantly true)))
  ([ctx fn-filter]
   (with-context-registry
     ctx
     #(compile.json-openapi/generate-openapi (or (some-> ctx :analysis :ns)
                                           (:app-name ctx)
                                           "postgres")
                                       fn-filter))))

(defn export-json-schema
  "Generates JSON Schema from a postgres typed context."
  [ctx]
  (with-context-registry
    ctx
    #(compile.json-schema/generate-json-schema)))

(defn export-typescript
  "Generates TypeScript definitions from a postgres typed context."
  [ctx]
  (with-context-registry
    ctx
    #(compile.ts-schema/generate-ts-schema)))
