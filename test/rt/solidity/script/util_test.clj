(ns rt.solidity.script.util-test
  (:require [rt.solidity.env-ganache :as env]
            [std.lang :as l])
  (:use code.test))

(l/script- :solidity
  {:runtime :web3
   :require [[rt.solidity :as s]]})

(fact:global
 {:setup    [(l/rt:restart)
             (env/start-ganache-server)]
  :teardown [(l/rt:stop)
             (env/stop-ganache-server)]})

^{:refer rt.solidity.script.util/ut:str-comp :added "4.0"}
(fact "compares two strings together"
  ^:hidden

  (s/with:temp
    (s/ut:str-comp "123"
                   "456"))
  => false

  (s/with:temp
    (s/ut:str-comp "123"
                   "123"))
  => true)
