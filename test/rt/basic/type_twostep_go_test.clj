(ns rt.basic.type-twostep-go-test
  (:use code.test)
  (:require [rt.basic.type-common :as common]
            [std.lang :as l]))

(l/script- :go
  {:runtime :twostep})

(def CANARY-GO
  (common/program-exists? "go"))

(defn.go ^{:- [:int]}
  add-10
  [:int x]
  (return (+ x 10)))

(defn.go ^{:- [:int]}
  add-20
  [:int x]
  (return (+ x 20)))

(fact "can return a value"
  (if CANARY-GO
    [(!.go
       (+ 1 2 3))
     
     (add-10 6)
     
     (!.go
       (-/add-20 (-/add-10 6)))
     
     (!.go
       (-/add-20 10))

     (!.go
       (+ 3 (x-len "hello")))]
    :go-unavailable)
  => (any [6 16 36 30 8]
          :go-unavailable))
