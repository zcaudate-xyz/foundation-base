(ns xt.db.runtime.parity-pg-sqlite-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[xt.db.helpers.sample-user-test :as sample-user]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-lib :as k]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.runtime.sql :as impl-sql]
             [xt.db.text.sql-manage :as manage]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.sample-test :as sample]
             [js.lib.driver-postgres :as js-postgres]
             [js.lib.driver-sqlite :as js-sqlite]]})

(def +touched-output+
  ["UserAccount" "UserProfile"])

(def +user-profile-tree+
  ["UserAccount"
   {"id" "00000000-0000-0000-0000-000000000000"}
   ["nickname"
    ["profile"
     ["first_name"]]]])

(def +nested-output+
  [["root" "Root"]])

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)
             (do (l/rt:scaffold :js)
                 true)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.runtime.sql/sql-process-event-sync
  :added "4.1"}
(fact "postgres and sqlite report the same touched sample tables"

  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [sqlite-conn]
           (dbsql/query-sync sqlite-conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (impl-sql/sql-process-event-sync sqlite-conn
                                            "add"
                                            {"Currency" (@! sample/+currency+)}
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))
           (var sqlite-out
                (xtd/arr-sort
                 (impl-sql/sql-process-event-sync sqlite-conn
                                                  "add"
                                                  {"UserAccount" [sample/RootUser]}
                                                  sample/Schema
                                                  sample/SchemaLookup
                                                  (ut/sqlite-opts nil))
                 k/identity
                 k/lt))
           (spec-promise/x:promise-then
            (dbsql/connect (js-postgres/driver) {:database "test-scratch"})
            (fn [pg-conn]
              (impl-sql/sql-process-event-sync pg-conn
                                               "add"
                                               {"Currency" (@! sample/+currency+)}
                                               sample/Schema
                                               sample/SchemaLookup
                                               (ut/postgres-opts sample/SchemaLookup))
              (var pg-out
                   (xtd/arr-sort
                    (impl-sql/sql-process-event-sync pg-conn
                                                     "add"
                                                     {"UserAccount" [sample/RootUser]}
                                                     sample/Schema
                                                     sample/SchemaLookup
                                                     (ut/postgres-opts sample/SchemaLookup))
                    k/identity
                    k/lt))
              (repl/notify [pg-out sqlite-out])))))))
  => [+touched-output+
      +touched-output+])

^{:refer xt.db.runtime.sql/sql-pull-sync
  :added "4.1"}
(fact "postgres and sqlite pull the same nested sample rows"

  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [sqlite-conn]
           (dbsql/query-sync sqlite-conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (impl-sql/sql-process-event-sync sqlite-conn
                                            "add"
                                            {"Currency" (@! sample/+currency+)}
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))
           (impl-sql/sql-process-event-sync sqlite-conn
                                            "add"
                                            {"UserAccount" [sample/RootUser]}
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))
           (var sqlite-out
                (xt/x:arr-map
                 (impl-sql/sql-pull-sync sqlite-conn
                                         sample/Schema
                                         (@! +user-profile-tree+)
                                         (ut/sqlite-opts nil))
                 (fn [row]
                   (var profile (xt/x:first (. row ["profile"])))
                   (return [(. row ["nickname"])
                            (. profile ["first_name"])]))))
           (spec-promise/x:promise-then
            (dbsql/connect (js-postgres/driver) {:database "test-scratch"})
            (fn [pg-conn]
              (impl-sql/sql-process-event-sync pg-conn
                                               "add"
                                               {"Currency" (@! sample/+currency+)}
                                               sample/Schema
                                               sample/SchemaLookup
                                               (ut/postgres-opts sample/SchemaLookup))
              (impl-sql/sql-process-event-sync pg-conn
                                               "add"
                                               {"UserAccount" [sample/RootUser]}
                                               sample/Schema
                                               sample/SchemaLookup
                                               (ut/postgres-opts sample/SchemaLookup))
              (var pg-out
                   (xt/x:arr-map
                    (impl-sql/sql-pull-sync pg-conn
                                            sample/Schema
                                            (@! +user-profile-tree+)
                                            (ut/postgres-opts sample/SchemaLookup))
                    (fn [row]
                      (var profile (xt/x:first (. row ["profile"])))
                      (return [(. row ["nickname"])
                               (. profile ["first_name"])]))))
              (repl/notify [pg-out sqlite-out])))))))
  => [+nested-output+
      +nested-output+])
