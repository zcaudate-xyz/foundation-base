(ns web3.lib.example-erc20-source-test
  (:require [rt.solidity.env-ganache :as env]
            [std.lang :as l])
  (:use code.test))

(l/script :solidity
  {:runtime :web3
   :require [[rt.solidity :as s]
             [web3.lib.example-erc20-source :as source]
             [web3.lib.example-erc20 :as erc20]]})

(fact:global
 {:setup [(s/rt:stop-ganache-server)
          (Thread/sleep 1000)
          (s/rt:start-ganache-server)
          (Thread/sleep 500)
          (l/rt:restart)
          (s/with:params {:caller-address (second env/+default-addresses+)
                          :caller-private-key (second env/+default-private-keys+)}
            (s/rt:deploy source/+default-contract+))]
  :teardown [(l/rt:stop)]})

^{:refer web3.lib.example-erc20-source/totalSupply :added "4.0"}
(fact "gets the totalSupply"

  (source/totalSupply)
  => integer?)

^{:refer web3.lib.example-erc20-source/balanceOf :added "4.0"
  :setup [(s/with:params {:caller-address (second env/+default-addresses+)
                          :caller-private-key (second env/+default-private-keys+)}
            (s/rt:deploy source/+default-contract+))]}
(fact "gets the balance of an address"

  (source/balanceOf (last env/+default-addresses+))
  => 0

  (source/balanceOf (second env/+default-addresses+))
  => integer?)

^{:refer web3.lib.example-erc20-source/transfer :added "4.0"
  :setup [(s/with:params {:caller-address (second env/+default-addresses+)
                          :caller-private-key (second env/+default-private-keys+)}
            (s/rt:deploy source/+default-contract+))]}
(fact "transfers to another balance"

  (s/with:params {:caller-address (second env/+default-addresses+)
                  :caller-private-key (second env/+default-private-keys+)}
    (s/with:measure
     (source/transfer (last env/+default-addresses+)
                      100)))
  => (contains-in
      [(approx 0.1 0.1) map?]))

^{:refer web3.lib.example-erc20-source/transferFrom :added "4.0"}
(fact "transfers from account, requires approval")

^{:refer web3.lib.example-erc20-source/approve :added "4.0"}
(fact "approves transfer for one account to another")
