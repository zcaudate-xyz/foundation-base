(ns lua.net.ws-native
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :lua
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.ws-native :as websocket]]
   :import [["resty.websocket.client" :as wsclient]]})

(defn.lua dispatch-ws [client event payload]
  (var callbacks (xt/x:get-key client "callbacks"))
  (var handler (xt/x:get-key callbacks event))
  (when (xt/x:is-function? handler) (handler payload))
  (return payload))

(defn.lua receive-loop [client]
  (var raw (xt/x:get-key client "raw"))
  (while (xt/x:not-nil? raw)
    (var '[data typ err] (. raw (recv_frame)))
    (cond (== typ "text") (-/dispatch-ws client "message" {"data" data})
          (or (== typ "close") (and (xt/x:nil? data) (xt/x:nil? err))) (break)
          (xt/x:not-nil? err) (do (-/dispatch-ws client "error" err) (break)))
    (:= raw (xt/x:get-key client "raw")))
  (-/dispatch-ws client "close" {})
  (return client))

(defn.lua connect-ws [client opts]
  (var url (websocket/prepare-url client (or opts {})))
  (var raw (. wsclient (new)))
  (. raw (set_timeout (or (xt/x:get-key opts "timeout") 5000)))
  (var '[ok err] (. raw (connect url)))
  (when (not ok) (xt/x:err err))
  (xt/x:set-key client "raw" raw)
  (var thread nil)
  (when (xt/x:get-key (xt/x:get-key client "defaults") "background")
    (:= thread (ngx.thread.spawn (fn [] (return (-/receive-loop client))))))
  (xt/x:set-key client "thread" thread)
  (return client))

(defn.lua disconnect-ws [client]
  (var raw (xt/x:get-key client "raw"))
  (xt/x:set-key client "raw" nil)
  (when (xt/x:not-nil? raw) (. raw (send_close)))
  (return client))

(defn.lua send-ws [client input]
  (var raw (xt/x:get-key client "raw"))
  (when (xt/x:not-nil? raw)
    (. raw (send_text input))
    (when (xt/x:nil? (xt/x:get-key client "thread"))
      (var '[data typ err] (. raw (recv_frame)))
      (cond (== typ "text") (-/dispatch-ws client "message" {"data" data})
            (xt/x:not-nil? err) (-/dispatch-ws client "error" err))))
  (return client))

(defn.lua add-listeners-ws [client m]
  (var callbacks (xt/x:get-key client "callbacks"))
  (xt/for:object [[event handler] m] (xt/x:set-key callbacks event handler))
  (return (xt/x:obj-keys m)))

(defn.lua start-heartbeat-ws [client name f interval] (return nil))
(defn.lua stop-heartbeat-ws [client name] (return client))

(defimpl.xt ^{:lang :lua}
  LuaWebsocketClient
  [raw defaults state callbacks thread]
  websocket/IWebsocket
  {websocket/connect -/connect-ws
   websocket/disconnect -/disconnect-ws
   websocket/send -/send-ws
   websocket/add-listeners -/add-listeners-ws}
  websocket/IWebsocketHeartbeat
  {websocket/start-heartbeat -/start-heartbeat-ws
   websocket/stop-heartbeat -/stop-heartbeat-ws})

(defn.lua create [defaults]
  (var client (-/LuaWebsocketClient nil (or defaults {}) {} {} nil))
  (xt/x:set-key client "::/override"
                {"connect" -/connect-ws
                 "disconnect" -/disconnect-ws
                 "send" -/send-ws
                 "add_listeners" -/add-listeners-ws
                 "start_heartbeat" -/start-heartbeat-ws
                 "stop_heartbeat" -/stop-heartbeat-ws})
  (return client))
