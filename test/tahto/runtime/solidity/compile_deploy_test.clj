(ns tahto.runtime.solidity.compile-deploy-test
  (:require [tahto.runtime.solidity.client :as client]
            [tahto.runtime.solidity.compile-common :as compile-common]
            [tahto.runtime.solidity.compile-deploy :as deploy]
            [tahto.runtime.solidity.compile-solc :as compile]
            [tahto.runtime.solidity.env-hardhat :as env]
            [tahto.core :as l]
            [std.lib.component :as component]
            [web3.lib.example-erc20 :as example-erc20])
  (:use code.test))

(l/script- :solidity
  {:config  {:mode :clean}
   :require [[tahto.runtime.solidity :as s]]})

(defn.sol ^{:- [:pure :internal]
            :static/returns [:string :memory]}
  test:hello []
  (return "HELLO WORLD"))

(fact:global
 {:setup    [(l/rt:restart)
             (env/start-hardhat-server)]
  :teardown [(l/rt:stop)
             (env/stop-hardhat-server)]})

^{:refer tahto.runtime.solidity.compile-deploy/deploy-base :added "4.0"
  :setup    [(def +rt+
              (compile/compile-rt-prep))
              (compile/compile-rt-eval
               +rt+
               '((fn []
                   (:= (!:G ethers) (require "ethers"))
                   (return "ready"))))]
  :teardown [(component/stop +rt+)]}
(fact "deploy abi"

  (deploy/deploy-base +rt+
                      "http://127.0.0.1:8545"
                      (compile/create-pointer-entry +rt+ test:hello)
                      [])
  => (contains-in
      {"status" true, "contractAddress" string?}))

^{:refer tahto.runtime.solidity.compile-deploy/deploy-pointer :added "4.0"
  :setup    [(def +rt+
              (compile/compile-rt-prep))
              (compile/compile-rt-eval
               +rt+
               '((fn []
                   (:= (!:G ethers) (require "ethers"))
                   (return "ready"))))]
  :teardown [(component/stop +rt+)]}
(fact "deploys a pointer"

  (deploy/deploy-pointer +rt+
                         "http://127.0.0.1:8545"
                         test:hello)
  => (contains-in
      {"status" true, "contractAddress" string?}))

^{:refer tahto.runtime.solidity.compile-deploy/deploy-module :added "4.0"
  :setup    [(def +rt+
              (compile/compile-rt-prep))
              (compile/compile-rt-eval
               +rt+
               '((fn []
                   (:= (!:G ethers) (require "ethers"))
                   (return "ready"))))]}
(fact "deploys a namespace on the blockchain"

  (deploy/deploy-module +rt+
                        "http://127.0.0.1:8545")
  => (contains-in
      {"status" true, "contractAddress" string?}))
