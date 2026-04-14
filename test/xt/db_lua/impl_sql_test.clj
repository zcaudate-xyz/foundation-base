(ns xt.db-lua.impl-sql-test
  (:require [std.lang :as l]
            [std.string.prose :as prose]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.db.impl-sql :as impl-sql]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.db.base-flatten :as f]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]
             [lua.nginx.driver-sqlite :as lua-sqlite]]})

(defn bootstrap-lua
  []
  (!.lua
   (var ngxsqlite (require "lsqlite3"))
   (:= (!:G INSTANCE) (dbsql/connect {:constructor lua-sqlite/connect-constructor
                                      :memory true}))
   (dbsql/query-sync INSTANCE
                     (str/join "\n\n"
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

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :lua)
                 true)
             (bootstrap-lua)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.impl-sql/CANARY :adopt true :added "4.0"}
(fact "checks that things are ok"
  ^:hidden

  (!.lua
   [(dbsql/query INSTANCE
                 "SELECT 1;"
                 nil)
    (k/sort
     (xtd/obj-keys
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
  => (prose/|
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

  (!.lua
   (impl-sql/sql-gen-delete "HELLO"
                            ["A" "B"]
                            (ut/sqlite-opts nil)))
  => ["DELETE FROM \"HELLO\" WHERE \"id\" = 'A';"
      "DELETE FROM \"HELLO\" WHERE \"id\" = 'B';"])

^{:refer xt.db.impl-sql/sql-process-event-sync :added "4.0"}
(fact "processes event sync data from database")

^{:refer xt.db.impl-sql/sql-process-event-remove :added "4.0"
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
  => (prose/|
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
    (impl-sql/sql-pull-sync
     INSTANCE
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
