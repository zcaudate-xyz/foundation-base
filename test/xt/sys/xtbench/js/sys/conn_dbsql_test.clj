(ns
 xtbench.js.sys.conn-dbsql-test
 (:require
  [rt.basic.type-common :as common]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :require
  [[xt.sys.conn-dbsql :as dbsql]
   [xt.lang.common-repl :as repl]
   [js.lib.driver-postgres :as js-postgres]
   [js.lib.driver-sqlite :as js-sqlite]]})

(def CANARY-DART (common/program-exists? "dart"))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.sys.conn-dbsql/connect, :added "4.0"}
(fact
 "connects to a database"
 ^{:hidden true}
 (notify/wait-on
  :js
  (dbsql/connect
   {:constructor js-postgres/connect-constructor}
   {:success (fn [conn] (dbsql/query conn "SELECT 1;" (repl/<!)))}))
 =>
 (any 1 [{"?column?" 1}])
 (notify/wait-on
  :js
  (dbsql/connect
   {:constructor js-sqlite/connect-constructor}
   {:success (fn [conn] (dbsql/query conn "SELECT 1;" (repl/<!)))}))
 =>
 1)
