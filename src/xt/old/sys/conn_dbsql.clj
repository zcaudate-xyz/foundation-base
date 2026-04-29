(ns xt.old.sys.conn-dbsql
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lib.sql-connection :as sql]]})

(defn.xt connect
  "connects to a database"
  {:added "4.0"}
  [driver opts]
  (when (xt/x:nil? opts)
    (:= opts {}))
  (return (sql/connect driver opts)))

(defn.xt disconnect
  "disconnects form database"
  {:added "4.0"}
  [conn]
  (return (sql/disconnect conn)))

(defn.xt query-base
  "calls query without the wrapper"
  {:added "4.0"}
  [conn raw]
  (return (sql/query conn raw)))

(defn.xt query
  "sends a query"
  {:added "4.0"}
  [conn raw]
  (return (sql/query conn raw)))

(defn.xt query-sync
  "sends a synchronous query"
  {:added "4.0"}
  [conn raw]
  (return (sql/query-sync conn raw)))
