(ns xt.db.runtime.parity-pg-sqlite-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :config {:dbname "test-scratch"}
     :require [[xt.db.helpers.sample-user-test :as sample-user]]}))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.runtime.sql :as impl-sql]
             [xt.db.text.base-flatten :as f]
             [xt.db.text.sql-manage :as manage]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-table :as sql-table]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]
             [js.lib.driver-postgres :as js-postgres]
             [js.lib.driver-sqlite :as js-sqlite]]})

(def +touched-output+
  ["UserAccount" "UserProfile"])

(def +postgres-env+
  {"host"     "127.0.0.1"
   "port"     "5432"
   "user"     "postgres"
   "password" "postgres"
   "database" "test-scratch"})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.runtime.sql/sql-process-event-sync
  :added "4.1"}
(fact "postgres and sqlite report the same touched sample tables"
  
  (notify/wait-on [:js 15000]
    (do
      (var run-queries nil)
      (:= run-queries
          (fn [conn queries]
            (if (== 0 (xt/x:len queries))
              (return (spec-promise/x:promise-run true))
              (return
               (spec-promise/x:promise-then
                (dbsql/ensure-promise
                 (dbsql/query conn (xt/x:first queries)))
                (fn [_]
                  (return (run-queries conn
                                       (xt/x:arr-slice queries 1 nil)))))))))
      (spec-promise/x:promise-then
       (dbsql/connect (js-sqlite/driver) {})
       (fn [sqlite-conn]
         (var root-user (xtd/obj-clone sample/RootUser))
         (xt/x:set-key root-user "password_hash" "root-hash")
         (xt/x:set-key root-user "password_salt" "root-salt")
         (var sqlite-flat
              (f/flatten-bulk sample/Schema
                              {"UserAccount" [root-user]}))
         (var sqlite-queries
              (manage/table-create-all
               sample/Schema
               sample/SchemaLookup
               (ut/sqlite-opts nil)))
         (xt/x:arr-push sqlite-queries
                        (raw/raw-insert "Currency"
                                        ["id" "type" "symbol" "native" "decimal"
                                         "name" "plural" "description"]
                                        (@! sample/+currency+)
                                        (ut/sqlite-opts nil)))
         (xtd/arr-assign sqlite-queries
                         (sql-table/table-emit-flat
                          sql-table/table-emit-insert
                          sample/Schema
                          sample/SchemaLookup
                          sqlite-flat
                          (ut/sqlite-opts nil)))
         (spec-promise/x:promise-then
          (run-queries sqlite-conn sqlite-queries)
          (fn [_]
            (var sqlite-out
                 (xtd/arr-sort
                  (xt/x:obj-keys sqlite-flat)
                  k/identity
                  k/lt))
            (spec-promise/x:promise-then
             (dbsql/connect (js-postgres/driver) (@! +postgres-env+))
             (fn [pg-conn]
               (var pg-flat
                    (f/flatten-bulk sample/Schema
                                    {"UserAccount" [root-user]}))
               (var pg-queries
                    [(raw/raw-delete "Currency"
                                     nil
                                     (ut/postgres-opts sample/SchemaLookup))
                     (raw/raw-delete "UserProfile"
                                     {:id "c4643895-b0ce-44cc-b07b-2386bf18d43b"}
                                     (ut/postgres-opts sample/SchemaLookup))
                     (raw/raw-delete "UserAccount"
                                     {:id "00000000-0000-0000-0000-000000000000"}
                                     (ut/postgres-opts sample/SchemaLookup))
                     (raw/raw-insert "Currency"
                                     ["id" "type" "symbol" "native" "decimal"
                                      "name" "plural" "description"]
                                     (@! sample/+currency+)
                                     (ut/postgres-opts sample/SchemaLookup))])
               (xtd/arr-assign pg-queries
                               (sql-table/table-emit-flat
                                sql-table/table-emit-insert
                                sample/Schema
                                sample/SchemaLookup
                                pg-flat
                                (ut/postgres-opts sample/SchemaLookup)))
               (spec-promise/x:promise-then
                (run-queries pg-conn pg-queries)
                (fn [_]
                  (var pg-out
                       (xtd/arr-sort
                        (xt/x:obj-keys pg-flat)
                        k/identity
                        k/lt))
                  (spec-promise/x:promise-then
                   (dbsql/ensure-promise (dbsql/disconnect sqlite-conn))
                   (fn [_]
                     (spec-promise/x:promise-then
                      (dbsql/ensure-promise (dbsql/disconnect pg-conn))
                      (fn [_]
                        (repl/notify [pg-out sqlite-out])))))))))))))))
  => [+touched-output+
      +touched-output+])
