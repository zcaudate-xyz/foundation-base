(ns python.net.conn-sqlite-test
  (:require [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[python.net.conn-sqlite :as sqlite]
             [xt.lang.spec-promise :as p]]})

(fact:global
 {:skip     (not (env/program-exists? "python3"))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.net.conn-sqlite/query-returns-rows? :added "4.1"}
(fact "checks whether a query returns rows"

  (!.py [(sqlite/query-returns-rows? "SELECT 1")
         (sqlite/query-returns-rows? "INSERT INTO foo VALUES (1)")
         (sqlite/query-returns-rows? "PRAGMA table_info(foo)")
         (sqlite/query-returns-rows? "  select 1")])
  => [true false true true])

^{:refer python.net.conn-sqlite/query-multi-statement? :added "4.1"}
(fact "checks whether a query contains multiple statements"

  (!.py [(sqlite/query-multi-statement? "SELECT 1")
         (sqlite/query-multi-statement? "SELECT 1; SELECT 2")
         (sqlite/query-multi-statement? "SELECT 1;  ")
         (sqlite/query-multi-statement? "CREATE TABLE foo (n INT); INSERT INTO foo VALUES (1)")])
  => [false true false true])

^{:refer python.net.conn-sqlite/decode-json-scalar :added "4.1"}
(fact "decodes a json scalar value"

  (!.py [(sqlite/decode-json-scalar "{\"a\": 1}")
         (sqlite/decode-json-scalar "[1, 2, 3]")
         (sqlite/decode-json-scalar "true")
         (sqlite/decode-json-scalar "null")
         (sqlite/decode-json-scalar "hello")])
  => [{"a" 1} [1 2 3] true nil "hello"])

^{:refer python.net.conn-sqlite/raw-query :added "4.1"}
(fact "runs a raw sqlite query"

  (!.py (do (var sqlite3 (python.core/pkg "sqlite3"))
            (var db (. sqlite3 (connect ":memory:" :check_same_thread false)))
            (var out (sqlite/raw-query db "SELECT 1 AS n, 'hello' AS s"))
            (. db (close))
            (return out)))
  => [[1 "hello"]]

  (!.py (do (var sqlite3 (python.core/pkg "sqlite3"))
            (var db (. sqlite3 (connect ":memory:" :check_same_thread false)))
            (var out (sqlite/raw-query db "SELECT 1"))
            (. db (close))
            (return out)))
  => 1)

^{:refer python.net.conn-sqlite/create :added "4.1"}
(fact "creates an sqlite client"

  (sqlite/create {})
  => (contains {"::" "python.net.conn_sqlite/PythonSqliteClient"
                "defaults" {}}))

^{:refer python.net.conn-sqlite/client-connect :added "4.1"}
(fact "connects an sqlite client"

  (!.py (do (var client (sqlite/create {}))
            (sqlite/client-connect client {})
            (var out (sqlite/client-query client "SELECT 1"))
            (return out)))
  => 1)

^{:refer python.net.conn-sqlite/client-disconnect :added "4.1"}
(fact "disconnects an sqlite client"

  (!.py (do (var client (sqlite/create {}))
            (sqlite/client-connect client {})
            (return (sqlite/client-disconnect client))))
  => true)

^{:refer python.net.conn-sqlite/client-query :added "4.1"}
(fact "queries an sqlite client"

  (!.py (do (var client (sqlite/create {}))
            (sqlite/client-connect client {})
            (var out (sqlite/client-query client "SELECT 1 AS n, 'hello' AS s"))
            (sqlite/client-disconnect client)
            (return out)))
  => [[1 "hello"]])

^{:refer python.net.conn-sqlite/client-query-async :added "4.1"}
(fact "queries an sqlite client asynchronously"

  (!.py (do (var client (sqlite/create {}))
            (sqlite/client-connect client {})
            (var p (sqlite/client-query-async client "SELECT 1"))
            (return (p/x:promise-native? p))))
  => true)
