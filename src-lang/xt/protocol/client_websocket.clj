(ns xt.protocol.client-websocket
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt IClientWebsocketDriver
  ["connect"
   "connect_sync"])

(def.xt IClientWebsocket
  ["disconnect"
   "disconnect_sync"
   "send"
   "send_sync"
   "add_listener"
   "add_listener_sync"])

(def.xt IClientWebsocketRuntimeDriver
  (proto/iface-combine [-/IClientWebsocketDriver]))

(def.xt IClientWebsocketRuntime
  (proto/iface-combine [-/IClientWebsocket]))
