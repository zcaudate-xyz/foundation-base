(ns xtbench.ruby.db.schema.sql-manage-test
  (:require [std.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.db.schema.base-schema :as sch]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.db.schema.sql-util :as ut]
             [xt.db.schema.sql-manage :as manage]
             [xt.old.db.sample-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +table-all+
  (mapv prose/join-lines
        [["CREATE TABLE IF NOT EXISTS \"UserAccount\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"nickname\" text,"
          "  \"password_hash\" text,"
          "  \"password_salt\" text,"
          "  \"password_updated\" integer,"
          "  \"is_super\" boolean,"
          "  \"is_suspended\" boolean,"
          "  \"is_official\" boolean"
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"UserProfile\" ("
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
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"UserNotification\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"account_id\" text REFERENCES \"UserAccount\","
          "  \"general\" text,"
          "  \"trading\" text,"
          "  \"funding\" text"
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"UserPrivilege\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"account_id\" text REFERENCES \"UserAccount\","
          "  \"type\" text,"
          "  \"start_time\" integer,"
          "  \"end_time\" integer"
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"Asset\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"currency_id\" text REFERENCES \"Currency\""
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"Wallet\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"slug\" text,"
          "  \"owner_id\" text REFERENCES \"UserAccount\""
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"WalletAsset\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"asset_id\" text REFERENCES \"Asset\","
          "  \"wallet_id\" text REFERENCES \"Wallet\""
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"Organisation\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"name\" text,"
          "  \"title\" text,"
          "  \"description\" text,"
          "  \"tags\" text,"
          "  \"owner_id\" text REFERENCES \"UserAccount\""
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"OrganisationAccess\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"organisation_id\" text REFERENCES \"Organisation\","
          "  \"account_id\" text REFERENCES \"UserAccount\","
          "  \"role\" text"
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"Currency\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"type\" text,"
          "  \"symbol\" text,"
          "  \"native\" text,"
          "  \"decimal\" integer,"
          "  \"name\" text,"
          "  \"plural\" text,"
          "  \"description\" text"
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"RegionCountry\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"ref\" integer,"
          "  \"name\" text,"
          "  \"name_native\" text,"
          "  \"iso\" text,"
          "  \"iso_numeric\" text,"
          "  \"iso_tld\" text,"
          "  \"iso_phone\" text,"
          "  \"capital\" text,"
          "  \"flag\" text,"
          "  \"region\" text,"
          "  \"subregion\" text,"
          "  \"latitude\" text,"
          "  \"longitude\" text,"
          "  \"translations\" text,"
          "  \"timezones\" text,"
          "  \"currencies\" text"
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"RegionState\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"ref\" integer,"
          "  \"name\" text,"
          "  \"code\" text,"
          "  \"country_id\" text REFERENCES \"RegionCountry\","
          "  \"latitude\" text,"
          "  \"longitude\" text"
          ");"]
         ["CREATE TABLE IF NOT EXISTS \"RegionCity\" ("
          "  \"id\" text PRIMARY KEY,"
          "  \"ref\" integer,"
          "  \"name\" text,"
          "  \"latitude\" text,"
          "  \"longitude\" text,"
          "  \"state_id\" text REFERENCES \"RegionState\","
          "  \"country_id\" text REFERENCES \"RegionCountry\""
          ");"]]))

^{:refer xt.db.schema.sql-manage/table-create-column :added "4.0"}
(fact "column creation function"

  (!.rb
   [(manage/table-create-column sample/Schema
                                (xtd/get-in sample/Schema
                                          ["Currency" "id"])
                                (ut/sqlite-opts nil))
    (manage/table-create-column sample/Schema
                                (xtd/get-in sample/Schema
                                          ["Currency" "id"])
                                (ut/postgres-opts sample/SchemaLookup))])
  => ["\"id\" text PRIMARY KEY"
      "\"id\" citext PRIMARY KEY"]

  (!.rb
   [(manage/table-create-column sample/Schema
                                (xtd/get-in sample/Schema
                                          ["UserProfile" "account"])
                                (ut/sqlite-opts nil))
    (manage/table-create-column sample/Schema
                                (xtd/get-in sample/Schema
                                          ["UserProfile" "account"])
                                (ut/postgres-opts sample/SchemaLookup))])
  => ["\"account_id\" text REFERENCES \"UserAccount\""
      "\"account_id\" uuid REFERENCES \"scratch-sample-db\".\"UserAccount\""])

^{:refer xt.db.schema.sql-manage/table-create :added "4.0"
  :setup [(def +currency-table+
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
                   (def +profile-table+
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
(fact "emits a table create string"

  (!.rb
   [(manage/table-create sample/Schema
                         "Currency"
                         (ut/sqlite-opts nil))
    (manage/table-create sample/Schema
                         "UserProfile"
                         (ut/sqlite-opts nil))])
  => [+currency-table+
      +profile-table+])

^{:refer xt.db.schema.sql-manage/table-create-all :added "4.0"}
(fact "creates all tables from schema"

  (!.rb
    (manage/table-create-all sample/Schema
                             sample/SchemaLookup
                             (ut/sqlite-opts nil)))
  => +table-all+)

^{:refer xt.db.schema.sql-manage/table-drop :added "4.0"}
(fact "creates a table statement"

  (!.rb
   (manage/table-drop sample/Schema
                      "Currency"
                      (ut/sqlite-opts nil)))
  => "DROP TABLE IF EXISTS \"Currency\";")

^{:refer xt.db.schema.sql-manage/table-drop-all :added "4.0"
  :setup [(def +drop-all+
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
(fact "drops all tables"

  (!.rb
   (manage/table-drop-all sample/Schema
                          sample/SchemaLookup
                          (ut/sqlite-opts nil)))
  => +drop-all+)

(comment
  (s/pedantic ['xt.db.schema.sql-manage])
  
  (s/run ['xt.db.schema.sql-manage])
  
  (s/seedgen-benchadd   '[xt.db.schema.sql-manage] {:lang [:dart :julia] :write true})
  (s/seedgen-langadd    '[xt.db.schema.sql-manage] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.db.schema.sql-manage] {:lang [:lua :python] :write true}))
