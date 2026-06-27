(ns xtbench.dart.db.text.sql-table-test
  (:require [hara.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.db.text.base-schema :as sch]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-table :as table]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.text.sql-table/table-update-single :added "4.0"}
(fact "generates single update statement"

  (!.dt
   (table/table-update-single sample/Schema
                              "UserAccount"
                              "AAA"
                              {:password-hash "HELLO"}
                              {}))
  => "UPDATE UserAccount\n SET password_hash = 'HELLO'\n WHERE id = 'AAA';")

^{:refer xt.db.text.sql-table/table-insert-single :added "4.0"}
(fact "generates single insert statement"

  (!.dt
   (table/table-insert-single sample/Schema
                              "UserAccount"
                              {:id "AAA" :password-hash "HELLO"}
                              {}))
  => "INSERT INTO UserAccount\n (id, password_hash)\n VALUES\n ('AAA','HELLO');")

^{:refer xt.db.text.sql-table/table-delete-single :added "4.0"}
(fact "generates single delete statement"

  (!.dt
   (table/table-delete-single sample/Schema
                              "UserAccount"
                              "AAA"
                              {}))
  => "DELETE FROM UserAccount WHERE id = 'AAA';")

^{:refer xt.db.text.sql-table/table-upsert-single :added "4.0"}
(fact "generates single upsert statement"

  (!.dt
   (table/table-upsert-single sample/Schema
                              "UserAccount"
                              {:id "AAA" :password-hash "HELLO"}
                              {}))
  => (prose/|
      "INSERT INTO UserAccount"
      " (id, password_hash)"
      " VALUES"
      " ('AAA','HELLO')"
      "ON CONFLICT (id) DO UPDATE SET"
      "password_hash=coalesce(\"excluded\".password_hash,password_hash);"))

^{:refer xt.db.text.sql-table/table-insert :added "4.0"
  :setup [(def +inserts+
            [(prose/|
              "INSERT INTO UserAccount"
              " (id, nickname, password_hash, password_salt, password_updated, is_super, is_suspended, is_official)"
              " VALUES"
              " ('00000000-0000-0000-0000-000000000000','root',NULL,NULL,'1630408723423619',TRUE,FALSE,FALSE);")
             (prose/|
              "INSERT INTO UserProfile"
              " (id, account_id, first_name, last_name, city, state_id, country_id, about, language, detail)"
              " VALUES"
              " ('c4643895-b0ce-44cc-b07b-2386bf18d43b','00000000-0000-0000-0000-000000000000','Root','User',NULL,NULL,NULL,NULL,'en','{\"hello\":\"world\"}');")])]}
(fact "creates an insert statement"

  (!.dt
   (table/table-insert sample/Schema
                       sample/SchemaLookup
                       "UserAccount"
                       sample/RootUser
                       {}))
  => +inserts+)

^{:refer xt.db.text.sql-table/table-upsert :added "4.0"
  :setup [(def +upserts+
            [(prose/|
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
             (prose/|
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

  (!.dt
   (table/table-upsert sample/Schema
                       sample/SchemaLookup
                       "UserAccount"
                       sample/RootUser
                       {}))
  => +upserts+)

^{:refer xt.db.text.sql-table/prepare-add-input :added "4.1"}
(fact "prepare add input"

  (!.dt
    (table/prepare-add-input {"UserAccount" [sample/RootUser]}
                             sample/Schema
                             sample/SchemaLookup
                             {}))
  => (clojure.string/join "\n\n" +upserts+)

  (!.dt
    (table/prepare-add-input {"Currency" [{"id" "USD"}]}
                             sample/Schema
                             sample/SchemaLookup
                             (ut/sqlite-opts sample/SchemaLookup)))

  (!.dt
    (table/prepare-add-input {"Currency" [{"id" "USD" "name" "US Dollar"}]}
                             sample/Schema
                             sample/SchemaLookup
                             (ut/sqlite-opts sample/SchemaLookup)))
  => "INSERT INTO \"Currency\"\n (\"id\", \"type\", \"symbol\", \"native\", \"decimal\", \"name\", \"plural\", \"description\")\n VALUES\n ('USD',NULL,NULL,NULL,NULL,'US Dollar',NULL,NULL)\nON CONFLICT (\"id\") DO UPDATE SET\n\"type\"=coalesce(\"excluded\".\"type\",\"type\"),\n\"symbol\"=coalesce(\"excluded\".\"symbol\",\"symbol\"),\n\"native\"=coalesce(\"excluded\".\"native\",\"native\"),\n\"decimal\"=coalesce(\"excluded\".\"decimal\",\"decimal\"),\n\"name\"=coalesce(\"excluded\".\"name\",\"name\"),\n\"plural\"=coalesce(\"excluded\".\"plural\",\"plural\"),\n\"description\"=coalesce(\"excluded\".\"description\",\"description\");"

  (!.dt
    (table/prepare-add-input {"Currency" [{"id" "USD" "name" "US Dollar"}]}
                             sample/Schema
                             sample/SchemaLookup
                             (ut/postgres-opts sample/SchemaLookup)))
  => "INSERT INTO \"scratch-sample-db\".\"Currency\"\n (\"id\", \"type\", \"symbol\", \"native\", \"decimal\", \"name\", \"plural\", \"description\")\n VALUES\n ('USD',NULL,NULL,NULL,NULL,'US Dollar',NULL,NULL)\nON CONFLICT (\"id\") DO UPDATE SET\n\"type\"=coalesce(\"excluded\".\"type\",\"type\"),\n\"symbol\"=coalesce(\"excluded\".\"symbol\",\"symbol\"),\n\"native\"=coalesce(\"excluded\".\"native\",\"native\"),\n\"decimal\"=coalesce(\"excluded\".\"decimal\",\"decimal\"),\n\"name\"=coalesce(\"excluded\".\"name\",\"name\"),\n\"plural\"=coalesce(\"excluded\".\"plural\",\"plural\"),\n\"description\"=coalesce(\"excluded\".\"description\",\"description\");")

^{:refer xt.db.text.sql-table/prepare-remove-input :added "4.1"}
(fact "prepare remove input"

  (!.dt
    (table/prepare-remove-input {"Currency" [{"id" "USD"}]}
                                sample/Schema
                                sample/SchemaLookup
                                (ut/sqlite-opts sample/SchemaLookup)))
  => "DELETE FROM \"Currency\" WHERE \"id\" = 'USD';"

  (!.dt
    (table/prepare-remove-input {"Currency" [{"id" "USD"}
                                             {"id" "AUD"}]}
                                sample/Schema
                                sample/SchemaLookup
                                (ut/sqlite-opts sample/SchemaLookup)))
  => "DELETE FROM \"Currency\" WHERE \"id\" = 'USD';\n\nDELETE FROM \"Currency\" WHERE \"id\" = 'AUD';"

  (!.dt
    (table/prepare-remove-input {"Currency" [{"id" "USD"}
                                             {"id" "AUD"}]}
                                sample/Schema
                                sample/SchemaLookup
                                (ut/postgres-opts sample/SchemaLookup)))
  => "DELETE FROM \"scratch-sample-db\".\"Currency\" WHERE \"id\" = 'USD';\n\nDELETE FROM \"scratch-sample-db\".\"Currency\" WHERE \"id\" = 'AUD';")

(comment
  (s/pedantic ['xt.db.text.sql-table])
  
  (s/run ['xt.db.text.sql-table])
  
  (s/seedgen-benchadd   '[xt.db.text.sql-table] {:lang [:dart :julia] :write true})
  (s/seedgen-langadd    '[xt.db.text.sql-table] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.db.text.sql-table] {:lang [:lua :python] :write true}))
