(ns lib.postgres.impl.impossibl-test
  (:use code.test)
  (:require [lib.postgres.impl.impossibl :refer :all]))

^{:refer lib.postgres.impl.impossibl/create-pool :added "4.1"}
(fact "create-pool returns a pooled connection from config"
  ^:hidden

  (try (create-pool {:host "127.0.0.1" :port 5432 :dbname "test"})
       (catch Throwable t t))
  => (any com.impossibl.postgres.jdbc.PGPooledConnection
          java.sql.SQLException))

^{:refer lib.postgres.impl.impossibl/execute-statement :added "4.1"}
(fact "execute-statement runs the execute fn with a connection from pool"
  ^:hidden

  (let [results [{:id 1}]
        mock-conn (reify java.sql.Connection
                    (close [_]))
        mock-pool (reify javax.sql.PooledConnection
                    (getConnection [_] mock-conn))]
    (execute-statement mock-pool "select 1" (constantly results)))
  => [{:id 1}])

^{:refer lib.postgres.impl.impossibl/notify-listener :added "4.1"}
(fact "notify-listener returns a PGNotificationListener proxy"
  ^:hidden

  (notify-listener {})
  => (partial instance? com.impossibl.postgres.api.jdbc.PGNotificationListener))

^{:refer lib.postgres.impl.impossibl/create-notify :added "4.1"}
(fact "create-notify attempts to create a direct notify connection"
  ^:hidden

  (try (create-notify {:dbname "test"} {:channel "ch"})
       (catch Throwable t t))
  => (contains-in
      [com.impossibl.postgres.jdbc.PGDirectConnection]))