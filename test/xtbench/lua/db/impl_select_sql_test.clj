(ns
 xtbench.lua.db.impl-select-sql-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require
  [[xt.db.base-schema :as sch]
   [xt.lang.common-lib :as k]
   [xt.db.sql-util :as ut]
   [xt.db.sql-graph :as graph]
   [xt.db.sql-raw :as raw]
   [xt.db.sql-manage :as manage]
   [xt.db.sample-scratch-test :as sample-scratch]
   [xt.sys.conn-dbsql :as dbsql]
   [js.lib.driver-postgres :as js-postgres]
   [xt.lang.common-repl :as repl]]})

(defn
 bootstrap-js
 []
 (notify/wait-on
  [:lua 5000]
  (dbsql/connect
   {:constructor js-postgres/connect-constructor,
    :database "test-scratch"}
   {:success (fn [conn] (:= (!:G CONN) conn) (repl/notify true))})))

(fact:global
 {:setup
  [(l/rt:restart)
   (bootstrap-js)
   (notify/wait-on
    :lua
    (dbsql/query
     CONN
     "CREATE SCHEMA IF NOT EXISTS \"scratch\";"
     (repl/<!)))
   (notify/wait-on
    :lua
    (dbsql/query
     CONN
     (manage/table-create
      sample-scratch/Schema
      "Entry"
      (ut/postgres-opts {"Entry" {"schema" "scratch"}}))
     (repl/<!)))
   (notify/wait-on
    :lua
    (dbsql/query CONN "DELETE FROM \"scratch\".\"Entry\";" (repl/<!)))
   (notify/wait-on
    :lua
    (dbsql/query
     CONN
     (raw/raw-insert
      "Entry"
      ["id" "name" "tags"]
      [{"id" "00000000-0000-0000-0000-000000000001",
        "name" "A-1",
        "tags" []}
       {"id" "00000000-0000-0000-0000-000000000002",
        "name" "A-2",
        "tags" []}]
      (ut/postgres-opts {"Entry" {"schema" "scratch"}}))
     (repl/<!)))],
  :teardown [(l/rt:stop)]})

^{:refer xt.db.impl-select-sql-test/CONNECTION,
  :adopt true,
  :added "4.0"}
(fact
 "CONNECTED"
 (notify/wait-on :lua (dbsql/query CONN "SELECT 1;" (repl/<!)))
 =>
 (any nil 1 [{"?column?" 1}])
 (notify/wait-on
  :lua
  (dbsql/query
   CONN
   "SELECT count(*) FROM \"scratch\".\"Entry\";"
   (repl/<!)))
 =>
 (any nil "2" 2 [{"count" "2"}] [{"count" 2}]))

^{:refer xt.db.impl-select-sql-test/QUERY, :adopt true, :added "4.0"}
(fact
 "runs select queries"
 ^{:hidden true}
 (notify/wait-on
  :lua
  (repl/notify
   (graph/select
    sample-scratch/Schema
    ["Entry" ["name" (ut/LIMIT 1)]]
    (ut/postgres-opts {"Entry" {"schema" "scratch"}}))))
 =>
 string?
 (notify/wait-on
  :lua
  (dbsql/query
   CONN
   (graph/select
    sample-scratch/Schema
    ["Entry" ["name" (ut/LIMIT 1)]]
    (ut/postgres-opts {"Entry" {"schema" "scratch"}}))
   (repl/<!)))
 =>
 vector?
 (notify/wait-on
  :lua
  (dbsql/query
   CONN
   (graph/select
    sample-scratch/Schema
    ["Entry" ["name" (ut/ORDER-BY ["name"]) (ut/LIMIT 5)]]
    (ut/postgres-opts {"Entry" {"schema" "scratch"}}))
   {:success (fn [result] (repl/notify result))}))
 =>
 vector?)
