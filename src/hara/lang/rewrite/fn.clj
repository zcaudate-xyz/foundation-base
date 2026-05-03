(ns hara.lang.rewrite.fn
  (:require [hara.lang.rewrite.common :as common]
            [std.lib.collection :as collection]))

(defn do-form?
  [form]
  (and (collection/form? form)
       (#{'do 'do*} (first form))))

(defn fn-form?
  [form]
  (and (collection/form? form)
       (= 'fn (first form))))

(defn fn-parts
  [form]
  (let [[_ head & tail] form]
    (if (symbol? head)
      [head (first tail) (rest tail)]
      [nil head tail])))

(defn splice-do*
  [forms]
  (mapcat (fn [form]
            (if (and (collection/form? form)
                     (= 'do* (first form)))
              (rest form)
              [form]))
          forms))

(defn wrap-body
  [body]
  (cond (empty? body)
        []

        (= 1 (count body))
        body

        :else
        [(apply list 'do body)]))

(defn rewrite-fn-form
  ([form rewrite-statements]
   (rewrite-fn-form form rewrite-statements {}))
  ([form rewrite-statements {:keys [prepare-body include-name?]
                             :or {prepare-body identity
                                  include-name? true}}]
   (let [[name args body] (fn-parts form)
         body             (-> body
                              rewrite-statements
                              prepare-body
                              splice-do*
                              wrap-body)]
     (common/with-form-meta form
       (apply list 'fn
              (concat (when (and include-name? name) [name])
                      [args]
                      body))))))

(defn rewrite-fn-body
  [form rewrite-statements]
  (rewrite-fn-form form rewrite-statements))

(defn normalize-fn
  [form rewrite-statements]
  (rewrite-fn-form form rewrite-statements {:include-name? false}))

(defn lambda-compatible?
  ([form]
   (lambda-compatible? form nil))
  ([form block-form?]
   (let [[name _ body] (fn-parts form)
         body-form     (first body)]
     (and (nil? name)
          (or (empty? body)
              (and (= 1 (count body))
                   (not (or (do-form? body-form)
                            (and block-form?
                                 (block-form? body-form))))))))))

(defn lift-named-lambda
  ([form rewrite-statements]
   (lift-named-lambda form rewrite-statements {}))
  ([form rewrite-statements {:keys [symbol-prefix]
                             :or {symbol-prefix "lifted_lambda__"}}]
   (let [[name _ _] (fn-parts form)
         sym        (or name (gensym symbol-prefix))
         binding    (list 'var sym (normalize-fn form rewrite-statements))]
     [[binding] sym])))
