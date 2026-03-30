(ns rt.basic.type-twostep-gcc-test
  (:use code.test)
  (:require [rt.basic.type-common :as common]
            [std.lang :as l]))

(l/script- :c
  {:runtime :twostep})

(def CANARY-GCC
  (common/program-exists? "gcc"))

(defn.c ^{:- [:int]}
  add-10
  [:int x]
  (return (+ x 10)))

(defn.c ^{:- [:int]}
  add-20
  [:int x]
  (return (+ x 20)))

(fact "gcc twostep can return values"
  (if CANARY-GCC
    [(!.c
       (+ 1 2 3))

     (add-10 6)

     (!.c
       (-/add-20 (-/add-10 6)))

     (!.c
       (-/add-20 10))]
    :gcc-unavailable)
  => (any [6 16 36 30]
           :gcc-unavailable))
