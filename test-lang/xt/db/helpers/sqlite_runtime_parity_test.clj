(ns xt.db.helpers.sqlite-runtime-parity-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do
  (l/script- :xtalk
    {:require [[xt.lang.spec-base :as xt]
               [xt.lang.common-lib :as k]
               [xt.lang.common-data :as xtd]
               [xt.lang.common-string :as str]
               [xt.lang.common-repl :as repl]
               [xt.lang.spec-promise :as spec-promise]
               [xt.protocol.impl.connection-sql :as dbsql]
               [xt.db.runtime.sql :as impl-sql]
               [xt.db.text.base-flatten :as f]
               [xt.db.text.sql-util :as ut]
               [xt.db.text.sql-raw :as raw]
               [xt.db.text.sql-table :as sql-table]
               [xt.db.text.sql-manage :as manage]
               [xt.db.helpers.data-main-test :as sample]]})

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

  ^{:seedgen/root {:all true
                   :langs [:lua.nginx :python :dart]
                   :lua.nginx {:extra [[lua.nginx.driver-sqlite :as lua-sqlite]]}
                   :python {:extra [[python.lib.driver-sqlite :as py-sqlite]]}
                   :dart {:extra [[dart.lib.driver-sqlite :as dart-sqlite]]}}}
  (l/script- :js
    {:runtime :basic
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
               [js.lib.driver-sqlite :as js-sqlite]]})

  ^{:seedgen/derived true}
  (l/script- :lua.nginx
    {:runtime :basic
     :config {:program :resty}
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
               [lua.nginx.driver-sqlite :as lua-sqlite]]})

  ^{:seedgen/derived true}
  (l/script- :python
    {:runtime :basic
     :require [[xt.lang.spec-base :as xt]
               [xt.lang.common-lib :as k]
               [xt.lang.common-data :as xtd]
               [xt.lang.common-string :as str]
               [xt.lang.common-repl :as repl]
               [xt.lang.spec-promise :as spec-promise]
               [xt.protocol.impl.connection-sql :as dbsql]
               [xt.db.runtime.sql :as impl-sql]
               [xt.db.text.base-flatten :as f]
               [xt.db.text.sql-util :as ut]
               [xt.db.text.sql-raw :as raw]
               [xt.db.text.sql-table :as sql-table]
               [xt.db.text.sql-manage :as manage]
               [xt.db.helpers.data-main-test :as sample]
               [python.lib.driver-sqlite :as py-sqlite]]})

  ^{:seedgen/derived true}
  (l/script- :dart
    {:runtime :twostep
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
               [dart.lib.driver-sqlite :as dart-sqlite]]})

  (defn sqlite-parity-js
    []
    (notify/wait-on [:js 2000]
      (spec-promise/x:promise-then
       (dbsql/connect (js-sqlite/driver) {})
       (fn [conn]
         (try
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
           (impl-sql/sql-process-event-sync conn
                                            "add"
                                            {"UserAccount" [sample/RootUser]}
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))
           (var nested
                (xt/x:arr-map
                 (impl-sql/sql-pull-sync conn
                                         sample/Schema
                                         (@! xt.db.helpers.sqlite-runtime-parity-test/+user-profile-tree+)
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
                                          (@! xt.db.helpers.sqlite-runtime-parity-test/+currency-bulk-tree+)
                                          (ut/sqlite-opts nil))
                  (fn [row]
                    (return (. row ["id"])))
                  k/lt)
                 (fn [row]
                   (return [(. row ["id"])
                            (. row ["name"])]))))
           (repl/notify
            (xt/x:json-encode [nested flat]))
           (catch e
             (repl/notify e)))))))

  (defn sqlite-parity-lua
    []
    (notify/wait-on [:lua.nginx 2000]
      (spec-promise/x:promise-then
       (dbsql/connect (lua-sqlite/driver) {:memory true})
       (fn [conn]
         (try
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
           (impl-sql/sql-process-event-sync conn
                                            "add"
                                            {"UserAccount" [sample/RootUser]}
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))
           (var nested
                (xt/x:arr-map
                 (impl-sql/sql-pull-sync conn
                                         sample/Schema
                                         (@! xt.db.helpers.sqlite-runtime-parity-test/+user-profile-tree+)
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
                                          (@! xt.db.helpers.sqlite-runtime-parity-test/+currency-bulk-tree+)
                                          (ut/sqlite-opts nil))
                  (fn [row]
                    (return (. row ["id"])))
                  k/lt)
                 (fn [row]
                   (return [(. row ["id"])
                            (. row ["name"])]))))
           (repl/notify
            (xt/x:json-encode [nested flat]))
           (catch e
             (repl/notify e)))))))

  (defn sqlite-parity-python
    []
    (notify/wait-on [:python 2000]
      (spec-promise/x:promise-then
       (dbsql/connect (py-sqlite/driver) {})
       (fn [conn]
         (try
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
                                         (@! xt.db.helpers.sqlite-runtime-parity-test/+user-profile-tree+)
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
                                          (@! xt.db.helpers.sqlite-runtime-parity-test/+currency-bulk-tree+)
                                          (ut/sqlite-opts nil))
                  (fn [row]
                    (return (. row ["id"])))
                  k/lt)
                 (fn [row]
                   (return [(. row ["id"])
                            (. row ["name"])]))))
           (repl/notify
            (xt/x:json-encode [nested flat]))
           (catch e
             (repl/notify e)))))))

  (defn sqlite-parity-dart
    []
    (!.dt
      (var conn nil)
      (dart-sqlite/connect-constructor
       {:memory true}
       (fn [err raw]
         (when (xt/x:not-nil? err)
           (throw err))
         (:= conn (dart-sqlite/wrap-connection raw))
         (return conn)))
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
      (impl-sql/sql-process-event-sync conn
                                       "add"
                                       {"UserAccount" [sample/RootUser]}
                                       sample/Schema
                                       sample/SchemaLookup
                                       (ut/sqlite-opts nil))
      (var nested
           (xt/x:arr-map
            (impl-sql/sql-pull-sync conn
                                    sample/Schema
                                    (@! xt.db.helpers.sqlite-runtime-parity-test/+user-profile-tree+)
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
                                     (@! xt.db.helpers.sqlite-runtime-parity-test/+currency-bulk-tree+)
                                     (ut/sqlite-opts nil))
             (fn [row]
               (return (. row ["id"])))
             xt/x:str-comp)
            (fn [row]
              (return [(. row ["id"])
                       (. row ["name"])]))))
      (xt/x:json-encode [nested flat]))))
