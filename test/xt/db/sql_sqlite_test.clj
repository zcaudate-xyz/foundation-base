(ns xt.db.sql-sqlite-test
  (:require [lib.docker :as docker]
            [net.http :as http]
            [std.json :as json]
            [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-repl :as repl]
             [xt.lang.base-lib :as k]
             [xt.db.sample-test :as sample]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db :as xdb]
             [xt.sys.conn-dbsql :as dbsql]
             [js.lib.driver-sqlite :as js-sqlite]]})

(defn reset-js
  []
  (notify/wait-on [:js 2000]
    (var initSql (require "sql.js"))
    (-> (initSql)
        (. (then (fn [SQL]
                   (:= (!:G SQL) SQL)
                   (:= (!:G DB) (js-sqlite/set-methods
                                 (new SQL.Database)))
                   (repl/notify DB)))))))

(fact:global
 {:setup    [(l/rt:restart)
             (!.js
              (:= (!:G initSqlJs) (require "sql.js")))
             (l/rt:scaffold :js)
             (reset-js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-sqlite/CANARY :adopt true :added "4.0"}
(fact "connects to an embedded sqlite file"
  ^:hidden

  (notify/wait-on :js
    (dbsql/connect {:constructor js-sqlite/connect-constructor}
                   {:success (fn [conn]
                               (dbsql/query conn "SELECT 1;"
                                            (repl/<!)))}))
  => 1)

^{:refer xt.db.sql-sqlite/CANARY.schema :adopt true :added "4.0"}
(fact "ensures that the results are the same"
  ^:hidden

  (!.js
   (k/join "\n\n"
           (manage/table-create-all
            sample/Schema
            sample/SchemaLookup
            (ut/sqlite-opts nil))))
  => string?)

^{:refer xt.db.sql-sqlite/CANARY.data :adopt true :added "4.0"}
(fact "ensures that the results are the same"
  ^:hidden

  (notify/wait-on :js
    (dbsql/query DB
                 (k/join "\n\n"
                         (table/table-upsert sample/Schema
                                             sample/SchemaLookup
                                             "Currency"
                                             sample/StatsToken
                                             (ut/sqlite-opts nil)))
                 {:success (fn [result]
                             (repl/notify result))}))
  => map?)
