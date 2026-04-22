(ns rt.solidity.compile-deploy-test
  (:require [rt.solidity.client :as client]
            [rt.solidity.compile-common :as compile-common]
            [rt.solidity.compile-deploy :as deploy]
            [rt.solidity.compile-solc :as compile]
            [rt.solidity.env-ganache :as env]
            [std.lang :as l]
            [std.lib.component :as component]
            [web3.lib.example-erc20 :as example-erc20])
  (:use code.test))

(l/script- :solidity
  {:config  {:mode :clean}
   :require [[rt.solidity :as s]]})

(defn.sol ^{:- [:pure :internal]
            :static/returns [:string :memory]}
  test:hello []
  (return "HELLO WORLD"))

(fact:global
 {:setup    [(l/rt:restart)
             (env/start-ganache-server)]
  :teardown [(l/rt:stop)
             (env/stop-ganache-server)]})

^{:refer rt.solidity.compile-deploy/deploy-base :added "4.0"
  :setup    [(def +rt+
               (compile/compile-rt-prep))
             (compile/compile-rt-eval
              +rt+
              '[(:= (!:G ethers) (require "ethers"))])]
  :teardown [(component/stop +rt+)]}
(fact "deploy abi"

  (deploy/deploy-base +rt+
                      "http://127.0.0.1:8545"
                      (compile/create-pointer-entry +rt+ test:hello)
                      [])
  => (contains-in
      {"status" true, "contractAddress" string?}))

^{:refer rt.solidity.compile-deploy/deploy-pointer :added "4.0"
  :setup    [(def +rt+
               (compile/compile-rt-prep))
             (compile/compile-rt-eval
              +rt+
              '[(:= (!:G ethers) (require "ethers"))])]
  :teardown [(component/stop +rt+)]}
(fact "deploys a pointer"

  (deploy/deploy-pointer +rt+
                         "http://127.0.0.1:8545"
                         test:hello)
  => (contains-in
      {"status" true, "contractAddress" string?})


  (deploy/deploy-pointer +rt+
                         "http://127.0.0.1:8545"
                         example-erc20/get-account-balance)
  => (contains-in
      {"status" true, "contractAddress" string?}))

^{:refer rt.solidity.compile-deploy/deploy-module :added "4.0"
  :setup    [(def +rt+
               (compile/compile-rt-prep))
             (compile/compile-rt-eval
              +rt+
              '[(:= (!:G ethers) (require "ethers"))])]}
(fact "deploys a namespace on the blockchain"

  (deploy/deploy-module +rt+
                        "http://127.0.0.1:8545"
                        web3.lib.example-erc20/+default-contract+)
  => (contains-in
      {"status" true, "contractAddress" string?}))
