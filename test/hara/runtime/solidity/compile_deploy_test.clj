(ns hara.runtime.solidity.compile-deploy-test
  (:require [hara.runtime.solidity.client :as client]
            [hara.runtime.solidity.compile-common :as compile-common]
            [hara.runtime.solidity.compile-deploy :as deploy]
            [hara.runtime.solidity.compile-solc :as compile]
            [hara.runtime.solidity.env-hardhat :as env]
            [hara.lang :as l]
            [std.lib.component :as component]
            [web3.lib.example-erc20 :as example-erc20])
  (:use code.test))

(l/script- :solidity
  {:config  {:mode :clean}
   :require [[hara.runtime.solidity :as s]]})

(defn.sol ^{:- [:pure :internal]
            :static/returns [:string :memory]}
  test:hello []
  (return "HELLO WORLD"))

(fact:global
 {:setup    [(l/rt:restart)
             (env/start-hardhat-server)]
  :teardown [(l/rt:stop)
             (env/stop-hardhat-server)]})

^{:refer hara.runtime.solidity.compile-deploy/deploy-base :added "4.0"
  :setup    [(def +rt+
              (compile/compile-rt-prep))
              (compile/compile-rt-eval
               +rt+
               '(do (:= (!:G ethers) (require "ethers"))
                    "ready"))]
  :teardown [(component/stop +rt+)]}
(fact "deploy abi"

  (deploy/deploy-base +rt+
                      "http://127.0.0.1:8545"
                      (compile/create-pointer-entry +rt+ test:hello)
                      [])
  => (contains-in
      {"status" true, "contractAddress" string?}))

^{:refer hara.runtime.solidity.compile-deploy/deploy-pointer :added "4.0"
  :setup    [(def +rt+
              (compile/compile-rt-prep))
              (compile/compile-rt-eval
               +rt+
               '(do (:= (!:G ethers) (require "ethers"))
                    "ready"))]
  :teardown [(component/stop +rt+)]}
(fact "deploys a pointer"

  (deploy/deploy-pointer +rt+
                         "http://127.0.0.1:8545"
                         test:hello)
  => (contains-in
      {"status" true, "contractAddress" string?}))

^{:refer hara.runtime.solidity.compile-deploy/deploy-module :added "4.0"
  :setup    [(def +rt+
              (compile/compile-rt-prep))
              (compile/compile-rt-eval
               +rt+
               '(do (:= (!:G ethers) (require "ethers"))
                    "ready"))]}
(fact "deploys a namespace on the blockchain"

  (deploy/deploy-module +rt+
                        "http://127.0.0.1:8545")
  => (contains-in
      {"status" true, "contractAddress" string?}))
