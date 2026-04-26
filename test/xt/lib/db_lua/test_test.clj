(ns xt.lib.db-lua.test-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.lib.db :as impl]
              [xt.lang.common-lib :as k]
              [xt.lang.common-data :as xtd]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-string :as str]
              [xt.lang.common-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.lib.db.base-flatten :as f]
             [xt.lib.db.sql-util :as ut]
             [xt.lib.db.sql-raw :as raw]
             [xt.lib.db.sql-manage :as manage]
             [xt.lib.db.sql-table :as table]
             [xt.lib.db.sample-test :as sample]
             [lua.nginx.driver-sqlite :as lua-sqlite]]})

(defn bootstrap-lua
  []
  (!.lua
   (var ngxsqlite (require "lsqlite3"))
   (:= (!:G DBSQL) (impl/db-create {"::" "db.sql"
                                    :constructor lua-sqlite/connect-constructor
                                    :memory true}
                                   sample/Schema
                                   sample/SchemaLookup
                                   (ut/sqlite-opts nil)))
   (dbsql/query-sync (xt/x:get-key DBSQL "instance")
                     (str/join "\n\n"
                             (manage/table-create-all
                              sample/Schema
                              sample/SchemaLookup
                              (ut/sqlite-opts nil))))
   (:= (!:G DBCACHE) (impl/db-create {"::" "db.cache"}
                                     sample/Schema
                                     sample/SchemaLookup
                                     (ut/sqlite-opts nil)))
   true))

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :lua)
                 true)
             (bootstrap-lua)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lib.db/process-event :added "4.0"}
(fact "processes an event"

  (!.lua
   [(xtd/arr-sort (impl/process-event
                   DBSQL
                   ["add" {"UserAccount" [sample/RootUser]}]
                   sample/Schema
                   sample/SchemaLookup
                   (ut/sqlite-opts nil))
                  k/identity
                  k/lt)
    (xtd/arr-sort (impl/process-event
                   DBCACHE
                   ["add" {"UserAccount" [sample/RootUser]}]
                   sample/Schema
                   sample/SchemaLookup
                   nil)
                  k/identity
                  k/lt)])
  => [["UserAccount" "UserProfile"]
      ["UserAccount" "UserProfile"]])

;; Additional tests would go here, with only (!.lua ...) assertions
