(ns hara.lang.rewrite.conditional
  (:require [hara.lang.rewrite.walk :as walk]
            [std.lib.collection :as collection]))

(def with-form-meta
  walk/with-form-meta)

(defn rewrite-conditional-expression-list
  [form rewrite-fn rewrite-conditional-expression rewrite-expression]
  (case (first form)
    quote
    form

    fn
    (rewrite-fn form)

    or
    (with-form-meta
      form
      (apply list 'or
             (map rewrite-conditional-expression (rest form))))

    and
    (with-form-meta
      form
      (apply list 'and
             (map rewrite-conditional-expression (rest form))))

    not
    (with-form-meta
      form
      (list 'not
            (rewrite-conditional-expression (second form))))

    :?
    (let [[_ test then else] form]
      (with-form-meta
        form
        (list :?
              (rewrite-conditional-expression test)
              (rewrite-expression then)
              (rewrite-expression else))))

    (let [head  (first form)
          head* (if (collection/form? head)
                  (rewrite-expression head)
                  head)]
      (with-form-meta
        form
        (apply list head*
               (map rewrite-expression (rest form)))))))

(defn rewrite-conditional-expression
  [form rewrite-list rewrite-expression wrap-truthy]
  (let [form* (walk/rewrite-form form rewrite-list rewrite-expression)]
    (wrap-truthy form form*)))
