(ns xt.db.sql-sqlite-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.json :as json]
            [net.http :as http]
            [lib.docker :as docker]
            [xt.lang.base-notify :as notify]))

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

(l/script- :lua
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
             [lua.nginx.driver-sqlite :as lua-sqlite]]})

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

(defn reset-lua
  []
  (!.lua
   (:= (!:G DB) (dbsql/connect {:constructor lua-sqlite/connect-constructor
                                :memory true}))
   DB))

(fact:global
 {:setup    [(l/rt:restart)
             (!.js
              (:= (!:G initSqlJs) (require "sql.js")))
             (l/rt:scaffold :js)
             (!.lua
              (:= (!:G ngxsqlite) (require "lsqlite3")))
             (l/rt:scaffold :lua)
             (reset-js)
             (reset-lua)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-sqlite/CANARY :adopt true :added "4.0"}
(fact "connects to an embeded sqlite file"
  ^:hidden
  
  (notify/wait-on :js
   (dbsql/connect {:constructor js-sqlite/connect-constructor}
                  {:success (fn [conn]
                              (dbsql/query conn "SELECT 1;"
                                           (repl/<!)))}))
  => 1
  
  (!.lua
   (var conn (dbsql/connect {:constructor lua-sqlite/connect-constructor
                             :memory true}))
   (dbsql/query conn "SELECT 1;"))
  => 1)

^{:refer xt.db.sql-sqlite/CANARY.schema :adopt true :added "4.0"}
(fact "ensures that the results are the same"
  ^:hidden
  
  (!.js
   (dbsql/query-sync DB
                     (k/join "\n\n"
                             (manage/table-create-all
                              sample/Schema
                              sample/SchemaLookup
                              (ut/sqlite-opts nil))))
   (k/arr-sort
    (k/json-decode
     (dbsql/query-sync DB "SELECT json_group_array(name) FROM sqlite_schema where type='table'"))
    k/identity
    k/lt))
  => ["Asset" "Currency" "Organisation" "OrganisationAccess"
      "RegionCity" "RegionCountry" "RegionState" "UserAccount"
      "UserNotification" "UserPrivilege" "UserProfile" "Wallet" "WalletAsset"]

  (!.lua
   (dbsql/query-sync DB
                     (k/join "\n\n"
                             (manage/table-create-all
                              sample/Schema
                              sample/SchemaLookup
                              (ut/sqlite-opts nil))))
   (k/arr-sort
    (k/json-decode
     (or (dbsql/query-sync DB "SELECT json_group_array(name) FROM sqlite_schema where type='table'")
         (dbsql/query-sync DB "SELECT json_group_array(name) FROM sqlite_master where type='table'")))
    k/identity
    k/lt))
  => ["Asset" "Currency" "Organisation" "OrganisationAccess"
      "RegionCity" "RegionCountry" "RegionState" "UserAccount"
      "UserNotification" "UserPrivilege" "UserProfile" "Wallet" "WalletAsset"])

^{:refer xt.db.sql-sqlite/CANARY.data :adopt true :added "4.0"}
(fact "ensures that the results are the same"
  ^:hidden

  (mapv set
        (!.js [(xdb/process-event {:instance DB}
                                  ["add" {"Currency" (@! sample/+currency+)}]
                                  sample/Schema
                                  sample/SchemaLookup
                                  (ut/sqlite-opts nil))
               (xdb/process-event {:instance DB}
                                  ["add" {"UserAccount" [sample/RootUser]}]
                                  sample/Schema
                                  sample/SchemaLookup
                                  (ut/sqlite-opts nil))
               (xdb/db-pull-sync {:instance DB
                                  :opts (ut/sqlite-opts nil)}
                                 sample/Schema
                                 ["Currency"
                                  ["id"]]
                                 nil)]))
  => [#{"Currency"}
      #{"UserProfile" "UserAccount"}
      #{{"id" "USD"} {"id" "XLM.T"} {"id" "STATS"} {"id" "XLM"}}]

  (mapv set
        (!.lua [(xdb/process-event {:instance DB}
                                   ["add" {"Currency" (@! sample/+currency+)}]
                                   sample/Schema
                                   sample/SchemaLookup
                                   (ut/sqlite-opts nil))
                (xdb/process-event {:instance DB}
                                   ["add" {"UserAccount" [sample/RootUser]}]
                                   sample/Schema
                                   sample/SchemaLookup
                                   (ut/sqlite-opts nil))
                (xdb/db-pull-sync {:instance DB
                                   :opts (ut/sqlite-opts nil)}
                                  sample/Schema
                                  ["Currency"
                                   ["id"]]
                                  nil)]))
  => [#{"Currency"}
      #{"UserProfile" "UserAccount"}
      #{{"id" "USD"} {"id" "XLM.T"} {"id" "STATS"} {"id" "XLM"}}])
