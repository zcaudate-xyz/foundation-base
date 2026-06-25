(ns xt.db.sqlite-basic-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.system :as db-system]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.net.conn-sql :as dbsql]
             [xt.db.text.sql-util :as sql-util]
             [xt.db.text.sql-manage :as sql-manage]
             [js.net.conn-sqlite :as js-sqlite]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sqlite-basic-test/basic-sqlite-db :added "4.1"}
(fact "connects to sqlite, creates db runtime, installs schema, seeds and queries"

  (notify/wait-on [:js 5000]
    (. (dbsql/connect (js-sqlite/create {}) {})
       (then (fn [conn]
               ;; 1. Create the db runtime
               (var db (db-system/db-create
                        {"::" "db.sql"
                         :instance conn}
                        (@! fixtures/+schema+)
                        (@! fixtures/+lookup+)
                        (sql-util/sqlite-opts nil)))

               ;; 2. Install schema (create tables)
               (dbsql/query
                conn
                (str/join "\n\n"
                          (sql-manage/table-create-all
                           (@! fixtures/+schema+)
                           (@! fixtures/+lookup+)
                           (sql-util/sqlite-opts nil))))

               ;; 3. Seed data
               (db-system/sync-event
                db
                {"db/sync" (@! fixtures/+entry-seed+)})

               ;; 4. Verify with db-exec-sync
               (var count (db-system/db-exec-sync
                           db
                           "SELECT count(*) FROM Entry;"))

               (repl/notify
                {"count" count})))))
  => {"count" 2})
