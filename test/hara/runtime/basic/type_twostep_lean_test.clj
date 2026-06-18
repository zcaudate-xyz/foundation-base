(ns hara.runtime.basic.type-twostep-lean-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [hara.runtime.basic.impl-annex.process-lean]
            [hara.lang :as l]))

(do hara.runtime.basic.impl-annex.process-lean/+lean-twostep+)

(l/script- :lean
  {:runtime :twostep :test-mode true})

(fact:global {:skip (not (env/program-exists? "lean")) :setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

(fact "lean twostep can return values"
  [(!.lean
     (+ 1 2 3))

   (!.lean
     (* (+ 2 3) 4))]
  => [6 20])
