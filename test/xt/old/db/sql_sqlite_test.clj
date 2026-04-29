(ns xt.old.db.sql-sqlite-test
  (:use code.test)
  (:require [net.http :as http]
             [std.json :as json]
             [std.lang :as l]
             [xt.lang.common-notify :as notify]
             [xt.lang.spec-promise :as spec-promise]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-string :as str]
             [xt.old.db.sample-test :as sample]
              [xt.old.db.sql-util :as ut]
              [xt.old.db.sql-raw :as raw]
              [xt.old.db.sql-manage :as manage]
             [xt.old.db.sql-table :as table]
             [xt.old.db :as xdb]
             [xt.old.sys.conn-dbsql :as dbsql]
             [js.lib.driver-sqlite :as js-sqlite]]})

(defn reset-js
  []
  (notify/wait-on [:js 2000]
    (spec-promise/x:promise-then
      (dbsql/connect (js-sqlite/driver) {})
      (fn [conn]
        (:= (!:G DB) conn)
        (repl/notify DB)))))

(fact:global
  {:setup    [(l/rt:restart)
              (l/rt:scaffold :js)
              (reset-js)]
   :teardown [(l/rt:stop)]})

^{:refer xt.old.db.sql-sqlite/CANARY :adopt true :added "4.0"}
(fact "connects to an embedded sqlite file"

  (notify/wait-on :js
    (spec-promise/x:promise-then
      (dbsql/connect (js-sqlite/driver) {})
      (fn [conn]
        (repl/notify
         (dbsql/query conn "SELECT 1;")))))
  => 1)

^{:refer xt.old.db.sql-sqlite/CANARY.schema :adopt true :added "4.0"}
(fact "ensures that the results are the same"

  (!.js
    (manage/table-create-all
     sample/Schema
     sample/SchemaLookup
     (ut/sqlite-opts nil)))
  => vector?

  (notify/wait-on :js
    (repl/notify
      (dbsql/query DB
                   (str/join "\n\n"
                             (manage/table-create-all
                              sample/Schema
                              sample/SchemaLookup
                              (ut/sqlite-opts nil))))))
  => true)

^{:refer xt.old.db.sql-sqlite/CANARY.data :adopt true :added "4.0"}
(fact "ensures that the results are the same"

  (notify/wait-on :js
    (repl/notify
      (dbsql/query DB
                   (str/join "\n\n"
                             (table/table-upsert sample/Schema
                                                 sample/SchemaLookup
                                                 "Currency"
                                                 @sample/+currency+
                                                 (ut/sqlite-opts nil)))))
  => vector?)
