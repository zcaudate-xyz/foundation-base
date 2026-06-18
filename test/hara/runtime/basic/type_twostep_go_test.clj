(ns hara.runtime.basic.type-twostep-go-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [hara.lang :as l]))

(l/script- :go
  {:runtime :twostep :test-mode true})

(fact:global {:skip (not (env/program-exists? "go")) :setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

(defn.go ^{:- [:int]}
  add-10
  [:int x]
  (return (+ x 10)))

(defn.go ^{:- [:int]}
  add-20
  [:int x]
  (return (+ x 20)))

(fact "can return a value"
  [(!.go
     (+ 1 2 3))
   
   (add-10 6)
   
   (!.go
     (-/add-20 (-/add-10 6)))
   
   (!.go
     (-/add-20 10))

    (!.go
      (+ 3 (x:len "hello")))]
  => [6 16 36 30 8])
