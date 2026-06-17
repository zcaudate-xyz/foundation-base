(ns haskell.live-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-haskell]
            [haskell.core :as h]
            [std.lib.env :as env])
  (:use code.test))

(fact:global
 {:skip (not (env/program-exists? "ghc"))})

(l/script- :haskell
  {:runtime :twostep
   :require [[haskell.core :as h]]})

(fact "live haskell prelude calls work"
  (!.hs (sum [1 2 3 4]))
  => 10

  (!.hs (length [1 2 3 4]))
  => 4

  (!.hs (map succ [1 2 3 4]))
  => [2 3 4 5]

  (!.hs (filter (fn [x] (> x 2)) [1 2 3 4]))
  => [3 4])

(fact "local haskell functions can be defined inline"
  (!.hs (letrec [add (fn [a b] (+ a b))]
          (add 1 2)))
  => 3)
