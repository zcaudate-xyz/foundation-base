(ns xt.old.impl-bitop-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.impl-bitop :refer :all]))

^{:seedgen/root {:all true, :langs [:js]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.impl-bitop :as bitop]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.impl-bitop/bit-count :added "4.1"}
(fact "counts set bits in representative integers"
  (!.js
    [(bitop/bit-count 0)
     (bitop/bit-count 1)
     (bitop/bit-count 7)
     (bitop/bit-count 255)
     (bitop/bit-count 1023)])
  => [0 1 3 8 10])
