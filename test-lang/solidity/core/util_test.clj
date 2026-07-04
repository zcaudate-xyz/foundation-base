(ns solidity.core.util-test
  (:use code.test)
  (:require [hara.lang :as l]
            [solidity.core.util :refer :all]))

(l/script- :solidity
  {:require [[solidity.core.util :as u]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer solidity.core.util/ut:str-comp :added "4.1"}
(fact "compiles a string comparison call"
  (l/with:macro-opts [(l/rt:macro-opts :solidity)]
    (l/emit-str '[(solidity.core/ut:str-comp "a" "b")] {:lang :solidity}))
  => #"ut__str_comp")
