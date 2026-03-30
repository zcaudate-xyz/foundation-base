(ns rt.basic.type-twostep-dart-test
  (:use code.test)
  (:require [rt.basic.type-common :as common]
            [std.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as lib]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(defn.dt add-10
  [x]
  (return
   (x:add x 10)))

(defn.dt add-20
  [x]
  (return
   (x:add x 20)))

(fact "can return a value"
  (if CANARY-DART
    [(!.dt
       (+ 1 2 3))

     (add-10 6)
     
     ^*(!.dt
         (-/add-20 (-/add-10 6)))
     
     ^*(!.dt
         (-/add-20 10))
     
      (!.dt
        (+ 3 (lib/len "hello")))]
    :dart-unavailable)
  => (any [6 16 36 30 8]
          :dart-unavailable))
