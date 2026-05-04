(ns hara.runtime.solidity.script.util-test
  (:require [hara.runtime.solidity.env-hardhat :as env]
            [hara.lang :as l]
            [solidity.core.util :as util])
  (:use code.test))

(l/script- :solidity
  {:runtime :web3
   :require [[hara.runtime.solidity :as s]
              [solidity.core.util :as util]]})

(fact:global
 {:setup    [(l/rt:restart)
             (env/start-hardhat-server)]
  :teardown [(l/rt:stop)
             (env/stop-hardhat-server)]})

^{:refer hara.runtime.solidity.script.util/ut:str-comp :added "4.0"}
(fact "compares two strings together"

  (s/with:temp
    (util/ut:str-comp "123"
                      "456"))
  => false

  (s/with:temp
    (util/ut:str-comp "123"
                      "123"))
  => true)
