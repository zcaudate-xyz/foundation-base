(ns rt.basic.type-basic-julia-test
  (:use code.test)
  (:require [rt.basic.type-common :as common]
            [std.lang.base.script :as script]
            [std.lang.model-annex.spec-julia :refer :all]))

(script/script- :julia
  {:runtime :basic})

(def CANARY-JULIA
  (common/program-exists? "julia"))

(defn.julia add-10 [x]
  (return (+ x 10)))

(defn.julia add-20 [x]
  (return (+ x 20)))

(fact "basic julia runtime can return values"

  (when CANARY-JULIA

    (!.julia
      (+ 1 2 3))


    (!.julia
      (pow 3 4))
    => 81
    
    (add-10 10)
    => 20

    (!.julia
      (-/add-10 (-/add-10 10)))
    => 30
    
    (!.julia
      (var add-10
           (fn [x]
             (return (+ x 10))))
      (add-10 6))
    => 16))

(comment
  (std.lang/rt:restart)
  )
