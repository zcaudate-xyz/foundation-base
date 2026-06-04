(ns xt.protocol.db-graph
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt IDbGraphDriver
  ["create"])

(def.xt IDbGraph
  ["pull"
   "pull_sync"
   "record_add"
   "record_delete"])

(def.xt IDbGraphRuntimeDriver
  (proto/iface-combine [-/IDbGraphDriver]))

(def.xt IDbGraphRuntime
  (proto/iface-combine [-/IDbGraph]))
