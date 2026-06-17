(ns hara.runtime.basic.type-twostep-dart-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [hara.runtime.basic.type-common :as common]
            [hara.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as lib]]})

(fact:global
 {:skip (not (env/program-exists? "dart"))})

(defn.dt add-10
  [x]
  (return
   (x:add x 10)))

(defn.dt add-20
  [x]
  (return
   (x:add x 20)))

(fact "can return a value"
  [(!.dt
     (+ 1 2 3))

   (add-10 6)
   
   ^*(!.dt
       (-/add-20 (-/add-10 6)))
   
   ^*(!.dt
       (-/add-20 10))
   
    (!.dt
      (+ 3 (x:len "hello")))]
  => [6 16 36 30 8])
