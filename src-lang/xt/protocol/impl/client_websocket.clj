(ns xt.protocol.impl.client-websocket
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as proto]
             [xt.protocol.client-websocket :as ws-if]]})

(defn.xt driver?
  "checks if a value is a wrapped websocket client driver"
  {:added "4.1.3"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "websocket.client.driver"
                   (xt/x:get-key obj "::")))))

(defn.xt client?
  "checks if a value is a wrapped websocket client"
  {:added "4.1.3"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "websocket.client"
                   (xt/x:get-key obj "::")))))

(defn.xt require-driver
  "ensures a value is a wrapped websocket client driver"
  {:added "4.1.3"}
  [value]
  (when (not (-/driver? value))
    (xt/x:err "Value is not a websocket client driver"))
  (return value))

(defn.xt require-client
  "ensures a value is a wrapped websocket client"
  {:added "4.1.3"}
  [value]
  (when (not (-/client? value))
    (xt/x:err "Value is not a websocket client"))
  (return value))

(defn.xt ensure-promise
  "wraps sync values in a native host promise while passing promises through"
  {:added "4.1.3"}
  [value]
  (if (promise/x:promise-native? value)
    (return value)
    (return (promise/x:promise-run value))))

(defn.xt default-disconnect-sync
  "disconnects a raw websocket client"
  {:added "4.1.3"}
  [raw code reason]
  (var close-fn (xt/x:get-key raw "close"))
  (when (not (xt/x:is-function? close-fn))
    (xt/x:err "Websocket client missing close implementation"))
  (if (xt/x:not-nil? code)
    (return (close-fn code (or reason "")))
    (return (close-fn))))

(defn.xt default-send-sync
  "sends a payload through a raw websocket client"
  {:added "4.1.3"}
  [raw payload]
  (var send-fn (xt/x:get-key raw "send"))
  (when (not (xt/x:is-function? send-fn))
    (xt/x:err "Websocket client missing send implementation"))
  (return (send-fn payload)))

(defn.xt default-add-listener-sync
  "attaches an event listener to a raw websocket client"
  {:added "4.1.3"}
  [raw event handler]
  (var add-fn (xt/x:get-key raw "addEventListener"))
  (if (xt/x:is-function? add-fn)
    (add-fn event handler)
    (xt/x:set-key raw (xt/x:cat "on" event) handler))
  (return raw))

(defn.xt client-create
  "wraps a raw websocket connection with the websocket client protocol"
  {:added "4.1.3"}
  [raw impl]
  (when (-/client? raw)
    (return raw))
  (:= impl (or impl {}))
  (var disconnect-sync-fn (or (xt/x:get-key impl "disconnect_sync")
                              (xt/x:get-key impl "disconnect-sync")
                              -/default-disconnect-sync))
  (var disconnect-fn (or (xt/x:get-key impl "disconnect")
                         disconnect-sync-fn))
  (var send-sync-fn (or (xt/x:get-key impl "send_sync")
                        (xt/x:get-key impl "send-sync")
                        -/default-send-sync))
  (var send-fn (or (xt/x:get-key impl "send")
                   send-sync-fn))
  (var add-listener-sync-fn (or (xt/x:get-key impl "add_listener_sync")
                                (xt/x:get-key impl "add-listener-sync")
                                (xt/x:get-key impl "add_listener")
                                (xt/x:get-key impl "add-listener")
                                -/default-add-listener-sync))
  (var add-listener-fn (or (xt/x:get-key impl "add_listener")
                           (xt/x:get-key impl "add-listener")
                           add-listener-sync-fn))
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[ws-if/IClientWebsocketRuntime
            {"disconnect" (fn [self code reason]
                            (var raw (xt/x:get-key self "_raw"))
                            (return (-/ensure-promise
                                     ((xt/x:get-key self "__disconnect") raw code reason))))
             "disconnect_sync" (fn [self code reason]
                                 (var raw (xt/x:get-key self "_raw"))
                                 (return ((xt/x:get-key self "__disconnect_sync") raw code reason)))
             "send"       (fn [self payload]
                            (var raw (xt/x:get-key self "_raw"))
                            (return (-/ensure-promise
                                     ((xt/x:get-key self "__send") raw payload))))
             "send_sync"  (fn [self payload]
                            (var raw (xt/x:get-key self "_raw"))
                            (return ((xt/x:get-key self "__send_sync") raw payload)))
             "add_listener" (fn [self event handler]
                              (var raw (xt/x:get-key self "_raw"))
                              (return (-/ensure-promise
                                       ((xt/x:get-key self "__add_listener")
                                        raw event handler))))
             "add_listener_sync" (fn [self event handler]
                                   (var raw (xt/x:get-key self "_raw"))
                                   (return ((xt/x:get-key self "__add_listener_sync")
                                            raw event handler)))}]])))
  (var client {"::" "websocket.client"
               "_raw" raw
               "_impl" impl
                "__disconnect" disconnect-fn
                "__disconnect_sync" disconnect-sync-fn
                "__send" send-fn
                "__send_sync" send-sync-fn
                "__add_listener" add-listener-fn
                "__add_listener_sync" add-listener-sync-fn})
  (xt/proto:set client protocol)
  (return client))

(defn.xt wrap-client
  "normalises websocket driver outputs into wrapped websocket clients"
  {:added "4.1.3"}
  [value client-impl]
  (when (xt/x:nil? value)
    (xt/x:err "Websocket client driver returned nil"))
  (if (-/client? value)
    (return value)
    (return (-/client-create value (or client-impl {})))))

(defn.xt driver-create
  "wraps a connector map with the websocket client driver protocol"
  {:added "4.1.3"}
  [impl]
  (:= impl (or impl {}))
  (var connect-sync-fn (or (xt/x:get-key impl "connect_sync")
                           (xt/x:get-key impl "connect-sync")
                           nil))
  (var connect-fn (or (xt/x:get-key impl "connect")
                      connect-sync-fn))
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[ws-if/IClientWebsocketRuntimeDriver
            {"connect" (fn [self url]
                         (var impl (xt/x:get-key self "_impl"))
                         (var connect-fn (xt/x:get-key self "__connect"))
                         (when (not (xt/x:is-function? connect-fn))
                           (xt/x:err "Websocket client driver missing connect implementation"))
                         (return
                          (promise/x:promise-then
                           (-/ensure-promise (connect-fn url))
                           (fn [out]
                             (return (-/wrap-client
                                      out
                                      (or (xt/x:get-key impl "client_impl")
                                          (xt/x:get-key impl "client-impl")
                                          {})))))))
             "connect_sync" (fn [self url]
                              (var impl (xt/x:get-key self "_impl"))
                              (var connect-sync-fn (xt/x:get-key self "__connect_sync"))
                              (when (not (xt/x:is-function? connect-sync-fn))
                                (xt/x:err "Websocket client driver missing connect_sync implementation"))
                              (return (-/wrap-client
                                       (connect-sync-fn url)
                                       (or (xt/x:get-key impl "client_impl")
                                           (xt/x:get-key impl "client-impl")
                                           {}))))}]])))
  (var driver {"::" "websocket.client.driver"
               "_impl" impl
               "__connect" connect-fn
               "__connect_sync" connect-sync-fn})
  (xt/proto:set driver protocol)
  (return driver))

(defn.xt connect
  "connects through the websocket client driver protocol"
  {:added "4.1.3"}
  [driver url]
  (:= driver (-/require-driver driver))
  (var connect-fn (xt/proto:method driver "connect"))
  (when (xt/x:nil? connect-fn)
    (xt/x:err "Websocket client driver missing connect method"))
  (return (-/ensure-promise
           (connect-fn driver url))))

(defn.xt connect-sync
  "connects synchronously through the websocket client driver protocol"
  {:added "4.1.3"}
  [driver url]
  (:= driver (-/require-driver driver))
  (var connect-sync-fn (xt/proto:method driver "connect_sync"))
  (when (xt/x:nil? connect-sync-fn)
    (xt/x:err "Websocket client driver missing connect_sync method"))
  (return (connect-sync-fn driver url)))

(defn.xt disconnect
  "disconnects through the websocket client protocol"
  {:added "4.1.3"}
  [client code reason]
  (:= client (-/require-client client))
  (var disconnect-fn (xt/proto:method client "disconnect"))
  (when (xt/x:nil? disconnect-fn)
    (xt/x:err "Websocket client missing disconnect method"))
  (return (-/ensure-promise
           (disconnect-fn client code reason))))

(defn.xt disconnect-sync
  "disconnects synchronously through the websocket client protocol"
  {:added "4.1.3"}
  [client code reason]
  (:= client (-/require-client client))
  (var disconnect-sync-fn (xt/proto:method client "disconnect_sync"))
  (when (xt/x:nil? disconnect-sync-fn)
    (xt/x:err "Websocket client missing disconnect_sync method"))
  (return (disconnect-sync-fn client code reason)))

(defn.xt send
  "sends through the websocket client protocol"
  {:added "4.1.3"}
  [client payload]
  (:= client (-/require-client client))
  (var send-fn (xt/proto:method client "send"))
  (when (xt/x:nil? send-fn)
    (xt/x:err "Websocket client missing send method"))
  (return (-/ensure-promise
           (send-fn client payload))))

(defn.xt send-sync
  "sends synchronously through the websocket client protocol"
  {:added "4.1.3"}
  [client payload]
  (:= client (-/require-client client))
  (var send-sync-fn (xt/proto:method client "send_sync"))
  (when (xt/x:nil? send-sync-fn)
    (xt/x:err "Websocket client missing send_sync method"))
  (return (send-sync-fn client payload)))

(defn.xt add-listener
  "attaches event listeners through the websocket client protocol"
  {:added "4.1.3"}
  [client event handler]
  (:= client (-/require-client client))
  (var add-listener-fn (xt/proto:method client "add_listener"))
  (when (xt/x:nil? add-listener-fn)
    (xt/x:err "Websocket client missing add_listener method"))
  (return (-/ensure-promise
           (add-listener-fn client event handler))))

(defn.xt add-listener-sync
  "attaches event listeners synchronously through the websocket client protocol"
  {:added "4.1.3"}
  [client event handler]
  (:= client (-/require-client client))
  (var add-listener-sync-fn (xt/proto:method client "add_listener_sync"))
  (when (xt/x:nil? add-listener-sync-fn)
    (xt/x:err "Websocket client missing add_listener_sync method"))
  (return (add-listener-sync-fn client event handler)))
