(ns tahto.runtime.solidity.script.util-test
  (:require [tahto.runtime.solidity.env-hardhat :as env]
            [tahto.core :as l]
            [solidity.core.util :as util])
  (:use code.test))

(l/script- :solidity
  {:runtime :web3
   :require [[tahto.runtime.solidity :as s]
              [solidity.core.util :as util]] :test-mode true})

(fact:global
 {:setup    [(l/rt:restart)
             (env/start-hardhat-server)]
  :teardown [(l/rt:stop)
             (env/stop-hardhat-server)]})

^{:refer solidity.core.util/ut:str-comp :added "4.0"}
(fact "compares two strings together"

  (s/with:temp
    (util/ut:str-comp "123"
                      "456"))
  => false

  (s/with:temp
    (util/ut:str-comp "123"
                      "123"))
  => true)
