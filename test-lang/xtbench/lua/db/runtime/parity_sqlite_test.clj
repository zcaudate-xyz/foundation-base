(ns xtbench.lua.db.runtime.parity-sqlite-test
  (:require [hara.runtime.basic.type-common :as common]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :lua.nginx
  {:config {:program :resty}
   :require [[xt.lang.spec-base :as xt]
            [xt.lang.common-lib :as k]
            [xt.lang.common-data :as xtd]
            [xt.lang.common-string :as str]
            [xt.lang.common-repl :as repl]
            [xt.lang.spec-promise :as spec-promise]
            [xt.protocol.impl.connection-sql :as dbsql]
            [xt.db.runtime.sql :as impl-sql]
            [xt.db.text.sql-util :as ut]
            [xt.db.text.sql-raw :as raw]
            [xt.db.text.sql-manage :as manage]
            [xt.db.helpers.data-main-test :as sample]
            [lua.nginx.driver-sqlite :as lua-sqlite]]
   :runtime :basic})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:ref xt.db.runtime.sql/sql-process-event-sync
  :setup [(def +sqlite-touched-output+
            ["UserAccount" "UserProfile"])]}
(fact "js runtime reports the touched sqlite tables"


  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}
  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query-sync conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (dbsql/query-sync conn
                             (raw/raw-insert "Currency"
                                             ["id" "type" "symbol" "native" "decimal"
                                              "name" "plural" "description"]
                                             (@! sample/+currency+)
                                             (ut/sqlite-opts nil)))
           (var out
                (xtd/arr-sort
                 (impl-sql/sql-process-event-sync conn
                                                  "add"
                                                  {"UserAccount" [sample/RootUser]}
                                                  sample/Schema
                                                  sample/SchemaLookup
                                                  (ut/sqlite-opts nil))
                 k/identity
                 k/lt))
           (repl/notify out)))))
  => +sqlite-touched-output+

  (notify/wait-on [:lua.nginx 5000]
    (-> (dbsql/connect (lua-sqlite/driver) {:memory true})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query-sync conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (dbsql/query-sync conn
                             (raw/raw-insert "Currency"
                                             ["id" "type" "symbol" "native" "decimal"
                                              "name" "plural" "description"]
                                             (@! sample/+currency+)
                                             (ut/sqlite-opts nil)))
           (var out
                (xtd/arr-sort
                 (impl-sql/sql-process-event-sync conn
                                                  "add"
                                                  {"UserAccount" [sample/RootUser]}
                                                  sample/Schema
                                                  sample/SchemaLookup
                                                  (ut/sqlite-opts nil))
                 k/identity
                 k/lt))
           (repl/notify out)))))
  => +sqlite-touched-output+

  (notify/wait-on [:python 5000]
    (-> (dbsql/connect (py-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
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
           (var flat-bulk
                (f/flatten-bulk sample/Schema
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
           (repl/notify ["UserAccount" "UserProfile"])))))
  => +sqlite-touched-output+)

^{:ref xt.db.runtime.sql/sql-pull-sync
  :setup [(def +user-profile-tree+
            ["UserAccount"
             ["nickname"
              ["profile"
               ["first_name"]]]])
          (def +sqlite-nested-output+
            [["root" "Root"]])]}
(fact "js runtime pulls nested sqlite sample data"


  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}
  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query-sync conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (impl-sql/sql-process-event-sync conn
                                            "add"
                                            {"UserAccount" [sample/RootUser]}
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))
           (var out
                (xt/x:arr-map
                 (impl-sql/sql-pull-sync conn
                                         sample/Schema
                                         (@! +user-profile-tree+)
                                         (ut/sqlite-opts nil))
                 (fn [row]
                   (var profile (xt/x:first (. row ["profile"])))
                   (return [(. row ["nickname"])
                            (. profile ["first_name"])]))))
           (repl/notify out)))))
  => +sqlite-nested-output+

  (notify/wait-on [:lua.nginx 5000]
    (-> (dbsql/connect (lua-sqlite/driver) {:memory true})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query-sync conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (impl-sql/sql-process-event-sync conn
                                            "add"
                                            {"UserAccount" [sample/RootUser]}
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))
           (var out
                (xt/x:arr-map
                 (impl-sql/sql-pull-sync conn
                                         sample/Schema
                                         (@! +user-profile-tree+)
                                         (ut/sqlite-opts nil))
                 (fn [row]
                   (var profile (xt/x:first (. row ["profile"])))
                   (return [(. row ["nickname"])
                            (. profile ["first_name"])]))))
           (repl/notify out)))))
  => +sqlite-nested-output+

  (notify/wait-on [:python 5000]
    (-> (dbsql/connect (py-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (xt/x:arr-each
            (manage/table-create-all
             sample/Schema
             sample/SchemaLookup
             (ut/sqlite-opts nil))
            (fn [query]
              (dbsql/query-sync conn query)))
           (var flat-bulk
                (f/flatten-bulk sample/Schema
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
           (var out
                (xt/x:arr-map
                 (impl-sql/sql-pull-sync conn
                                         sample/Schema
                                         (@! +user-profile-tree+)
                                         (ut/sqlite-opts nil))
                 (fn [row]
                   (var profile (xt/x:first (. row ["profile"])))
                   (return [(. row ["nickname"])
                            (. profile ["first_name"])]))))
           (repl/notify out)))))
  => +sqlite-nested-output+)

^{:ref xt.db.runtime.sql/sql-pull-sync.currencies
  :setup [(def +currency-bulk-tree+
            ["Currency"
             {"id" ["in" [["USD" "XLM"]]]}
             ["id" "name"]])

          (def +sqlite-currencies-output+
            [["USD" "US Dollar"]
             ["XLM" "Stellar Coin"]])]}
(fact "js runtime pulls sorted sqlite currencies"

  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}
  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query-sync conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (dbsql/query-sync conn
                             (raw/raw-insert "Currency"
                                             ["id" "type" "symbol" "native" "decimal"
                                              "name" "plural" "description"]
                                             (@! sample/+currency+)
                                             (ut/sqlite-opts nil)))
           (var out
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
           (repl/notify out)))))
  => +sqlite-currencies-output+

  (notify/wait-on [:lua.nginx 5000]
    (-> (dbsql/connect (lua-sqlite/driver) {:memory true})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query-sync conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (dbsql/query-sync conn
                             (raw/raw-insert "Currency"
                                             ["id" "type" "symbol" "native" "decimal"
                                              "name" "plural" "description"]
                                             (@! sample/+currency+)
                                             (ut/sqlite-opts nil)))
           (var out
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
           (repl/notify out)))))
  => +sqlite-currencies-output+

  (notify/wait-on [:python 5000]
    (-> (dbsql/connect (py-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
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
           (var out
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
           (repl/notify out)))))
  => +sqlite-currencies-output+)
