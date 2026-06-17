(ns hara.runtime.basic.type-basic-julia-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [hara.lang.script :as script]
            [hara.model.annex.spec-julia :refer :all]))

(script/script- :julia
  {:runtime :basic})

(fact:global {:skip (not (env/program-exists? "julia"))})

(defn.julia add-10 [x]
  (return (+ x 10)))

(defn.julia add-20 [x]
  (return (+ x 20)))

(fact "basic julia runtime can return values"

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
  => 16)

(comment
  (hara.lang/rt:restart)
  )
