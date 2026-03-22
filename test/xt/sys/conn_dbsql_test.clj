(ns xt.sys.conn-dbsql-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.sys.conn-dbsql :as dbsql]
             [xt.lang.base-lib :as k]
             [lua.nginx.driver-postgres :as lua-postgres]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.sys.conn-dbsql :as dbsql]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.lib.driver-postgres :as js-postgres]
             [js.lib.driver-sqlite :as js-sqlite]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.sys.conn-dbsql/connect :added "4.0"}
(fact "connects to a database"
  ^:hidden
  
  (!.lua
   (var conn (dbsql/connect {:constructor lua-postgres/connect-constructor}))
   (dbsql/query conn "SELECT 1;"))
  => 1

  (notify/wait-on :js
    (dbsql/connect {:constructor js-postgres/connect-constructor}
                   {:success (fn [conn]
                               (dbsql/query conn "SELECT 1;"
                                            (repl/<!)))}))
  
  => (any 1 [{"?column?" 1}])

  (notify/wait-on :js
    (:= (!:G initSqlJs) (require "sql.js"))
    (dbsql/connect {:constructor js-sqlite/connect-constructor}
                   {:success (fn [conn]
                               (dbsql/query conn "SELECT 1;"
                                            (repl/<!)))}))
  => 1)

^{:refer xt.sys.conn-dbsql/disconnect :added "4.0"}
(fact "disconnects form database")

^{:refer xt.sys.conn-dbsql/query-base :added "4.0"}
(fact "calls query without the wrapper")

^{:refer xt.sys.conn-dbsql/query :added "4.0"}
(fact "sends a query")

^{:refer xt.sys.conn-dbsql/query-sync :added "4.0"}
(fact "sends a synchronous query")
