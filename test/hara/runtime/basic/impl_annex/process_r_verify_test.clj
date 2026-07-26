tahto/runtime/basic/impl_annex/process_r_verify_test.clj:1:(ns tahto.runtime.basic.impl_annex.process-r-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :r
tahto/runtime/basic/impl_annex/process_r_verify_test.clj:8:  tahto.runtime.basic.impl_annex.process-r-verify-test
  {:runtime :verify})

tahto/runtime/basic/impl_annex/process_r_verify_test.clj:11:^{:refer tahto.runtime.basic.impl_annex.process-r/CANARY :added "4.0"}
(fact "starts the r verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/r]))

(fact:global
 {:skip (not (env/program-exists? "Rscript"))})

tahto/runtime/basic/impl_annex/process_r_verify_test.clj:19:^{:refer tahto.runtime.basic.impl_annex.process-r/!.r :added "4.0"}
(fact "validates a simple r expression through the runtime"
  (do (defrun.R test-expr [] (+ 1 2 3))
      (string? (!.R test-expr)))
  => true)
