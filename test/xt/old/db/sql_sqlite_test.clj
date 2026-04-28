(ns xt.old.db.sql-sqlite-test
  (:use code.test)
  (:require [net.http :as http]
            [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

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
    (dbsql/connect {:constructor js-sqlite/connect-constructor}
                   {:success (fn [conn]
                               (:= (!:G DB) conn)
                               (repl/notify DB))})))

(fact:global
  {:setup    [(l/rt:restart)
              (l/rt:scaffold :js)
              (reset-js)]
   :teardown [(l/rt:stop)]})

^{:refer xt.old.db.sql-sqlite/CANARY :adopt true :added "4.0"}
(fact "connects to an embedded sqlite file"

  (notify/wait-on :js
    (dbsql/connect {:constructor js-sqlite/connect-constructor}
                   {:success (fn [conn]
                               (dbsql/query conn "SELECT 1;"
                                            (repl/<!)))}))
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
    (dbsql/query DB
                 (str/join "\n\n"
                          (manage/table-create-all
                           sample/Schema
                           sample/SchemaLookup
                           (ut/sqlite-opts nil)))
                 {:success (fn [_]
                             (repl/notify true))}))
  => true)

^{:refer xt.old.db.sql-sqlite/CANARY.data :adopt true :added "4.0"}
(fact "ensures that the results are the same"

  (notify/wait-on :js
    (dbsql/query DB
                 (str/join "\n\n"
                          (table/table-upsert sample/Schema
                                              sample/SchemaLookup
                                              "Currency"
                                              @sample/+currency+
                                              (ut/sqlite-opts nil)))
                 {:success (fn [result]
                             (repl/notify result))}))
  => vector?)
