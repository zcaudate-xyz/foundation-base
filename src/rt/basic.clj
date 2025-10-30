(ns rt.basic
  (:require [rt.basic.server-websocket :as server]
            [rt.basic.server-basic :as server-basic]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-container :as container]
            [rt.basic.type-twostep :as twostep]
            [rt.basic.type-remote-port :as remote-port]
            [rt.basic.type-websocket :as websocket]
            [std.lib :as h]
            [std.concurrent :as cc]))

(h/intern-in
 basic/rt-basic-port
 basic/rt-basic
 basic/rt-basic:create
 
 oneshot/rt-oneshot
 oneshot/rt-oneshot:create
 twostep/rt-twostep
 twostep/rt-twostep:create
 remote-port/rt-remote-port
 remote-port/rt-remote-port:create
 websocket/rt-websocket
 websocket/rt-websocket:create)

(defn clean-relay
  "cleans the relay on the server"
  {:added "4.0"}
  [rt]
  (let [record (server-basic/get-server (:id rt)
                                        (:lang rt))]
    (if-let [relay (server-basic/get-relay record)]
      (cc/send relay {:op :clean}))))
