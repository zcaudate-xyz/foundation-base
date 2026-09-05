(ns lang.runtime.basic
  (:require [lang.runtime.basic.server-basic :as server-basic]
            [lang.runtime.basic.server-websocket :as server]
            [lang.runtime.basic.type-basic :as basic]
            [lang.runtime.basic.type-container :as container]
            [lang.runtime.basic.type-oneshot :as oneshot]
            [lang.runtime.basic.type-remote-port :as remote-port]
            [lang.runtime.basic.type-twostep :as twostep]
            [lang.runtime.basic.type-websocket :as websocket]
            [std.concurrent :as cc]
            [std.lib.foundation :as f]))

(f/intern-in
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
