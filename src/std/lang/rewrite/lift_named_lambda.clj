(ns std.lang.rewrite.lift-named-lambda
  (:require [std.lib.collection :as collection]))

(defn with-form-meta
  [source out]
  (if (instance? clojure.lang.IObj out)
    (with-meta out (meta source))
    out))

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

(defn rewrite-fn-body
  [form rewrite-statements]
  (let [[name args body] (fn-parts form)
        body             (-> body
                             rewrite-statements
                             splice-do*
                             wrap-body)]
    (with-form-meta form
      (apply list 'fn
             (concat (when name [name])
                     [args]
                     body)))))

(defn normalize-fn
  [form rewrite-statements]
  (let [[_ args body] (fn-parts form)
        body          (-> body
                          rewrite-statements
                          splice-do*
                          wrap-body)]
    (with-form-meta form
      (apply list 'fn args body))))

(defn lambda-compatible?
  ([form]
   (lambda-compatible? form nil))
  ([form block-form?]
   (let [[name _ body] (fn-parts form)
         body-form     (first body)]
     (and (nil? name)
          (= 1 (count body))
          (not (or (do-form? body-form)
                   (and block-form?
                        (block-form? body-form))))))))

(defn lift-named-lambda
  ([form rewrite-statements]
   (lift-named-lambda form rewrite-statements {}))
  ([form rewrite-statements {:keys [symbol-prefix]
                             :or {symbol-prefix "lifted_lambda__"}}]
   (let [[name _ _] (fn-parts form)
         sym        (or name (gensym symbol-prefix))
         binding    (list 'var sym (normalize-fn form rewrite-statements))]
     [[binding] sym])))
