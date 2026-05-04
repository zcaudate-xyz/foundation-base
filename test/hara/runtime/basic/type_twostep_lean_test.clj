(ns hara.runtime.basic.type-twostep-lean-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-lean]
            [hara.runtime.basic.type-common :as common]
            [hara.lang :as l]))

(do hara.runtime.basic.impl-annex.process-lean/+lean-twostep+)

(l/script- :lean
  {:runtime :twostep})

(def CANARY-LEAN
  (common/program-exists? "lean"))

(fact "lean twostep can return values"
  (if CANARY-LEAN
    [(!.lean
       (+ 1 2 3))

     (!.lean
       (* (+ 2 3) 4))]
    :lean-unavailable)
  => (any [6 20]
          :lean-unavailable))
