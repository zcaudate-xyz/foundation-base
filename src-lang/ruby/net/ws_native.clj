(ns ruby.net.ws-native
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.ws-native :as websocket]]})

(defn.rb dispatch-ws [client event payload]
  (var handler (xt/x:get-key (xt/x:get-key client "callbacks") event))
  (when (xt/x:is-function? handler) (handler payload))
  (return payload))

(defn.rb ws-connect-raw [url]
  (require "websocket-client-simple")
  (return (:- "WebSocket::Client::Simple.connect(url)")))

(defn.rb register-listener [raw event handler]
  (:- "raw.on(event.to_sym, &handler)")
  (return raw))

(defn.rb message-data [message]
  (return (:- "message.respond_to?(:data) ? message.data : message.to_s")))

(defn.rb connect-ws [client opts]
  (var url (websocket/prepare-url client (or opts {})))
  (var resolve-fn nil)
  (var reject-fn nil)
  (var init
       (promise/x:promise-new
        (fn [resolve reject]
          (:= resolve-fn resolve)
          (:= reject-fn reject))))
  (xt/x:set-key client "readyState" 0)
  (var raw (-/ws-connect-raw url))
  (xt/x:set-key client "raw" raw)
  (-/register-listener raw "open"
                       (fn []
                         (xt/x:set-key client "readyState" 1)
                         (resolve-fn client)
                         (-/dispatch-ws client "open" {})))
  (-/register-listener raw "message"
                       (fn [message]
                         (-/dispatch-ws client "message"
                                        {"data" (-/message-data message)})))
  (-/register-listener raw "error"
                       (fn [err]
                         (xt/x:set-key client "readyState" 3)
                         (reject-fn err)
                         (-/dispatch-ws client "error" err)))
  (-/register-listener raw "close"
                       (fn [event]
                         (xt/x:set-key client "readyState" 3)
                         (-/dispatch-ws client "close" event)))
  (return init))

(defn.rb disconnect-ws [client]
  (var raw (xt/x:get-key client "raw"))
  (xt/x:set-key client "raw" nil)
  (xt/x:set-key client "readyState" 3)
  (when (xt/x:not-nil? raw) (. raw (close)))
  (return client))

(defn.rb send-ws [client input]
  (var raw (xt/x:get-key client "raw"))
  (when (xt/x:not-nil? raw) (. raw (send input)))
  (return client))

(defn.rb add-listeners-ws [client m]
  (var callbacks (xt/x:get-key client "callbacks"))
  (xt/for:object [[event handler] m]
    (xt/x:set-key callbacks event handler))
  (return (xt/x:obj-keys m)))

(defn.rb start-heartbeat-ws [client name f interval] (return nil))
(defn.rb stop-heartbeat-ws [client name] (return client))

(defimpl.xt ^{:lang :ruby}
  RubyWebsocketClient
  [raw defaults state callbacks readyState]
  websocket/IWebsocket
  {websocket/connect -/connect-ws
   websocket/disconnect -/disconnect-ws
   websocket/send -/send-ws
   websocket/add-listeners -/add-listeners-ws}
  websocket/IWebsocketHeartbeat
  {websocket/start-heartbeat -/start-heartbeat-ws
   websocket/stop-heartbeat -/stop-heartbeat-ws})

(defn.rb create [defaults]
  (return (-/RubyWebsocketClient nil (or defaults {}) {} {} 3)))
