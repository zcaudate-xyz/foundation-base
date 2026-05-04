(ns xt.db.instance.sql-python-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do
  ^{:seedgen/root {:all true}}
  (l/script- :python
    {:runtime :basic
     :require [[xt.lang.spec-base :as xt]
                [xt.db.instance.sql :as impl-sql]
                [xt.lang.common-lib :as k]
                [xt.lang.common-data :as xtd]
                [xt.lang.common-string :as str]
                [xt.lang.common-repl :as repl]
                [xt.lang.spec-promise :as spec-promise]
                [xt.protocol.impl.connection-sql :as dbsql]
                [xt.db.text.base-flatten :as f]
                [xt.db.text.sql-util :as ut]
                [xt.db.text.sql-raw :as raw]
                [xt.db.text.sql-table :as sql-table]
                [xt.db.text.sql-manage :as manage]
                [xt.db.helpers.data-main-test :as sample]
                [python.lib.driver-sqlite :as py-sqlite]]})

  (fact:global
   {:setup    [(l/rt:restart)
                (do (l/rt:scaffold :python)
                    true)]
    :teardown [(l/rt:stop)]})

  (def +user-profile-tree+
    ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]])

  (def +currency-bulk-tree+
    ["Currency"
     {"id" ["in" [["USD" "XLM"]]]}
     ["id" "name"]])

  (def +sqlite-parity-output+
    "[[[\"root\",\"Root\"]],[[\"USD\",\"US Dollar\"],[\"XLM\",\"Stellar Coin\"]]]")

  ^{:seedgen/root {:all true}}
  (defn.py sqlite-parity-runtime-py
    []
    (var conn (py-sqlite/wrap-connection
               (py-sqlite/connect-constructor {})))
    (xt/x:arr-each
     (manage/table-create-all
      sample/Schema
      sample/SchemaLookup
      (ut/sqlite-opts nil))
     (fn [query]
       (dbsql/query-sync conn query)))
    (dbsql/query-sync conn
                      (raw/raw-insert "Currency"
                                      ["id" "type" "symbol" "native" "decimal"
                                       "name" "plural" "description"]
                                      (@! sample/+currency+)
                                      (ut/sqlite-opts nil)))
    (var flat-bulk (f/flatten-bulk sample/Schema
                                   {"UserAccount" [sample/RootUser]}))
    (xt/x:arr-each
     (sql-table/table-emit-flat
      sql-table/table-emit-upsert
      sample/Schema
      sample/SchemaLookup
      flat-bulk
      (ut/sqlite-opts nil))
     (fn [query]
       (dbsql/query-sync conn query)))
    (var nested
         (xt/x:arr-map
          (impl-sql/sql-pull-sync conn
                                  sample/Schema
                                  (@! +user-profile-tree+)
                                  (ut/sqlite-opts nil))
          (fn [row]
            (var profile (xt/x:first (. row ["profile"])))
            (return [(. row ["nickname"])
                     (. profile ["first_name"])]))))
    (var flat
         (xt/x:arr-map
          (xtd/arr-sort
           (impl-sql/sql-pull-sync conn
                                   sample/Schema
                                   (@! +currency-bulk-tree+)
                                   (ut/sqlite-opts nil))
           (fn [row]
             (return (. row ["id"])))
           k/lt)
          (fn [row]
            (return [(. row ["id"])
                     (. row ["name"])]))))
    (return (xt/x:json-encode [nested flat])))

  ^{:refer xt.db.instance.sql/sql-pull-sync :added "4.1"}
  (fact "returns the expected nested sqlite output in python"

    (!.py
      (-/sqlite-parity-runtime-py))
    => +sqlite-parity-output+))
