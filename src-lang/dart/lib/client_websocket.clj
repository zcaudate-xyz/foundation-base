(ns dart.lib.client-websocket
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-websocket :as wsrt]]})

(defn.dt default-connect
  "connects using async or sync connector behaviour"
  {:added "4.1.3"}
  [source url]
  (var connect-fn (xt/x:get-key source "connect"))
  (when (xt/x:is-function? connect-fn)
    (return (connect-fn url)))
  (return (-/default-connect-sync source url)))

(defn.dt default-connect-sync
  "connects synchronously using a provided websocket connector"
  {:added "4.1.3"}
  [source url]
  (var connect-sync-fn (xt/x:get-key source "connect_sync"))
  (when (xt/x:is-function? connect-sync-fn)
    (return (connect-sync-fn url)))
  (xt/x:err "Dart websocket client missing connect_sync implementation"))

(defn.dt client
  "wraps a raw dart websocket object with the websocket client protocol"
  {:added "4.1.3"}
  [raw]
  (return (wsrt/client-create raw {})))

(defn.dt driver
  "wraps a dart websocket connector with the websocket driver protocol"
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
