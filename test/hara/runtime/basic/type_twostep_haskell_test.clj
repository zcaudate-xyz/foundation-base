(ns hara.runtime.basic.type-twostep-haskell-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [hara.runtime.basic.impl-annex.process-haskell]
            [hara.lang :as l]))

(do hara.runtime.basic.impl-annex.process-haskell/+haskell-twostep+)

(l/script- :haskell
  {:runtime :twostep})

(fact:global
 {:skip (not (env/program-exists? "ghc"))})

(fact "ghc twostep can return values"
  [(!.hs
     (+ 1 2 3))

   (!.hs
     (letrec [x 1
              y 2]
        (+ x y)))]
  => [6 3])
