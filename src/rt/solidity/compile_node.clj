(ns rt.solidity.compile-node
  (:require [rt.solidity.compile-common :as compile-common]
            [rt.solidity.compile-solc :as compile-solc]
            [std.lib :as h]
            [std.lang :as l]
            [js.lib.eth-bench :as eth-bench]
            [js.lib.eth-lib :as eth-lib]))

(defn rt-get-id
  "gets the rt node id"
  {:added "4.0"}
  [& [rt]]
  (:id (:node (or rt (l/rt (.getName *ns*) :solidity)))))

(defn rt-get-contract-address
  "gets the current contract address"
  {:added "4.0"}
  [& [rt]]
  (compile-common/get-contract-address (rt-get-id rt)))

(defn rt-get-contract
  "gets the current contract"
  {:added "4.0"}
  [& [address rt]]
  (let [address   (or address
                      (compile-common/get-contract-address (rt-get-id rt)))
        [type id] (get @rt.solidity.env-ganache/+contracts+ address)]
    (assoc (get-in @compile-common/+compiled+
                   (cond-> [type id]
                     (= type :module) (conj (boolean compile-common/*open-methods*))))
           :address address)))

(defn rt-set-contract
  "sets the compiled contract"
  {:added "4.0"}
  [address m & [rt]]
  (let [rt (:node (or rt (l/rt (.getName *ns*) :solidity)))
        _  (compile-solc/create-module-entry rt m)]
    (swap! rt.solidity.env-ganache/+contracts+
           assoc address
           (if (:ns m)
             [:module (:ns m)]))))

(defn rt-get-caller-address
  "gets the caller address"
  {:added "4.0"}
  [& [rt]]
  (compile-common/get-caller-address (rt-get-id rt)))

(defn rt-get-caller-private-key
  "gets the caller private-key"
  {:added "4.0"}
  [& [rt]]
  (compile-common/get-caller-private-key (rt-get-id rt)))

(defn rt-get-node
  "gets the node runtime"
  {:added "4.0"}
  [& [rt]]
  (:node (or rt (l/rt (.getName *ns*) :solidity))))

(defn rt-get-address
  "gets the address of the signer"
  {:added "4.0"}
  [& [rt]]
  (compile-solc/compile-rt-eval
   (rt-get-node rt)
   (list `eth-lib/get-signer-address
         (compile-common/get-url rt)
         (rt-get-caller-private-key rt))))

(defn rt:node-get-block-number
  "gets the current block number
 
   (compile-node/rt:node-get-block-number)
   => number?"
  {:added "4.0"}
  [& [rt]]
  (compile-solc/compile-rt-eval
   (rt-get-node rt)
   (list `eth-lib/getBlockNumber
         (list `eth-lib/new-rpc-provider))
   'js.core/toString))

(defn rt:node-get-balance
  "gets the current balance"
  {:added "4.0"}
  [& [address rt]]
  (compile-solc/compile-rt-eval
   (rt-get-node rt)
   (list `eth-lib/getBalance
         (list `eth-lib/new-rpc-provider )
         (or address (rt-get-address rt))
         #_(compile-common/get-caller-address
          (rt-get-id rt)))
   'js.core/toString))

(defn rt:node-ping
  "pings the node"
  {:added "4.0"}
  [& [rt]]
  (h/p:rt-invoke-ptr (rt-get-node rt)
                     {:form "pong"}
                     []))

(defn rt:send-wei
  "sends wei to another address"
  {:added "4.0"}
  [to-address amount & [rt]]
  (compile-solc/compile-rt-eval
   (rt-get-node rt)
   (list '. (list `eth-bench/send-wei
                  (:url rt)
                  (rt-get-caller-private-key rt)
                  to-address
                  amount)
         '(then (fn [tx]
                  (return (. tx (wait))))))))

(defn rt:node-eval
  "evaluates a form in the node runtime"
  {:added "4.0"}
  [form & [rt]]
  (compile-solc/compile-rt-eval
   (rt-get-node rt)
   form))

(defn rt:node-past-events
  "gets past events
 
   #_#_#_(compile-node/rt:node-past-events)
   => vector?"
  {:added "4.0"}
  [name & [opts address rt]]
  (let [address (or address
                    (rt-get-contract-address rt))
        abi   (:abi (rt-get-contract address))]
    (compile-solc/compile-rt-eval
     (rt-get-node rt)
     (list `eth-bench/get-past-events
           (compile-common/get-url rt)
           address abi name opts))))

(defmacro with:measure
  "measures balance change before and after call"
  {:added "4.0"}
  [& body]
  `(let [~'start (rt:node-get-balance)
         ~'out       (do ~@body)
         ~'end   (rt:node-get-balance)]
     [(/ (- ~'start ~'end)
         1.0E15)
      ~'out]))


(comment
  (defn rt:node-history-clear
    "clears node history"
    {:added "4.0"}
    [& [rt]]
    (h/p:rt-invoke-ptr (rt-get-node rt)
                       eth/history-clear
                       []))

  (defn rt:node-history-count
    "counts node history"
    {:added "4.0"}
    [& [rt]]
    (h/p:rt-invoke-ptr (rt-get-node rt)
                       eth/history-count
                       []))

  (defn rt:node-history-latest
    "gets latest history"
    {:added "4.0"}
    [& [rt]]
    (h/p:rt-invoke-ptr (rt-get-node rt)
                       eth/history-latest
                       []))

  (defn rt:node-history-at
    "gets history at index"
    {:added "4.0"}
    [& [rt]]
    (h/p:rt-invoke-ptr (rt-get-node rt)
                       eth/history-at
                       [])))
