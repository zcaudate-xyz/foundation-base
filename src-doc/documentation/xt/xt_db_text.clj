(ns documentation.xt-db-text
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.text.base-schema :as sch]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-table :as table]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.db.text"
         :subtitle "Schema, graph, tree, SQL, and PGREST builders."
         :lead "`xt.db.text` turns schema and query descriptions into portable database text: flattened schemas, scoped trees, SQL calls, raw SQL, tables, views, and PGREST graph/tree forms."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Database backends differ, but most application code wants to describe schemas, joins, scopes, and views once. The text layer is where those descriptions become concrete query strings or REST shapes."

[[:chapter {:title "Internal usage" :link "internal"}]]

"System and node layers depend on the text builders for backend-specific execution. Tests under `test-lang/xt/db/text` are the best source of examples for schema, SQL, tree, graph, and view behavior."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Encoding SQL values"}]]

"Before assembling full statements, the text layer turns Clojure/xtalk values into SQL literals and WHERE clauses. `xt.db.text.sql-util` provides the low-level encoders."

(fact "encode primitive values to SQL"
  ^{:refer xt.db.text.sql-util/encode-value :added "4.0"}
  (!.js
    [(ut/encode-value nil)
     (ut/encode-value true)
     (ut/encode-value "hello")
     (ut/encode-value "o'reilly")
     (ut/encode-value {:a 1})])
  => ["NULL" "TRUE" "'hello'" "'o''reilly'" "'{\"a\":1}'"]

  ^{:refer xt.db.text.sql-util/encode-query-string :added "4.0"}
  (!.js
    [(ut/encode-query-string {:id "XLM"} "WHERE" {})
     (ut/encode-query-string {:name ["neq" "hello"]} "WHERE" {})])
  => ["WHERE id = 'XLM'"
      "WHERE name != 'hello'"])

[[:section {:title "Building raw SQL"}]]

"With value encoding in place, `xt.db.text.sql-raw` assembles complete INSERT, UPDATE, DELETE, and SELECT strings from table names, column lists, and maps."

(fact "generate raw SQL statements"
  ^{:refer xt.db.text.sql-raw/raw-select :added "4.0"}
  (!.js
    (raw/raw-select "Currency"
                    {:id "XLM"}
                    ["id" "name" "type"]
                    {}))
  => "SELECT id, name, type\n  FROM Currency\n WHERE id = 'XLM';"

  ^{:refer xt.db.text.sql-raw/raw-insert :added "4.0"}
  (!.js
    (raw/raw-insert "Currency"
                    ["id" "name" "type"]
                    [{:id "XLM" :name "Stellar" :type "crypto"}]
                    {}))
  => "INSERT INTO Currency\n (id, name, type)\n VALUES\n ('XLM','Stellar','crypto');"

  ^{:refer xt.db.text.sql-raw/raw-update :added "4.0"}
  (!.js
    (raw/raw-update "Currency"
                    {:id "XLM"}
                    {:name "Stellar Lumens"}
                    {}))
  => "UPDATE Currency\n SET name = 'Stellar Lumens'\n WHERE id = 'XLM';"

  ^{:refer xt.db.text.sql-raw/raw-delete :added "4.0"}
  (!.js
    (raw/raw-delete "Currency"
                    {:id "XLM"}
                    {}))
  => "DELETE FROM Currency WHERE id = 'XLM';")

[[:section {:title "Schema introspection"}]]

"`xt.db.text.base-schema` reads a schema definition and returns table names, column lists, and key classifications. The examples use the sample schema from `xt.db.helpers.data-main-test`."

(fact "inspect tables and columns"
  ^{:refer xt.db.text.base-schema/list-tables :added "4.0"}
  (!.js
    (sch/list-tables sample/Schema))
  => (just ["Asset"
            "Currency"
            "Organisation"
            "OrganisationAccess"
            "RegionCity"
            "RegionCountry"
            "RegionState"
            "UserAccount"
            "UserNotification"
            "UserPrivilege"
            "UserProfile"
            "Wallet"
            "WalletAsset"]
           :in-any-order)

  ^{:refer xt.db.text.base-schema/data-keys :added "4.0"}
  (!.js
    (sch/data-keys sample/Schema "Currency"))
  => ["id" "type" "symbol" "native" "decimal" "name" "plural" "description"]

  ^{:refer xt.db.text.base-schema/table-columns :added "4.0"}
  (!.js
    (sch/table-columns sample/Schema "UserProfile"))
  => ["id" "account_id" "first_name" "last_name" "city"
      "state_id" "country_id" "about" "language" "detail"])

[[:section {:title "Schema-driven single-row SQL"}]]

"Given a schema, `xt.db.text.sql-table` generates CRUD statements for a single row without requiring manual column lists."

(fact "generate single-row statements from schema"
  ^{:refer xt.db.text.sql-table/table-insert-single :added "4.0"}
  (!.js
    (table/table-insert-single sample/Schema
                               "UserAccount"
                               {:id "AAA" :password-hash "HELLO"}
                               {}))
  => "INSERT INTO UserAccount\n (id, password_hash)\n VALUES\n ('AAA','HELLO');"

  ^{:refer xt.db.text.sql-table/table-update-single :added "4.0"}
  (!.js
    (table/table-update-single sample/Schema
                               "UserAccount"
                               "AAA"
                               {:password-hash "WORLD"}
                               {}))
  => "UPDATE UserAccount\n SET password_hash = 'WORLD'\n WHERE id = 'AAA';"

  ^{:refer xt.db.text.sql-table/table-upsert-single :added "4.0"}
  (!.js
    (table/table-upsert-single sample/Schema
                               "UserAccount"
                               {:id "AAA" :password-hash "HELLO"}
                               {}))
  => "INSERT INTO UserAccount\n (id, password_hash)\n VALUES\n ('AAA','HELLO')\nON CONFLICT (id) DO UPDATE SET\npassword_hash=coalesce(\"excluded\".password_hash,password_hash);"

  ^{:refer xt.db.text.sql-table/table-delete-single :added "4.0"}
  (!.js
    (table/table-delete-single sample/Schema
                               "UserAccount"
                               "AAA"
                               {}))
  => "DELETE FROM UserAccount WHERE id = 'AAA';")

[[:section {:title "End-to-end: bulk upserts from nested data"}]]

"For real inputs, `xt.db.text.sql-table/prepare-add-input` flattens nested data across linked tables and emits ordered SQLite-compatible upsert statements."

(fact "flatten nested data into ordered upserts"
  ^{:refer xt.db.text.sql-table/prepare-add-input :added "4.1"}
  (!.js
    (table/prepare-add-input {"Currency" [{:id "USD" :name "US Dollar"}]}
                             sample/Schema
                             sample/SchemaLookup
                             (ut/sqlite-opts sample/SchemaLookup)))
  => "INSERT INTO \"Currency\"\n (\"id\", \"type\", \"symbol\", \"native\", \"decimal\", \"name\", \"plural\", \"description\")\n VALUES\n ('USD',NULL,NULL,NULL,NULL,'US Dollar',NULL,NULL)\nON CONFLICT (\"id\") DO UPDATE SET\n\"type\"=coalesce(\"excluded\".\"type\",\"type\"),\n\"symbol\"=coalesce(\"excluded\".\"symbol\",\"symbol\"),\n\"native\"=coalesce(\"excluded\".\"native\",\"native\"),\n\"decimal\"=coalesce(\"excluded\".\"decimal\",\"decimal\"),\n\"name\"=coalesce(\"excluded\".\"name\",\"name\"),\n\"plural\"=coalesce(\"excluded\".\"plural\",\"plural\"),\n\"description\"=coalesce(\"excluded\".\"description\",\"description\");")

[[:chapter {:title "API" :link "api"}]]

