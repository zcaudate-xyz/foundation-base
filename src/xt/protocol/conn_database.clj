(ns xt.protocol.conn-database
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defabstract.xt connect
  "opens a backend-defined database connection"
  {:added "4.1"}
  [opts callback]
  (xt/x:err "xt.protocol.conn-database/connect is abstract"))

(defabstract.xt disconnect
  "closes a backend-defined database connection"
  {:added "4.1"}
  [conn callback]
  (xt/x:err "xt.protocol.conn-database/disconnect is abstract"))

(defabstract.xt query
  "runs an asynchronous database query"
  {:added "4.1"}
  [conn input callback]
  (xt/x:err "xt.protocol.conn-database/query is abstract"))

(defabstract.xt query-sync
  "runs a synchronous database query"
  {:added "4.1"}
  [conn input]
  (xt/x:err "xt.protocol.conn-database/query-sync is abstract"))
