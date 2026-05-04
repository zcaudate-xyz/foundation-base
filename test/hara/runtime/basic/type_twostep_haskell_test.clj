(ns hara.runtime.basic.type-twostep-haskell-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-haskell]
            [hara.runtime.basic.type-common :as common]
            [hara.lang :as l]))

(do hara.runtime.basic.impl-annex.process-haskell/+haskell-twostep+)

(l/script- :haskell
  {:runtime :twostep})

(def CANARY-GHC
  (common/program-exists? "ghc"))

(fact "ghc twostep can return values"
  (if CANARY-GHC
    [(!.hs
       (+ 1 2 3))

     (!.hs
       (letrec [x 1
                y 2]
          (+ x y)))]
     :ghc-unavailable)
  => (any [6 3]
           :ghc-unavailable))
