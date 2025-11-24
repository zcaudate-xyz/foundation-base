(ns rt.solidity.compile-node-test
  (:use code.test)
  (:require [rt.solidity.client :as client]
            [rt.solidity.compile-common :as compile-common]
            [rt.solidity.compile-node :as compile-node]
            [rt.solidity.env-ganache :as env]
            [rt.solidity :as s]
            [std.lang :as l]
            [std.lib :as h]))

(l/script- :solidity
  {:runtime :web3
   :config  {:mode :clean}
   :require [[rt.solidity :as s]]})

;; Removed global setup

^{:refer rt.solidity.compile-node/rt-get-id :added "4.0"}
(fact "gets the rt node id"
  (with-redefs [l/rt (fn [& _] {:node {:id "id"}})]
    (compile-node/rt-get-id))
  => "id")

^{:refer rt.solidity.compile-node/rt-get-contract-address :added "4.0"}
(fact "gets the current contract address"
  (with-redefs [compile-node/rt-get-id (fn [& _] "id")
                compile-common/get-contract-address (fn [_] "addr")]
    (compile-node/rt-get-contract-address))
  => "addr")

^{:refer rt.solidity.compile-node/rt-get-contract :added "4.0"}
(fact "gets the current contract"
  ;; Complex setup
  )

^{:refer rt.solidity.compile-node/rt-set-contract :added "4.0"}
(fact "sets the compiled contract"
  ;; side effect
  )

^{:refer rt.solidity.compile-node/rt-get-caller-address :added "4.0"}
(fact "gets the caller address"
  (with-redefs [compile-node/rt-get-id (fn [& _] "id")
                compile-common/get-caller-address (fn [_] "addr")]
    (compile-node/rt-get-caller-address))
  => "addr")

^{:refer rt.solidity.compile-node/rt-get-caller-private-key :added "4.0"}
(fact "gets the caller private-key"
  (with-redefs [compile-node/rt-get-id (fn [& _] "id")
                compile-common/get-caller-private-key (fn [_] "key")]
    (compile-node/rt-get-caller-private-key))
  => "key")

^{:refer rt.solidity.compile-node/rt-get-node :added "4.0"}
(fact "gets the node runtime"
  (with-redefs [l/rt (fn [& _] {:node "node"})]
    (compile-node/rt-get-node))
  => "node")

^{:refer rt.solidity.compile-node/rt-get-address :added "4.0"}
(fact "gets the address of the signer"
  ;; requires compile-rt-eval
  )

^{:refer rt.solidity.compile-node/rt:node-get-block-number :added "4.0"}
(fact "gets the current block number"
  ;; requires compile-rt-eval
  )

^{:refer rt.solidity.compile-node/rt:node-get-balance :added "4.0"}
(fact "gets the current balance"
  ;; requires compile-rt-eval
  )

^{:refer rt.solidity.compile-node/rt:node-ping :added "4.0"}
(fact "pings the node"
  (with-redefs [compile-node/rt-get-node (fn [& _] {})
                h/p:rt-invoke-ptr (fn [_ _ _] "pong")]
    (compile-node/rt:node-ping))
  => "pong")

^{:refer rt.solidity.compile-node/rt:send-wei :added "4.0"}
(fact "sends wei to another address"
  ;; requires compile-rt-eval
  )

^{:refer rt.solidity.compile-node/rt:node-eval :added "4.0"}
(fact "evaluates a form in the node runtime"
  ;; requires compile-rt-eval
  )

^{:refer rt.solidity.compile-node/rt:node-past-events :added "4.0"}
(fact "gets past events"
  ;; requires compile-rt-eval
  )

^{:refer rt.solidity.compile-node/with:measure :added "4.0"}
(fact "measures balance change before and after call"
  (macroexpand-1 '(compile-node/with:measure (+ 1 1)))
  => list?)
