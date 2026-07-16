(ns dart.net.ws-native
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.ws-native :as websocket]]
   :import [["dart:io" :as io]]})

(defn.dt connect-ws
  [client opts]
  (var url (websocket/prepare-url client (or opts {})))
  (return
   (. (io.WebSocket.connect url)
      (then
       (fn [raw]
         (xt/x:set-key client "raw" raw)
         (. raw
            (listen
             (fn [message]
               (var handler (xt/x:get-key (xt/x:get-key client "callbacks") "message"))
               (when (xt/x:is-function? handler)
                 (handler {"data" message})))))
         (return client))))))

(defn.dt disconnect-ws
  [client]
  (var raw (xt/x:get-key client "raw"))
  (when (xt/x:not-nil? raw)
    (. raw (close)))
  (xt/x:set-key client "raw" nil)
  (return client))

(defn.dt send-ws
  [client input]
  (var raw (xt/x:get-key client "raw"))
  (when (xt/x:not-nil? raw)
    (. raw (add input)))
  (return client))

(defn.dt add-listeners-ws
  [client m]
  (xt/for:object [[event handler] m]
    (xt/x:set-key (xt/x:get-key client "callbacks") event handler))
  (return (xt/x:obj-keys m)))

(defn.dt start-heartbeat-ws [client name f interval]
  (return nil))

(defn.dt stop-heartbeat-ws [client name]
  (return client))

(defimpl.xt ^{:lang :dart}
  DartWebsocketClient
  [raw defaults state callbacks]
  websocket/IWebsocket
  {websocket/connect -/connect-ws
   websocket/disconnect -/disconnect-ws
   websocket/send -/send-ws
   websocket/add-listeners -/add-listeners-ws}

  websocket/IWebsocketHeartbeat
  {websocket/start-heartbeat -/start-heartbeat-ws
   websocket/stop-heartbeat -/stop-heartbeat-ws})

(defn.dt create
  [defaults]
  (return (-/DartWebsocketClient nil (or defaults {}) {} {})))
