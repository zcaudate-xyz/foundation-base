(ns hara.runtime.basic.impl.process-julia-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :julia
  hara.runtime.basic.impl.process-julia-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-julia/CANARY :added "4.0"}
(fact "starts the julia verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/julia]))

(fact:global
 {:skip (not (env/program-exists? "julia"))})

^{:refer hara.runtime.basic.impl.process-julia/!.julia :added "4.0"}
(fact "validates a simple julia expression through the runtime"
  (do (defrun.julia test-expr [] (+ 1 2 3))
      (string? (!.julia test-expr)))
  => true)
