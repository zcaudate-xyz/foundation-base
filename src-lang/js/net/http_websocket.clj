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
  [client opts])

(defn.js disconnect-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client])

(defn.js send-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client input])

(defn.js add-listeners-ws
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client m])

(defimpl.xt HttpWebsocketClient
  [defaults]
  [websocket/IWebsocket
   {websocket/connect -/connect-ws
    websocket/disconnect -/disconnect-ws
    websocket/send -/send-ws
    websocket/add-listeners -/add-listeners-ws}])

(defn.js create
  [defaults]
  (return
   (-/HttpWebsocketClient defaults)))
