(ns xt.protocol.redis-connection
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt IRedisConnectionDriver
  ["connect"])

(def.xt IRedisConnection
  ["disconnect"
   "exec"])

(def.xt IRedisRuntimeDriver
  (proto/iface-combine [-/IRedisConnectionDriver]))

(def.xt IRedisRuntimeConnection
  (proto/iface-combine [-/IRedisConnection]))
