(ns lua.ext.sochain
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.json :as json]))

(l/script :lua
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-log :as event-log]
             [lua.nginx :as n]
             [lua.nginx.task :as t]
             [lua.nginx.pusher :as push]
             [lua.nginx.http-client :as http-client]
             [lua.nginx.ws-client :as ws-client]]
   :export  [MODULE]})

(defn.lua create-pusher-defaults
  "creates connection defaults"
  {:added "0.1"}
  []
  (return
   {"host" "pusher.chain.so"
    "port" 443
    "app_key" "e9f5cc20074501ca7395"}))

(defn.lua create-pusher
  "creates the connection"
  {:added "0.1"}
  [auto-start options init-fn]
  (:= options (k/obj-assign
               (-/create-pusher-defaults)
               options))
  (var p (push/make-pusher-raw options init-fn))
  (when auto-start
    (push/pusher-activate p options))
  (return p))

(defn.lua test-pusher
  "tests that pusher is working"
  {:added "4.0"}
  [options]
  (:= options (k/obj-assign
                   (-/create-pusher-defaults)
                   options))
  (var p (push/make-pusher-raw options))
  (var conn (push/make-pusher-connect p))
  (ws-client/close (. conn ws))
  (return true))

(defn.lua subscribe-address
  "subscribes to a events on an address"
  {:added "0.1"}
  [p network address]
  (return
   (push/send-event p "subscribe"
                      {:channel (k/cat "address_" (k/to-lowercase network) "_" address)}
                      true)))

(defn.lua subscribe-network
  "subscribes to events on the network"
  {:added "0.1"}
  [p network]
  (return
   (push/send-event p "subscribe"
                      {:channel (k/cat "blockchain_update_" (k/to-lowercase network))}
                      true)))

(defn.lua get-response
  "helper function for response"
  {:added "0.1"}
  [res err]
  (when err
    (return {:status "error"
             :message err}))
  (var #{status
         body} res)
  (when (== status 200)
    (return (k/json-decode body)))

  (var '[ok out] (pcall (fn []
                          (return (k/json-decode body)))))
  
  (return (:? ok out {:status "error"
                      :message body})))

(defn.lua get-request
  "helper function for sochain request"
  {:added "0.1"}
  [url]
  (var retries 0)
  (while true
    (var conn (http-client/new))
    (var '[res err] (http-client/request-uri
                     conn url
                     {:method "GET"
                      :headers {"Accept" "application/json"
                                "Content-Type" "application/json"}
                      :ssl_verify false}))
    (cond err
          (do (n/sleep 0.5)
              (:= retries (+ retries 1)))

          (< retries 5)
          (return (-/get-response res err))

          :else
          (return (-/get-response res err)))))

(defn.lua get-block-info
  "gets the block info"
  {:added "0.1"}
  [network block-no]
  (var url (k/cat "https://chain.so/api/v2/get_block/" (k/to-lowercase network) "/" block-no))
  (return (-/get-request url)))

(defn.lua get-network-info
  "gets the current network info"
  {:added "0.1"}
  [network]
  (var url (k/cat "https://chain.so/api/v2/get_info/" (k/to-lowercase network)))
  (return (-/get-request url)))

(defn.lua get-network-block
  "gets the current block on the network"
  {:added "0.1"}
  [network]
  (var #{status
         data} (-/get-network-info network))
  (var #{blocks} data)
  (return (-/get-block-info network blocks)))

(defn.lua get-address-info
  "gets address information"
  {:added "0.1"}
  [network address min-confirmations]
  (var url (k/cat "https://chain.so/api/v2/get_address_balance/"
                  (k/to-lowercase network) "/" address
                  (:? (k/is-number? min-confirmations)
                      (k/cat "/" min-confirmations)
                      "")))
  (return (-/get-request url)))

(defn.lua get-tx-info
  "gets transaction info"
  {:added "0.1"}
  [network tx-id]
  (var url (k/cat "https://chain.so/api/v2/get_tx/" (k/to-lowercase network) "/" tx-id))
  (return (-/get-request url)))

(defn.lua get-tx-unspent
  "gets the unspent transactions"
  {:added "0.1"}
  [network address after-tx-id]
  (var url (k/cat "https://chain.so/api/v2/get_tx_unspent/"
                  (k/to-lowercase network) "/" address
                  (:? (k/not-nil? after-tx-id)
                      (k/cat "/" after-tx-id)
                      "")))
  (return (-/get-request url)))

(defn.lua get-tx-received
  "gets all received txs"
  {:added "4.0"}
  [network address after-tx-id]
  (var url (k/cat "https://chain.so/api/v2/get_tx_received/"
                  (k/to-lowercase network) "/" address
                  (:? (k/not-nil? after-tx-id)
                      (k/cat "/" after-tx-id)
                      "")))
  (return (-/get-request url)))

(defn.lua get-tx-last
  "gets last unspent tx"
  {:added "4.0"}
  [network address after-tx-id]
  (var candidate nil)
  (while true 
    (var #{status
           data} (-/get-tx-unspent network address after-tx-id))
    (var #{txs} data)
    (when (k/not-empty? txs)
      (:= candidate (k/last txs)))
    (cond (== 100 (k/len txs))
          (:= after-tx-id (. candidate ["txid"]))

          :else
          (return candidate))))

(defn.lua send-tx
  "sends a transaction to the network"
  {:added "0.1"}
  [network tx-hex]
  (var conn (http-client/new))
  (var url (k/cat "https://chain.so/api/v2/send_tx/" (k/to-lowercase network)))
  (var '[res err] (http-client/request-uri
                   conn url
                   {:method "POST"
                    :headers {"Accept" "application/json"
                              "Content-Type" "application/json"}
                    :body (k/json-encode {:tx-hex tx-hex})
                    :ssl_verify false}))
  
  (return (-/get-response res err)))

(defn.lua set-network-listener
  "sets the network listener"
  {:added "0.1"}
  [p network block-fn tx-fn]
  (:= network (k/to-lowercase network))
  (return
   (push/pusher-add-listener
    p
    (k/cat "network_" network)
    (fn [id m t]
      (var #{data channel} m)
      (when (== channel (k/cat "blockchain_update_" network))
        (when (and (== (. data type) "tx")
                   tx-fn)
          (tx-fn m))
        (when (and (== (. data type) "block")
                   block-fn)
          (block-fn m)))))))

(defn.lua set-address-listener
  "sets the address listener"
  {:added "0.1"}
  [p network update-fn]
  (:= network (k/to-lowercase network))
  (var listener-id (k/cat "address_" network))
  (return
   (push/pusher-add-listener
    p
    listener-id
    (fn [id m t]
      (var #{data channel} m)
      (when (k/is-string? channel)
        (when (and (== (. data type) "address")
                   update-fn)
          (update-fn m)))))))

(def.lua MODULE (!:module))
