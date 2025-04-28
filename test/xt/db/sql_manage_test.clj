(ns xt.db.sql-manage-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.db.base-flatten :as f]
             [xt.db.cache-util :as cache-util]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-graph :as graph]
             [xt.db.sql-util :as ut]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-manage :as manage]
             [xt.db.sample-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-manage :as manage]
             [xt.db.sample-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)
             (l/rt:scaffold :python)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-manage/table-create.sqlite :adopt true :added "4.0"}
(fact "workflow for sql.js"
  
  (notify/wait-on :js
   (var initSql (require "sql.js"))
   (-> (initSql)
       (. (then (fn [res]
                  (:= (!:G SQL) res)
                  (:= (!:G DB) (new SQL.Database))
                  (repl/notify true))))))
  => true
  
  (!.js
   (DB.run (k/join "\n\n"
                   (manage/table-create-all
                    sample/Schema
                    sample/SchemaLookup
                    (ut/sqlite-opts nil))))
   (DB.run
    (raw/raw-insert "Currency"
                    ["id" "type" "symbol" "native" "decimal" "name" "plural" "description"]
                    (@! sample/+currency+)
                    (ut/sqlite-opts nil)))
   (DB.run
    (k/join "\n\n"
            (table/table-insert sample/Schema
                              sample/SchemaLookup
                              "UserAccount"
                              [sample/RootUserFull]
                              (ut/sqlite-opts nil))))
   true)
  => true
  
  (!.js
   (DB.exec "SELECT name FROM sqlite_schema where type='table'"))
  => [{"values"
       [["Currency"]
        ["RegionCountry"]
        ["RegionState"]
        ["RegionCity"]
        ["UserAccount"]
        ["UserProfile"]
        ["UserNotification"]
        ["UserPrivilege"]
        ["Asset"]
        ["Wallet"]
        ["WalletAsset"]
        ["Organisation"]
        ["OrganisationAccess"]],
       "columns" ["name"]}]
  
  (!.js
   (DB.exec "pragma table_info('Currency')"))
  => [{"columns" ["cid" "name" "type" "notnull" "dflt_value" "pk"],
       "values" [[0 "id" "TEXT" 0 nil 1]
                 [1 "type" "TEXT" 0 nil 0]
                 [2 "symbol" "TEXT" 0 nil 0]
                 [3 "native" "TEXT" 0 nil 0]
                 [4 "decimal" "INTEGER" 0 nil 0]
                 [5 "name" "TEXT" 0 nil 0]
                 [6 "plural" "TEXT" 0 nil 0]
                 [7 "description" "TEXT" 0 nil 0]]}]
  
  (set (!.js
        (-> (DB.exec
             (graph/select sample/Schema
                           ["Asset"
                            ["id"
                             ["currency"
                              ["id"]]]]
                           (ut/sqlite-opts nil)))
            (. [0] ["values"] [0] [0])
            k/json-decode)))
  => #{{"currency" [{"id" "XLM"}],
        "id" "222de282-ca29-4d04-81dd-86ec3f9189cf"}
       {"currency" [{"id" "XLM.T"}],
        "id" "9261d072-b7f5-41df-935a-c36fe13acf14"}
       {"currency" [{"id" "STATS"}],
        "id" "63acfd25-4b1b-4de4-aa82-909019c95591"}
       {"currency" [{"id" "USD"}],
        "id" "9e576e3e-c73e-4d18-92b4-f975c1bed3d4"}}
  
  (!.js
   (-> (DB.exec
        (graph/select sample/Schema
                      ["UserAccount"
                       ["*/data"
                        ["profile"]
                        ["wallets"
                         ["*/data"
                          ["entries"
                           ["*/data"
                            ["asset"
                           ["*/data"
                            ["currency"]]]]]]]]]
                      (ut/sqlite-opts nil)))
       (. [0] ["values"] [0] [0])
       k/json-decode))
  => [{"is_official" 0,
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
       "wallets"
       [{"id" "531f3edb-b9d4-4c8e-8419-22edfe715b15",
         "slug" "default",
         "entries"
         [{"asset"
           [{"currency"
             [{"native" "Δ",
               "id" "STATS",
               "plural" "Stat coins",
               "name" "Stat Coin",
               "symbol" "Δ",
               "type" "digital",
               "description" "Default Currency for Statstrade",
               "decimal" 0}],
             "id" "63acfd25-4b1b-4de4-aa82-909019c95591"}],
           "id" "6eb2fa48-c753-41c6-abda-c680828da1d2"}
          {"asset"
           [{"currency"
             [{"native" nil,
               "id" "XLM",
               "plural" "Stellar coins",
               "name" "Stellar Coin",
               "symbol" "XLM",
               "type" "crypto",
               "description"
               "Default Currency for the Stellar Blockchain",
               "decimal" -1}],
             "id" "222de282-ca29-4d04-81dd-86ec3f9189cf"}],
           "id" "4b146b40-947a-42a5-b116-2ad8816c4078"}
          {"asset"
           [{"currency"
             [{"native" nil,
               "id" "XLM.T",
               "plural" "Stellar TestNet coins",
               "name" "Stellar TestNet Coin",
               "symbol" "XLM.T",
               "type" "crypto",
               "description"
               "Default Currency for the Stellar TestNet Blockchain",
               "decimal" -1}],
             "id" "9261d072-b7f5-41df-935a-c36fe13acf14"}],
           "id" "2b3d4318-8cea-4420-a31c-f110d8198654"}
          {"asset"
           [{"currency"
             [{"native" nil,
               "id" "USD",
               "plural" "US Dollars",
               "name" "US Dollar",
               "symbol" "USD",
               "type" "fiat",
               "description"
               "Default Current for the United States of America",
               "decimal" 2}],
             "id" "9e576e3e-c73e-4d18-92b4-f975c1bed3d4"}],
           "id" "38889fdc-de34-4161-bb37-f8844d67ee5a"}]}],
       "password_updated" 1630408723423619,
       "is_super" 1}])

^{:refer xt.db.sql-manage/table-create-column :added "4.0"}
(fact "column creation function"
  ^:hidden
  
  (!.js
   [(manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["Currency" "id"])
                                (ut/sqlite-opts nil))
    (manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["Currency" "id"])
                                (ut/postgres-opts sample/SchemaLookup))])
  => ["\"id\" text PRIMARY KEY"
      "\"id\" citext PRIMARY KEY"]

  (!.lua
   [(manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["Currency" "id"])
                                (ut/sqlite-opts nil))
    (manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["Currency" "id"])
                                (ut/postgres-opts sample/SchemaLookup))])
  => ["\"id\" text PRIMARY KEY"
      "\"id\" citext PRIMARY KEY"]

  (!.py
   [(manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["Currency" "id"])
                                (ut/sqlite-opts nil))
    (manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["Currency" "id"])
                                (ut/postgres-opts sample/SchemaLookup))])
  => ["\"id\" text PRIMARY KEY"
      "\"id\" citext PRIMARY KEY"]

  
  (!.js
   [(manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["UserProfile" "account"])
                                (ut/sqlite-opts nil))
    (manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["UserProfile" "account"])
                                (ut/postgres-opts sample/SchemaLookup))])
  => ["\"account_id\" text REFERENCES \"UserAccount\""
      "\"account_id\" uuid REFERENCES \"scratch/xt.db.sample-user-test\".\"UserAccount\""]

  

  (!.lua
   [(manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["UserProfile" "account"])
                                (ut/sqlite-opts nil))
    (manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["UserProfile" "account"])
                                (ut/postgres-opts sample/SchemaLookup))])
  => ["\"account_id\" text REFERENCES \"UserAccount\""
      "\"account_id\" uuid REFERENCES \"scratch/xt.db.sample-user-test\".\"UserAccount\""]

  

  (!.py
   [(manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["UserProfile" "account"])
                                (ut/sqlite-opts nil))
    (manage/table-create-column sample/Schema
                                (k/get-in sample/Schema
                                          ["UserProfile" "account"])
                                (ut/postgres-opts sample/SchemaLookup))])
  => ["\"account_id\" text REFERENCES \"UserAccount\""
      "\"account_id\" uuid REFERENCES \"scratch/xt.db.sample-user-test\".\"UserAccount\""])

^{:refer xt.db.sql-manage/table-create :added "4.0"
  :setup [(def +currency-table+
            (std.string/|
             "CREATE TABLE IF NOT EXISTS \"Currency\" ("
             "  \"id\" text PRIMARY KEY,"
             "  \"type\" text,"
             "  \"symbol\" text,"
             "  \"native\" text,"
             "  \"decimal\" integer,"
             "  \"name\" text,"
             "  \"plural\" text,"
             "  \"description\" text"
             ");"))
          (def +profile-table+
            (std.string/|
             "CREATE TABLE IF NOT EXISTS \"UserProfile\" ("
             "  \"id\" text PRIMARY KEY,"
             "  \"account_id\" text REFERENCES \"UserAccount\","
             "  \"first_name\" text,"
             "  \"last_name\" text,"
             "  \"city\" text,"
             "  \"state_id\" text REFERENCES \"RegionState\","
             "  \"country_id\" text REFERENCES \"RegionCountry\","
             "  \"about\" text,"
             "  \"language\" text,"
             "  \"detail\" text"
             ");"))]}
(fact "emits a table create string"
  ^:hidden
  
  (!.js
   [(manage/table-create sample/Schema
                         "Currency"
                         (ut/sqlite-opts nil))
    (manage/table-create sample/Schema
                         "UserProfile"
                         (ut/sqlite-opts nil))])
  => [+currency-table+
      +profile-table+]

  (!.lua
   [(manage/table-create sample/Schema
                         "Currency"
                         (ut/sqlite-opts nil))
    (manage/table-create sample/Schema
                         "UserProfile"
                         (ut/sqlite-opts nil))])
  => [+currency-table+
      +profile-table+]

  (!.py
   [(manage/table-create sample/Schema
                         "Currency"
                         (ut/sqlite-opts nil))
    (manage/table-create sample/Schema
                         "UserProfile"
                         (ut/sqlite-opts nil))])
  => [+currency-table+
      +profile-table+])

^{:refer xt.db.sql-manage/table-create-all :added "4.0"}
(fact "creates all tables from schema"
  ^:hidden
  
  (def +table-all+
    (!.js
     (manage/table-create-all sample/Schema
                              sample/SchemaLookup
                              (ut/sqlite-opts nil))))

  (!.lua
   (manage/table-create-all sample/Schema
                              sample/SchemaLookup
                              (ut/sqlite-opts nil)))
  => +table-all+

  (!.py
   (manage/table-create-all sample/Schema
                            sample/SchemaLookup
                            (ut/sqlite-opts nil)))
  => +table-all+)

^{:refer xt.db.sql-manage/table-drop :added "4.0"}
(fact "creates a table statement"
  ^:hidden
  
  (!.js
   (manage/table-drop sample/Schema
                      "Currency"
                      (ut/sqlite-opts nil)))
  => "DROP TABLE IF EXISTS \"Currency\";"

  (!.lua
   (manage/table-drop sample/Schema
                      "Currency"
                      (ut/sqlite-opts nil)))
  => "DROP TABLE IF EXISTS \"Currency\";"

  (!.py
   (manage/table-drop sample/Schema
                      "Currency"
                      (ut/sqlite-opts nil)))
  => "DROP TABLE IF EXISTS \"Currency\";")

^{:refer xt.db.sql-manage/table-drop-all :added "4.0"
  :setup [(def +drop-all+
            ["DROP TABLE IF EXISTS \"OrganisationAccess\";"
             "DROP TABLE IF EXISTS \"Organisation\";"
             "DROP TABLE IF EXISTS \"WalletAsset\";"
             "DROP TABLE IF EXISTS \"Wallet\";"
             "DROP TABLE IF EXISTS \"Asset\";"
             "DROP TABLE IF EXISTS \"UserPrivilege\";"
             "DROP TABLE IF EXISTS \"UserNotification\";"
             "DROP TABLE IF EXISTS \"UserProfile\";"
             "DROP TABLE IF EXISTS \"UserAccount\";"
             "DROP TABLE IF EXISTS \"RegionCity\";"
             "DROP TABLE IF EXISTS \"RegionState\";"
             "DROP TABLE IF EXISTS \"RegionCountry\";"
             "DROP TABLE IF EXISTS \"Currency\";"])]}
(fact "drops all tables"
  ^:hidden
  
  (!.js
   (manage/table-drop-all sample/Schema
                          sample/SchemaLookup
                          (ut/sqlite-opts nil)))
  => +drop-all+

  (!.lua
   (manage/table-drop-all sample/Schema
                          sample/SchemaLookup
                          (ut/sqlite-opts nil)))
  => +drop-all+

  (!.py
   (manage/table-drop-all sample/Schema
                          sample/SchemaLookup
                          (ut/sqlite-opts nil)))
  => +drop-all+)
