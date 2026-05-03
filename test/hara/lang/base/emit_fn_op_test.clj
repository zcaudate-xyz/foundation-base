(ns hara.lang.base.emit-fn-op-test
  (:require [hara.lang.base.emit-common :as common]
            [hara.lang.base.emit-fn :as fn]
            [hara.lang.base.emit-helper :as helper]
            [hara.lang.base.grammar :as grammar]
            [std.string.prose :as prose])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer hara.lang.base.emit-block/test-fn-emit.fn :adopt true :added "4.0"}
(fact "emit do*"

  (fn/test-fn-loop '(fn [] (return (+ a 1)))
                   +grammar+
                   {})
  => (prose/|
      "function (){"
      "  (return (+ a 1));"
      "}")

  (fn/test-fn-loop '(fn:> (+ a 1))
                   +grammar+
                   {})
  => "(fn [] (return (+ a 1)))"

  (fn/test-fn-emit '(fn [] (return (+ a 1)))
                   +grammar+
                   {})
  => (prose/|
      "function (){"
      "  return a + 1;"
      "}")  

  (fn/test-fn-emit '(fn:> (+ a 1))
                         +grammar+
                         {})
  => (prose/|
      "function (){"
      "  return a + 1;"
      "}"))

^{:refer hara.lang.base.emit-block/test-fn-emit.name :adopt true :added "4.0"}
(fact "emit do*"

  (fn/test-fn-loop '(fn hello [a := 1
                                     b := (+ 1 2)]
                            (return (+ a 1)))
                         +grammar+
                         {})
  => (prose/|
      "function hello(a = 1,b = (+ 1 2)){"
      "  (return (+ a 1));"
      "}")

  (fn/test-fn-emit '(fn hello [a := 1
                                     b := (+ 1 2)]
                        (return (+ a 1)))
                     +grammar+
                     {})
  
  => (prose/| "function hello(a = 1,b = 1 + 2){"
                   "  return a + 1;"
                   "}"))

