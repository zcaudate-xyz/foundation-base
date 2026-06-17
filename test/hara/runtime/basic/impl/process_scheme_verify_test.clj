(ns hara.runtime.basic.impl.process-scheme-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :scheme
  hara.runtime.basic.impl.process-scheme-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-scheme/CANARY :added "4.0"}
(fact "starts the scheme verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/scheme]))

(fact:global
 {:skip (not (env/program-exists? "racket"))})

^{:refer hara.runtime.basic.impl.process-scheme/!.scheme :added "4.0"}
(fact "validates a simple scheme expression through the runtime"
  (do (defrun.scheme test-expr [] (+ 1 2 3))
      (string? (!.scheme test-expr)))
  => true)
