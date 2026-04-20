(ns xt.db.impl-select-sql-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.lang.common-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-graph :as graph]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sample-scratch-test :as sample-scratch]
             [xt.sys.conn-dbsql :as dbsql]
             [js.lib.driver-postgres :as js-postgres]
             [xt.lang.common-repl :as repl]]})

(defn ^{:lang-exceptions {:lua {:skip true}
                          :python {:skip true}
                          :dart {:skip true}}}
  bootstrap-js
  []
  (notify/wait-on [:js 5000]
    (dbsql/connect {:constructor js-postgres/connect-constructor
                    :database "test-scratch"}
                   {:success (fn [conn]
                               (:= (!:G CONN) conn)
                               (repl/notify true))})))

(fact:global
 ^{:lang-exceptions {:lua {:skip true}
                     :python {:skip true}
                     :dart {:skip true}}}
 {:setup    [(l/rt:restart)
              (l/rt:scaffold :js)
              (bootstrap-js)
             (notify/wait-on :js
               (dbsql/query CONN
                            "CREATE SCHEMA IF NOT EXISTS \"scratch\";"
                            (repl/<!)))
             (notify/wait-on :js
               (dbsql/query CONN
                            (manage/table-create
                             sample-scratch/Schema
                             "Entry"
                             (ut/postgres-opts {"Entry" {"schema" "scratch"}}))
                            (repl/<!)))
             (notify/wait-on :js
               (dbsql/query CONN
                            "DELETE FROM \"scratch\".\"Entry\";"
                            (repl/<!)))
             (notify/wait-on :js
               (dbsql/query CONN
                            (raw/raw-insert
                             "Entry"
                             ["id" "name" "tags"]
                             [{"id" "00000000-0000-0000-0000-000000000001"
                               "name" "A-1"
                               "tags" []}
                              {"id" "00000000-0000-0000-0000-000000000002"
                               "name" "A-2"
                               "tags" []}]
                             (ut/postgres-opts {"Entry" {"schema" "scratch"}}))
                            (repl/<!)))]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.impl-select-sql-test/CONNECTION
  :adopt true
  :added "4.0"
  :lang-exceptions {:lua {:skip true}
                    :python {:skip true}
                    :dart {:skip true}}}
(fact "CONNECTED"

  (notify/wait-on :js
    (dbsql/query CONN "SELECT 1;" (repl/<!)))
  => (any nil 1 [{"?column?" 1}])

  (notify/wait-on :js
    (dbsql/query CONN "SELECT count(*) FROM \"scratch\".\"Entry\";" (repl/<!)))
  => (any nil "2" 2 [{"count" "2"}] [{"count" 2}]))

^{:refer xt.db.impl-select-sql-test/QUERY
  :adopt true
  :added "4.0"
  :lang-exceptions {:lua {:skip true}
                    :python {:skip true}
                    :dart {:skip true}}}
(fact "builds select queries"
  ^:hidden
  (graph/select sample-scratch/Schema
                ["Entry"
                 ["name"
                  (ut/LIMIT 1)]]
                (ut/postgres-opts {"Entry" {"schema" "scratch"}}))
  => string?)

^{:refer xt.db.impl-select-sql-test/QUERY
  :adopt true
  :added "4.0"
  :lang-exceptions {:lua {:skip true}
                    :python {:skip true}
                    :dart {:skip true}}}
(fact "runs select queries against scratch db"
  ^:hidden
  (notify/wait-on :js
    (dbsql/query CONN
                 (graph/select sample-scratch/Schema
                               ["Entry"
                                ["name"
                                 (ut/LIMIT 1)]]
                               (ut/postgres-opts {"Entry" {"schema" "scratch"}}))
                 (repl/<!)))
  => vector?

  (notify/wait-on :js
    (dbsql/query CONN
                 (graph/select sample-scratch/Schema
                               ["Entry"
                                ["name"
                                 (ut/ORDER-BY ["name"])
                                 (ut/LIMIT 5)]]
                               (ut/postgres-opts {"Entry" {"schema" "scratch"}}))
                 {:success (fn [result]
                             (repl/notify result))}))
  => vector?)
