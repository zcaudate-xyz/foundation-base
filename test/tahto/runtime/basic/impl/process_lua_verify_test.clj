(ns tahto.runtime.basic.impl.process-lua-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :lua
  tahto.runtime.basic.impl.process-lua-verify-test
  {:runtime :verify})

^{:refer tahto.runtime.basic.impl.process-lua/CANARY :added "4.0"}
(fact "starts the lua verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/lua]))

(fact:global
 {:skip (not (env/program-exists? "luac"))})

^{:refer tahto.runtime.basic.impl.process-lua/!.lua :added "4.0"}
(fact "validates a simple lua expression through the runtime"
  (do (defrun.lua test-expr []
        (+ 1 2 3))
      (string? (!.lua test-expr)))
  => true)
