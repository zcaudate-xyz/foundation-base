(ns xt.db.impl-select-sql-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-graph :as graph]
             [xt.db.sql-manage :as manage]
             [xt.db.sample-scratch-test :as sample-scratch]
             [xt.sys.conn-dbsql :as dbsql]
             [js.lib.driver-postgres :as js-postgres]
             [xt.lang.base-repl :as repl]]})

(defn bootstrap-js
  []
  (notify/wait-on [:js 5000]
    (dbsql/connect {:constructor js-postgres/connect-constructor
                    :database "test-scratch"}
                   {:success (fn [conn]
                               (:= (!:G CONN) conn)
                               (repl/notify true))})))

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (bootstrap-js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.impl-select-sql-test/CONNECTION :adopt true :added "4.0"}
(fact "CONNECTED"
  (notify/wait-on :js
    (dbsql/query CONN "SELECT 1;" (repl/<!)))
  => (any nil 1 [{"?column?" 1}]))

^{:refer xt.db.impl-select-sql-test/QUERY :adopt true :added "4.0"}
(fact "runs select queries"
  ^:hidden

  (notify/wait-on :js
    (dbsql/query CONN
                 (graph/select sample-scratch/Schema
                               ["Entry"
                                ["name"
                                 (ut/LIMIT 1)]]
                               (ut/postgres-opts sample-scratch/SchemaLookup))
                 (repl/<!)))
  => string?

  (notify/wait-on :js
    (dbsql/query CONN
                 (graph/select sample-scratch/Schema
                               ["Entry"
                                ["name"
                                 (ut/ORDER-BY ["name"])
                                 (ut/LIMIT 5)]]
                               (ut/postgres-opts sample-scratch/SchemaLookup))
                 {:success (fn [result]
                             (repl/notify (k/json-decode result)))}))
  => vector?)
