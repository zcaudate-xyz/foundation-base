(ns xt.db.runtime.parity-pg-sqlite-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :config {:dbname "test-scratch"}}))

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
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.db.text.sql-manage :as manage]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-table :as sql-table]
             [xt.db.text.sql-util :as ut]
             [js.lib.driver-postgres :as js-postgres]
             [js.lib.driver-sqlite :as js-sqlite]]})

(def +touched-output+
  ["Task"])

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
(fact "postgres and sqlite report the same touched fixture tables"
  
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
                 (dbsql/query-async conn (xt/x:first queries)))
                (fn [_]
                  (return (run-queries conn
                                       (xt/x:arr-slice queries 1 nil)))))))))
      (spec-promise/x:promise-then
       (dbsql/connect (js-sqlite/driver) {})
       (fn [sqlite-conn]
         (var payload fixtures/Seed)
         (var sqlite-flat
              (f/flatten-bulk fixtures/Schema
                              payload))
         (var sqlite-opts (ut/sqlite-opts nil))
         (var sqlite-queries
              (manage/table-create-all
               fixtures/Schema
               fixtures/Lookup
               sqlite-opts))
         (xtd/arr-assign sqlite-queries
                         (sql-table/table-emit-flat
                          sql-table/table-emit-insert
                          fixtures/Schema
                          fixtures/Lookup
                          sqlite-flat
                          sqlite-opts))
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
                    (f/flatten-bulk fixtures/Schema
                                    payload))
               (var pg-lookup {"Task" {"position" 0
                                       "schema" "public"}})
               (var pg-opts (ut/postgres-opts pg-lookup))
               (var pg-queries
                    (manage/table-create-all
                     fixtures/Schema
                     pg-lookup
                     pg-opts))
               (xt/x:arr-push pg-queries
                              (raw/raw-delete "Task"
                                              nil
                                              pg-opts))
               (xtd/arr-assign pg-queries
                               (sql-table/table-emit-flat
                                sql-table/table-emit-insert
                                fixtures/Schema
                                pg-lookup
                                pg-flat
                                pg-opts))
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
