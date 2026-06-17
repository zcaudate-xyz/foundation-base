(ns hara.runtime.basic.impl.process-xtalk-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :xtalk
  hara.runtime.basic.impl.process-xtalk-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-xtalk/CANARY :added "4.0"}
(fact "starts the xtalk verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/xtalk]))

(fact:global
 {:skip (not (env/program-exists? "chez"))})

^{:refer hara.runtime.basic.impl.process-xtalk/!.xt :added "4.0"}
(fact "validates a simple xtalk expression through the runtime"
  (do (defrun.xt test-expr [] (+ 1 2 3))
      (string? (!.xt test-expr)))
  => true)
