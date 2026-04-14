(ns xt.db-lua.sql-sqlite-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-string :as str]
             [xt.db.sample-test :as sample]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db :as xdb]
             [xt.sys.conn-dbsql :as dbsql]
             [lua.nginx.driver-sqlite :as lua-sqlite]]})

(defn reset-lua
  []
  (!.lua
   (:= (!:G DB) (dbsql/connect {:constructor lua-sqlite/connect-constructor
                                :memory true}))
   DB))

(fact:global
 {:setup    [(l/rt:restart)
             (!.lua
              (:= (!:G ngxsqlite) (require "lsqlite3")))
             (l/rt:scaffold :lua)
             (reset-lua)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db-lua.sql-sqlite/CANARY :adopt true :added "4.0"}
(fact "connects to an embedded sqlite file"
  ^:hidden

  (!.lua
   (var conn (dbsql/connect {:constructor lua-sqlite/connect-constructor
                             :memory true}))
   (dbsql/query conn "SELECT 1;"))
  => 1)

^{:refer xt.db-lua.sql-sqlite/TABLE-CREATE :adopt true :added "4.0"}
(fact "creates tables"
  ^:hidden

  (!.lua
   (str/join "\n\n"
           (manage/table-create-all
            sample/Schema
            sample/SchemaLookup
            (ut/sqlite-opts nil))))
  => string?)
