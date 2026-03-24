(ns rt.postgres.compile.common
  (:require [clojure.string :as str]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-jsonb :as typed-jsonb]
            [rt.postgres.grammar.typed-shape :as shape]))

(defn def-name
  "Returns the display name for a typed definition."
  [x]
  (some-> x :name name))

(defn unique-defs
  "Filters registry values to unique definitions by name."
  [defs]
  (->> defs
       (group-by def-name)
       (map (fn [[_ v]] (first v)))))

(defn select-shape-columns
  "Restricts a JsonbShape to the selected columns, preserving shape metadata."
  [base-shape cols]
  (if (and (types/jsonb-shape? base-shape) (seq cols))
    (let [wanted-cols (set (map #(if (keyword? %) % (keyword (name %))) cols))]
      (update base-shape :fields select-keys wanted-cols))
    base-shape))

(defn infer-jsonb-arg-access-shape
  "Infers a partial JsonbShape for a JSONB argument from field access patterns."
  [arg-name fn-def & [role]]
  (typed-jsonb/infer-jsonb-arg-access-shape arg-name fn-def role))

(defn resolve-table-def
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

(defn resolve-type
  "Resolves any type to the requested output format using +type-formats+."
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

(defn form-uses-tracked?
  [form tracked]
  (boolean
   (some #(and (symbol? %) (contains? tracked %))
         (tree-seq coll? seq form))))

(defn form-uses-track-param?
  [form tracked]
  (boolean
   (some (fn [node]
           (and (map? node)
                (contains? node :track)
                (form-uses-tracked? (:track node) tracked)))
         (tree-seq coll? seq form))))

(defn find-table-op-in-body
  "Searches body for pg/t:insert or pg/g:insert calls with arg-name.
   Also traces through derived let bindings and function calls.
   Returns the table symbol if found, nil otherwise."
  [body arg-name]
  (letfn [(scan-form [form tracked]
          (cond (seq? form)
                  (let [op (first form)
                        args (vec (rest form))]
                    (cond
                      (and (#{'pg/t:insert 'pg/g:insert 'pg/t:update 'pg/g:update} op)
                           (contains? tracked (nth form 2 nil)))
                      (second form)

                      (and (#{'pg/t:update 'pg/g:update} op)
                           (let [params (nth form 2 nil)]
                             (and (map? params)
                                  (form-uses-tracked? (:set params) tracked))))
                      (second form)

                      (and (= 'let op)
                           (sequential? (second form)))
                      (loop [bindings (partition 2 (second form))
                             tracked tracked]
                        (if-let [[binding expr] (first bindings)]
                          (or (scan-form expr tracked)
                              (recur (next bindings)
                                     (cond-> tracked
                                       (and (symbol? binding)
                                            (form-uses-tracked? expr tracked))
                                       (conj binding))))
                          (scan-forms (drop 2 form) tracked)))

                      (symbol? op)
                      (let [arg-pos (first (keep-indexed (fn [idx itm]
                                                           (when (contains? tracked itm) idx))
                                                         args))
                            op-name (name op)
                            fn-def (or (types/get-type op)
                                       (types/get-type (symbol op-name))
                                       (first (filter (fn [f]
                                                        (and (types/fn-def? f)
                                                             (= op-name (:name f))))
                                                      (vals @types/*type-registry*))))]
                        (or (when (and (some? arg-pos)
                                       (types/fn-def? fn-def))
                              (when-let [target-arg (nth (:inputs fn-def) arg-pos nil)]
                                (when (= :jsonb (:type target-arg))
                                  (find-table-op-in-body
                                   (get-in fn-def [:body-meta :raw-body])
                                   (:name target-arg)))))
                            (scan-forms form tracked)))

                      :else
                      (scan-forms form tracked)))

                  (coll? form)
                  (scan-forms form tracked)

                  :else nil))
          (scan-forms [forms tracked]
            (some #(scan-form % tracked) forms))]
    (scan-form body #{arg-name})))

(defn find-table-update-spec-in-body
  "Searches body for pg/t:update or pg/g:update forms that consume arg-name
   in their :set payload. Returns a map with table and selected columns when
   found, nil otherwise."
  [body arg-name]
  (letfn [(scan-form [form tracked]
            (cond
              (seq? form)
              (let [op (first form)
                    args (vec (rest form))]
                (cond
                  (and (#{'pg/t:update 'pg/g:update} op)
                       (let [params (nth form 2 nil)]
                         (and (map? params)
                              (form-uses-tracked? (:set params) tracked))))
                  (let [params (nth form 2 nil)]
                    {:table (second form)
                     :columns (:columns params)
                     :set (:set params)
                     :op op})

                  (and (= 'let op)
                       (sequential? (second form)))
                  (loop [bindings (partition 2 (second form))
                         tracked tracked]
                    (if-let [[binding expr] (first bindings)]
                      (or (scan-form expr tracked)
                          (recur (next bindings)
                                 (cond-> tracked
                                   (and (symbol? binding)
                                        (form-uses-tracked? expr tracked))
                                   (conj binding))))
                      (scan-forms (drop 2 form) tracked)))

                  (symbol? op)
                  (let [arg-pos (first (keep-indexed (fn [idx itm]
                                                       (when (contains? tracked itm) idx))
                                                     args))
                        op-name (name op)
                        fn-def (or (types/get-type op)
                                   (types/get-type (symbol op-name))
                                   (first (filter (fn [f]
                                                    (and (types/fn-def? f)
                                                         (= op-name (:name f))))
                                                  (vals @types/*type-registry*))))]
                    (or (when (and (some? arg-pos)
                                   (types/fn-def? fn-def))
                          (when-let [target-arg (nth (:inputs fn-def) arg-pos nil)]
                            (when (= :jsonb (:type target-arg))
                              (scan-form
                               (get-in fn-def [:body-meta :raw-body])
                               #{(:name target-arg)}))))
                        (scan-forms form tracked)))

                  :else
                  (scan-forms form tracked)))

              (coll? form)
              (scan-forms form tracked)

              :else nil))
          (scan-forms [forms tracked]
            (some #(scan-form % tracked) forms))]
    (scan-form body #{arg-name})))

(defn find-table-track-spec-in-body
  "Searches body for pg/t:update or pg/t:insert forms that consume arg-name
   via :track. Returns a map with table and track form when found."
  [body arg-name]
  (letfn [(scan-form [form tracked]
            (cond
              (seq? form)
              (let [op (first form)
                    args (vec (rest form))]
                (cond
                  (and (#{'pg/t:insert 'pg/g:insert 'pg/t:update 'pg/g:update 'pg/t:upsert 'pg/g:upsert} op)
                       (let [params (nth form 2 nil)]
                         (and (map? params)
                              (form-uses-track-param? params tracked))))
                  (let [params (nth form 2 nil)]
                    {:table (second form)
                     :track (:track params)
                     :op op})

                  (and (= 'let op)
                       (sequential? (second form)))
                  (loop [bindings (partition 2 (second form))
                         tracked tracked]
                    (if-let [[binding expr] (first bindings)]
                      (or (scan-form expr tracked)
                          (recur (next bindings)
                                 (cond-> tracked
                                   (and (symbol? binding)
                                        (form-uses-tracked? expr tracked))
                                   (conj binding))))
                      (scan-forms (drop 2 form) tracked)))

                  (symbol? op)
                  (let [arg-pos (first (keep-indexed (fn [idx itm]
                                                       (when (contains? tracked itm) idx))
                                                     args))
                        op-name (name op)
                        fn-def (or (types/get-type op)
                                   (types/get-type (symbol op-name))
                                   (first (filter (fn [f]
                                                    (and (types/fn-def? f)
                                                         (= op-name (:name f))))
                                                  (vals @types/*type-registry*))))]
                    (or (when (and (some? arg-pos)
                                   (types/fn-def? fn-def))
                          (when-let [target-arg (nth (:inputs fn-def) arg-pos nil)]
                            (when (= :jsonb (:type target-arg))
                              (scan-form
                               (get-in fn-def [:body-meta :raw-body])
                               #{(:name target-arg)}))))
                        (scan-forms form tracked)))

                  :else
                  (scan-forms form tracked)))

              (coll? form)
              (scan-forms form tracked)

              :else nil))
          (scan-forms [forms tracked]
            (some #(scan-form % tracked) forms))]
    (scan-form body #{arg-name})))

(defn resolve-called-fn
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

(declare infer-jsonb-arg-shape*)

(defn infer-jsonb-arg-table-shape*
  [arg-name fn-def visited]
  (let [fn-key (symbol (or (:ns fn-def) "") (:name fn-def))
        arg-role (some (fn [arg]
                         (when (= arg-name (:name arg))
                           (:role arg)))
                       (:inputs fn-def))
        meta-table (get-in fn-def [:body-meta :api/meta :table])
        meta-cols (get-in fn-def [:body-meta :api/meta :columns])
        aliases (get-in fn-def [:body-meta :aliases] {})
        body (get-in fn-def [:body-meta :raw-body])]
    (or
     (when-let [table-def (resolve-table-def meta-table)]
       (select-shape-columns
        (shape/table->shape table-def)
        meta-cols))

     (when-let [{:keys [table columns]} (find-table-update-spec-in-body body arg-name)]
       (when-let [table-def (or (types/get-type table)
                                (types/get-type (symbol (name table)))
                                (types/get-type (symbol (str "-/" (name table))))
                                (first (filter #(= (name table) (:name %))
                                               (vals @types/*type-registry*))))]
         (when (types/table-def? table-def)
           (select-shape-columns
            (shape/table->shape table-def)
            columns))))

     (when-let [table-sym (and body
                               (if (= :track arg-role)
                                 (or (:table (find-table-track-spec-in-body body arg-name))
                                     (find-table-op-in-body body arg-name))
                                 (find-table-op-in-body body arg-name)))]
       (when-let [table-def (or (types/get-type table-sym)
                                (types/get-type (symbol (name table-sym)))
                                (types/get-type (symbol (str "-/" (name table-sym))))
                                (first (filter #(= (name table-sym) (:name %))
                                               (vals @types/*type-registry*))))]
         (when (types/table-def? table-def)
           (shape/table->shape table-def))))

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
                        (infer-jsonb-arg-table-shape*
                         (:name target-arg)
                         called-fn
                         (conj visited fn-key)))))))))
          forms))))))

(defn infer-jsonb-arg-shape
  "Infers a JsonbShape for a :jsonb argument when used with table operations."
  [arg-name fn-def & [role]]
  (infer-jsonb-arg-shape* arg-name fn-def #{} role))

(defn infer-jsonb-arg-shape*
  [arg-name fn-def visited & [role]]
  (if (= :track role)
    nil
    (let [table-shape (infer-jsonb-arg-table-shape* arg-name fn-def visited)
          access-shape (typed-jsonb/infer-jsonb-arg-access-shape
                        arg-name
                        fn-def
                        table-shape
                        role)]
      (or access-shape table-shape))))
