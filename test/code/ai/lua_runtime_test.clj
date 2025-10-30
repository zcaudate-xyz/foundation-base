;; test/code/ai/lua_runtime_test.clj
(ns test.code.ai.lua-runtime-test
  (:require [std.lang :as l]
            [std.lib :as h] ;; Keep h for h/once if it's needed for setup
            [rt.basic.type-basic :as basic]
            [std.lang.base.impl :as impl] ;; For emit-as
            [code.test :as test]
            [code.test.checker.common :as common]))

;; Define the basic Lua runtime
(def +lua-rt+ (basic/rt-basic {:lang :lua
                               :program :luajit}))

;; Emit the evaluate-string function to the runtime
(h/once
 (impl/emit-as +lua-rt+ '(lua-rt/evaluate-string)))

(test/facts "Lua Runtime Evaluation"

  (test/fact "evaluates valid lua string"
    (l/rt:invoke +lua-rt+ '(lua-rt/evaluate-string "return 1 + 1"))
    => {:status :success :result 2})

  (test/fact "handles lua errors"
    (l/rt:invoke +lua-rt+ '(lua-rt/evaluate-string "error('test error')"))
    => {:status :error :message "test error"})

  (test/fact "handles syntax errors"
    (l/rt:invoke +lua-rt+ '(lua-rt/evaluate-string "function("))
    => (common/satisfies (fn [res]
                           (and (= :error (:status res))
                                (common/includes? (:message res) "syntax error"))))))
