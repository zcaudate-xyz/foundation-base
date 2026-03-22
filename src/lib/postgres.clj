(ns lib.postgres
  (:require [lib.docker :as docker]
            [lib.jdbc :as jdbc]
            [lib.postgres.connection :as conn]
            [std.json :as json]
            [std.lib.atom]
            [std.lib.collection]
            [std.lib.component]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.impl :refer [defimpl]]
            [std.lib.network]))

(defn start-pg-temp-init
  "initialises a temp database"
  {:added "4.0"}
  ([{:keys [dbname] :as pg}]
   (when-not dbname (std.lib.foundation/error "Missing dbname"))
   (with-open [conn (conn/conn-create (assoc pg :dbname "postgres"))]
     (with-open [c (.getConnection conn)]
       (let [args (jdbc/fetch c [(format "SELECT 1 FROM pg_database WHERE datname = '%s'" dbname)])]
         (when (empty? args)
           (jdbc/execute c [(format "CREATE DATABASE \"%s\"" dbname)])))))))

(defn wait-for-pg
  "waits for the postgres database to come online"
  {:added "4.0"}
  [{:keys [instance host port container temp] :as pg} limit sleep]
  (let [_   (if (zero? limit) (std.lib.foundation/error "Database not responsive"))
        res (try (with-open [conn (conn/conn-create (assoc pg :dbname "postgres"))])
                 (catch com.impossibl.postgres.jdbc.PGSQLSimpleException e
                   (Thread/sleep sleep)
                   :error))]
    (if (= res :error)
      (recur pg (dec limit) sleep))))

(defn start-pg-raw
  "starts the database"
  {:added "4.0"}
  ([{:keys [instance host port container temp] :as pg}]
   (when container
     (std.lib.network/wait-for-port host port {:timeout 5000})
     (wait-for-pg pg 10 1000))
   (when temp
     (start-pg-temp-init pg))
   (when-not @instance
     (reset! instance (conn/conn-create pg)))
   pg))

(def ^{:arglists '([pg])} start-pg (std.lib.component/wrap-start start-pg-raw
                                                    [{:key :container
                                                      :setup     docker/start-runtime
                                                      :teardown  docker/stop-runtime}]))

(defn stop-pg-temp-teardown
  "tears down a temp database"
  {:added "4.0"}
  ([{:keys [dbname] :as pg}]
   (when-not dbname (std.lib.foundation/error "Missing dbname"))
   (with-open [conn (conn/conn-create (assoc pg :dbname "postgres"))]
     (with-open [c (.getConnection conn)]
       (let [args (jdbc/fetch c [(format "SELECT 1 FROM pg_database WHERE datname = '%s'" dbname)])]
         (when (not (empty? args))
           (jdbc/execute c [(format "DROP DATABASE \"%s\"" dbname)])))))))

(defn stop-pg-raw
  "stops the postgres runtime"
  {:added "4.0"}
  ([{:keys [instance notifications temp] :as pg}]
   (if @instance
     (std.lib.atom/swap-return! instance (fn [conn]
                                [(doto conn (conn/conn-close)) nil])))
   (std.lib.collection/map-vals (fn [{:keys [raw]}]
                 (std.lib.env/close raw))
               @notifications)
   (reset! notifications {})
   (when (and temp (not= temp :create))
     (stop-pg-temp-teardown pg))
   pg))

(def ^{:arglists '([pg])}
  stop-pg
  (std.lib.component/wrap-stop stop-pg-raw
               [{:key :container
                 :setup     docker/start-runtime
                 :teardown  docker/stop-runtime}]))
