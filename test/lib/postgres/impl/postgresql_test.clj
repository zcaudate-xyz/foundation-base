(ns lib.postgres.impl.postgresql-test
  (:use code.test)
  (:require [lib.postgres.impl.postgresql :refer :all])
  (:import (javax.sql PooledConnection)))

(defn mock-conn []
  (reify java.sql.Connection
    (close [_])))

(defn mock-pooled-conn []
  (reify PooledConnection
    (close [_])
    (getConnection [_] (mock-conn))
    (addConnectionEventListener [_ _])
    (removeConnectionEventListener [_ _])))

^{:refer lib.postgres.impl.postgresql/create-pool :added "4.1"}
(fact "creates a pooled connection function"
  create-pool
  => fn?)

^{:refer lib.postgres.impl.postgresql/execute-statement :added "4.1"}
(fact "executes a statement against a pooled connection"
  (execute-statement (mock-pooled-conn) :input (fn [_ input] input))
  => :input)
(fact "create-pool returns a pooled connection from config"
  ^:hidden

  (try (create-pool {:host "127.0.0.1" :port 5432 :dbname "test"})
       (catch Throwable t t))
  => (any org.postgresql.ds.PGPooledConnection
          java.sql.SQLException
          org.postgresql.util.PSQLException))

^{:refer lib.postgres.impl.postgresql/execute-statement :added "4.1"}
(fact "execute-statement runs execute fn with a connection from pool"
  ^:hidden

  (let [results [{:col 42}]
        mock-conn (reify java.sql.Connection
                    (close [_]))
        mock-pool (reify javax.sql.PooledConnection
                    (getConnection [_] mock-conn))]
    (execute-statement mock-pool "select 1" (constantly results)))
  => [{:col 42}])
