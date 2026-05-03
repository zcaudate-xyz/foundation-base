(ns hara.lang.rewrite.unpack
  (:require [std.lib.collection :as collection]))

(defn unpack-form?
  [form]
  (and (collection/form? form)
       (= 'x:unpack (first form))
       (= 2 (count form))))

(defn any-unpack?
  [args]
  (boolean (some unpack-form? args)))

(defn rewrite-arg
  [arg rewrite-expression ordinary-fn unpack-fn]
  (if (unpack-form? arg)
    (unpack-fn (rewrite-expression (second arg)))
    (ordinary-fn (rewrite-expression arg))))

(defn rewrite-args
  [args rewrite-expression ordinary-fn unpack-fn]
  (map #(rewrite-arg % rewrite-expression ordinary-fn unpack-fn) args))
