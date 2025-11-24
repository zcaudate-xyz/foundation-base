(ns rt.solidity-test
  (:use code.test)
  (:require [rt.solidity.client :as client]
            [rt.solidity.compile-common :as compile-common]
            [rt.solidity.env-ganache :as env]
            [rt.solidity :as s]
            [std.lang :as l]
            [std.lib :as h]))

(l/script- :solidity
  {:runtime :web3
   :require [[rt.solidity :as s]]
   :static  {:contract ["Hello"]}})

;; Removed global setup

^{:refer rt.solidity/exec-rt-web3 :added "4.0"}
(fact "helper function for executing a command via node"
  (with-redefs [l/rt (fn [_] {:runtime :web3})
                client/stop-web3 (fn [_] nil)]
    (s/exec-rt-web3 nil (fn [_] :ok)))
  => :ok)

^{:refer rt.solidity/rt:print :added "4.0"}
(comment "prints out the contract"
  ;; Prints
  )

^{:refer rt.solidity/rt:deploy-ptr :added "4.0"}
(fact "deploys a ptr a contract"
  ;; Requires web3
  )

^{:refer rt.solidity/rt:deploy :added "4.0"}
(fact "deploys current namespace as contract"
  ;; Requires web3
  )

^{:refer rt.solidity/rt:contract :added "4.0"}
(fact "gets the contract"
  ;; Requires web3
  )

^{:refer rt.solidity/rt:bytecode-size :added "4.0"}
(fact "gets the bytecode size"
  ;; Requires web3
  )

(comment
  (s/rt-get-contract-address)
  (s/rt-ge))
