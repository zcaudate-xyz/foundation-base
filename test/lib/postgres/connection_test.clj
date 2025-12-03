(ns lib.postgres.connection-test
  (:use code.test)
  
  (:require [lib.postgres.connection :as conn]
            [lib.jdbc :as jdbc]
            [lib.jdbc.protocol :as protocol])
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

^{:refer lib.postgres.connection/conn-create :added "4.0"}
(fact "creates a pooled connection"
  ^:hidden
  
  (try (conn/conn-create {:dbname "test"})
       (catch Throwable t t))
  => (any java.sql.SQLException
          com.impossibl.postgres.jdbc.PGPooledConnection
          com.impossibl.postgres.jdbc.PGSQLSimpleException))

^{:refer lib.postgres.connection/conn-close :added "4.0"}
(fact "closes a connection"
  ^:hidden
  
  (conn/conn-close (mock-pooled-conn)) => nil)

^{:refer lib.postgres.connection/conn-execute :added "4.0"}
(fact "executes a command"
  ^:hidden
  
  (with-redefs [conn/conn-create (constantly (mock-pooled-conn))]
    (let [pool (conn/conn-create {:dbname "test"})]
      (conn/conn-execute pool "select 1;" (constantly [{:?column? 1}]))))
  => [{:?column? 1}])

^{:refer lib.postgres.connection/notify-listener :added "4.0"}
(fact "creates a notification listener"
  ^:hidden
  
  (conn/notify-listener {})
  => (partial instance? com.impossibl.postgres.api.jdbc.PGNotificationListener))

^{:refer lib.postgres.connection/notify-create :added "4.0"}
(fact "creates a notify channel"
  ^:hidden
  
  (try (conn/notify-create {:dbname "test"} {:channel "ch"})
       (catch Throwable t t))
  => (contains-in
      [com.impossibl.postgres.jdbc.PGDirectConnection]))
