(ns rt.basic.type-twostep-lean-test
  (:use code.test)
  (:require [rt.basic.impl-annex.process-lean]
            [rt.basic.type-common :as common]
            [std.lang :as l]))

(do rt.basic.impl-annex.process-lean/+lean-twostep+)

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
