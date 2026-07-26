tahto/runtime/basic/impl_annex/process_matlab_verify_test.clj:1:(ns tahto.runtime.basic.impl_annex.process-matlab-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :matlab
  {:runtime :verify})

tahto/runtime/basic/impl_annex/process_matlab_verify_test.clj:10:^{:refer tahto.runtime.basic.impl_annex.process-matlab/CANARY :added "4.0"}
(fact "starts the matlab verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/matlab]))

(fact:global
 {:skip (not (env/program-exists? "octave-cli"))})

tahto/runtime/basic/impl_annex/process_matlab_verify_test.clj:18:^{:refer tahto.runtime.basic.impl_annex.process-matlab/!.matlab :added "4.0"}
(fact "validates a simple matlab expression through the runtime"

  (string? (!.mat (+ 1 2 3)))
  => true)
