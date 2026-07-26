tahto/runtime/basic/type_basic_julia_test.clj:1:(ns tahto.runtime.basic.type-basic-julia-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [tahto.core :as l]
            [tahto.core.script :as script]
tahto/runtime/basic/type_basic_julia_test.clj:6:            [tahto.model.annex.spec-julia :refer :all]))

(script/script- :julia
  {:runtime :basic
   :test-mode true})

(fact:global
 {:skip (not (env/program-exists? "julia"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

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
  (tahto.core/rt:restart)
  )
