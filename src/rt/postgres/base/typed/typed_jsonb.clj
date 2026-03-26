(ns rt.postgres.base.typed.typed-jsonb
  (:require [clojure.string :as str]
            [rt.postgres.base.typed.typed-common :as types]))

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
  (boolean
   (and (seq? expr)
        (or (#{:-> :->>} (first expr))
            (= 'pg/field-id (first expr))))))

(defn append-path
  [path segment]
  (when (and (types/jsonb-path? path) segment)
    (types/make-jsonb-path
     (conj (vec (:segments path)) segment)
     (:root-var path))))

(declare expr-jsonb-path)

(defn access-descriptors
  [ctx expr]
  (when (accessor-expr? expr)
    (let [op (first expr)
          source-path (expr-jsonb-path ctx (second expr))
          field-name (nth expr 2 nil)
          field-key (when (string? field-name)
                      (keyword field-name))]
      (cond
        (and (= 'pg/field-id op) source-path field-key)
        (keep identity
              [{:path (append-path source-path
                                   (keyword (str field-name "_id")))
                :field-info (field-info :uuid)}
               (when-let [nested-path (some-> source-path
                                              (append-path field-key)
                                              (append-path :id))]
                 {:path nested-path
                  :field-info (field-info :uuid)})])

        :else
        (when-let [path (and source-path
                             field-key
                             (append-path source-path field-key))]
          [{:path path
            :field-info (field-info (case op
                                      :-> :jsonb
                                      :->> :text))}])))))

(defn access-descriptor
  [ctx expr]
  (first (access-descriptors ctx expr)))

(defn source-root-shape
  [ctx source-path]
  (when (types/jsonb-path? source-path)
    (types/get-var-shape ctx (:root-var source-path))))

(defn source-field-info
  [ctx source-path field-key accessor-type]
  (let [shape (source-root-shape ctx source-path)
        source-field-info (when (and shape field-key)
                            (get-in shape [:fields field-key]))]
    (or source-field-info
        (field-info (case accessor-type
                      :-> :jsonb
                      :->> :text)))))

(declare js-keys-form->keywords)

(defn js-select-shape
  [ctx form]
  (when (and (seq? form)
             (symbol? (first form))
             (= "js-select" (name (first form))))
    (let [source-expr (second form)
          keys-expr (nth form 2 nil)
          source-path (expr-jsonb-path ctx source-expr)
          source-shape (source-root-shape ctx source-path)
          field-keys (js-keys-form->keywords keys-expr)]
      (when (and (types/jsonb-shape? source-shape)
                 (seq field-keys))
        (types/make-jsonb-shape
         (into {}
               (map (fn [k]
                      [k (source-field-info ctx source-path k :->)]))
               field-keys)
         (:source-table source-shape)
         (:confidence source-shape)
         (:nullable? source-shape))))))

(defn expr-jsonb-path
  [ctx expr]
  (cond
    (types/jsonb-path? expr)
    expr

    (symbol? expr)
    (types/get-var-path ctx expr)

    :else
    (some-> (access-descriptor ctx expr) :path)))

(defn set-binding-descriptors
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
                              (rt.postgres.base.typed.typed-jsonb/field-info
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

(defn js-keys-form->keywords
  "Extracts keyword field names from common `(js [...])`/vector forms."
  [form]
  (let [v (cond
            (vector? form) form
            (and (seq? form)
                 (symbol? (first form))
                 (= "js" (name (first form)))
                 (vector? (second form)))
            (second form)
            :else nil)]
    (when (vector? v)
      (->> v
           (keep (fn [x]
                   (cond
                     (string? x) (keyword (str/replace x "_" "-"))
                     (keyword? x) (keyword (str/replace (name x) "_" "-"))
                     :else nil)))
           (vec)))))

(defn js-select-descriptors
  "Generates descriptors for `(js-select <jsonb> (js [\"k\" ...]))`-style access."
  [ctx form]
  (when (and (seq? form)
             (symbol? (first form))
             (= "js-select" (name (first form))))
    (let [source-expr (second form)
          keys-expr (nth form 2 nil)
          source-path (expr-jsonb-path ctx source-expr)
          field-keys (js-keys-form->keywords keys-expr)]
      (when (and source-path (seq field-keys))
        (mapv (fn [k]
                {:path (append-path source-path k)
                 :field-info (source-field-info ctx source-path k :->)})
              field-keys)))))

(defn analyze-binding
  [ctx [binding expr]]
  (let [ctx' (scan-form ctx expr)
        projected-shape (js-select-shape ctx' expr)
        source-name (when (and projected-shape
                               (seq? expr)
                               (symbol? (first expr))
                               (= "js-select" (name (first expr)))
                               (symbol? (second expr)))
                      (second expr))
        ctx'' (cond-> ctx'
                (and projected-shape (symbol? binding))
                (types/add-binding binding :jsonb :shape projected-shape)
                source-name
                (types/add-binding source-name :jsonb :shape projected-shape))]
    (apply-descriptors ctx''
                       (binding-descriptors ctx'' binding expr))))

(defn scan-form
  [ctx form]
  (cond
    (seq? form)
    (let [ctx' (apply-descriptors ctx
                                  (concat (if (accessor-expr? form)
                                            (access-descriptors ctx form)
                                            [])
                                          (or (js-select-descriptors ctx form)
                                              [])))]
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
  ([arg-name fn-def]
   (infer-jsonb-arg-access-shape arg-name fn-def nil nil))
  ([arg-name fn-def seed-shape]
   (infer-jsonb-arg-access-shape arg-name fn-def seed-shape nil))
  ([arg-name fn-def seed-shape role]
   (when-not (= :track role)
     (let [body (get-in fn-def [:body-meta :raw-body])
           ctx  (types/make-context
                 {arg-name :jsonb}
                 (cond-> {}
                   seed-shape (assoc arg-name seed-shape))
                 {arg-name (types/make-jsonb-path [] arg-name)})
           out  (reduce scan-form ctx body)]
       (when-let [shape (types/get-var-shape out arg-name)]
         (when (seq (:fields shape))
           (assoc shape
                  :confidence :medium
                  :nullable? true)))))))
