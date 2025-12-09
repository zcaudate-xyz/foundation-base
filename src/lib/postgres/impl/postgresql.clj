(ns lib.postgres.impl.postgresql
  (:require [lib.jdbc :as jdbc]
            [lib.jdbc.protocol :as jdbc.protocol]
            [std.lib :as h])
  (:import (javax.sql PooledConnection)
           (org.postgresql.jdbc PgConnection
                                PgArray)
           (org.postgresql.util PGobject)
           (org.postgresql.ds PGConnectionPoolDataSource)))

(extend-protocol jdbc.protocol/ISQLResultSetReadColumn
  PgArray
  (-from-sql-type [this conn metadata i]
    (seq (.getArray this)))

  PGobject
  (-from-sql-type [this conn metadata i]
    (.getValue this)))

(defn create-pool
  [{:keys [host port user pass dbname]
    :as m}]
  (let [ds (doto (PGConnectionPoolDataSource.)
             (.setServerName host)
             (.setPortNumber port)
             (cond-> dbname (.setDatabaseName dbname)))
        ds (cond-> ds
             user (doto (.setUser user))
             pass (doto (.setPassword pass)))]
    (.getPooledConnection ds)))

(defn execute-statement
  [pool input execute]
  (with-open [conn (.getConnection pool)]
    (execute conn input)))
