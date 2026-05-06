(ns python.lib.driver-sqlite-test
  (:require [hara.lang :as l]
            [python.lib.driver-sqlite :refer :all])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as sql]
             [python.lib.driver-sqlite :as py-sqlite]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.lib.driver-sqlite/query-returns-rows? :added "4.1"}
(fact "detects sqlite queries that should return rows"

  (!.py
    [(py-sqlite/query-returns-rows? "SELECT 1;")
     (py-sqlite/query-returns-rows? "  pragma table_info(test);")
     (py-sqlite/query-returns-rows? "WITH sample AS (SELECT 1) SELECT * FROM sample;")
     (py-sqlite/query-returns-rows? "VALUES (1);")
     (py-sqlite/query-returns-rows? "INSERT INTO test VALUES (1);")])
  => [true true true true false])

^{:refer python.lib.driver-sqlite/raw-query :added "4.1"}
(fact "runs raw sqlite queries and normalises the result shape"

  (!.py
    (var db (py-sqlite/connect-constructor {}))
    (var one (py-sqlite/raw-query db "SELECT 1;"))
    (py-sqlite/raw-query db "CREATE TABLE test (id INTEGER, name TEXT);")
    (py-sqlite/raw-query db "INSERT INTO test (id, name) VALUES (1, 'alpha');")
    (var name (py-sqlite/raw-query db "SELECT name FROM test;"))
    [one name])
  => [1 "alpha"])

^{:refer python.lib.driver-sqlite/wrap-connection :added "4.1"}
(fact "wraps sqlite connections with the SQL runtime protocol"

  (!.py
    (var conn (py-sqlite/wrap-connection
               (py-sqlite/connect-constructor {})))
    (var created (sql/query conn "CREATE TABLE test (id INTEGER, name TEXT);"))
    (sql/query conn "INSERT INTO test (id, name) VALUES (1, 'alpha');")
    (var name (sql/query-sync conn "SELECT name FROM test;"))
    [(sql/connection? conn)
     created
     name
     (sql/disconnect conn)])
  => [true [] "alpha" true])

^{:refer python.lib.driver-sqlite/connect-constructor :added "4.1"}
(fact "constructs sqlite3 connections with an in-memory default"

  (!.py
    (var db (py-sqlite/connect-constructor {}))
    (py-sqlite/raw-query db "SELECT 3;"))
  => 3)

^{:refer python.lib.driver-sqlite/driver :added "4.1"}
(fact "connects through the driver wrapper"

  (notify/wait-on :python
    (spec-promise/x:promise-then
     (sql/connect (py-sqlite/driver) {})
     (fn [conn]
       (var created (sql/query conn "CREATE TABLE test (id INTEGER);"))
       (sql/query conn "INSERT INTO test (id) VALUES (7);")
       (var out (sql/query-sync conn "SELECT id FROM test;"))
       (repl/notify
        [(sql/connection? conn)
         created
         out
         (sql/disconnect conn)]))))
  => [true [] 7 true])
