(ns hara.typed
  (:refer-clojure :exclude [load-file])
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-parse :as parse]
            [hara.typed.xtalk-check :as check]))

(defn namespace-aliases
  [ns-obj]
  (into {}
        (map (fn [[alias target-ns]]
               [alias (ns-name target-ns)]))
        (ns-aliases ns-obj)))

(defn register-spec-form!
  ([sym type-form spec-meta]
   (register-spec-form! sym type-form spec-meta {}))
  ([sym type-form spec-meta aliases]
   (let [ns-sym (some-> sym namespace symbol)
         spec-sym (symbol (name sym))
         spec (parse/parse-spec-decl ns-sym spec-sym type-form spec-meta aliases
                                     {:file (or *file* "UNKNOWN")})]
     (types/register-spec! sym spec)
     spec)))

(defmacro defspec.xt
  [spec-sym & body]
  (let [[docstring body] (if (string? (first body))
                           [(first body) (rest body)]
                           [nil body])
        [attr-map body] (if (map? (first body))
                          [(first body) (rest body)]
                          [nil body])
        type-form (first body)
        full-sym (symbol (str (ns-name *ns*)) (name spec-sym))
        aliases (namespace-aliases *ns*)
        spec-meta (cond-> (merge (meta spec-sym) attr-map)
                    docstring (assoc :docstring docstring))]
    `(do
       (hara.typed/register-spec-form!
         '~full-sym
         '~type-form
         '~spec-meta
         '~aliases)
        nil)))


;; ─────────────────────────────────────────────────────────────────────────────
;; Context API
;; ─────────────────────────────────────────────────────────────────────────────

(defn- analysis->registry
  [analysis]
  (let [current @types/*type-registry*]
    (try
      (types/clear-registry!)
      (parse/register-types! analysis)
      @types/*type-registry*
      (finally
        (reset! types/*type-registry* current)))))

(defn load-analysis
  "Creates an xtalk typed context from parsed analysis."
  [analysis]
  {:domain :xtalk
   :analysis analysis
   :registry (analysis->registry analysis)})

(defn load-file
  "Creates an xtalk typed context from a source file."
  [file-path]
  (load-analysis (parse/analyze-file file-path)))

(defn load-ns
  "Creates an xtalk typed context from a namespace."
  [ns-sym]
  (load-analysis (parse/analyze-namespace ns-sym)))

(defn load-registry
  "Creates an xtalk typed context from a registry map, defaulting to the current registry."
  ([]
   (load-registry @types/*type-registry*))
  ([registry]
   {:domain :xtalk
    :registry registry}))

(defn with-context-registry
  "Runs `f` with the context registry visible to existing checking internals."
  [ctx f]
  (let [current @types/*type-registry*]
    (try
      (reset! types/*type-registry* (:registry ctx))
      (f)
      (finally
        (reset! types/*type-registry* current)))))

(defn entries
  "Returns all xtalk registry entries in a context."
  [ctx]
  (vals (:registry ctx)))

(defn entry
  "Returns one xtalk registry entry from a context."
  [ctx sym]
  (get-in ctx [:registry sym]))

(defn declaration
  "Returns one typed declaration from a context by declaration kind."
  [ctx sym kind]
  (get (types/entry-declarations (entry ctx sym)) kind))

(defn spec-def
  "Returns a spec declaration from a context."
  [ctx sym]
  (declaration ctx sym :spec))

(defn macro-def
  "Returns a macro declaration from a context."
  [ctx sym]
  (declaration ctx sym :macro))

(defn value-def
  "Returns a value declaration from a context."
  [ctx sym]
  (declaration ctx sym :value))


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
  "Resolves an xtalk function definition from a context."
  [ctx fn-ref]
  (or (when (types/fn-def? fn-ref) fn-ref)
      (when (symbol? fn-ref)
        (some-> (entry ctx fn-ref) :fn))
      (missing-function! fn-ref)))

(defn function-report
  "Returns the xtalk type-check report for a function in a context."
  [ctx fn-ref]
  (with-context-registry
    ctx
    #(check/check-fn-def (function-def ctx fn-ref))))

(defn function-input
  "Returns all input types as data, or one input type by argument symbol."
  ([ctx fn-ref]
   (mapv (fn [arg]
           {:name (:name arg)
            :type (types/type->data (:type arg))})
         (:inputs (function-def ctx fn-ref))))
  ([ctx fn-ref arg-sym]
   (or (some (fn [arg]
               (when (= arg-sym (:name arg))
                 (types/type->data (:type arg))))
             (:inputs (function-def ctx fn-ref)))
       (missing-argument! fn-ref arg-sym))))

(defn function-output
  "Returns the declared xtalk output type as data."
  [ctx fn-ref]
  (types/type->data (:output (function-def ctx fn-ref))))

(defn namespace-report
  "Returns type-check reports for every function in a context namespace."
  [ctx]
  (with-context-registry
    ctx
    #(let [ns-sym (some-> ctx :analysis :ns)]
       {:namespace ns-sym
        :functions (mapv check/check-fn-def
                         (->> (entries ctx)
                              (keep :fn)
                              (filter (fn [fn-def]
                                        (or (nil? ns-sym)
                                            (= (str ns-sym) (:ns fn-def)))))))})))
