(ns xt.db.sql-table-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.db.base-flatten :as f]
             [xt.db.cache-util :as cache-util]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-graph :as graph]
             [xt.db.sql-util :as ut]
             [xt.lang.base-lib :as k]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :js)
                 (l/rt:scaffold :lua)
                 (l/rt:scaffold :python)
                 true)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-table/table-update-single :added "4.0"}
(fact "generates single update statement"
  ^:hidden
  
  (!.js
   (table/table-update-single sample/Schema
                              "UserAccount"
                              "AAA"
                              {:password-hash "HELLO"}
                              {}))
  => "UPDATE UserAccount\n SET password_hash = 'HELLO'\n WHERE id = 'AAA';")

^{:refer xt.db.sql-table/table-insert-single :added "4.0"}
(fact "generates single insert statement"
  ^:hidden
  
  (!.js
   (table/table-insert-single sample/Schema
                              "UserAccount"
                              {:id "AAA" :password-hash "HELLO"}
                              {}))
  => "INSERT INTO UserAccount\n (id, password_hash)\n VALUES\n ('AAA','HELLO');")

^{:refer xt.db.sql-table/table-delete-single :added "4.0"}
(fact "generates single delete statement"
  ^:hidden
  
  (!.js
   (table/table-delete-single sample/Schema
                              "UserAccount"
                              "AAA"
                              {}))
  => "DELETE FROM UserAccount WHERE id = 'AAA';")

^{:refer xt.db.sql-table/table-upsert-single :added "4.0"}
(fact "generates single upsert statement"
  ^:hidden
  
  (!.js
   (table/table-upsert-single sample/Schema
                              "UserAccount"
                              {:id "AAA" :password-hash "HELLO"}
                              {}))
  => (std.string/|
      "INSERT INTO UserAccount"
      " (id, password_hash)"
      " VALUES"
      " ('AAA','HELLO')"
      "ON CONFLICT (id) DO UPDATE SET"
      "password_hash=coalesce(\"excluded\".password_hash,password_hash);"))

^{:refer xt.db.sql-table/table-filter-id :added "4.0"}
(fact "predicate for flat entry")

^{:refer xt.db.sql-table/table-get-data :added "4.0"}
(fact "gets data flat entry")

^{:refer xt.db.sql-table/table-emit-flat :added "4.0"}
(fact "emit util for insert and upsert")

^{:refer xt.db.sql-table/table-insert :added "4.0"
  :setup [(def +inserts+
            [(std.string/|
              "INSERT INTO UserAccount"
              " (id, nickname, password_hash, password_salt, password_updated, is_super, is_suspended, is_official)"
              " VALUES"
              " ('00000000-0000-0000-0000-000000000000','root',NULL,NULL,'1630408723423619',TRUE,FALSE,FALSE);")
             (std.string/|
              "INSERT INTO UserProfile"
              " (id, account_id, first_name, last_name, city, state_id, country_id, about, language, detail)"
              " VALUES"
              " ('c4643895-b0ce-44cc-b07b-2386bf18d43b','00000000-0000-0000-0000-000000000000','Root','User',NULL,NULL,NULL,NULL,'en','{\"hello\":\"world\"}');")])]}
(fact "creates an insert statement"
  ^:hidden
  
  (!.js
   (table/table-insert sample/Schema
                       sample/SchemaLookup
                       "UserAccount"
                       sample/RootUser
                       {}))
  
  => +inserts+

  (!.lua
   (table/table-insert sample/Schema
                       sample/SchemaLookup
                       "UserAccount"
                       sample/RootUser
                       {}))
  => vector?
  
  
  (!.py
   (table/table-insert sample/Schema
                       sample/SchemaLookup
                       "UserAccount"
                       sample/RootUser
                       {}))
  => vector?)

^{:refer xt.db.sql-table/table-upsert :added "4.0"
  :setup [(def +upserts+
            [(std.string/|
              "INSERT INTO UserAccount"
              " (id, nickname, password_hash, password_salt, password_updated, is_super, is_suspended, is_official)"
              " VALUES"
              " ('00000000-0000-0000-0000-000000000000','root',NULL,NULL,'1630408723423619',TRUE,FALSE,FALSE)"
              "ON CONFLICT (id) DO UPDATE SET"
              "nickname=coalesce(\"excluded\".nickname,nickname),"
              "password_hash=coalesce(\"excluded\".password_hash,password_hash),"
              "password_salt=coalesce(\"excluded\".password_salt,password_salt),"
              "password_updated=coalesce(\"excluded\".password_updated,password_updated),"
              "is_super=coalesce(\"excluded\".is_super,is_super),"
              "is_suspended=coalesce(\"excluded\".is_suspended,is_suspended),"
              "is_official=coalesce(\"excluded\".is_official,is_official);")
             (std.string/|
              "INSERT INTO UserProfile"
              " (id, account_id, first_name, last_name, city, state_id, country_id, about, language, detail)"
              " VALUES"
              " ('c4643895-b0ce-44cc-b07b-2386bf18d43b','00000000-0000-0000-0000-000000000000','Root','User',NULL,NULL,NULL,NULL,'en','{\"hello\":\"world\"}')"
              "ON CONFLICT (id) DO UPDATE SET"
              "account_id=coalesce(\"excluded\".account_id,account_id),"
              "first_name=coalesce(\"excluded\".first_name,first_name),"
              "last_name=coalesce(\"excluded\".last_name,last_name),"
              "city=coalesce(\"excluded\".city,city),"
              "state_id=coalesce(\"excluded\".state_id,state_id),"
              "country_id=coalesce(\"excluded\".country_id,country_id),"
              "about=coalesce(\"excluded\".about,about),"
              "language=coalesce(\"excluded\".language,language),"
              "detail=coalesce(\"excluded\".detail,detail);")])]}
(fact "generate upsert statement"
  ^:hidden
  
  (!.js
   (table/table-upsert sample/Schema
                       sample/SchemaLookup
                       "UserAccount"
                       sample/RootUser
                       {}))
  
  => +upserts+

  (!.lua
   (table/table-upsert sample/Schema
                       sample/SchemaLookup
                       "UserAccount"
                       sample/RootUser
                       {}))
  => vector?

  (!.py
   (table/table-upsert sample/Schema
                       sample/SchemaLookup
                       "UserAccount"
                       sample/RootUser
                       {}))
  => vector?)

(comment
  (!.lua (tostring 1630408723423619))
  )
