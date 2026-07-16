(ns python.net.ws-native
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.ws-native :as websocket]]
   :import [["websocket" :as wsclient]
            ["threading" :as threading]]})

(defn.py dispatch-ws [client event payload]
  (var callbacks (xt/x:get-key client "callbacks"))
  (var handler (xt/x:get-key callbacks event))
  (when (xt/x:is-function? handler) (handler payload))
  (return payload))

(defn.py receive-loop [client]
  (var raw (xt/x:get-key client "raw"))
  (while (xt/x:not-nil? raw)
    (try
      (var message (. raw (recv)))
      (when (xt/x:nil? message) (break))
      (-/dispatch-ws client "message" {"data" message})
      (catch err
        (-/dispatch-ws client "error" err)
        (break)))
    (:= raw (xt/x:get-key client "raw")))
  (-/dispatch-ws client "close" {})
  (return client))

(defn.py connect-ws [client opts]
  (var url (websocket/prepare-url client (or opts {})))
  (var raw (. wsclient (create_connection url)))
  (xt/x:set-key client "raw" raw)
  (var thread (. threading (Thread :target -/receive-loop :args [client] :daemon true)))
  (xt/x:set-key client "thread" thread)
  (. thread (start))
  (return client))

(defn.py disconnect-ws [client]
  (var raw (xt/x:get-key client "raw"))
  (xt/x:set-key client "raw" nil)
  (when (xt/x:not-nil? raw) (. raw (close)))
  (return client))

(defn.py send-ws [client input]
  (var raw (xt/x:get-key client "raw"))
  (when (xt/x:not-nil? raw) (. raw (send input)))
  (return client))

(defn.py add-listeners-ws [client m]
  (var callbacks (xt/x:get-key client "callbacks"))
  (xt/for:object [[event handler] m] (xt/x:set-key callbacks event handler))
  (return (xt/x:obj-keys m)))

(defn.py start-heartbeat-ws [client name f interval] (return nil))
(defn.py stop-heartbeat-ws [client name] (return client))

(defimpl.xt ^{:lang :python}
  PythonWebsocketClient
  [raw defaults state callbacks thread]
  websocket/IWebsocket
  {websocket/connect -/connect-ws
   websocket/disconnect -/disconnect-ws
   websocket/send -/send-ws
   websocket/add-listeners -/add-listeners-ws}
  websocket/IWebsocketHeartbeat
  {websocket/start-heartbeat -/start-heartbeat-ws
   websocket/stop-heartbeat -/stop-heartbeat-ws})

(defn.py create [defaults]
  (return (-/PythonWebsocketClient nil (or defaults {}) {} {} nil)))
