(ns lib.postgres.impl.postgresql-test
  (:use code.test)
  (:require [lib.postgres.impl.postgresql :refer :all]))

^{:refer lib.postgres.impl.postgresql/create-pool :added "4.1"}
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