(ns
 xtbench.dart.db.impl-select-view-test
 (:require
  [std.lang :as l]
  [std.string.prose :as prose]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.db.base-schema :as sch]
   [xt.lang.common-lib :as k]
   [xt.db.sql-util :as ut]
   [xt.db.sql-graph :as graph]
   [xt.db.sql-view :as view]
   [xt.db.sql-manage :as manage]
   [xt.db.sample-scratch-test :as sample-scratch]
   [xt.sys.conn-dbsql :as dbsql]
   [js.lib.driver-postgres :as js-postgres]
   [xt.lang.common-repl :as repl]]})

(defn
 bootstrap-js
 []
 (notify/wait-on
  [:dart 5000]
  (dbsql/connect
   {:constructor js-postgres/connect-constructor,
    :database "test-scratch"}
   {:success (fn [conn] (:= (!:G CONN) conn) (repl/notify true))})))

(fact:global
 {:setup [(l/rt:restart) (bootstrap-js)], :teardown [(l/rt:stop)]})

^{:refer xt.db.impl-select-view-test/CONNECTION,
  :adopt true,
  :added "4.0"}
(fact
 "CONNECTED"
 (notify/wait-on :dart (dbsql/query CONN "SELECT 1;" (repl/<!)))
 =>
 (any nil 1 [{"?column?" 1}]))

^{:refer xt.db.impl-select-view-test/VIEW-QUERY, :added "4.0"}
(fact
 "queries views"
 ^{:hidden true}
 (view/query-select
  sample-scratch/Schema
  {:view
   {:table "Entry",
    :type "select",
    :tag "by-name",
    :query {"name" "{{i-name}}"}},
   :input [{:symbol "i-name", :type "text"}]}
  ["A-1"]
  {}
  true)
 =>
 ["Entry"
  {"custom" [], "where" [{"name" "A-1"}], "links" [], "data" ["id"]}]
 (view/query-select
  sample-scratch/Schema
  {:view
   {:table "Entry",
    :type "select",
    :tag "by-name",
    :query {"name" "{{i-name}}"}},
   :input [{:symbol "i-name", :type "text"}]}
  ["A-1"]
  {})
 =>
 "SELECT id FROM Entry\n  WHERE name = 'A-1'")
