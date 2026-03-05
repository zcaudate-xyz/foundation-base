(ns bb.lang.model.spec-scheme
  (:require [bb.lang.base.emit :as emit]
            [bb.lang.base.grammar :as grammar]
            [bb.lang.base.util :as ut]
            [bb.lang.base.book :as book]
            [bb.lang.base.script :as script]
            [bb.string :as str]
            [bb.lib :as h]
            [bb.lang.model.spec-xtalk :as xtalk]))

(def +replace+
  '{==    =
    fn:>  lambda
    fn    lambda
    nil   '()})

(def +transform+
  {'return   (fn [[_ & args]]
               (first args))})

(def +features+
  (grammar/build-min [:coroutine
                      :xtalk
                      :xtalk-math
                      :xtalk-type
                      :xtalk-lu
                      :xtalk-iter
                      :xtalk-async
                      :xtalk-obj
                      :xtalk-arr
                      :xtalk-fn
                      :xtalk-str
                      :xtalk-js]))

(defn emit-scheme
  "emits code into scheme schema"
  {:added "4.0"}
  [form mopts]
  (pr-str
   (h/prewalk (fn [x]
                (if (h/form? x)
                  (or (if-let [f (get +transform+ (first x))]
                        (f x))
                      (if-let [v (get +replace+ (first x))]
                        (cons v (rest x)))
                      x)
                  x))
              form)))

(def +grammar+
  (grammar/grammar :scm
    (grammar/to-reserved +features+)
    {:emit #'emit-scheme}))

(def +meta+ (book/book-meta {}))

(def +book+
  (book/book {:lang :scheme
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
