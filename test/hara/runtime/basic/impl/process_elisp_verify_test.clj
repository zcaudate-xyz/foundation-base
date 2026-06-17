(ns hara.runtime.basic.impl.process-elisp-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :elisp
  hara.runtime.basic.impl.process-elisp-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-elisp/CANARY :added "4.0"}
(fact "starts the elisp verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/elisp]))

(fact:global
 {:skip (not (env/program-exists? "emacs"))})

^{:refer hara.runtime.basic.impl.process-elisp/!.elisp :added "4.0"}
(fact "validates a simple elisp expression through the runtime"
  (do (defrun.elisp test-expr [] (+ 1 2 3))
      (string? (!.elisp test-expr)))
  => true)
