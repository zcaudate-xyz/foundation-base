(ns python.lib.client-websocket
  (:require [hara.lang :as l]))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-websocket :as wsrt]]})

(defn.py default-connect
  "connects using async or sync connector behaviour"
  {:added "4.1.3"}
  [source url]
  (var connect-fn (xt/x:get-key source "connect"))
  (when (xt/x:is-function? connect-fn)
    (return (connect-fn url)))
  (return (-/default-connect-sync source url)))

(defn.py default-connect-sync
  "connects synchronously using a provided websocket connector"
  {:added "4.1.3"}
  [source url]
  (var connect-sync-fn (xt/x:get-key source "connect_sync"))
  (when (xt/x:is-function? connect-sync-fn)
    (return (connect-sync-fn url)))
  (xt/x:err "Python websocket client missing connect_sync implementation"))

(defn.py client
  "wraps a raw python websocket object with the websocket client protocol"
  {:added "4.1.3"}
  [raw]
  (return (wsrt/client-create raw {})))

(defn.py driver
  "wraps a python websocket connector with the websocket driver protocol"
  {:added "4.1.3"}
  [raw]
  (var source (:? (xt/x:is-function? raw)
                  {"connect_sync" raw}
                  (xt/x:obj-clone (or raw {}))))
  (return
   (wsrt/driver-create
    {"connect" (fn [url]
                 (return (-/default-connect source url)))
     "connect_sync" (fn [url]
                      (return (-/default-connect-sync source url)))
     "client_impl" (or (xt/x:get-key source "client_impl")
                       (xt/x:get-key source "client-impl")
                       {})})))
