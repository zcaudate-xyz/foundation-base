(ns std.lang.model.spec-scheme
  (:require [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-xtalk :as xtalk]
            [std.lib.collection]
            [std.lib.walk]))

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
   (std.lib.walk/prewalk (fn [x]
                (if (std.lib.collection/form? x)
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
