(ns js.net.http-websocket
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-websocket :as websocket]]})

(defn.js connect-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client opts]
  (var url  (websocket/prepare-url client input))
  (var raw (new WebSocket url))
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

(defimpl.xt ^{:lang :js}
  HttpWebsocketClient
  [defaults]
  websocket/IWebsocket
  {websocket/connect -/connect-ws
   websocket/disconnect -/disconnect-ws
   websocket/send -/send-ws
   websocket/add-listeners -/add-listeners-ws})

(defn.js create
  [defaults]
  (return
   (-/HttpWebsocketClient defaults)))
