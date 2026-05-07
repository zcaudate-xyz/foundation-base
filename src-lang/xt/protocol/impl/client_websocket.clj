(ns xt.protocol.impl.client-websocket
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
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

(defn.xt default-disconnect
  "disconnects a raw websocket client"
  {:added "4.1.3"}
  [raw code reason]
  (var close-fn (xt/x:get-key raw "close"))
  (when (not (xt/x:is-function? close-fn))
    (xt/x:err "Websocket client missing close implementation"))
  (if (xt/x:not-nil? code)
    (return (close-fn code (or reason "")))
    (return (close-fn))))

(defn.xt default-send
  "sends a payload through a raw websocket client"
  {:added "4.1.3"}
  [raw payload]
  (var send-fn (xt/x:get-key raw "send"))
  (when (not (xt/x:is-function? send-fn))
    (xt/x:err "Websocket client missing send implementation"))
  (return (send-fn payload)))

(defn.xt default-add-listener
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
  (:= impl (or impl {}))
  (var disconnect-fn (or (xt/x:get-key impl "disconnect")
                         -/default-disconnect))
  (var send-fn (or (xt/x:get-key impl "send")
                   -/default-send))
  (var add-listener-fn (or (xt/x:get-key impl "add_listener")
                           (xt/x:get-key impl "add-listener")
                           -/default-add-listener))
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[ws-if/IClientWebsocketRuntime
           {"disconnect" (fn [self code reason]
                           (var raw (xt/x:get-key self "_raw"))
                           (return ((xt/x:get-key self "__disconnect") raw code reason)))
            "send"       (fn [self payload]
                           (var raw (xt/x:get-key self "_raw"))
                           (return ((xt/x:get-key self "__send") raw payload)))
            "add_listener" (fn [self event handler]
                             (var raw (xt/x:get-key self "_raw"))
                             (return ((xt/x:get-key self "__add_listener")
                                      raw event handler)))}]])))
  (var client {"::" "websocket.client"
               "_raw" raw
               "_impl" impl
               "__disconnect" disconnect-fn
               "__send" send-fn
               "__add_listener" add-listener-fn})
  (xt/proto:set client protocol)
  (return client))

(defn.xt driver-create
  "wraps a connector map with the websocket client driver protocol"
  {:added "4.1.3"}
  [impl]
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[ws-if/IClientWebsocketRuntimeDriver
           {"connect" (fn [self url]
                        (var impl (xt/x:get-key self "_impl"))
                        (var connect-fn (xt/x:get-key impl "connect"))
                        (when (not (xt/x:is-function? connect-fn))
                          (xt/x:err "Websocket client driver missing connect implementation"))
                        (var out (connect-fn url))
                        (when (-/client? out)
                          (return out))
                        (when (xt/x:nil? out)
                          (xt/x:err "Websocket client driver returned nil"))
                        (return (-/client-create out
                                                 (or (xt/x:get-key impl "client_impl")
                                                     (xt/x:get-key impl "client-impl")
                                                     {}))))}]])))
  (var driver {"::" "websocket.client.driver"
               "_impl" impl})
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
  (return (connect-fn driver url)))

(defn.xt disconnect
  "disconnects through the websocket client protocol"
  {:added "4.1.3"}
  [client code reason]
  (:= client (-/require-client client))
  (var disconnect-fn (xt/proto:method client "disconnect"))
  (when (xt/x:nil? disconnect-fn)
    (xt/x:err "Websocket client missing disconnect method"))
  (return (disconnect-fn client code reason)))

(defn.xt send
  "sends through the websocket client protocol"
  {:added "4.1.3"}
  [client payload]
  (:= client (-/require-client client))
  (var send-fn (xt/proto:method client "send"))
  (when (xt/x:nil? send-fn)
    (xt/x:err "Websocket client missing send method"))
  (return (send-fn client payload)))

(defn.xt add-listener
  "attaches event listeners through the websocket client protocol"
  {:added "4.1.3"}
  [client event handler]
  (:= client (-/require-client client))
  (var add-listener-fn (xt/proto:method client "add_listener"))
  (when (xt/x:nil? add-listener-fn)
    (xt/x:err "Websocket client missing add_listener method"))
  (return (add-listener-fn client event handler)))
