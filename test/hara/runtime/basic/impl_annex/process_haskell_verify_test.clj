(ns hara.runtime.basic.impl_annex.process-haskell-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :haskell
  hara.runtime.basic.impl_annex.process-haskell-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl_annex.process-haskell/CANARY :added "4.0"}
(fact "starts the haskell verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/haskell]))

(fact:global
 {:skip (not (env/program-exists? "ghc"))})

^{:refer hara.runtime.basic.impl_annex.process-haskell/!.hs :added "4.0"}
(fact "validates a simple haskell expression through the runtime"
  (do (defn.hs test-expr [] (+ 1 2 3))
      (string? (!.hs (test-expr))))
  => true)
