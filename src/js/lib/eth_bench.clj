(ns js.lib.eth-bench
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script :js
  {:require [[js.lib.eth-lib :as eth-lib :include [:fn]]
             [js.lib.eth-solc :as eth-solc  :include [:fn]]
             [xt.lang.base-lib :as k]
             [js.core :as j]]})

(defn.js send-wei
  "sends currency for bench"
  {:added "4.0"}
  [url private-key to-address amount]
  (var signer (eth-lib/get-signer url private-key))
  (return (eth-lib/send-wei signer to-address amount)))

(defn.js contract-deploy
  "deploys the contract"
  {:added "4.0"}
  [url private-key abi bytecode init-args overrides]
  (var signer (eth-lib/get-signer url private-key))
  (return (. (eth-lib/contract-deploy signer abi bytecode init-args overrides)
             (then (fn:> [contract]
                     {:status true
                      :size (j/toFixed (/ (k/len bytecode) 2 1024)
                                       3)
                      :contractAddress (. contract address)}))

             (catch (fn:> [err]
                      {:status false
                       :data err})))))

(defn.js contract-run
  "runs the contract given address and arguments"
  {:added "4.0"}
  [url private-key address abi fn-name args overrides]
  (var signer (eth-lib/get-signer url private-key))
  (return (. (eth-lib/contract-run signer address abi fn-name args overrides)
             (then (fn [res]
                     (return
                      (:? (and (k/obj? res)
                               (. res wait))
                          (. res (wait))
                          res))))
             (then (fn [res]
                     (return
                      (k/walk res
                              (fn [o]
                                (cond  (== "BigNumber"
                                           (k/type-native o))
                                       (return
                                        (j/toString o))
                                       
                                       :else
                                       (return o)))
                              k/identity)))))))

(defn.js get-past-events
  "gets all past events"
  {:added "4.0"}
  [url address abi name opts]
  (var provider (eth-lib/new-rpc-provider url))
  (var contract (eth-lib/new-contract address abi provider))
  (var filter   (. contract filters [name]))
  (return (. contract (queryFilter filter))))

(comment
  )
