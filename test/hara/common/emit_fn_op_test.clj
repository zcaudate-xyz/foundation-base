tahto/common/emit_fn_op_test.clj:1:(ns tahto.common.emit-fn-op-test
tahto/common/emit_fn_op_test.clj:2:  (:require [tahto.common.emit-common :as common]
tahto/common/emit_fn_op_test.clj:3:            [tahto.common.emit-fn :as fn]
tahto/common/emit_fn_op_test.clj:4:            [tahto.common.emit-helper :as helper]
tahto/common/emit_fn_op_test.clj:5:            [tahto.common.grammar :as grammar]
            [std.string.prose :as prose])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

tahto/common/emit_fn_op_test.clj:16:^{:refer tahto.common.emit-block/test-fn-emit.fn :adopt true :added "4.0"}
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

tahto/common/emit_fn_op_test.clj:48:^{:refer tahto.common.emit-block/test-fn-emit.name :adopt true :added "4.0"}
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

