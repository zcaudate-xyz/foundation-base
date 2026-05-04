(ns hara.runtime.solidity-test
  (:require [hara.runtime.solidity :as s]
            [hara.runtime.solidity.client :as client]
            [hara.runtime.solidity.compile-common :as compile-common]
            [hara.runtime.solidity.env-ganache :as env]
            [hara.lang :as l])
  (:use code.test))

(l/script- :solidity
  {:runtime :web3
   :require [[hara.runtime.solidity :as s]]
   :static  {:contract ["Hello"]}})

;; Removed global setup

^{:refer hara.runtime.solidity/exec-rt-web3 :added "4.0"}
(fact "helper function for executing a command via node"
  (with-redefs [l/rt (fn [_] {:runtime :web3})
                client/stop-web3 (fn [_] nil)]
    (s/exec-rt-web3 nil (fn [_] :ok)))
  => :ok)

^{:refer hara.runtime.solidity/rt:print :added "4.0"}
(comment "prints out the contract"
  ;; Prints
  )

^{:refer hara.runtime.solidity/rt:deploy-ptr :added "4.0"}
(fact "deploys a ptr a contract"
  ;; Requires web3
  )

^{:refer hara.runtime.solidity/rt:deploy :added "4.0"}
(fact "deploys current namespace as contract"
  ;; Requires web3
  )

^{:refer hara.runtime.solidity/rt:contract :added "4.0"}
(fact "gets the contract"
  ;; Requires web3
  )

^{:refer hara.runtime.solidity/rt:bytecode-size :added "4.0"}
(fact "gets the bytecode size"
  ;; Requires web3
  )

(comment
  (s/rt-get-contract-address)
  (s/rt-ge))
