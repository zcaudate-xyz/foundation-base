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

(defn.js default-heartbeat-fn
  "default heartbeat sends a raw heartbeat string"
  {:added "4.1.3"}
  [client name]
  (return (websocket/send client "heartbeat")))

(defn.js start-heartbeat-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client name f interval]
  (var #{defaults state} client)
  (var heartbeats (or (xt/x:get-key state "heartbeats") {}))
  (var stop-fn (xt/x:get-key heartbeats name))
  (when (xt/x:is-function? stop-fn)
    (stop-fn))
  (:= f (or f
            (xt/x:get-key defaults "heartbeat_fn")
            -/default-heartbeat-fn))
  (:= interval (or interval
                   (xt/x:get-key defaults "heartbeat_interval")
                   30000))
  (var timer (setInterval
              (fn []
                (f client name))
              interval))
  (xt/x:set-key heartbeats name
                (fn []
                  (clearInterval timer)
                  (xt/x:del-key heartbeats name)
                  (return true)))
  (xt/x:set-key state "heartbeats" heartbeats)
  (return timer))

(defn.js stop-heartbeat-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client name]
  (var #{state} client)
  (var heartbeats (or (xt/x:get-key state "heartbeats") {}))
  (var stop-fn (xt/x:get-key heartbeats name))
  (when (xt/x:is-function? stop-fn)
    (stop-fn))
  (return client))

(defimpl.xt ^{:lang :js}
  WebsocketClient
  [raw defaults state]
  websocket/IWebsocket
  {websocket/connect -/connect-ws
   websocket/disconnect -/disconnect-ws
   websocket/send -/send-ws
   websocket/add-listeners -/add-listeners-ws}

  websocket/IWebsocketHeartbeat
  {websocket/start-heartbeat -/start-heartbeat-ws
   websocket/stop-heartbeat -/stop-heartbeat-ws})

(defn.js create
  "creates a new HttpWebsocketClient"
  {:added "4.1.3"}
  [defaults]
  (return
   (-/WebsocketClient nil
                      defaults
                      {"heartbeats" {}
                       "callbacks"  {}})))
