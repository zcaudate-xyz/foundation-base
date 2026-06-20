(ns hara.runtime.basic.type-twostep-gcc-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [hara.lang :as l]))

(l/script- :c
  {:runtime :twostep})

(fact:global {:skip (not (env/program-exists? "gcc")) :setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

(defn.c ^{:- [:int]}
  add-10
  [:int x]
  (return (+ x 10)))

(defn.c ^{:- [:int]}
  add-20
  [:int x]
  (return (+ x 20)))

(fact "gcc twostep can return values"
  [(!.c
     (+ 1 2 3))

   (add-10 6)

   (!.c
     (-/add-20 (-/add-10 6)))

   (!.c
     (-/add-20 10))]
  => [6 16 36 30])
