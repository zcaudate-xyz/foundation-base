(ns rt.postgres.base.typed.typed-infer
  (:require [clojure.string :as str]
            [rt.postgres.base.typed.typed-common :as types]
            [rt.postgres.base.typed.typed-jsonb :as typed-jsonb]
            [rt.postgres.base.typed.typed-resolve :as typed-resolve]
            [rt.postgres.base.typed.typed-shape :as shape]))

(defn select-shape-columns
  "Restricts a JsonbShape to the selected columns, preserving shape metadata."
  [base-shape cols]
  (if (and (types/jsonb-shape? base-shape) (seq cols))
    (let [wanted-cols (set (map #(if (keyword? %) % (keyword (name %))) cols))]
      (update base-shape :fields select-keys wanted-cols))
    base-shape))

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
            (cond
              (seq? form)
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

(defn infer-jsonb-arg-access-shape
  ([arg-name fn-def]
   (infer-jsonb-arg-access-shape arg-name fn-def nil nil))
  ([arg-name fn-def seed-shape]
   (infer-jsonb-arg-access-shape arg-name fn-def seed-shape nil))
  ([arg-name fn-def seed-shape role]
   (typed-jsonb/infer-jsonb-arg-access-shape arg-name fn-def seed-shape role)))

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
                  (when-let [called-fn (typed-resolve/resolve-called-fn op aliases)]
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
