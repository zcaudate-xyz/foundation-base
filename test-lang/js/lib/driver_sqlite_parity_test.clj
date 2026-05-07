(ns js.lib.driver-sqlite-parity-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.db.runtime.sql :as impl-sql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-manage :as manage]
             [xt.db.helpers.data-main-test :as sample]
             [js.lib.driver-sqlite :as js-sqlite]
             [js.lib.driver-sqlite-wasm :as js-sqlite-wasm]]})

(comment
  (notify/wait-on [:js 5000]
    (-> (sql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (sql/query-sync conn
                           (str/join "\n\n"
                                     (manage/table-create-all
                                      sample/Schema
                                      sample/SchemaLookup
                                      (ut/sqlite-opts nil))))))))

  (!.js
    (str/join "\n\n"
              (manage/table-create-all
               sample/Schema
               sample/SchemaLookup
               (ut/sqlite-opts nil)))))


(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(fact "native and wasm constructors expose the same observable sqlite behaviour"

  (notify/wait-on [:js 5000]
    (-> (js-sqlite/connect-constructor {})
        (spec-promise/x:promise-then
         (fn [db]
           (var create-out (js-sqlite/raw-query db "CREATE TABLE test (id INTEGER, name TEXT);"))
           (js-sqlite/raw-query db "INSERT INTO test (id, name) VALUES (1, 'alpha'), (2, 'beta');")
           (var scalar (js-sqlite/raw-query db "SELECT name FROM test WHERE id = 1;"))
           (var table (js-sqlite/raw-query db "SELECT id, name FROM test ORDER BY id;"))
           (var db-out {"create" create-out
                        "scalar" scalar
                        "table"  table})
           (. db (close))
           (repl/notify db-out)))))
  => {"create" []
      "scalar" "alpha"
      "table" [{"columns" ["id" "name"]
                "values"  [[1 "alpha"]
                           [2 "beta"]]}]}
  
  (notify/wait-on [:js 5000]
    (-> (js-sqlite-wasm/connect-constructor {})
        (spec-promise/x:promise-then
         (fn [db]
           (var create-out (js-sqlite-wasm/raw-query db "CREATE TABLE test (id INTEGER, name TEXT);"))
           (js-sqlite-wasm/raw-query db "INSERT INTO test (id, name) VALUES (1, 'alpha'), (2, 'beta');")
           (var scalar (js-sqlite-wasm/raw-query db "SELECT name FROM test WHERE id = 1;"))
           (var table (js-sqlite-wasm/raw-query db "SELECT id, name FROM test ORDER BY id;"))
           (var db-out {"create" create-out
                        "scalar" scalar
                        "table"  table})
           (. db (close))
           (repl/notify db-out)))))
  => {"create" []
      "scalar" "alpha"
      "table" [{"columns" ["id" "name"]
                "values"  [[1 "alpha"]
                           [2 "beta"]]}]})

(fact "native and wasm drivers stay in parity across the same workflow"

  (notify/wait-on [:js 5000]
    (-> (sql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [db]
           (sql/query-sync db "CREATE TABLE test (id INTEGER, name TEXT);")
           (sql/query-sync db "INSERT INTO test (id, name) VALUES (7, 'alpha'), (8, 'beta');")
           (var db-out {"connection" (sql/connection? db)
                        "query" (sql/query db "SELECT name FROM test WHERE id = 8;")
                        "query_sync" (sql/query-sync db "SELECT id FROM test WHERE name = 'alpha';")
                        "table" (sql/query-sync db "SELECT id, name FROM test ORDER BY id;")})
           (sql/disconnect db)
           (repl/notify db-out)))))
  => {"table" [{"values" [[7 "alpha"] [8 "beta"]],
                "columns" ["id" "name"]}],
      "query_sync" 7,
      "query" "beta",
      "connection" true}

  (notify/wait-on [:js 5000]
    (-> (sql/connect (js-sqlite-wasm/driver) {})
        (spec-promise/x:promise-then
         (fn [db]
           (sql/query-sync db "CREATE TABLE test (id INTEGER, name TEXT);")
           (sql/query-sync db "INSERT INTO test (id, name) VALUES (7, 'alpha'), (8, 'beta');")
           (var db-out {"connection" (sql/connection? db)
                        "query" (sql/query db "SELECT name FROM test WHERE id = 8;")
                        "query_sync" (sql/query-sync db "SELECT id FROM test WHERE name = 'alpha';")
                        "table" (sql/query-sync db "SELECT id, name FROM test ORDER BY id;")})
           (sql/disconnect db)
           (repl/notify db-out)))))
  => {"table" [{"values" [[7 "alpha"] [8 "beta"]],
                "columns" ["id" "name"]}],
      "query_sync" 7,
      "query" "beta",
      "connection" true})


(comment
  (fact "native and wasm drivers can explore the shared sqlite sample dataset"

    (notify/wait-on [:js 5000]
      (-> (sample-exploration-output (js-sqlite/driver))
          (spec-promise/x:promise-then repl/notify)))
    => {"add" ["UserAccount" "UserProfile"]
        "nested" [["root" "Root"]]
        "currencies" [["USD" "US Dollar"]
                      ["XLM" "Stellar Coin"]]}

    (notify/wait-on [:js 5000]
      (-> (sample-exploration-output (js-sqlite-wasm/driver))
          (spec-promise/x:promise-then repl/notify)))
    => {"add" ["UserAccount" "UserProfile"]
        "nested" [["root" "Root"]]
        "currencies" [["USD" "US Dollar"]
                      ["XLM" "Stellar Coin"]]})

  (fact "native and wasm drivers can remove seeded sample data in parity"

    (notify/wait-on [:js 5000]
      (-> (sample-removal-output (js-sqlite/driver))
          (spec-promise/x:promise-then repl/notify)))
    => {"add" ["UserAccount" "UserProfile"]
        "remove" ["UserAccount" "UserProfile"]
        "after-remove" []}

    (notify/wait-on [:js 5000]
      (-> (sample-removal-output (js-sqlite-wasm/driver))
          (spec-promise/x:promise-then repl/notify)))
    => {"add" ["UserAccount" "UserProfile"]
        "remove" ["UserAccount" "UserProfile"]
        "after-remove" []})
  )
