(ns std.lang.rewrite.inline-do
  (:require [std.lang.rewrite.walk :as walk]
            [std.lib.collection :as collection]))

(def with-form-meta
  walk/with-form-meta)

(defn do-expression?
  [form]
  (and (collection/form? form)
       (#{'do 'do*} (first form))))

(declare rewrite-inline-do)

(defn rewrite-inline-do-list
  [form]
  (let [rewritten (with-form-meta form
                    (apply list (map rewrite-inline-do form)))]
    (if (and (= 'return (first rewritten))
             (= 2 (count rewritten))
             (do-expression? (second rewritten)))
      (let [expr (second rewritten)
            body (rest expr)]
        (with-form-meta
          form
          (apply list 'do*
                 (concat (butlast body)
                         [(with-form-meta
                            rewritten
                            (list 'return (last body)))]))))
      rewritten)))

(defn rewrite-inline-do
  [form]
  (cond
    (and (collection/form? form)
         (= 'quote (first form)))
    form

    (collection/form? form)
    (rewrite-inline-do-list form)

    :else
    (walk/rewrite-form form rewrite-inline-do-list rewrite-inline-do)))
