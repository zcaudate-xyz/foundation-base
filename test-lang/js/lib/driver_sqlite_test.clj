(ns js.lib.driver-sqlite-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.impl.connection-sql :as sql]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [js.lib.driver-sqlite :as js-sqlite]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.driver-sqlite/raw-query :added "4.0"}
(fact "raw-query normalises sqlite results for scalars and row sets"

  (notify/wait-on [:js 5000]
    (spec-promise/x:promise-then
     (js-sqlite/connect-constructor {})
     (fn [db]
       (var create-out (js-sqlite/raw-query db "CREATE TABLE test (id INTEGER, name TEXT);"))
       (js-sqlite/raw-query db "INSERT INTO test (id, name) VALUES (1, 'alpha'), (2, 'beta');")
       (var scalar (js-sqlite/raw-query db "SELECT name FROM test WHERE id = 1;"))
       (var json-array (js-sqlite/raw-query db "SELECT json_array(1, 2, 3);"))
       (var table (js-sqlite/raw-query db "SELECT id, name FROM test ORDER BY id;"))
       (. db (close))
       (repl/notify {"create" create-out
                     "scalar" scalar
                     "json" json-array
                     "table"  table}))))
  => {"create" []
      "scalar" "alpha"
      "json" [1 2 3]
      "table" [{"columns" ["id" "name"]
                "values"  [[1 "alpha"]
                           [2 "beta"]]}]})

^{:refer js.lib.driver-sqlite/wrap-connection :added "4.1"}
(fact "wraps a raw sqlite connection with the sql protocol"

  (notify/wait-on [:js 5000]
    (spec-promise/x:promise-then
     (js-sqlite/connect-constructor {})
     (fn [raw]
       (var conn (js-sqlite/wrap-connection raw))
       (sql/query-sync conn "CREATE TABLE test (id INTEGER, name TEXT);")
       (sql/query-sync conn "INSERT INTO test (id, name) VALUES (1, 'alpha');")
       (repl/notify {"connection" (sql/connection? conn)
                     "query" (sql/query conn "SELECT name FROM test WHERE id = 1;")
                     "query_sync" (sql/query-sync conn "SELECT id FROM test WHERE name = 'alpha';")
                     "disconnect" (sql/disconnect conn)}))))
  => {"connection" true
      "query" "alpha"
      "query_sync" 1
      "disconnect" true})

^{:refer js.lib.driver-sqlite/make-instance :added "4.0"}
(fact "creates sqlite instances with sensible default and custom options"

  (!.js
   (var calls [])
   (var sqlite3 {"oo1" {"DB" (fn [filename flags]
                               (. calls (push [filename flags]))
                               (return {"filename" filename
                                        "flags" flags}))}})
   (var default-db (js-sqlite/make-instance sqlite3 nil))
   (var custom-db (js-sqlite/make-instance sqlite3 {"filename" "sample.db"
                                                    "flags"    "ct"}))
   {"calls"   calls
    "default" [(. default-db ["filename"])
               (. default-db ["flags"])]
    "custom"  [(. custom-db ["filename"])
               (. custom-db ["flags"])]})
  => {"calls"   [[":memory:" "c"]
                 ["sample.db" "ct"]]
      "default" [":memory:" "c"]
      "custom"  ["sample.db" "ct"]})

^{:refer js.lib.driver-sqlite/connect-constructor :added "4.0"}
(fact "connects to an embedded sqlite database"

  (notify/wait-on [:js 5000]
    (spec-promise/x:promise-then
     (js-sqlite/connect-constructor {})
     (fn [db]
       (var scalar (js-sqlite/raw-query db "SELECT 3;"))
       (. db (close))
       (repl/notify scalar))))
  => 3)

^{:refer js.lib.driver-sqlite/driver :added "4.1"}
(fact "connects through the driver wrapper for end-to-end sqlite access"

  (notify/wait-on [:js 5000]
    (spec-promise/x:promise-then
     (sql/connect (js-sqlite/driver) {})
     (fn [conn]
       (sql/query-sync conn "CREATE TABLE test (id INTEGER, name TEXT);")
       (sql/query-sync conn "INSERT INTO test (id, name) VALUES (7, 'alpha'), (8, 'beta');")
       (repl/notify {"connection" (sql/connection? conn)
                     "query" (sql/query conn "SELECT name FROM test WHERE id = 8;")
                     "query_sync" (sql/query-sync conn "SELECT id FROM test WHERE name = 'alpha';")
                     "table" (sql/query-sync conn "SELECT id, name FROM test ORDER BY id;")
                     "disconnect" (sql/disconnect conn)}))))
  => {"connection" true
      "query" "beta"
      "query_sync" 7
      "table" [{"columns" ["id" "name"]
                "values"  [[7 "alpha"]
                           [8 "beta"]]}]
      "disconnect" true})
