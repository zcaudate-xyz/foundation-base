(ns xt.db.runtime.sql-sqlite-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.db.runtime.sql :as impl-sql]
             [xt.db.text.sql-manage :as manage]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]
             [python.lib.driver-sqlite :as py-sqlite]]})

(def +currency-tree+
  ["Currency"
   {"id" ["in" [["USD" "XLM"]]]}
   ["id" "name"]])

(def +currency-output+
  [["USD" "US Dollar"]
   ["XLM" "Stellar Coin"]])

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.sql/sql-pull-sync :added "4.1"}
(fact "pulls decoded rows from the live sqlite runtime"

  (!.py
    (var conn (py-sqlite/wrap-connection
               (py-sqlite/connect-constructor {})))
    (sql/query
     conn
     (xt/x:str-join
      "\n\n"
      [(xt/x:str-join "\n\n"
                      (manage/table-create-all
                       sample/Schema
                       sample/SchemaLookup
                       (ut/sqlite-opts nil)))
       (raw/raw-insert "Currency"
                       ["id" "type" "symbol" "native" "decimal"
                        "name" "plural" "description"]
                       (@! sample/+currency+)
                       (ut/sqlite-opts nil))]))
    (var out
         (xt/x:arr-map
          (xtd/arr-sort
           (impl-sql/sql-pull-sync
            conn
            sample/Schema
            (@! +currency-tree+)
            (ut/sqlite-opts nil))
           (fn [row]
             (return (. row ["id"])))
           k/lt)
          (fn [row]
            (return [(. row ["id"])
                     (. row ["name"])]))))
    (sql/disconnect conn)
    out)
  => +currency-output+)

^{:refer xt.db.runtime.sql/sql-pull-sync.string :added "4.1"}
(fact "rejects string pull query results"

  (!.py
    (impl-sql/sql-pull-sync
     (sql/connection-create
      {}
      {"query" (fn [_conn _input]
                      (return "[{\"id\":\"USD\"}]"))})
     sample/Schema
     ["Currency" ["id"]]
     (ut/sqlite-opts nil)))
  => (throws))

^{:refer xt.db.runtime.sql/sql-delete-sync :added "4.1"}
(fact "deletes live sqlite rows through query"

  (!.py
    (var conn (py-sqlite/wrap-connection
               (py-sqlite/connect-constructor {})))
    (sql/query
     conn
     (xt/x:str-join
      "\n\n"
      [(xt/x:str-join "\n\n"
                      (manage/table-create-all
                       sample/Schema
                       sample/SchemaLookup
                       (ut/sqlite-opts nil)))
       (raw/raw-insert "Currency"
                       ["id" "type" "symbol" "native" "decimal"
                        "name" "plural" "description"]
                       (@! sample/+currency+)
                       (ut/sqlite-opts nil))]))
    (impl-sql/sql-delete-sync
     conn
     sample/Schema
     "Currency"
     ["USD"]
     (ut/sqlite-opts nil))
    (var out
         (xt/x:arr-map
          (xtd/arr-sort
           (impl-sql/sql-pull-sync
            conn
            sample/Schema
            (@! +currency-tree+)
            (ut/sqlite-opts nil))
           (fn [row]
             (return (. row ["id"])))
           k/lt)
          (fn [row]
            (return [(. row ["id"])
                     (. row ["name"])]))))
    (sql/disconnect conn)
    {"count" (xt/x:len out)
     "value" out})
  => {"count" 1
      "value" [["XLM" "Stellar Coin"]]})

^{:refer xt.db.runtime.sql/sql-clear :added "4.1"}
(fact "treats clear as a live sqlite no-op success"

  (!.py
    (var conn (py-sqlite/wrap-connection
               (py-sqlite/connect-constructor {})))
    (sql/query
     conn
     (xt/x:str-join
      "\n\n"
      [(xt/x:str-join "\n\n"
                      (manage/table-create-all
                       sample/Schema
                       sample/SchemaLookup
                       (ut/sqlite-opts nil)))
       (raw/raw-insert "Currency"
                       ["id" "type" "symbol" "native" "decimal"
                        "name" "plural" "description"]
                       (@! sample/+currency+)
                       (ut/sqlite-opts nil))]))
    (var cleared (impl-sql/sql-clear conn))
    (var count (sql/query conn "SELECT COUNT(*) FROM Currency;"))
    (sql/disconnect conn)
    [cleared count])
  => [true 4])
