(ns xt.runtime.interface-sql-connection
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.runtime.interface-spec :as spec]]})

(def.xt ISqlConnectionDriver
  ["connect"])

(def.xt ISqlConnection
  ["disconnect"
   "query"
   "query_sync"])

(def.xt ISqlRuntimeDriver
  (spec/iface-combine [-/ISqlConnectionDriver]))

(def.xt ISqlRuntimeConnection
  (spec/iface-combine [-/ISqlConnection]))
