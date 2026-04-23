(ns
 xtbench.lua.db.sql-manage-test
 (:require
  [std.lang :as l]
  [std.string.prose :as prose]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require
  [[xt.db.base-schema :as sch]
   [xt.lang.spec-base :as xt]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-string :as str]
   [xt.db.sql-util :as ut]
   [xt.db.sql-manage :as manage]
   [xt.db.sample-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart) (l/rt:scaffold :lua)],
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-manage/table-create-column, :added "4.0"}
(fact
 "column creation function"
 ^{:hidden true}
 (!.lua
  [(manage/table-create-column
    sample/Schema
    (xtd/get-in sample/Schema ["Currency" "id"])
    (ut/sqlite-opts nil))
   (manage/table-create-column
    sample/Schema
    (xtd/get-in sample/Schema ["Currency" "id"])
    (ut/postgres-opts sample/SchemaLookup))])
 =>
 ["\"id\" text PRIMARY KEY" "\"id\" citext PRIMARY KEY"]
 (!.lua
  [(manage/table-create-column
    sample/Schema
    (xtd/get-in sample/Schema ["UserProfile" "account"])
    (ut/sqlite-opts nil))
   (manage/table-create-column
    sample/Schema
    (xtd/get-in sample/Schema ["UserProfile" "account"])
    (ut/postgres-opts sample/SchemaLookup))])
 =>
 ["\"account_id\" text REFERENCES \"UserAccount\""
  "\"account_id\" uuid REFERENCES \"scratch-sample-db\".\"UserAccount\""])

^{:refer xt.db.sql-manage/table-create,
  :added "4.0",
  :setup
  [(def
    +currency-table+
    (prose/|
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
   (def
    +profile-table+
    (prose/|
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
(fact
 "emits a table create string"
 ^{:hidden true}
 (!.lua
  [(manage/table-create sample/Schema "Currency" (ut/sqlite-opts nil))
   (manage/table-create
    sample/Schema
    "UserProfile"
    (ut/sqlite-opts nil))])
 =>
 [+currency-table+ +profile-table+])

^{:refer xt.db.sql-manage/table-create-all, :added "4.0"}
(fact
 "creates all tables from schema"
 ^{:hidden true}
 (def
  +table-all+
  (!.lua
   (manage/table-create-all
    sample/Schema
    sample/SchemaLookup
    (ut/sqlite-opts nil))))
 (!.lua
  (manage/table-create-all
   sample/Schema
   sample/SchemaLookup
   (ut/sqlite-opts nil)))
 =>
 +table-all+)

^{:refer xt.db.sql-manage/table-drop, :added "4.0"}
(fact
 "creates a table statement"
 ^{:hidden true}
 (!.lua
  (manage/table-drop sample/Schema "Currency" (ut/sqlite-opts nil)))
 =>
 "DROP TABLE IF EXISTS \"Currency\";")

^{:refer xt.db.sql-manage/table-drop-all,
  :added "4.0",
  :setup
  [(def
    +drop-all+
    ["DROP TABLE IF EXISTS \"RegionCity\";"
     "DROP TABLE IF EXISTS \"RegionState\";"
     "DROP TABLE IF EXISTS \"RegionCountry\";"
     "DROP TABLE IF EXISTS \"Currency\";"
     "DROP TABLE IF EXISTS \"OrganisationAccess\";"
     "DROP TABLE IF EXISTS \"Organisation\";"
     "DROP TABLE IF EXISTS \"WalletAsset\";"
     "DROP TABLE IF EXISTS \"Wallet\";"
     "DROP TABLE IF EXISTS \"Asset\";"
     "DROP TABLE IF EXISTS \"UserPrivilege\";"
     "DROP TABLE IF EXISTS \"UserNotification\";"
     "DROP TABLE IF EXISTS \"UserProfile\";"
     "DROP TABLE IF EXISTS \"UserAccount\";"])]}
(fact
 "drops all tables"
 (!.lua
  (manage/table-drop-all
   sample/Schema
   sample/SchemaLookup
   (ut/sqlite-opts nil)))
 =>
 +drop-all+)
