(ns rt.postgres.grammar.typed-jsonb
  (:require [clojure.string :as str]
            [rt.postgres.grammar.typed-common :as types]))

(defn symbol->field-key
  [sym]
  (when (symbol? sym)
    (some-> sym
            name
            (str/replace #"^[A-Za-z]-" "")
            keyword)))

(defn field-info
  ([field-type]
   (field-info field-type {}))
  ([field-type opts]
   (merge {:type (or field-type :jsonb)
           :nullable? true}
          opts)))

(defn typed-binding-form?
  [binding]
  (and (seq? binding)
       (keyword? (first binding))
       (symbol? (second binding))))

(defn accessor-expr?
  [expr]
  (and (seq? expr)
       (or (#{:-> :->>} (first expr))
           (= 'pg/field-id (first expr)))))

(defn append-path
  [path segment]
  (when (and (types/jsonb-path? path) segment)
    (types/make-jsonb-path
     (conj (vec (:segments path)) segment)
     (:root-var path))))

(declare expr-jsonb-path)

(defn access-descriptor
  [ctx expr]
  (when (accessor-expr? expr)
    (let [op (first expr)
          source-path (expr-jsonb-path ctx (second expr))
          field-name (nth expr 2 nil)
          field-key (when (string? field-name)
                      (keyword field-name))]
      (when-let [path (and source-path
                           field-key
                           (append-path source-path field-key))]
        {:path path
         :field-info (field-info (case op
                                   'pg/field-id :uuid
                                   :-> :jsonb
                                   :->> :text))}))))

(defn expr-jsonb-path
  [ctx expr]
  (cond
    (types/jsonb-path? expr)
    expr

    (symbol? expr)
    (types/get-var-path ctx expr)

    :else
    (some-> (access-descriptor ctx expr) :path)))

(defn- set-binding-descriptors
  [source-path binding]
  (into []
        (keep (fn [entry]
                (cond
                  (typed-binding-form? entry)
                  (let [field-key (symbol->field-key (second entry))]
                    (when field-key
                      {:var (second entry)
                       :path (append-path source-path field-key)
                       :binding-type {:kind :cast
                                      :type (first entry)}
                       :field-info (field-info (first entry))}))

                  (symbol? entry)
                  (let [field-key (symbol->field-key entry)]
                    (when field-key
                      {:var entry
                       :path (append-path source-path field-key)
                       :binding-type :jsonb
                       :field-info (field-info :jsonb)}))

                  :else
                  nil)))
        binding))

(defn binding-descriptors
  [ctx binding expr]
  (let [source-path (expr-jsonb-path ctx expr)
        access (access-descriptor ctx expr)]
    (cond
      (and (set? binding)
           source-path)
      (set-binding-descriptors source-path binding)

      (symbol? binding)
      (when-let [path source-path]
        [{:var binding
          :path path
          :binding-type :jsonb
          :field-info (when (seq (:segments path))
                        (field-info :jsonb))}])

      (typed-binding-form? binding)
      (when-let [path (or (some-> access :path)
                          source-path)]
        [{:var (second binding)
          :path path
          :binding-type {:kind :cast
                         :type (first binding)}
          :field-info (field-info (first binding))}])

      :else
      [])))

(defn descriptor-shape
  [{:keys [path field-info]}]
  (let [segments (vec (:segments path))]
    (when (seq segments)
      (letfn [(build [remaining]
                (let [segment (first remaining)
                      tail (next remaining)]
                  (types/make-jsonb-shape
                   {segment (if tail
                              (rt.postgres.grammar.typed-jsonb/field-info
                               :jsonb
                               {:shape (build tail)})
                              field-info)})))]
        (build segments)))))

(defn update-root-shape
  [ctx descriptor]
  (if-let [shape-add (descriptor-shape descriptor)]
    (update ctx :jsonb-shapes
            (fn [shapes]
              (update shapes
                      (get-in descriptor [:path :root-var])
                      #(types/merge-shapes (or % (types/empty-jsonb-shape))
                                           shape-add))))
    ctx))

(defn apply-descriptor
  [ctx descriptor]
  (let [ctx' (update-root-shape ctx descriptor)]
    (cond-> ctx'
      (:var descriptor)
      (types/add-binding (:var descriptor)
                         (:binding-type descriptor)
                         :path (:path descriptor)
                         :shape (types/get-var-shape ctx' (:var descriptor))))))

(defn apply-descriptors
  [ctx descriptors]
  (reduce apply-descriptor ctx descriptors))

(declare scan-form)

(defn- analyze-binding
  [ctx [binding expr]]
  (let [ctx' (scan-form ctx expr)]
    (apply-descriptors ctx'
                       (binding-descriptors ctx' binding expr))))

(defn scan-form
  [ctx form]
  (cond
    (seq? form)
    (let [ctx' (apply-descriptors ctx
                                  (if-let [descriptor (and (accessor-expr? form)
                                                           (access-descriptor ctx form))]
                                    [descriptor]
                                    []))]
      (if (and (= 'let (first form))
               (sequential? (second form)))
        (let [ctx'' (reduce analyze-binding ctx' (partition 2 (second form)))]
          (reduce scan-form ctx'' (drop 2 form)))
        (reduce scan-form ctx' form)))

    (coll? form)
    (reduce scan-form ctx form)

    :else
    ctx))

(defn infer-jsonb-arg-access-shape
  [arg-name fn-def]
  (let [body (get-in fn-def [:body-meta :raw-body])
        ctx  (types/make-context
              {arg-name :jsonb}
              {}
              {arg-name (types/make-jsonb-path [] arg-name)})
        out  (reduce scan-form ctx body)]
    (when-let [shape (types/get-var-shape out arg-name)]
      (when (seq (:fields shape))
        (assoc shape
               :confidence :medium
               :nullable? true)))))
