(ns hara.runtime.basic.impl.process-go-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :go
  hara.runtime.basic.impl.process-go-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-go/CANARY :added "4.0"}
(fact "starts the go verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/go]))

(fact:global
 {:skip (not (env/program-exists? "go"))})

^{:refer hara.runtime.basic.impl.process-go/!.go :added "4.0"}
(fact "validates a simple go expression through the runtime"
  (do (defn.go ^{:- [:int]} test-expr []
        (return (+ 1 2 3)))
      (string? (!.go (test-expr))))
  => true)
