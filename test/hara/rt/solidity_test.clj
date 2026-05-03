(ns hara.rt.solidity-test
  (:require [hara.rt.solidity :as s]
            [hara.rt.solidity.client :as client]
            [hara.rt.solidity.compile-common :as compile-common]
            [hara.rt.solidity.env-ganache :as env]
            [hara.lang :as l])
  (:use code.test))

(l/script- :solidity
  {:runtime :web3
   :require [[hara.rt.solidity :as s]]
   :static  {:contract ["Hello"]}})

;; Removed global setup

^{:refer hara.rt.solidity/exec-rt-web3 :added "4.0"}
(fact "helper function for executing a command via node"
  (with-redefs [l/rt (fn [_] {:runtime :web3})
                client/stop-web3 (fn [_] nil)]
    (s/exec-rt-web3 nil (fn [_] :ok)))
  => :ok)

^{:refer hara.rt.solidity/rt:print :added "4.0"}
(comment "prints out the contract"
  ;; Prints
  )

^{:refer hara.rt.solidity/rt:deploy-ptr :added "4.0"}
(fact "deploys a ptr a contract"
  ;; Requires web3
  )

^{:refer hara.rt.solidity/rt:deploy :added "4.0"}
(fact "deploys current namespace as contract"
  ;; Requires web3
  )

^{:refer hara.rt.solidity/rt:contract :added "4.0"}
(fact "gets the contract"
  ;; Requires web3
  )

^{:refer hara.rt.solidity/rt:bytecode-size :added "4.0"}
(fact "gets the bytecode size"
  ;; Requires web3
  )

(comment
  (s/rt-get-contract-address)
  (s/rt-ge))
