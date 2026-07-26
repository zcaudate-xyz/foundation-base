tahto/runtime/basic/impl/process_julia_verify_test.clj:1:(ns tahto.runtime.basic.impl.process-julia-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :julia
tahto/runtime/basic/impl/process_julia_verify_test.clj:8:  tahto.runtime.basic.impl.process-julia-verify-test
  {:runtime :verify})

tahto/runtime/basic/impl/process_julia_verify_test.clj:11:^{:refer tahto.runtime.basic.impl.process-julia/CANARY :added "4.0"}
(fact "starts the julia verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/julia]))

(fact:global
 {:skip (not (env/program-exists? "julia"))})

tahto/runtime/basic/impl/process_julia_verify_test.clj:19:^{:refer tahto.runtime.basic.impl.process-julia/!.jl :added "4.0"}
(fact "validates a simple julia expression through the runtime"
  (do (defrun.jl test-expr [] (+ 1 2 3))
      (string? (!.jl test-expr)))
  => true)
