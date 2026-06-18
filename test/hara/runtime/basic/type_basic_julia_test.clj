(ns hara.runtime.basic.type-basic-julia-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [hara.lang.script :as script]
            [hara.model.annex.spec-julia :refer :all]))

(script/script- :julia
  {:runtime :basic})

(fact:global {:skip (not (env/program-exists? "julia"))})

(defn.jl add-10 [x]
  (return (+ x 10)))

(defn.jl add-20 [x]
  (return (+ x 20)))

(fact "basic julia runtime can return values"

  (!.jl
    (+ 1 2 3))


  (!.jl
    (pow 3 4))
  => 81

  (add-10 10)
  => 20

  (!.jl
    (-/add-10 (-/add-10 10)))
  => 30

  (!.jl
    (var add-10
         (fn [x]
           (return (+ x 10))))
    (add-10 6))
  => 16)

(comment
  (hara.lang/rt:restart)
  )
