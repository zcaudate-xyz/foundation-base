(ns xt.protocol.client-websocket
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt IClientWebsocketDriver
  ["connect"])

(def.xt IClientWebsocket
  ["disconnect"
   "send"
   "add_listener"])

(def.xt IClientWebsocketRuntimeDriver
  (proto/iface-combine [-/IClientWebsocketDriver]))

(def.xt IClientWebsocketRuntime
  (proto/iface-combine [-/IClientWebsocket]))
