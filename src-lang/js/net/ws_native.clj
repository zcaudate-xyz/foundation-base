(ns js.net.ws-native
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.ws-native :as websocket]]})

(defn.js connect-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client opts]
  (var url  (websocket/prepare-url client (or opts {})))
  (var listeners (xt/x:get-key opts "listeners"))
  (var raw (new WebSocket url))
  (when (xt/x:not-nil? listeners)
    (xt/for:object [[k handler] listeners]
      (. raw (addEventListener k handler))))
  (xt/x:set-key client "raw" raw)
  (return client))

(defn.js disconnect-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client]
  (var #{raw} client)
  (when raw
    (. raw (close 1000 "done")))
  (xt/x:set-key client "raw" nil)
  (return client))

(defn.js send-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client input]
  (var #{raw} client)
  (when raw
    (return
     (. raw (send input)))))

(defn.js add-listeners-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client m]
  (var #{raw} client)
  (when raw
    (xt/for:object [[k handler] m]
      (. raw (addEventListener k handler)))
    (return (xt/x:obj-keys m))))

(defn.js start-heartbeat-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client name f interval]
  (var #{heartbeats} client)
  (var stop-fn (xt/x:get-key heartbeats name))
  (when (xt/x:is-function? stop-fn)
    (stop-fn))
  (var timer (setInterval
              (fn []
                (f client name))
              interval))
  (xt/x:set-key heartbeats name
                (fn []
                  (clearInterval timer)
                  (xt/x:del-key heartbeats name)
                  (return true)))
  (return timer))

(defn.js stop-heartbeat-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client name]
  (var #{heartbeats} client)
  (var stop-fn (xt/x:get-key heartbeats name))
  (when (xt/x:is-function? stop-fn)
    (stop-fn))
  (return client))

(defimpl.xt ^{:lang :js}
  HttpWebsocketClient
  [defaults heartbeats]
  websocket/IWebsocket
  {websocket/connect -/connect-ws
   websocket/disconnect -/disconnect-ws
   websocket/send -/send-ws
   websocket/add-listeners -/add-listeners-ws}

  http-fetch/IHttpHeartbeat
  {http-fetch/start-heartbeat -/start-heartbeat-ws
   http-fetch/stop-heartbeat -/stop-heartbeat-ws})

(defn.js create
  "creates a new HttpWebsocketClient"
  {:added "4.1.3"}
  [defaults]
  (return
   (-/HttpWebsocketClient defaults {})))

;;
;; Raw Websocket Client
;;

(defn.js connect-raw
  "connects a raw websocket client, creating the socket if needed"
  {:added "4.1"}
  [client opts]
  (var raw (xt/x:get-key client "raw"))
  (if (xt/x:not-nil? raw)
    (return client))
  (var url (websocket/prepare-url client (or opts {})))
  (:= raw (new WebSocket url))
  (xt/x:set-key client "raw" raw)
  (return client))

(defn.js disconnect-raw
  "disconnects a raw websocket client"
  {:added "4.1"}
  [client]
  (var raw (xt/x:get-key client "raw"))
  (when raw
    (. raw (close 1000 "done")))
  (return client))

(defn.js send-raw
  "sends through a raw websocket client"
  {:added "4.1"}
  [client input]
  (var raw (xt/x:get-key client "raw"))
  (when raw
    (. raw (send input)))
  (return client))

(defn.js add-listeners-raw
  "adds listeners to the underlying raw websocket socket"
  {:added "4.1"}
  [client m]
  (var raw (xt/x:get-key client "raw"))
  (when raw
    (xt/for:object [[k handler] m]
      (. raw (addEventListener k handler)))
    (return (xt/x:obj-keys m))))

(defn.js start-heartbeat-raw
  "raw websocket clients do not provide a built-in heartbeat"
  {:added "4.1"}
  [client name f interval]
  (return nil))

(defn.js stop-heartbeat-raw
  "raw websocket clients do not provide a built-in heartbeat"
  {:added "4.1"}
  [client name]
  (return nil))

(defimpl.xt ^{:lang :js}
  RawWebsocketClient
  [defaults raw]
  websocket/IWebsocket
  {websocket/connect -/connect-raw
   websocket/disconnect -/disconnect-raw
   websocket/send -/send-raw
   websocket/add-listeners -/add-listeners-raw}

  http-fetch/IHttpHeartbeat
  {http-fetch/start-heartbeat -/start-heartbeat-raw
   http-fetch/stop-heartbeat -/stop-heartbeat-raw})

(defn.js wrap
  "wraps an existing raw websocket socket in a ws-native client"
  {:added "4.1"}
  [defaults raw]
  (return
   (-/RawWebsocketClient defaults raw)))
