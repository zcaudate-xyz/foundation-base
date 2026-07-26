tahto/runtime/solidity/script/util_test.clj:1:(ns tahto.runtime.solidity.script.util-test
tahto/runtime/solidity/script/util_test.clj:2:  (:require [tahto.runtime.solidity.env-hardhat :as env]
            [tahto.core :as l]
            [solidity.core.util :as util])
  (:use code.test))

(l/script- :solidity
  {:runtime :web3
tahto/runtime/solidity/script/util_test.clj:9:   :require [[tahto.runtime.solidity :as s]
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
