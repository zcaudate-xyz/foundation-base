(ns hara.runtime.basic.impl.process-c-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :c
  hara.runtime.basic.impl.process-c-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-c/CANARY :added "4.0"}
(fact "starts the c verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/c]))

(fact:global
 {:skip (not (env/program-exists? "gcc"))})

^{:refer hara.runtime.basic.impl.process-c/!.c :added "4.0"}
(fact "validates a simple c expression through the runtime"
  (do (defn.c ^{:- [:int]} test-expr []
        (return (+ 1 2 3)))
      (string? (!.c (test-expr))))
  => true)
