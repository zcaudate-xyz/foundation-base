(ns rt.basic.type-twostep-dart-test
  (:use code.test)
  (:require [std.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as lib]]})


(defn.dt add-10
  [x]
  (return
   (x:add x 10)))

(fact "can return a value"
  
  (!.dt
    (+ 1 2 3))
  => 6

  (add-10 6)
  => 16

  (!.dt
    (+ 3 (lib/len "hello")))
  => 8)

