(ns std.lang.rewrite.hoist
  (:require [std.lang.rewrite.lift-named-lambda :as lift]
            [std.lib.collection :as collection]))

(defn create-rewriter
  [{:keys [fn-tags symbol-prefix bulk-do*?]
    :or {fn-tags #{'fn}
         symbol-prefix "lifted_lambda__"
         bulk-do*? (fn [form mopts]
                     (and (:bulk (meta form))
                          (not (get-in mopts [:emit :body :transform]))))}}]
  (letfn [(function-form?
            [form]
            (and (collection/form? form)
                 (contains? fn-tags (first form))))

          (rewrite-expression-coll
            [form]
            (reduce (fn [[prefix out] item]
                      (let [[item-prefix item-out] (rewrite-expression item)]
                        [(into prefix item-prefix)
                         (conj out item-out)]))
                    [[] []]
                    form))

          (rewrite-expression-map
            [form]
            (reduce (fn [[prefix out] [k v]]
                      (let [[kp ko] (rewrite-expression k)
                            [vp vo] (rewrite-expression v)]
                        [(into (into prefix kp) vp)
                         (conj out [ko vo])]))
                    [[] []]
                    form))

          (rewrite-fn-body
            [form]
            (lift/rewrite-fn-body form rewrite-statements))

          (rewrite-expression
            [form]
            (cond (function-form? form)
                  (if (lift/lambda-compatible? form)
                    [[] form]
                    (lift/lift-named-lambda form
                                            rewrite-statements
                                            {:symbol-prefix symbol-prefix}))

                  (and (collection/form? form)
                       (= 'quote (first form)))
                  [[] form]

                  (collection/form? form)
                  (let [[prefix out] (rewrite-expression-coll form)]
                    [prefix (lift/with-form-meta form (apply list out))])

                  (vector? form)
                  (let [[prefix out] (rewrite-expression-coll form)]
                    [prefix (lift/with-form-meta form (vec out))])

                  (set? form)
                  (let [[prefix out] (rewrite-expression-coll form)]
                    [prefix (lift/with-form-meta form (set out))])

                  (map? form)
                  (let [[prefix out] (rewrite-expression-map form)]
                    [prefix (lift/with-form-meta form (into (empty form) out))])

                  :else
                  [[] form]))

          (wrap-prefix
            [form prefix out]
            (if (empty? prefix)
              out
              (lift/with-form-meta form
                (apply list 'do* (concat prefix [out])))))

          (rewrite-var
            [form]
            (let [[tag sym & args] form]
              (if (empty? args)
                form
                (let [bound   (last args)
                      leading (butlast args)]
                  (if (function-form? bound)
                    (lift/with-form-meta form
                      (apply list tag sym
                             (concat leading
                                     [(lift/normalize-fn bound rewrite-statements)])))
                    (let [[prefix bound-out] (rewrite-expression bound)
                          out (lift/with-form-meta form
                                (apply list tag sym (concat leading [bound-out])))]
                      (wrap-prefix form prefix out)))))))

          (rewrite-return
            [form]
            (let [[tag & args] form]
              (if (empty? args)
                form
                (let [[prefix value] (rewrite-expression (first args))
                      out (lift/with-form-meta form (list tag value))]
                  (wrap-prefix form prefix out)))))

          (rewrite-if
            [form]
            (let [[tag test then else] form
                  [prefix test-out] (rewrite-expression test)
                  out (lift/with-form-meta form
                        (apply list tag
                               (cond-> [test-out
                                        (rewrite-statement then)]
                                 else (conj (rewrite-statement else)))))]
              (wrap-prefix form prefix out)))

          (rewrite-when
            [form]
            (let [[tag test & body] form
                  [prefix test-out] (rewrite-expression test)
                  out (lift/with-form-meta form
                        (apply list tag test-out (rewrite-statements body)))]
              (wrap-prefix form prefix out)))

          (rewrite-do
            [form]
            (let [[tag & body] form]
              (lift/with-form-meta form
                (apply list tag
                       (-> body
                           rewrite-statements
                           lift/splice-do*)))))

          (rewrite-defn
            [form]
            (let [[tag name args & body] form]
              (lift/with-form-meta form
                (apply list tag name args
                       (-> body
                           rewrite-statements
                           lift/splice-do*)))))

          (rewrite-statement
            [form]
            (cond (not (collection/form? form))
                  form

                  :else
                  (case (first form)
                    (do do*)      (rewrite-do form)
                    (var var*)    (rewrite-var form)
                    :=            (rewrite-var form)
                    return        (rewrite-return form)
                    if            (rewrite-if form)
                    when          (rewrite-when form)
                    (defn defn- defgen)
                    (rewrite-defn form)
                    (if (function-form? form)
                      (rewrite-fn-body form)
                      (let [[prefix out] (rewrite-expression form)]
                        (wrap-prefix form prefix out))))))

          (rewrite-statements
            [forms]
            (map rewrite-statement forms))

          (rewrite-stage
            [form {:keys [mopts]}]
            (cond (collection/form? form)
                  (rewrite-statement form)

                  (vector? form)
                  (let [rewritten (mapv rewrite-statement form)]
                    (if (bulk-do*? form mopts)
                      (lift/with-form-meta form
                        (apply list 'do*
                               (lift/splice-do* rewritten)))
                      (lift/with-form-meta form rewritten)))

                  :else
                  form))]
    {:function-form?      function-form?
     :rewrite-expression  rewrite-expression
     :rewrite-statement   rewrite-statement
     :rewrite-statements  rewrite-statements
     :rewrite-stage       rewrite-stage}))
