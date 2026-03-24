(ns rt.postgres.typed
  (:require [clojure.string :as str]
            [rt.postgres.compile.common :as compile.common]
            [rt.postgres.compile.json-openapi :as compile.json-openapi]
            [rt.postgres.compile.json-schema :as compile.json-schema]
            [rt.postgres.compile.ts-schema :as compile.ts-schema]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.typed-analyze :as analyze]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-parse :as parse]
            [rt.postgres.grammar.typed-shape :as shape]))

(declare get-function-def with-app-typed-registry report-json resolve-function-def)

;; ─────────────────────────────────────────────────────────────────────────────
;; Type Registry API
;; ─────────────────────────────────────────────────────────────────────────────

(defn clear-registry!
  "Clears the global type registry."
  []
  (types/clear-registry!))

(defn register-type!
  "Registers a type in the global registry."
  [sym type-ref]
  (types/register-type! sym type-ref))

(defn get-type
  "Retrieves a type from the global registry."
  [sym]
  (types/get-type sym))

;; ─────────────────────────────────────────────────────────────────────────────
;; Source Analysis API
;; ─────────────────────────────────────────────────────────────────────────────

(defn analyze-file
  "Analyzes a Clojure source file for type definitions."
  [file-path]
  (parse/analyze-file file-path))

(defn analyze-namespace
  "Analyzes a namespace for type definitions."
  [ns-sym]
  (parse/analyze-namespace ns-sym))

(defn analyze-and-register!
  "Analyzes a namespace and registers all types."
  [ns-sym]
  (-> ns-sym
    parse/analyze-namespace
    parse/register-types!))

(defn make-function-report
  "Generates a JSON-friendly infer report for one function in a namespace."
  [ns-sym fn-sym]
  (analyze/reset-cache!)
  (let [analysis (-> ns-sym
                     parse/analyze-namespace
                     parse/register-types!)
        fn-name (name fn-sym)]
    (when-let [fn-def (some #(when (= fn-name (:name %)) %)
                            (:functions analysis))]
      (analyze/infer-report fn-def))))

(defn get-function-report
  "Retrieves a cached infer report for a registered function."
  [fn-ref]
  (let [fn-def (resolve-function-def fn-ref)]
    (when fn-def
      (analyze/infer-report fn-def))))

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

(defn get-app-function-report
  "Retrieves a cached infer report from an app typed payload."
  [app-name fn-sym]
  (with-app-typed-registry
    app-name
    (fn [typed-payload]
      (some-> (get-in typed-payload [:functions fn-sym])
              get-function-report))))

(defn get-function-report-json
  "Serializes a registered function infer report to JSON."
  ([fn-ref]
   (some-> (get-function-report fn-ref)
           report-json))
  ([fn-ref pretty?]
   (some-> (get-function-report fn-ref)
           (report-json pretty?))))

(defn get-app-function-report-json
  "Serializes an app function infer report to JSON."
  ([app-name fn-sym]
   (some-> (get-app-function-report app-name fn-sym)
           report-json))
  ([app-name fn-sym pretty?]
   (some-> (get-app-function-report app-name fn-sym)
           (report-json pretty?))))

(defn get-function-def
  "Retrieves a registered function definition by namespaced symbol."
  [fn-sym]
  (let [fn-def (types/get-type fn-sym)]
    (when (types/fn-def? fn-def)
      fn-def)))

(defn fn-ref->fn-sym
  "Best-effort conversion of a function reference into a namespaced symbol."
  [fn-ref]
  (letfn [(nsish->str [x]
            (cond
              (instance? clojure.lang.Namespace x) (str (ns-name x))
              (symbol? x) (str x)
              (string? x) x
              :else nil))
          (nameish->str [x]
            (cond
              (symbol? x) (name x)
              (keyword? x) (name x)
              (string? x) x
              :else nil))]
    (cond
      (symbol? fn-ref)
      (if (namespace fn-ref)
        fn-ref
        (symbol (str (ns-name *ns*)) (name fn-ref)))

      (var? fn-ref)
      (let [{:keys [ns name]} (meta fn-ref)]
        (when (and ns name)
          (symbol (str (ns-name ns)) (str name))))

      ;; std.lang's `defn.pg` functions are often referenced by value in user ns,
      ;; but those values are pointers (IDeref) rather than IFn/Var. Their deref
      ;; value (BookEntry-ish) carries `:module`/`:id` we can use to resolve.
      (instance? clojure.lang.IDeref fn-ref)
      (let [d (try (deref fn-ref)
                   (catch Throwable _ nil))
            module-str (or (some-> (get d :module) nsish->str)
                           (some-> (get d :namespace) nsish->str)
                           (some-> (get d :ns) nsish->str))
            id-val (or (get d :id)
                       (get d :name))
            id-str (nameish->str id-val)]
        (cond
          (and (symbol? id-val) (namespace id-val))
          id-val

          (and module-str id-str)
          (symbol module-str id-str)

          :else nil))

      (instance? clojure.lang.IFn fn-ref)
      (let [class-name (.getName (class fn-ref))
            parts (str/split class-name #"\$")
            ns-part (first parts)
            fn-part (second parts)]
        (when (and ns-part fn-part (not (str/blank? ns-part)) (not (str/blank? fn-part)))
          (symbol (-> ns-part (str/replace "_" "-"))
                  (-> fn-part (str/replace "_" "-")))))

      :else nil)))

(defn resolve-function-def
  "Resolves a FnDef from either a FnDef, a namespaced symbol, a Var, or a function value.
   If the global registry doesn't contain the function yet, attempts to analyze and register
   the owning namespace, then resolves again."
  [fn-ref]
  (cond
    (types/fn-def? fn-ref)
    fn-ref

    (types/fn-def? (get-function-def fn-ref))
    (get-function-def fn-ref)

    :else
    (when-let [fn-sym (fn-ref->fn-sym fn-ref)]
      (or (get-function-def fn-sym)
          (do (analyze-and-register! (symbol (namespace fn-sym)))
              (get-function-def fn-sym))))))

(defn get-app-function-def
  "Retrieves a function definition from an app typed payload."
  [app-name fn-sym]
  (let [fn-def (get-in (app/app-typed app-name) [:functions fn-sym])]
    (when (types/fn-def? fn-def)
      fn-def)))

(defn with-app-typed-registry
  [app-name f]
  (let [typed-payload (app/app-typed app-name)
        current @types/*type-registry*]
    (try
      (swap! types/*type-registry*
             (fn [registry]
               (-> registry
                   (merge (:tables typed-payload))
                   (merge (:enums typed-payload))
                   (merge (:functions typed-payload)))))
      (f typed-payload)
      (finally
        (reset! types/*type-registry* current)))))

(defn get-function-input-shape
  "Infers a JsonbShape for a JSONB function input.
   `fn-ref` may be a FnDef or a namespaced symbol in the registry."
  [fn-ref arg-sym]
  (let [fn-def (resolve-function-def fn-ref)]
    (when (and fn-def arg-sym)
      (compile.common/infer-jsonb-arg-shape arg-sym fn-def))))

(defn get-app-function-input-shape
  "Infers a JsonbShape for a JSONB function input from an app typed payload."
  [app-name fn-sym arg-sym]
  (with-app-typed-registry
    app-name
    (fn [typed-payload]
      (when-let [fn-def (get-in typed-payload [:functions fn-sym])]
        (get-function-input-shape fn-def arg-sym)))))

(defn get-function-input-schema
  "Formats an inferred JSONB input shape for a function.
   Formats: `:shape`, `:openapi`, `:json-schema`, `:typescript`."
  ([fn-ref arg-sym]
   (get-function-input-schema fn-ref arg-sym :shape))
  ([fn-ref arg-sym format]
   (when-let [input-shape (get-function-input-shape fn-ref arg-sym)]
     (case format
       :shape input-shape
       :openapi (compile.json-openapi/shape->openapi input-shape)
       :json-schema (compile.json-schema/shape->json-schema input-shape)
       :typescript (compile.ts-schema/shape->ts-interface input-shape)
       (throw (ex-info "Unknown input schema format"
                       {:format format
                        :available [:shape :openapi :json-schema :typescript]}))))))

(defn get-app-function-input-schema
  "Formats an inferred JSONB input shape from an app typed payload."
  ([app-name fn-sym arg-sym]
   (get-app-function-input-schema app-name fn-sym arg-sym :shape))
  ([app-name fn-sym arg-sym format]
   (when-let [input-shape (get-app-function-input-shape app-name fn-sym arg-sym)]
     (case format
       :shape input-shape
       :openapi (compile.json-openapi/shape->openapi input-shape)
       :json-schema (compile.json-schema/shape->json-schema input-shape)
       :typescript (compile.ts-schema/shape->ts-interface input-shape)
       (throw (ex-info "Unknown input schema format"
                       {:format format
                        :available [:shape :openapi :json-schema :typescript]}))))))

(defn get-function-output-shape
  "Infers a JsonbShape for a function's JSON/object output."
  [fn-ref]
  (let [fn-def (resolve-function-def fn-ref)]
    (when fn-def
      (some-> fn-def
              analyze/cached-infer
              inferred->shape))))

(defn get-app-function-output-shape
  "Infers a JsonbShape for a function output from an app typed payload."
  [app-name fn-sym]
  (with-app-typed-registry
    app-name
    (fn [typed-payload]
      (some-> (get-in typed-payload [:functions fn-sym])
              get-function-output-shape))))

(defn get-function-output-schema
  "Formats an inferred JSON/object output shape for a function.
   Formats: `:shape`, `:openapi`, `:json-schema`, `:typescript`."
  ([fn-ref]
   (get-function-output-schema fn-ref :shape))
  ([fn-ref format]
   (when-let [output-shape (get-function-output-shape fn-ref)]
     (format-shape output-shape format))))

(defn get-app-function-output-schema
  "Formats an inferred JSON/object output shape from an app typed payload."
  ([app-name fn-sym]
   (get-app-function-output-schema app-name fn-sym :shape))
  ([app-name fn-sym format]
   (when-let [output-shape (get-app-function-output-shape app-name fn-sym)]
     (format-shape output-shape format))))

(defn report-json
  "Serializes an infer report to JSON."
  ([report]
   (analyze/report-json report))
  ([report pretty?]
   (analyze/report-json report pretty?)))

(defn make-function-json
  "Generates JSON for one function infer report in a namespace."
  ([ns-sym fn-sym]
   (when-let [report (make-function-report ns-sym fn-sym)]
     (report-json report)))
  ([ns-sym fn-sym pretty?]
   (when-let [report (make-function-report ns-sym fn-sym)]
     (report-json report pretty?))))

;; ─────────────────────────────────────────────────────────────────────────────
;; Schema Generation API
;; ─────────────────────────────────────────────────────────────────────────────

(defn make-openapi
  "Generates a complete OpenAPI 3.0 spec for the given namespace.
   
   Example:
   (make-openapi 'gwdb.core.user (constantly true))  ; all functions
   (make-openapi 'gwdb.core.user #(= :sb/auth (get-in % [:body-meta :expose])))"
  [root-ns fn-filter]
  (types/clear-registry!)
  (analyze/reset-cache!)
  (-> root-ns parse/analyze-namespace parse/register-types!)
  (compile.json-openapi/generate-openapi root-ns fn-filter))

(defn make-json-schema
  "Generates JSON Schema definitions for all tables and enums."
  []
  (compile.json-schema/generate-json-schema))

(defn make-typescript
  "Generates TypeScript interfaces for all tables and enums."
  []
  (compile.ts-schema/generate-ts-schema))



(defn get-table-shape
  "Gets the shape for a registered table by name."
  [table-name]
  (when-let [table (types/get-type table-name)]
    (when (types/table-def? table)
      (shape/table->shape table))))

(defn list-tables
  "Returns all registered table definitions."
  []
  (filter types/table-def? (vals @types/*type-registry*)))

(defn list-functions
  "Returns all registered function definitions."
  []
  (filter types/fn-def? (vals @types/*type-registry*)))

(defn list-enums
  "Returns all registered enum definitions."
  []
  (filter types/enum-def? (vals @types/*type-registry*)))

;; ─────────────────────────────────────────────────────────────────────────────
;; Runtime Integration API
;; ─────────────────────────────────────────────────────────────────────────────

(defn load-runtime-tables
  "Loads tables from (pg/app app-name) runtime output.
   Input: {:TableName [:col1 {:type :uuid} ...] ...}"
  [tables-map]
  (into {}
        (map (fn [[table-name entries]]
               [table-name (parse/parse-runtime-table table-name entries)]))
        tables-map))

(defn register-runtime-tables!
  "Registers runtime tables into the global type registry."
  [runtime-tables]
  (doseq [[table-name table-def] runtime-tables]
    (swap! types/*type-registry* assoc table-name table-def)))

;; ─────────────────────────────────────────────────────────────────────────────
;; Type Function API
;; ─────────────────────────────────────────────────────────────────────────────

(defn app-name-from-static
  [app]
  (cond
    (sequential? app) (first app)
    app app
    :else nil))

(defn fn-ref->app-name
  [fn-ref fn-def]
  (or (some-> (get-in fn-def [:body-meta :static/application])
              app-name-from-static)
      (when (instance? clojure.lang.IDeref fn-ref)
        (let [d (try (deref fn-ref)
                     (catch Throwable _ nil))]
          (some-> (get d :static/application)
                  app-name-from-static)))))

(defn arg-type
  [arg]
  (let [t (:type arg)]
    (cond
      (keyword? t) t
      (and (map? t) (keyword? (:name t))) (:name t)
      :else nil)))

(defn arg-type-name
  [arg]
  (when-let [t (arg-type arg)]
    (name t)))

(defn jsonb-arg?
  [arg]
  (or (= :jsonb (arg-type arg))
      (some #{:jsonb} (:modifiers arg))))

(defn format-primitive
  [format t]
  (case format
    :shape {:type t}
    :openapi (compile.common/resolve-type t :openapi)
    :json-schema (compile.common/resolve-type t :jschema)
    :typescript (compile.common/resolve-type t :ts)))

(defn build-input-schemas
  [fn-def format]
  (into {}
        (keep (fn [arg]
                (let [arg-name (:name arg)
                      t (arg-type arg)]
                  (if (jsonb-arg? arg)
                    (when-let [shape (get-function-input-shape fn-def arg-name)]
                      [(keyword arg-name)
                       {:shape shape
                        :schema (format-shape shape format)}])
                    (when t
                      [(keyword arg-name)
                       {:type t
                        :schema (format-primitive format t)}]))))
              (:inputs fn-def))))

(defn build-output-schema
  [fn-def format]
  (when-let [output-shape (get-function-output-shape fn-def)]
    {:shape output-shape
     :schema (format-shape output-shape format)}))

(defn Type
  ([fn-ref]
   (Type fn-ref {:format :shape}))
  ([fn-ref opts]
   (let [format   (get opts :format :shape)
         debug?   (boolean (get opts :debug))
         fn-def   (resolve-function-def fn-ref)
         app-name (fn-ref->app-name fn-ref fn-def)]
     (when fn-def
       (let [render (fn []
                      (let [report (analyze/infer-report fn-def)
                            input-schemas (build-input-schemas fn-def format)
                            output-schema (build-output-schema fn-def format)]
                        (-> report
                            (assoc :input {:args (mapv (fn [arg]
                                                         {:name (:name arg)
                                                          :type (arg-type-name arg)
                                                          :modifiers (:modifiers arg)})
                                                       (:inputs fn-def))
                                           :schemas input-schemas})
                            (assoc :output output-schema))))
             debug-print (fn [typed-payload]
                           (when debug?
                             (println "Type debug app-name:" app-name)
                             (println "Type debug app-present:" (boolean (app/app app-name)))
                             (println "Type debug typed tables:" (count (:tables typed-payload)))))
             run (fn []
                   (if (and app-name (app/app app-name))
                     (with-app-typed-registry app-name
                       (fn [typed-payload]
                         (debug-print typed-payload)
                         (render)))
                     (render)))]
         (when app-name
           (when-not (app/app app-name)
             (try
               (app/app-rebuild-tables app-name)
               (catch Throwable _ nil))))
         (run))))))

(comment
  (keys (into {}  (rt.postgres/app "scratch")))
  
  (parse/analyze-namespace 'gwdb.test.scratch)
  
  (#'rt.postgres.typed/make-openapi 'gwdb.test.scratch (constantly true))
  (make-typescript)
  (make-json-schema)
  (make-openapi 'gwdb.test.scratch (constantly true)))
