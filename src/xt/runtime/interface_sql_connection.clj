(ns xt.runtime.interface-connection-sql
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt ISqlConnectionDriver
  ["connect"])

(def.xt ISqlConnection
  ["disconnect"
   "query"
   "query_sync"])

(def.xt ISqlRuntimeDriver
  (proto/iface-combine [-/ISqlConnectionDriver]))

(def.xt ISqlRuntimeConnection
  (proto/iface-combine [-/ISqlConnection]))
