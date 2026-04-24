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

^{:refer std.lang.base.preprocess-value/process-value-form :added "4.1"}
(fact "block-valued xtalk macros are lowered in value positions"
  (value-standalone 'x:type-native js/+grammar+)
  => '(fn [value]
        (do
          (when (== value nil)
            (return nil))
          (var t := (typeof value))
          (if (== t "object")
            (cond
              (Array.isArray value)
              (return "array")
              :else
              (do
                (var tn := (. value ["constructor"] ["name"]))
                (if (== tn "Object")
                  (return "object")
                  (return tn))))
            (return t))))

  (process-value-form '(return (x:type-native obj))
                      js/+grammar+
                      '{:module {:id JS.core
                                 :link {- JS.core}}})
  => '(do
        (when (== obj nil)
          (return nil))
        (var t := (typeof obj))
        (if (== t "object")
          (cond
            (Array.isArray obj)
            (return "array")
            :else
            (do
              (var tn := (. obj ["constructor"] ["name"]))
              (if (== tn "Object")
                (return "object")
                (return tn))))
          (return t)))

  (process-value-form '(g (x:type-native obj))
                      js/+grammar+
                      '{:module {:id JS.core
                                 :link {- JS.core}}})
  => '(g ((fn [value]
            (do
              (when (== value nil)
                (return nil))
              (var t := (typeof value))
              (if (== t "object")
                (cond
                  (Array.isArray value)
                  (return "array")
                  :else
                  (do
                    (var tn := (. value ["constructor"] ["name"]))
                    (if (== tn "Object")
                      (return "object")
                      (return tn))))
                (return t))))
          obj)))
