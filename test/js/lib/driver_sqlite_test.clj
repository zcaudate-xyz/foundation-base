(ns js.lib.driver-sqlite-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.sys.conn-dbsql :as dbsql]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.lib.driver-sqlite :as js-sqlite]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.driver-sqlite/raw-query :added "4.0" :unchecked true}
(fact "raw query for sql lite results")

^{:refer js.lib.driver-sqlite/set-methods :added "4.0" :unchecked true}
(fact "sets the query and disconnect methods")

^{:refer js.lib.driver-sqlite/make-instance :added "4.0" :unchecked true}
(fact "creates a instance once SQL is loaded")

^{:refer js.lib.driver-sqlite/connect-constructor :added "4.0" :unchecked true}
(fact "connects to an embeded sqlite file"

  (notify/wait-on :js
    (:= (!:G initSqlJs) (require "sql.js"))
    (dbsql/connect {:constructor js-sqlite/connect-constructor}
                   {:success (fn [conn]
                               (dbsql/query conn "SELECT 1;"
                                            (repl/<!)))}))
  => 1)
