(ns lib.postgres-test
  (:use code.test)
  (:require [lib.postgres :as base]
            [lib.postgres.connection :as conn]
            [lib.jdbc :as jdbc]
            [lib.jdbc.protocol :as protocol]
            [std.lib :as h])
  (:import (javax.sql PooledConnection)))

(defn mock-conn []
  (reify java.sql.Connection
    (close [_])
    (createStatement [_]
      (reify java.sql.Statement
        (close [_])
        (execute [_ _] true)
        (executeQuery [_ _]
          (reify java.sql.ResultSet
            (next [_] false)
            (close [_])))))))

(defn mock-pooled-conn []
  (reify PooledConnection
    (close [_])
    (getConnection [_] (mock-conn))
    (addConnectionEventListener [_ _])
    (removeConnectionEventListener [_ _])))

^{:refer lib.postgres/start-pg-temp-init :added "4.0"}
(fact "initialises a temp database"
  (with-redefs [conn/conn-create (constantly (mock-pooled-conn))
                jdbc/fetch (constantly [])
                jdbc/execute (constantly 1)]
    (base/start-pg-temp-init {:dbname "test"}))
  => 1)

^{:refer lib.postgres/wait-for-pg :added "4.0"}
(fact "waits for the postgres database to come online"
  (with-redefs [conn/conn-create (constantly (mock-pooled-conn))]
    (base/wait-for-pg {} 1 1))
  => nil)

^{:refer lib.postgres/start-pg-raw :added "4.0"}
(fact "starts the database"
  (with-redefs [conn/conn-create (constantly (mock-pooled-conn))]
    (base/start-pg-raw {:instance (atom nil) :temp false}))
  => map?)

^{:refer lib.postgres/stop-pg-temp-teardown :added "4.0"}
(fact "tears down a temp database"
  (with-redefs [conn/conn-create (constantly (mock-pooled-conn))
                jdbc/fetch (constantly [{:a 1}])
                jdbc/execute (constantly 1)]
    (base/stop-pg-temp-teardown {:dbname "test-temp-db"}))
  => 1)

^{:refer lib.postgres/stop-pg-raw :added "4.0"}
(fact "stops the postgres runtime"
  (with-redefs [conn/conn-close (constantly nil)]
    (base/stop-pg-raw {:instance (atom (mock-pooled-conn)) :notifications (atom {})}))
  => map?)
