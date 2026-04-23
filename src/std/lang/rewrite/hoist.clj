(ns std.lang.rewrite.hoist
  (:require [std.lang.rewrite.lift-named-lambda :as lift]
            [std.lib.collection :as collection]))

(defn create-rewriter
  [{:keys [fn-tags symbol-prefix bulk-do*? block-form?]
    :or {fn-tags #{'fn}
         symbol-prefix "lifted_lambda__"
         block-form? (fn [form grammar]
                       (and (collection/form? form)
                            (= :block (get-in grammar [:reserved (first form) :type]))))
         bulk-do*? (fn [form mopts]
                     (and (:bulk (meta form))
                          (not (get-in mopts [:emit :body :transform]))))}}]
  (letfn [(function-form?
            [form]
            (and (collection/form? form)
                 (contains? fn-tags (first form))))

          (lambda-compatible?
            [form grammar]
            (lift/lambda-compatible?
             form
             #(block-form? % grammar)))

          (rewrite-expression-coll
            [form grammar]
            (reduce (fn [[prefix out] item]
                      (let [[item-prefix item-out] (rewrite-expression item grammar)]
                        [(into prefix item-prefix)
                         (conj out item-out)]))
                    [[] []]
                    form))

          (rewrite-expression-map
            [form grammar]
            (reduce (fn [[prefix out] [k v]]
                      (let [[kp ko] (rewrite-expression k grammar)
                            [vp vo] (rewrite-expression v grammar)]
                        [(into (into prefix kp) vp)
                         (conj out [ko vo])]))
                    [[] []]
                    form))

          (rewrite-fn-body
            [form grammar]
            (lift/rewrite-fn-body form #(rewrite-statements % grammar)))

          (rewrite-expression
            [form grammar]
            (cond (function-form? form)
                  (if (lambda-compatible? form grammar)
                    [[] form]
                    (lift/lift-named-lambda form
                                            #(rewrite-statements % grammar)
                                            {:symbol-prefix symbol-prefix}))

                  (and (collection/form? form)
                       (= 'quote (first form)))
                  [[] form]

                  (collection/form? form)
                  (let [[prefix out] (rewrite-expression-coll form grammar)]
                    [prefix (lift/with-form-meta form (apply list out))])

                  (vector? form)
                  (let [[prefix out] (rewrite-expression-coll form grammar)]
                    [prefix (lift/with-form-meta form (vec out))])

                  (set? form)
                  (let [[prefix out] (rewrite-expression-coll form grammar)]
                    [prefix (lift/with-form-meta form (set out))])

                  (map? form)
                  (let [[prefix out] (rewrite-expression-map form grammar)]
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
            [form grammar]
            (let [[tag sym & args] form]
              (if (empty? args)
                form
                (let [bound   (last args)
                      leading (butlast args)]
                  (if (function-form? bound)
                    (lift/with-form-meta form
                      (apply list tag sym
                             (concat leading
                                     [(lift/normalize-fn bound #(rewrite-statements % grammar))])))
                    (let [[prefix bound-out] (rewrite-expression bound grammar)
                          out (lift/with-form-meta form
                                (apply list tag sym (concat leading [bound-out])))]
                      (wrap-prefix form prefix out)))))))

          (rewrite-return
            [form grammar]
            (let [[tag & args] form]
              (if (empty? args)
                form
                (let [[prefix value] (rewrite-expression (first args) grammar)
                      out (lift/with-form-meta form (list tag value))]
                  (wrap-prefix form prefix out)))))

          (rewrite-if
            [form grammar]
            (let [[tag test then else] form
                  [prefix test-out] (rewrite-expression test grammar)
                  out (lift/with-form-meta form
                        (apply list tag
                               (cond-> [test-out
                                        (rewrite-statement then grammar)]
                                 else (conj (rewrite-statement else grammar)))))]
              (wrap-prefix form prefix out)))

          (rewrite-when
            [form grammar]
            (let [[tag test & body] form
                  [prefix test-out] (rewrite-expression test grammar)
                  out (lift/with-form-meta form
                        (apply list tag test-out (rewrite-statements body grammar)))]
              (wrap-prefix form prefix out)))

          (rewrite-do
            [form grammar]
            (let [[tag & body] form]
              (lift/with-form-meta form
                (apply list tag
                       (-> body
                           (rewrite-statements grammar)
                           lift/splice-do*)))))

          (rewrite-defn
            [form grammar]
            (let [[tag name args & body] form]
              (lift/with-form-meta form
                (apply list tag name args
                       (-> body
                           (rewrite-statements grammar)
                           lift/splice-do*)))))

          (rewrite-statement
            [form grammar]
            (cond (not (collection/form? form))
                  form

                  :else
                  (case (first form)
                    (do do*)      (rewrite-do form grammar)
                    (var var*)    (rewrite-var form grammar)
                    :=            (rewrite-var form grammar)
                    return        (rewrite-return form grammar)
                    if            (rewrite-if form grammar)
                    when          (rewrite-when form grammar)
                    (defn defn- defgen)
                    (rewrite-defn form grammar)
                    (if (function-form? form)
                      (rewrite-fn-body form grammar)
                      (let [[prefix out] (rewrite-expression form grammar)]
                        (wrap-prefix form prefix out))))))

          (rewrite-statements
            [forms grammar]
            (map #(rewrite-statement % grammar) forms))

          (rewrite-stage
            [form {:keys [mopts grammar]}]
            (cond (collection/form? form)
                  (rewrite-statement form grammar)

                  (vector? form)
                  (let [rewritten (mapv #(rewrite-statement % grammar) form)]
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
