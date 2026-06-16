(ns xt.net.ws-legacy
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt client?
  "checks if a value looks like a connected websocket client"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (xt/x:is-function? (xt/x:get-key obj "send"))
               (xt/x:is-function? (xt/x:get-key obj "close")))))

(defn.xt driver?
  "checks if a value is a websocket driver (factory)"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (or (xt/x:is-function? (xt/x:get-key obj "connect"))
                   (xt/x:is-function? (xt/x:get-key obj "connect_sync"))))))

(defn.xt driver-create
  "wraps a connector map with the legacy websocket driver protocol"
  {:added "4.1"}
  [impl]
  (return {"connect"      (or (xt/x:get-key impl "connect")
                              (xt/x:get-key impl "connect_sync"))
           "connect_sync" (or (xt/x:get-key impl "connect_sync")
                              (xt/x:get-key impl "connect"))
           "client_impl"  (or (xt/x:get-key impl "client_impl")
                              (xt/x:get-key impl "client-impl")
                              {})}))

(defn.xt connect
  "connects through the websocket driver protocol"
  {:added "4.1"}
  [driver url]
  (cond (-/client? driver)
        (return driver)

        (-/driver? driver)
        (do (var connect-fn (xt/x:get-key driver "connect"))
            (return (connect-fn url)))

        :else
        (xt/x:err "websocket connect expects a driver or client")))

(defn.xt connect-sync
  "connects synchronously through the websocket driver protocol"
  {:added "4.1"}
  [driver url]
  (cond (-/client? driver)
        (return driver)

        (-/driver? driver)
        (do (var connect-fn (xt/x:get-key driver "connect_sync"))
            (return (connect-fn url)))

        :else
        (xt/x:err "websocket connect-sync expects a driver or client")))

(defn.xt disconnect
  "disconnects through the websocket client protocol"
  {:added "4.1"}
  [client]
  (var close-fn (xt/x:get-key client "close"))
  (when (xt/x:is-function? close-fn)
    (. client (close 1000 "done")))
  (return client))

(defn.xt send
  "sends through the websocket client protocol"
  {:added "4.1"}
  [client payload]
  (var send-fn (xt/x:get-key client "send"))
  (when (xt/x:is-function? send-fn)
    (. client (send payload)))
  (return client))

(defn.xt add-listener
  "attaches an event listener through the websocket client protocol"
  {:added "4.1"}
  [client event handler]
  (cond (xt/x:is-function? (xt/x:get-key client "addEventListener"))
        (. client (addEventListener event handler false))

        (xt/x:is-function? (xt/x:get-key client "on"))
        (. client (on event handler))

        :else
        (xt/x:set-key client (xt/x:cat "on" event) handler))
  (return handler))

(defn.xt ensure-promise
  "wraps sync values in a native promise while passing promises through"
  {:added "4.1"}
  [value]
  (if (promise/x:promise-native? value)
    (return value)
    (return (promise/x:promise-run value))))
