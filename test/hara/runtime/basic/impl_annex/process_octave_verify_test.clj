(ns hara.runtime.basic.impl_annex.process-octave-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :octave
  hara.runtime.basic.impl_annex.process-octave-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl_annex.process-octave/CANARY :added "4.0"}
(fact "starts the octave verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/octave]))

(fact:global
 {:skip (not (env/program-exists? "octave-cli"))})

^{:refer hara.runtime.basic.impl_annex.process-octave/!.octave :added "4.0"}
(fact "validates a simple octave expression through the runtime"
  (string? (!.octave (+ 1 2 3)))
  => true)
