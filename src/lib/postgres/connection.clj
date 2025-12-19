(ns lib.postgres.connection
  (:require [lib.jdbc :as jdbc]
            [lib.jdbc.protocol :as jdbc.protocol]
            [lib.jdbc.resultset :as jdbc.rs]
            [std.json :as json]
            [std.lib :as h])
  (:import (java.sql DriverManager
                     ResultSet)
           (javax.sql PooledConnection)
           (java.net InetSocketAddress)))

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

(def +impls+
  (atom {:impossibl  {:status :pending
                      :ns 'lib.postgres.impl.impossibl}
         :postgresql {:status :pending
                      :ns 'lib.postgres.impl.postgresql}}))

(defn load-impl [vendor]
  (let [entry (get @+impls+ vendor)]
    (if (= (:status entry) :loaded)
      entry
      (try
        (require (:ns entry))
        (swap! +impls+ assoc-in [vendor :status] :loaded)
        (get @+impls+ vendor)
        (catch Throwable t
          (swap! +impls+ assoc-in [vendor :status] :error)
          (h/error "Implementation not found" {:vendor vendor}))))))

(defn- get-env [k]
  (System/getenv k))

(defn default-vendor []
  (let [val (get-env "DEFAULT_RT_POSTGRES_IMPL")]
    (case val
      "lib.postgres.impl.postgresql" :postgresql
      "lib.postgres.impl.impossibl" :impossibl
      :impossibl)))

(defn ^PooledConnection conn-create
  "creates a pooled connection"
  {:added "4.0"}
  ([m]
   (let [vendor (or (:vendor m) (default-vendor))
         {:keys [ns]} (load-impl vendor)
         create-pool (ns-resolve ns 'create-pool)]
     (create-pool (assoc m :vendor vendor)))))

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
   (let [vendor (if (let [cls-name (.getName (class pool))]
                      (or (.startsWith cls-name "org.postgresql")
                          (.startsWith cls-name "lib.postgres.impl.postgresql")))
                  :postgresql
                  :impossibl)
         {:keys [ns]} (load-impl vendor)
         execute-statement (ns-resolve ns 'execute-statement)]
     (execute-statement pool input execute))))

(defn notify-listener
  "creates a notification listener"
  {:added "4.0"}
  [m]
  (let [{:keys [ns]} (load-impl :impossibl)
        notify-listener (ns-resolve ns 'notify-listener)]
    (notify-listener m)))

(defn notify-create
  "creates a notify channel"
  {:added "4.0"}
  ([{:keys [vendor] :or {vendor :impossibl} :as m} config]
   (if (not= vendor :impossibl)
     (h/error "Only impossibl driver supports notifications" {:vendor vendor})
     (let [{:keys [ns]} (load-impl :impossibl)
           create-notify (ns-resolve ns 'create-notify)]
       (create-notify m config)))))
