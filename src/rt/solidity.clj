(ns rt.solidity
  (:require [rt.solidity.client :as client]
            [rt.solidity.compile-common :as compile-common]
            [rt.solidity.compile-deploy :as compile-deploy]
            [rt.solidity.compile-node :as compile-node]
            [rt.solidity.compile-solc :as compile-solc]
            [rt.solidity.env-ganache :as env-ganache]
            [rt.solidity.grammar :as grammar]
            [rt.solidity.script.builtin :as builtin]
            [rt.solidity.script.util :as util]
            [std.lang :as l]
            [std.lib.env]
            [std.lib.foundation])
  (:refer-clojure :exclude [assert require bytes]))

(std.lib.foundation/intern-all rt.solidity.script.util)

(std.lib.foundation/intern-in [rt:start-ganache-server env-ganache/start-ganache-server]
             [rt:stop-ganache-server env-ganache/stop-ganache-server]

             compile-common/with:caller-address
             compile-common/with:caller-payment
             compile-common/with:caller-private-key
             compile-common/with:contract-address
             compile-common/with:gas-limit
             compile-common/with:clean
             compile-common/with:url
             compile-common/with:params
             compile-common/with:temp
             compile-common/with:suppress-errors
             compile-common/with:open-methods
             compile-common/with:closed-methods
             compile-common/with:stringify
             
             compile-node/with:measure
             compile-node/rt:send-wei
             compile-node/rt:node-ping
             compile-node/rt:node-past-events
             compile-node/rt:node-get-balance
             compile-node/rt:node-get-block-number
             compile-node/rt:node-eval

             compile-node/rt-get-contract
             compile-node/rt-get-contract-address
             compile-node/rt-get-caller-private-key
             compile-node/rt-get-caller-address
             compile-node/rt-get-id
             compile-node/rt-get-node
             compile-node/rt-set-contract
             
             compile-solc/create-module-entry
             compile-solc/create-pointer-entry
             
             client/rt-web3
             client/rt-web3:create
             client/start-web3
             client/stop-web3)

(defn ^{:style/indent 1}
  exec-rt-web3
  "helper function for executing a command via node"
  {:added "4.0"}
  [rt f]
  (let [rt (or rt (l/rt :solidity))
        is-web3 (= :web3 (:runtime rt))
        rt (if is-web3
             rt
             (client/rt-web3 {:lang :solidity}))
        [ok output] (try
                      [true (f rt)]
                      (catch Throwable t
                        [false t]))
        _  (when (not is-web3)
             (client/stop-web3 rt))]
    (if ok
      output
      (throw output))))

(defn rt:print
  "prints out the contract"
  {:added "4.0"}
  [& [m no-lines]]
  (let [code (if (and (:module m)
                      (:id m))
               (compile-solc/compile-ptr-code m)
               (compile-solc/compile-module-code m))]
    (if (or no-lines (:no-lines m))
      (std.lib.env/p  code)
      (std.lib.env/pl code))))

(defn rt:deploy-ptr
  "deploys a ptr a contract"
  {:added "4.0"}
  [ptr & [rt]]
  (exec-rt-web3 rt
    (fn [rt]
      (compile-deploy/deploy-pointer
       (:node rt)
       (compile-common/get-url rt)
       ptr))))

(defn rt:deploy
  "deploys current namespace as contract"
  {:added "4.0"}
  [& [m rt]]
  (exec-rt-web3 rt
    (fn [rt]
      (compile-deploy/deploy-module
       (:node rt)
       (compile-common/get-url rt)
       m))))

(defn rt:contract
  "gets the contract"
  {:added "4.0"}
  [& [input refresh rt]]
  (exec-rt-web3 rt
    (fn [rt]
      (let [f (if (:lang input)
                compile-solc/create-pointer-entry
                compile-solc/create-module-entry)]
        (f (:node rt) input refresh)))))

(defn rt:bytecode-size
  "gets the bytecode size"
  {:added "4.0"}
  [& [input refresh rt]]
  (let [contract (rt:contract input refresh rt)]
    (float (/ (count (:bytecode contract))
              2
              1024))))



