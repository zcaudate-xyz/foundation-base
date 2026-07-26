tahto/runtime/basic/type_twostep_haskell_test.clj:1:(ns tahto.runtime.basic.type-twostep-haskell-test
  (:use code.test)
  (:require [std.lib.env :as env]
tahto/runtime/basic/type_twostep_haskell_test.clj:4:            [tahto.runtime.basic.impl-annex.process-haskell]
            [tahto.core :as l]))

tahto/runtime/basic/type_twostep_haskell_test.clj:7:(do tahto.runtime.basic.impl-annex.process-haskell/+haskell-twostep+)

(l/script- :haskell
  {:runtime :twostep :test-mode true})

(fact:global {:skip (not (env/program-exists? "ghc")) :setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

(fact "ghc twostep can return values"
  [(!.hs
     (+ 1 2 3))

   (!.hs
     (letrec [x 1
              y 2]
        (+ x y)))]
  => [6 3])
