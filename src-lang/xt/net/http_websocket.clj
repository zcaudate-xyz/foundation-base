(ns xt.net.http-websocket
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-util :as util]]})

(def.xt IWebsocket
  ["connect"
   "disconnect"
   "send"
   "add_listeners"])

(defn.xt create-base
  [type methods defaults]
  (return
   (xt/x:obj-assign {"::"   (or type "xt.net.http-websocket")
                     "defaults" (or defaults {})}
                    (protocol/proto-spec
                     [[-/IWebsocket methods]]))))

(defn.xt connect
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client opts]
  (var connect-fn (xt/x:get-key client "connect"))
  (return (protocol/ensure-promise
           (connect-fn client input opts))))

(defn.xt disconnect
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client]
  (var disconnect-fn (xt/x:get-key client "disconnect"))
  (return (disconnect-fn client)))

(defn.xt send
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client input]
  (var send-fn (xt/x:get-key client "send"))
  (return (send-fn client input)))

(defn.xt add-listeners
  "takes the client and a map of handlers"
  {:added "4.1.3"}
  [client m]
  (var add-fn (xt/x:get-key client "add_listeners"))
  (return (add-fn client m)))
