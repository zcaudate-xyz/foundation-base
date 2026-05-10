(ns lua.lib.client-websocket
  (:require [hara.lang :as l]))

(l/script :lua.nginx
  {:require [[xt.lang.spec-base :as xt]
             [lua.nginx.ws-client :as ngxws]
             [xt.protocol.impl.client-websocket :as wsrt]]})

(defn.lua default-connect
  "connects using async or sync connector behaviour"
  {:added "4.1.3"}
  [source url]
  (var connect-fn (xt/x:get-key source "connect"))
  (when (xt/x:is-function? connect-fn)
    (return (connect-fn url)))
  (return (-/default-connect-sync source url)))

(defn.lua default-connect-sync
  "connects synchronously using a provided websocket connector or resty client"
  {:added "4.1.3"}
  [source url]
  (var connect-sync-fn (xt/x:get-key source "connect_sync"))
  (when (xt/x:is-function? connect-sync-fn)
    (return (connect-sync-fn url)))
  (var client (ngxws/new))
  (local '[ok err] (. client (connect url (or (xt/x:get-key source "connect_options")
                                              {}))))
  (when (not ok)
    (xt/x:err (xt/x:cat "Failed to connect lua websocket client - "
                        (xt/x:to-string err))))
  (return client))

(defn.lua default-send-sync
  "sends a websocket text frame through resty client or a generic send method"
  {:added "4.1.3"}
  [raw payload]
  (var send-text-fn (xt/x:get-key raw "send_text"))
  (if (xt/x:is-function? send-text-fn)
    (return (send-text-fn payload))
    (return (wsrt/default-send-sync raw payload))))

(defn.lua client
  "wraps a raw lua websocket object with the websocket client protocol"
  {:added "4.1.3"}
  [raw]
  (return
   (wsrt/client-create
    raw
    {"send_sync" -/default-send-sync})))

(defn.lua driver
  "wraps a lua websocket connector with the websocket driver protocol"
  {:added "4.1.3"}
  [raw]
  (var source (:? (xt/x:is-function? raw)
                  {"connect_sync" raw}
                  (xt/x:obj-clone (or raw {}))))
  (return
   (wsrt/driver-create
     {"connect" (fn [url]
                 (var connect-fn (xt/x:get-key source "connect"))
                 (when (xt/x:is-function? connect-fn)
                   (return (connect-fn url)))
                 (var connect-sync-fn (xt/x:get-key source "connect_sync"))
                 (when (xt/x:is-function? connect-sync-fn)
                   (return (connect-sync-fn url)))
                 (var client (ngxws/new))
                 (local '[ok err] (. client (connect url (or (xt/x:get-key source "connect_options")
                                                            {}))))
                 (when (not ok)
                   (xt/x:err (xt/x:cat "Failed to connect lua websocket client - "
                                       (xt/x:to-string err))))
                 (return client))
      "connect_sync" (fn [url]
                       (var connect-sync-fn (xt/x:get-key source "connect_sync"))
                       (when (xt/x:is-function? connect-sync-fn)
                         (return (connect-sync-fn url)))
                       (var client (ngxws/new))
                       (local '[ok err] (. client (connect url (or (xt/x:get-key source "connect_options")
                                                                  {}))))
                       (when (not ok)
                         (xt/x:err (xt/x:cat "Failed to connect lua websocket client - "
                                             (xt/x:to-string err))))
                       (return client))
      "client_impl" {"send_sync" -/default-send-sync}})))
