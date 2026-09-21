(ns pune.common.data-swap-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script- :js
  {:runtime :basic
   :require [[pune.common.data-swap :as ut-swap]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]}) 

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +fprices+
  [0.00000001
   0.00000021
   0.00001
   0.00021
   0.1
   2.1
   100
   2100
   10000
   2100000])

(def +vprices+
  [[0 1]
   [0 21]
   [0 1000]
   [0 21000]
   [3 10000]
   [4 21000]
   [6 10000]
   [7 21000]
   [8 10000]
   [10 21000]])

^{:refer pune.common.data-swap/fprice-to-vprice :added "0.1"}
(fact "float price to vector price"
  ^:hidden

  (mapv ut-swap/fprice-to-vprice
        +fprices+)
  => +vprices+)

^{:refer pune.common.data-swap/vprice-to-fprice :added "0.1"
  :setup []}
(fact "vector price to float price"
  ^:hidden
  
  (mapv (comp double ut-swap/vprice-to-fprice)
        +vprices+)
  => (contains [1.0E-8 2.1E-7 1.0E-5 2.1E-4 (approx 0.1) (approx 2.1) 100.0 2100.0 10000.0 2100000.0])
  
  ;; reconstructable
  (mapv ut-swap/fprice-to-vprice
        (mapv (comp double ut-swap/vprice-to-fprice)
              +vprices+))
  => +vprices+)

^{:refer pune.common.data-swap/vprice-to-position :added "0.1"}
(fact "vector price to trade position"
  ^:hidden
  
  (mapv ut-swap/vprice-to-position
        +vprices+)
  => [1 21 1000 21000 310000 421000 610000 721000 810000 1021000]

  (mapv ut-swap/position-to-vprice
        (mapv ut-swap/vprice-to-position
              +vprices+))
  => +vprices+)

^{:refer pune.common.data-swap/fprice-to-position :added "0.1"}
(fact "float price to trade position"
  ^:hidden

  (mapv ut-swap/fprice-to-position
        +fprices+)
  => [1 21 1000 21000 310000 421000 610000 721000 810000 1021000]

  (mapv (comp double ut-swap/position-to-fprice)
        (mapv ut-swap/fprice-to-position
              +fprices+))
  => (contains [1.0E-8 2.1E-7 1.0E-5 2.1E-4 (approx 0.1) (approx 2.1) 100.0 2100.0 10000.0 2100000.0]))

^{:refer pune.common.data-swap/position-to-vprice :added "0.1"}
(fact "trade position to vector price")

^{:refer pune.common.data-swap/position-to-fprice :added "0.1"}
(fact "trade position to float price")

