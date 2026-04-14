(ns rt.basic.type-twostep-haskell-test
  (:use code.test)
  (:require [rt.basic.impl-annex.process-haskell]
            [rt.basic.type-common :as common]
            [std.lang :as l]))

(do rt.basic.impl-annex.process-haskell/+haskell-twostep+)

(l/script- :haskell
  {:runtime :twostep})

(def CANARY-GHC
  (common/program-exists? "ghc"))

(fact "ghc twostep can return values"
  (if CANARY-GHC
    [(!.hs
       (+ 1 2 3))

     (!.hs
       (let [x 1
             y 2]
         (+ x y)))]
    :ghc-unavailable)
  => (any [6 3]
          :ghc-unavailable))
