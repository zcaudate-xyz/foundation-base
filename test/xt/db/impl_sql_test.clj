(ns xt.db.impl-sql-test
  (:require [std.lang :as l]
            [std.string.prose]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.db.impl-sql :as impl-sql]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.db.base-flatten :as f]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]
             [lua.nginx.driver-sqlite :as lua-sqlite]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.db.impl-sql :as impl-sql]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.db.base-flatten :as f]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]
             [js.lib.driver-sqlite :as js-sqlite]]})

(defn bootstrap-lua
  []
  (!.lua
   (var ngxsqlite (require "lsqlite3"))
   (:= (!:G INSTANCE) (dbsql/connect {:constructor lua-sqlite/connect-constructor
                                      :memory true}))
   (dbsql/query-sync INSTANCE
                     (k/join "\n\n"
                             (manage/table-create-all
                              sample/Schema
                              sample/SchemaLookup
                              (ut/sqlite-opts nil))))
   (dbsql/query-sync INSTANCE
                     (raw/raw-insert "Currency"
                                     ["id" "type" "symbol" "native" "decimal"
                                      "name" "plural" "description"]
                                     (@! sample/+currency+)
                                     (ut/sqlite-opts nil)))
   true))

(defn bootstrap-js
  []
  (notify/wait-on [:js 2000]
    (var initSql (require "sql.js"))
    (-> (initSql)
        (. (then (fn [SQL]
                   (:= (!:G SQL) SQL)
                   (:= (!:G INSTANCE) (js-sqlite/set-methods
                                       (new SQL.Database)))
                   (dbsql/query-sync INSTANCE
                                     (k/join "\n\n"
                                             (manage/table-create-all
                                              sample/Schema
                                                   sample/SchemaLookup
                                                   (ut/sqlite-opts nil))))
                   (dbsql/query-sync INSTANCE
                                     (raw/raw-insert "Currency"
                                                     ["id" "type" "symbol" "native" "decimal"
                                                      "name" "plural" "description"]
                                                     (@! sample/+currency+)
                                                     (ut/sqlite-opts nil)))
                   (repl/notify true)))))))

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :js)
                 true)
             (do (l/rt:scaffold :lua)
                 true)
             (bootstrap-js)
             (bootstrap-lua)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.impl-sql/CANARY :adopt true :added "4.0"}
(fact "checks that things are ok"
  ^:hidden

  (!.js
   [(dbsql/query INSTANCE
                 "SELECT 1;"
                 nil)
    (k/sort
     (k/obj-keys
      (f/flatten-bulk sample/Schema
                      {"UserAccount"
                       [sample/RootUser]})))])
  => [1 ["UserAccount" "UserProfile"]]

  (!.lua
   [(dbsql/query INSTANCE
                      "SELECT 1;"
                      nil)
    (k/sort
     (k/obj-keys
      (f/flatten-bulk sample/Schema
                      {"UserAccount"
                       [sample/RootUser]})))])
  => [1 ["UserAccount" "UserProfile"]])

^{:refer xt.db.impl-sql/sql-process-event-remove.lua :adopt true :added "4.0"
  :setup [(!.lua
           (k/sort (impl-sql/sql-process-event-sync
                    INSTANCE
                    "add"
                    {"UserAccount" [sample/RootUser]}
                    sample/Schema
                    sample/SchemaLookup
                    (ut/sqlite-opts nil))))]}
(fact "removes data from database"
  ^:hidden
  
  (!.lua
   (impl-sql/sql-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]]
    (ut/sqlite-opts nil)))
  => [{"nickname" "root", "profile" [{"first_name" "Root"}]}]
  
  (!.lua
   (impl-sql/sql-process-event-remove
    INSTANCE
    "input"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    (ut/sqlite-opts nil)))
  => (std.string.prose/|
      "DELETE FROM \"UserAccount\" WHERE \"id\" = '00000000-0000-0000-0000-000000000000';"
      ""
      "DELETE FROM \"UserProfile\" WHERE \"id\" = 'c4643895-b0ce-44cc-b07b-2386bf18d43b';")

  (sort (!.lua
        (impl-sql/sql-process-event-remove
         INSTANCE
         "remove" {"UserAccount" [sample/RootUser]}
         sample/Schema
         sample/SchemaLookup
         (ut/sqlite-opts nil))))
  => ["UserAccount" "UserProfile"]
  
  (!.lua
   (impl-sql/sql-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]]
    (ut/sqlite-opts nil)))
  => empty?)

^{:refer xt.db.impl-sql/sql-gen-delete :added "4.0"}
(fact "generates the delete statements"
  ^:hidden
  
  (!.js
   (impl-sql/sql-gen-delete "HELLO"
                            ["A" "B"]
                            (ut/sqlite-opts nil)))
  => ["DELETE FROM \"HELLO\" WHERE \"id\" = 'A';"
      "DELETE FROM \"HELLO\" WHERE \"id\" = 'B';"]
  
  (!.lua
   (impl-sql/sql-gen-delete "HELLO"
                            ["A" "B"]
                            (ut/sqlite-opts nil)))
  => ["DELETE FROM \"HELLO\" WHERE \"id\" = 'A';"
      "DELETE FROM \"HELLO\" WHERE \"id\" = 'B';"])

^{:refer xt.db.impl-sql/sql-process-event-sync :added "4.0"}
(fact "processes event sync data from database")

^{:refer xt.db.impl-sql/sql-process-event-remove :added "4.0"
  :setup [(!.js
           (k/sort (impl-sql/sql-process-event-sync
                    INSTANCE
                    "add"
                    {"UserAccount" [sample/RootUser]}
                    sample/Schema
                    sample/SchemaLookup
                    (ut/sqlite-opts nil))))]}
(fact "removes data from database"
  ^:hidden
  
  (!.js
   (impl-sql/sql-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]]
    (ut/sqlite-opts nil)))
  => [{"nickname" "root", "profile" [{"first_name" "Root"}]}]
  
  (!.js
   (impl-sql/sql-process-event-remove
    INSTANCE
    "input"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    (ut/sqlite-opts nil)))
  => (std.string.prose/|
      "DELETE FROM \"UserAccount\" WHERE \"id\" = '00000000-0000-0000-0000-000000000000';"
      ""
      "DELETE FROM \"UserProfile\" WHERE \"id\" = 'c4643895-b0ce-44cc-b07b-2386bf18d43b';")

  (sort (!.js
        (impl-sql/sql-process-event-remove
         INSTANCE
         "remove" {"UserAccount" [sample/RootUser]}
         sample/Schema
         sample/SchemaLookup
         (ut/sqlite-opts nil))))
  => ["UserAccount" "UserProfile"]
  
  (!.js
   (impl-sql/sql-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]]
    (ut/sqlite-opts nil)))
  => empty?)

^{:refer xt.db.impl-sql/sql-pull-sync :added "4.0"
  :setup [(def +account+
            (contains-in
             [{"is_official" 0,
               "nickname" "root",
               "profile"
               [{"city" nil,
                 "about" nil,
                 "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                 "last_name" "User",
                 "first_name" "Root",
                 "language" "en"}],
               "id" "00000000-0000-0000-0000-000000000000",
               "is_suspended" 0,
               "password_updated" number?
               "is_super" 1}]))]}
(fact "runs a pull statement"
  ^:hidden
  
  [(set (!.js
         (impl-sql/sql-pull-sync INSTANCE
                           sample/Schema
                           ["Currency"
                            ["id"]]
                           (ut/sqlite-opts nil))))
   (!.js
    (impl-sql/sql-process-event-sync
     INSTANCE
     "add"
     {"UserAccount" [sample/RootUser]}
     sample/Schema
     sample/SchemaLookup
     (ut/sqlite-opts nil))
    (impl-sql/sql-pull-sync
     INSTANCE
     sample/Schema
     ["UserAccount"
      ["*/data"
       ["profile"]]]
     (ut/sqlite-opts nil)))]
  => (contains [#{{"id" "USD"} {"id" "XLM.T"} {"id" "STATS"} {"id" "XLM"}}
                +account+])
  
  [(set (!.lua
         (impl-sql/sql-pull-sync INSTANCE
                                 sample/Schema
                                 ["Currency"
                                  ["id"]]
                                 (ut/sqlite-opts nil))))
   (!.lua
    (impl-sql/sql-process-event-sync
     INSTANCE
     "add"
     {"UserAccount" [sample/RootUser]}
     sample/Schema
     sample/SchemaLookup
     (ut/sqlite-opts nil))
    (impl-sql/sql-pull-sync INSTANCE
                            sample/Schema
                            ["UserAccount"
                             ["*/data"
                              ["profile"]]]
                            (ut/sqlite-opts nil)))]
  => (contains [#{{"id" "USD"} {"id" "XLM.T"} {"id" "STATS"} {"id" "XLM"}}
                +account+]))

^{:refer xt.db.impl-sql/sql-delete-sync :added "4.0"}
(fact "deletes sync data from sql db")

^{:refer xt.db.impl-sql/sql-clear :added "4.0"}
(fact "clears the sql db")
