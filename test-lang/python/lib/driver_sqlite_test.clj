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

^{:refer python.lib.driver-sqlite/query-multi-statement? :added "4.1"}
(fact "detects multi-statement sqlite scripts"

  (!.py
    [(py-sqlite/query-multi-statement? "SELECT 1;")
     (py-sqlite/query-multi-statement? "CREATE TABLE test (id INTEGER); INSERT INTO test VALUES (1);")
     (py-sqlite/query-multi-statement? "CREATE TABLE test (id INTEGER)\nINSERT INTO test VALUES (1)")])
  => [false true false])

^{:refer python.lib.driver-sqlite/raw-query :added "4.1"}
(fact "runs raw sqlite queries and normalises the result shape"

  (!.py
    (var db (py-sqlite/connect-constructor {}))
    (var one (py-sqlite/raw-query db "SELECT 1;"))
    (py-sqlite/raw-query db "CREATE TABLE test (id INTEGER, name TEXT);")
     (py-sqlite/raw-query db "INSERT INTO test (id, name) VALUES (1, 'alpha');")
     (var name (py-sqlite/raw-query db "SELECT name FROM test;"))
    (var json-array (py-sqlite/raw-query db "SELECT json_array(1, 2, 3);"))
    [one name json-array])
  => [1 "alpha" [1 2 3]])

^{:refer python.lib.driver-sqlite/raw-query.multi :added "4.1"}
(fact "runs multi-statement sqlite scripts through executescript"

  (!.py
    (var db (py-sqlite/connect-constructor {}))
    (py-sqlite/raw-query db
                         "CREATE TABLE test (id INTEGER, name TEXT);
                          INSERT INTO test (id, name) VALUES (1, 'alpha');
                          INSERT INTO test (id, name) VALUES (2, 'beta');")
    (py-sqlite/raw-query db "SELECT COUNT(*) FROM test;"))
  => 2)

^{:refer python.lib.driver-sqlite/wrap-connection :added "4.1"}
(fact "wraps sqlite connections with promise query and sync query-sync support"

  (!.py
    (var conn (py-sqlite/wrap-connection
               (py-sqlite/connect-constructor {})))
     (sql/query-sync conn "CREATE TABLE test (id INTEGER, name TEXT);")
     (sql/query-sync conn "INSERT INTO test (id, name) VALUES (1, 'alpha');")
     [(sql/connection? conn)
      (spec-promise/x:promise-native? (sql/query conn "SELECT name FROM test;"))
      (sql/query-sync conn "SELECT id FROM test;")
      (sql/disconnect conn)])
  => [true true 1 true])

^{:refer python.lib.driver-sqlite/wrap-connection.query-sync-script :added "4.1"}
(fact "query-sync supports sqlite bootstrap scripts"

  (!.py
    (var conn (py-sqlite/wrap-connection
               (py-sqlite/connect-constructor {})))
    (sql/query-sync conn
                    "CREATE TABLE test (id INTEGER, name TEXT);
                     INSERT INTO test (id, name) VALUES (1, 'alpha');
                     INSERT INTO test (id, name) VALUES (2, 'beta');")
    [(sql/query-sync conn "SELECT COUNT(*) FROM test;")
     (sql/disconnect conn)])
  => [2 true])

^{:refer python.lib.driver-sqlite/connect-constructor :added "4.1"}
(fact "constructs sqlite3 connections with an in-memory default"

  (!.py
    (var db (py-sqlite/connect-constructor {}))
    (py-sqlite/raw-query db "SELECT 3;"))
  => 3)

^{:refer python.lib.driver-sqlite/driver :added "4.1"}
(fact "connects through the driver wrapper with async query and sync query-sync support"

  (notify/wait-on [:python 5000]
    (spec-promise/x:promise-then
     (sql/connect (py-sqlite/driver) {})
     (fn [conn]
       (sql/query-sync conn "CREATE TABLE test (id INTEGER);")
       (sql/query-sync conn "INSERT INTO test (id) VALUES (7);")
       (spec-promise/x:promise-then
        (sql/query conn "SELECT id FROM test;")
        (fn [out]
          (repl/notify
           [(sql/connection? conn)
            out
            (sql/query-sync conn "SELECT id FROM test;")
            (sql/disconnect conn)]))))))
  => [true 7 7 true])
