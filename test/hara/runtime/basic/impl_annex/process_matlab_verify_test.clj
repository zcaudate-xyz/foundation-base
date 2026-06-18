(ns hara.runtime.basic.impl_annex.process-matlab-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :matlab
  hara.runtime.basic.impl_annex.process-matlab-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl_annex.process-matlab/CANARY :added "4.0"}
(fact "starts the matlab verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/matlab]))

(fact:global
 {:skip (not (env/program-exists? "octave-cli"))})

^{:refer hara.runtime.basic.impl_annex.process-matlab/!.matlab :added "4.0"}
(fact "validates a simple matlab expression through the runtime"
  (string? (!.matlab (+ 1 2 3)))
  => true)
