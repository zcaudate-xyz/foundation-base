(ns hara.runtime.basic.impl.process-bash-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :bash
  hara.runtime.basic.impl.process-bash-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-bash/CANARY :added "4.0"}
(fact "starts the bash verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/bash]))

(fact:global
 {:skip (not (env/program-exists? "bash"))})

^{:refer hara.runtime.basic.impl.process-bash/!.sh :added "4.0"}
(fact "validates a simple bash expression through the runtime"
  (do (defrun.sh test-expr []
        (+ 1 2 3))
      (string? (!.sh test-expr)))
  => true)
