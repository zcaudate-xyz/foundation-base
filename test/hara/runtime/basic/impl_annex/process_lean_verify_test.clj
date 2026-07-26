tahto/runtime/basic/impl_annex/process_lean_verify_test.clj:1:(ns tahto.runtime.basic.impl_annex.process-lean-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :lean
tahto/runtime/basic/impl_annex/process_lean_verify_test.clj:8:  tahto.runtime.basic.impl_annex.process-lean-verify-test
  {:runtime :verify})

tahto/runtime/basic/impl_annex/process_lean_verify_test.clj:11:^{:refer tahto.runtime.basic.impl_annex.process-lean/CANARY :added "4.0"}
(fact "starts the lean verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/lean]))

(fact:global
 {:skip (not (env/program-exists? "lean"))})

tahto/runtime/basic/impl_annex/process_lean_verify_test.clj:19:^{:refer tahto.runtime.basic.impl_annex.process-lean/!.lean :added "4.0"}
(fact "validates a simple lean expression through the runtime"
  (do (defn.lean test-expr [] (+ 1 2 3))
      (string? (!.lean (test-expr))))
  => true)
