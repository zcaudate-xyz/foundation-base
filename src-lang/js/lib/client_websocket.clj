(ns js.lib.client-websocket
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-websocket :as wsrt]]})

(defn.js default-connect
  "connects using a provided async connector or the host WebSocket constructor"
  {:added "4.1.3"}
  [source url]
  (var connect-fn (xt/x:get-key source "connect"))
  (when (xt/x:is-function? connect-fn)
    (return (connect-fn url)))
  (var ctor (or (xt/x:get-key source "WebSocket")
                WebSocket))
  (when (not (xt/x:is-function? ctor))
    (xt/x:err "JS websocket client missing connect implementation"))
  (return (new ctor url)))

(defn.js client
  "wraps a raw js websocket object with the websocket client protocol"
  {:added "4.1.3"}
  [raw]
  (return (wsrt/client-create raw {})))

(defn.js driver
  "wraps a js websocket connector with the websocket driver protocol"
  {:added "4.1.3"}
  [raw]
  (var source (:? (xt/x:is-function? raw)
                  {"connect" raw}
                  (xt/x:obj-clone (or raw {}))))
  (var connect-sync-fn (xt/x:get-key source "connect_sync"))
  (when (and (xt/x:nil? (xt/x:get-key source "connect"))
             (xt/x:is-function? connect-sync-fn))
    (xt/x:set-key source
                  "connect"
                  (fn [url]
                    (return (connect-sync-fn url)))))
  (xt/x:del-key source "connect_sync")
  (return
   (wsrt/driver-create
    {"connect" (fn [url]
                 (return (-/default-connect source url)))})))
