(ns lib.postgres.impl.impossibl
  (:require [lib.jdbc :as jdbc]
            [lib.jdbc.protocol :as jdbc.protocol]
            [std.json :as json]
            [std.lib :as h])
  (:import (javax.sql PooledConnection)
           (com.impossibl.postgres.jdbc PGConnectionPoolDataSource
                                        PGDirectConnection
                                        PGDataSource
                                        PGArray
                                        PGBuffersArray
                                        PGBuffersStruct$Binary
                                        PGSQLSimpleException)
           (com.impossibl.postgres.api.data InetAddr)
           (com.impossibl.postgres.api.jdbc PGNotificationListener)))

(extend-protocol jdbc.protocol/ISQLResultSetReadColumn
  PGArray
  (-from-sql-type [this conn metadata i]
    (seq (.getArray this)))

  PGBuffersArray
  (-from-sql-type [this conn metadata i]
    (map #(jdbc.protocol/-from-sql-type
           %
           conn metadata 0)
         (.getArray this)))

  PGBuffersStruct$Binary
  (-from-sql-type [this conn metadata i]
    (seq (.getAttributes this))))

(extend-type com.impossibl.postgres.api.data.InetAddr
  jdbc.protocol/ISQLResultSetReadColumn
  (-from-sql-type [this conn metadata i]
    (str this)))

(defn create-pool
  [{:keys [host port user pass dbname]
    :or {host (or (System/getenv "DEFAULT_RT_POSTGRES_HOST")
                  "127.0.0.1")
         port (h/parse-long
               (or (System/getenv "DEFAULT_RT_POSTGRES_PORT")
                   "5432"))
         user (or (System/getenv "DEFAULT_RT_POSTGRES_USER")
                  "postgres")
         pass (or (System/getenv "DEFAULT_RT_POSTGRES_PASS")
                  "postgres")}
    :as m}]
  (let [ds (doto (PGConnectionPoolDataSource.)
             (.setHost host)
             (.setPort port)
             (cond-> dbname (.setDatabaseName dbname)))
        ds (cond-> ds
             user (doto (.setUser user))
             pass (doto (.setPassword pass)))]
    (.getPooledConnection ds)))

(defn execute-statement
  [pool input execute]
  (try (with-open [conn (.getConnection pool)]
         (execute conn input))
       (catch Throwable e
         (cond (instance? java.sql.BatchUpdateException e)
               (throw e)

               (instance? PGSQLSimpleException e)
               (let [detail (.getDetail ^PGSQLSimpleException e)]
                 (if detail
                   (throw (ex-info (.getMessage e)
                                   (merge {:ex/type  :rt.postgres/exception}
                                          (try (json/read detail json/+keyword-spear-mapper+)
                                               (catch Throwable t
                                                 {:message  detail})))))
                   (throw e)))

               :else
               (if (= (.getMessage e)
                      "No result set available")
                 nil
                 (throw e))))))

(defn ^PGNotificationListener notify-listener
  "creates a notification listener"
  {:added "4.0"}
  [{:keys [on-notify
           on-close]}]
  (proxy [com.impossibl.postgres.api.jdbc.PGNotificationListener] []
    (notification [id ch payload]
      (if on-notify (on-notify id ch payload)))
    (closed []
      (if on-close (on-close)))))

(defn create-notify
  "creates a notify channel"
  {:added "4.0"}
  [{:keys [host port user pass dbname]
    :or {host (or (System/getenv "DEFAULT_RT_POSTGRES_HOST")
                   "127.0.0.1")
          port (h/parse-long
                (or (System/getenv "DEFAULT_RT_POSTGRES_PORT")
                    "5432"))
          user (or (System/getenv "DEFAULT_RT_POSTGRES_USER")
                   "postgres")
          pass (or (System/getenv "DEFAULT_RT_POSTGRES_PASS")
                   "postgres")}}
   {:keys [channel on-close on-notify]
    :as m}]
  (let [listener (notify-listener m)
        ds (doto (PGDataSource.)
             (.setHost host)
             (.setPort port)
             (cond-> dbname (.setDatabaseName dbname)))
        ds (cond-> ds
             user (doto (.setUser user))
             pass (doto (.setPassword pass)))
        ^PGDirectConnection conn (.getConnection ds)]
    (.addNotificationListener conn
                              channel
                              listener)
    (jdbc/execute conn (str "LISTEN " channel ";"))
    [conn listener]))
