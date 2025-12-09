(ns lib.postgres.connection
  (:require [lib.jdbc :as jdbc]
            [lib.jdbc.protocol :as jdbc.protocol]
            [lib.jdbc.resultset :as jdbc.rs]
            [std.json :as json]
            [std.lib :as h])
  (:import (java.sql DriverManager
                     ResultSet)
           (javax.sql PooledConnection)
           (java.net InetSocketAddress)
           (com.impossibl.postgres.system Settings)
           (com.impossibl.postgres.jdbc PGDirectConnection
                                        PGDataSource
                                        PGArray
                                        PGBuffersArray
                                        PGBuffersStruct$Binary
                                        PGSQLSimpleException)
           (com.impossibl.postgres.api.data InetAddr)
           (com.impossibl.postgres.api.jdbc PGNotificationListener)
           (org.postgresql.jdbc PgConnection
                                PgArray)
           (org.postgresql.util PGobject)))



(def ^:dynamic *execute* nil)

;;
;; the postgres runtime is pretty special because
;; a lot of features where put into it, including
;; a while new language compilation cycle emulating
;; graphql but compiling directly to sql.
;;
;; - it is an embeded runtime
;; - code is data
;; - it is typechecked
;; - it allows running multiple languages
;; - it is schema based
;; - execution is acid (great for transactions)
;; 

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
    (seq (.getAttributes this)))

  PgArray
  (-from-sql-type [this conn metadata i]
    (seq (.getArray this)))

  PGobject
  (-from-sql-type [this conn metadata i]
    (.getValue this)))


(extend-type com.impossibl.postgres.api.data.InetAddr
  jdbc.protocol/ISQLResultSetReadColumn
  (-from-sql-type [this conn metadata i]
    (str this)))

(defn ^PooledConnection conn-create
  "creates a pooled connection"
  {:added "4.0"}
  ([{:keys [host port user pass dbname vendor]
     :or {host (or (System/getenv "DEFAULT_RT_POSTGRES_HOST")
                   "127.0.0.1")
          port (h/parse-long
                (or (System/getenv "DEFAULT_RT_POSTGRES_PORT")
                    "5432"))
          user (or (System/getenv "DEFAULT_RT_POSTGRES_USER")
                   "postgres")
          pass (or (System/getenv "DEFAULT_RT_POSTGRES_PASS")
                   "postgres")
          vendor :impossibl}
     :as m}]
   (if (= vendor :postgresql)
     (let [ds (doto (org.postgresql.ds.PGConnectionPoolDataSource.)
                (.setServerName host)
                (.setPortNumber port)
                (cond-> dbname (.setDatabaseName dbname)))
           ds (cond-> ds
                user (doto (.setUser user))
                pass (doto (.setPassword pass)))]
       (.getPooledConnection ds))
     (let [ds (doto (com.impossibl.postgres.jdbc.PGConnectionPoolDataSource.)
                (.setHost host)
                (.setPort port)
                (cond-> dbname (.setDatabaseName dbname)))
           ds (cond-> ds
                user (doto (.setUser user))
                pass (doto (.setPassword pass)))]
       (.getPooledConnection ds)))))

(defn conn-close
  "closes a connection"
  {:added "4.0"}
  [conn]
  (cond (instance? javax.sql.PooledConnection conn)
        (.close ^javax.sql.PooledConnection conn)

        (instance? java.lang.AutoCloseable conn)
        (.close ^java.lang.AutoCloseable conn)

        (nil? conn)
        conn
        
        :else
        (h/error "Not closeable." {:type (type conn)
                                  :input conn})))

(defn conn-execute
  "executes a command"
  {:added "4.0"}
  ([^PooledConnection pool input]
   (conn-execute pool input (or *execute*
                                jdbc/execute)))
  ([^PooledConnection pool input execute]
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
                  (throw e)))))))

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

(defn notify-create
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
