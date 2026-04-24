(ns std.lang.base.preprocess-value-test
  (:use code.test)
  (:require [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.preprocess-value :refer :all]
            [std.lang.model.spec-js :as js]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.preprocess-value/value-template-args :added "4.1"}
(fact "derives template value args from arglists metadata"
  (value-template-args
   (with-meta
     (fn [_ sym value] [sym value])
     {:arglists '([ctx sym value])}))
  => '[sym value]

  (value-template-args
   (with-meta
     (fn [_ [sym value]] [sym value])
     {:arglists '(( [ctx sym value] ))}))
  => '[sym value]

  (value-template-args
   '([x y])
   (with-meta
     (fn [_ a b & more] [a b more])
     {:arglists '([ctx a b & more])}))
  => '[x y])

^{:refer std.lang.base.preprocess-value/value-standalone :added "4.1"}
(fact "callable xtalk intrinsics use shared value-standalone compilation"
  (value-standalone 'x:add +grammar+)
  => '(fn [x y] (return (+ x y)))

  (value-standalone 'x:arr-push js/+grammar+)
  => '(fn [arr value]
        (. arr (push value))
        (return arr))

  (value-standalone 'for:object js/+grammar+)
  => nil

  (value-standalone 'hello
                    {:reserved {'hello {:emit :macro
                                        :macro (with-meta
                                                 (fn [[_ a b]]
                                                   (list '+ a b))
                                                 {:arglists '([_ a b])})
                                        :value/standalone true}}})
  => '(fn [a b] (return (+ a b))))
