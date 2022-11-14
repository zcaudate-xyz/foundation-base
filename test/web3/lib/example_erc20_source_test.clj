(ns web3.lib.example-erc20-source-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [rt.solidity.env-ganache :as env]))

(l/script :solidity
  {:runtime :web3
   :require [[rt.solidity :as s]
             [web3.lib.example-erc20-source :as source]
             [web3.lib.example-erc20 :as erc20]]})

(fact:global
 {:setup [(l/rt:restart)
          (s/with:params {:caller-address (second env/+default-addresses+)
                          :caller-private-key (second env/+default-private-keys+)}
            (s/rt:deploy source/+default-contract+))]
  :teardown [(l/rt:stop)]})

^{:refer web3.lib.example-erc20-source/totalSupply :added "4.0"}
(fact "gets the totalSupply"
  ^:hidden
  
  (source/totalSupply)
  => integer?)

^{:refer web3.lib.example-erc20-source/balanceOf :added "4.0"
  :setup [(s/with:params {:caller-address (second env/+default-addresses+)
                          :caller-private-key (second env/+default-private-keys+)}
            (s/rt:deploy source/+default-contract+))]}
(fact "gets the balance of an address"
  ^:hidden
  
  (source/balanceOf (last env/+default-addresses+))
  => 0

  (source/balanceOf (second env/+default-addresses+))
  => integer?)

^{:refer web3.lib.example-erc20-source/transfer :added "4.0"
  :setup [(s/with:params {:caller-address (second env/+default-addresses+)
                          :caller-private-key (second env/+default-private-keys+)}
            (s/rt:deploy source/+default-contract+))]}
(fact "transfers to another balance"
  ^:hidden

  (s/with:params {:caller-address (second env/+default-addresses+)
                  :caller-private-key (second env/+default-private-keys+)}
    (s/with:measure
     (source/transfer (last env/+default-addresses+)
                      100)))
  => (contains-in
      [(approx 0.05 0.04) map?]))

^{:refer web3.lib.example-erc20-source/transferFrom :added "4.0"}
(fact "transfers from account, requires approval")

^{:refer web3.lib.example-erc20-source/approve :added "4.0"}
(fact "approves transfer for one account to another")
