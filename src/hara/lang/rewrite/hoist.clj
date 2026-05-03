(ns hara.lang.rewrite.hoist
  (:require [hara.lang.rewrite.common :as common]
            [hara.lang.rewrite.fn :as fnrw]
            [std.lib.collection :as collection]))

(defn create-rewriter
  [{:keys [fn-tags symbol-prefix bulk-do*? block-form? lambda-compatible?]
    :or {fn-tags #{'fn}
         symbol-prefix "lifted_lambda__"
         block-form? (fn [form grammar]
                       (and (collection/form? form)
                            (= :block (get-in grammar [:reserved (first form) :type]))))
          lambda-compatible? (fn [form grammar]
                               (fnrw/lambda-compatible?
                                form
                                #(block-form? % grammar)))
         bulk-do*? (fn [form mopts]
                     (and (:bulk (meta form))
                           (not (get-in mopts [:emit :body :transform]))))}}]
  (letfn [(function-form?
            [form]
            (and (collection/form? form)
                 (contains? fn-tags (first form))))

          (lambda-compatible-form?
            [form grammar]
            (lambda-compatible? form grammar))

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
             (fnrw/rewrite-fn-body form #(rewrite-statements % grammar)))

           (rewrite-expression
             [form grammar]
             (cond (function-form? form)
                    (if (lambda-compatible-form? form grammar)
                      [[] form]
                      (fnrw/lift-named-lambda form
                                              #(rewrite-statements % grammar)
                                              {:symbol-prefix symbol-prefix}))

                  (and (collection/form? form)
                       (= 'quote (first form)))
                  [[] form]

                   (collection/form? form)
                   (let [[prefix out] (rewrite-expression-coll form grammar)]
                     [prefix (common/with-form-meta form (apply list out))])

                   (vector? form)
                   (let [[prefix out] (rewrite-expression-coll form grammar)]
                     [prefix (common/with-form-meta form (vec out))])

                   (set? form)
                   (let [[prefix out] (rewrite-expression-coll form grammar)]
                     [prefix (common/with-form-meta form (set out))])

                   (map? form)
                   (let [[prefix out] (rewrite-expression-map form grammar)]
                     [prefix (common/with-form-meta form (into (empty form) out))])

                  :else
                  [[] form]))

          (wrap-prefix
             [form prefix out]
             (if (empty? prefix)
               out
               (common/with-form-meta form
                 (apply list 'do* (concat prefix [out])))))

          (rewrite-var
            [form grammar]
            (let [[tag sym & args] form]
              (if (empty? args)
                form
                (let [bound   (last args)
                      leading (butlast args)]
                   (if (function-form? bound)
                     (common/with-form-meta form
                       (apply list tag sym
                              (concat leading
                                      [(fnrw/normalize-fn bound #(rewrite-statements % grammar))])))
                     (let [[prefix bound-out] (rewrite-expression bound grammar)
                           out (common/with-form-meta form
                                 (apply list tag sym (concat leading [bound-out])))]
                       (wrap-prefix form prefix out)))))))

          (rewrite-return
            [form grammar]
            (let [[tag & args] form]
              (if (empty? args)
                 form
                 (let [[prefix values] (rewrite-expression-coll args grammar)
                       out (common/with-form-meta form (apply list tag values))]
                   (wrap-prefix form prefix out)))))

          (rewrite-if
            [form grammar]
             (let [[tag test then else] form
                   [prefix test-out] (rewrite-expression test grammar)
                   out (common/with-form-meta form
                         (apply list tag
                                (cond-> [test-out
                                         (rewrite-statement then grammar)]
                                 else (conj (rewrite-statement else grammar)))))]
              (wrap-prefix form prefix out)))

          (rewrite-when
            [form grammar]
             (let [[tag test & body] form
                   [prefix test-out] (rewrite-expression test grammar)
                   out (common/with-form-meta form
                         (apply list tag test-out (rewrite-statements body grammar)))]
               (wrap-prefix form prefix out)))

          (rewrite-do
             [form grammar]
             (let [[tag & body] form]
               (common/with-form-meta form
                 (apply list tag
                        (-> body
                            (rewrite-statements grammar)
                            fnrw/splice-do*)))))

          (rewrite-defn
             [form grammar]
             (let [[tag name args & body] form]
               (common/with-form-meta form
                 (apply list tag name args
                        (-> body
                            (rewrite-statements grammar)
                            fnrw/splice-do*)))))

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
                       (common/with-form-meta form
                         (apply list 'do*
                                (fnrw/splice-do* rewritten)))
                       (common/with-form-meta form rewritten)))

                  :else
                  form))]
    {:function-form?      function-form?
     :rewrite-expression  rewrite-expression
     :rewrite-statement   rewrite-statement
     :rewrite-statements  rewrite-statements
     :rewrite-stage       rewrite-stage}))
