(ns xt.db.schema.base-schema-test
  (:use code.test)
  (:require [std.json :as json]
            [std.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.schema.base-schema :as sch]
             [xt.db.schema.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.schema.base-schema :as sch]
             [xt.db.schema.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.schema.base-schema :as sch]
             [xt.db.schema.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.schema.base-schema/get-ident-id :added "4.0"}
(fact "gets the ident id for a schema entry"

  (!.js
    (sch/get-ident-id {"ident" "name"
                       "type" "text"}))
  => "name"

  (!.js
    (sch/get-ident-id {"ident" "owner"
                       "type" "ref"}))
  => "owner_id"

  (!.lua
    (sch/get-ident-id {"ident" "name"
                       "type" "text"}))
  => "name"

  (!.lua
    (sch/get-ident-id {"ident" "owner"
                       "type" "ref"}))
  => "owner_id"

  (!.py
    (sch/get-ident-id {"ident" "name"
                       "type" "text"}))
  => "name"

  (!.py
    (sch/get-ident-id {"ident" "owner"
                       "type" "ref"}))
  => "owner_id")

^{:refer xt.db.schema.base-schema/list-tables :added "4.0"}
(fact "list schema tables"

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

  (!.lua
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

  (!.py
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
           :in-any-order))

^{:refer xt.db.schema.base-schema/get-cached-schema :added "4.0"}
(fact "get lookup"

  (!.js (sch/get-cached-schema sample/Schema))
  => map?

  (!.lua (sch/get-cached-schema sample/Schema))
  => map?

  (!.py (sch/get-cached-schema sample/Schema))
  => map?)

^{:refer xt.db.schema.base-schema/create-data-keys :added "4.0"}
(fact "creates data keys"

  (!.js
    (sch/create-data-keys sample/SchemaCurrency "Currency"))
  => ["id" "type" "symbol" "native" "decimal" "name" "plural" "description"]

  (!.lua
    (sch/create-data-keys sample/SchemaCurrency "Currency"))
  => ["id" "type" "symbol" "native" "decimal" "name" "plural" "description"]

  (!.py
    (sch/create-data-keys sample/SchemaCurrency "Currency"))
  => ["id" "type" "symbol" "native" "decimal" "name" "plural" "description"])

^{:refer xt.db.schema.base-schema/create-ref-keys :added "4.0"}
(fact "creates ref keys"

  (!.js (sch/create-ref-keys sample/Schema "UserProfile"))
  => (just ["account" "state" "country"]  :in-any-order)

  (!.lua (sch/create-ref-keys sample/Schema "UserProfile"))
  => (just ["account" "state" "country"]  :in-any-order)

  (!.py (sch/create-ref-keys sample/Schema "UserProfile"))
  => (just ["account" "state" "country"]  :in-any-order))

^{:refer xt.db.schema.base-schema/create-rev-keys :added "4.0"}
(fact "creates rev keys"

  (!.js
    (sch/create-rev-keys sample/Schema "UserAccount"))
  => (just ["organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"]
           :in-any-order)

  (!.lua
    (sch/create-rev-keys sample/Schema "UserAccount"))
  => (just ["organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"]
           :in-any-order)

  (!.py
    (sch/create-rev-keys sample/Schema "UserAccount"))
  => (just ["organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"]
           :in-any-order))

^{:refer xt.db.schema.base-schema/create-table-entries :added "4.0"}
(fact "creates the table keys"

  (!.js
    (sch/create-table-entries sample/Schema "Wallet"))
  => [{"ident" "id",
       "primary" true,
       "scope" "id",
       "order" 0,
       "type" "uuid",
       "cardinality" "one"}
      {"ident" "slug",
       "scope" "data",
       "order" 1,
       "type" "citext",
       "cardinality" "one",
       "sql" {"default" "default"}}
      {"ident" "owner",
       "scope" "ref",
       "order" 2,
       "required" true,
       "type" "ref",
       "ref"
       {"key" "owner",
        "rkey" "_owner",
        "link"
        {"lang" "postgres",
         "id" "UserAccount",
         "section" "code",
         "module" "xt.db.helpers.seed-user-test"},
        "type" "forward",
        "rident" "wallets",
        "rval" "wallets",
        "ns" "UserAccount",
        "val" "owner"},
       "cardinality" "one"}]

  (!.lua
    (sch/create-table-entries sample/Schema "Wallet"))
  => [{"ident" "id",
       "primary" true,
       "scope" "id",
       "order" 0,
       "type" "uuid",
       "cardinality" "one"}
      {"ident" "slug",
       "scope" "data",
       "order" 1,
       "type" "citext",
       "cardinality" "one",
       "sql" {"default" "default"}}
      {"ident" "owner",
       "scope" "ref",
       "order" 2,
       "required" true,
       "type" "ref",
       "ref"
       {"key" "owner",
        "rkey" "_owner",
        "link"
        {"lang" "postgres",
         "id" "UserAccount",
         "section" "code",
         "module" "xt.db.helpers.seed-user-test"},
        "type" "forward",
        "rident" "wallets",
        "rval" "wallets",
        "ns" "UserAccount",
        "val" "owner"},
       "cardinality" "one"}]

  (!.py
    (sch/create-table-entries sample/Schema "Wallet"))
  => [{"ident" "id",
       "primary" true,
       "scope" "id",
       "order" 0,
       "type" "uuid",
       "cardinality" "one"}
      {"ident" "slug",
       "scope" "data",
       "order" 1,
       "type" "citext",
       "cardinality" "one",
       "sql" {"default" "default"}}
      {"ident" "owner",
       "scope" "ref",
       "order" 2,
       "required" true,
       "type" "ref",
       "ref"
       {"key" "owner",
        "rkey" "_owner",
        "link"
        {"lang" "postgres",
         "id" "UserAccount",
         "section" "code",
         "module" "xt.db.helpers.seed-user-test"},
        "type" "forward",
        "rident" "wallets",
        "rval" "wallets",
        "ns" "UserAccount",
        "val" "owner"},
       "cardinality" "one"}])

^{:refer xt.db.schema.base-schema/create-defaults :added "4.0"}
(fact "creates defaults from sql inputs"

  (!.js
    (sch/create-defaults sample/Schema "Wallet"))
  => {"slug" "default"}

  (!.lua
    (sch/create-defaults sample/Schema "Wallet"))
  => {"slug" "default"}

  (!.py
    (sch/create-defaults sample/Schema "Wallet"))
  => {"slug" "default"})

^{:refer xt.db.schema.base-schema/create-all-keys :added "4.0"
  :setup [(def +all-wallet+
            {"table"
             [{"ident" "id",
               "primary" true,
               "scope" "id",
               "order" 0,
               "type" "uuid",
               "cardinality" "one"}
              {"ident" "slug",
               "scope" "data",
               "order" 1,
               "type" "citext",
               "cardinality" "one",
               "sql" {"default" "default"}}
              {"ident" "owner",
               "scope" "ref",
               "order" 2,
               "required" true,
               "type" "ref",
               "ref"
               {"key" "owner",
                "rkey" "_owner",
                "link"
                {"lang" "postgres",
                 "id" "UserAccount",
                 "section" "code",
                 "module" "xt.db.helpers.seed-user-test"},
                "type" "forward",
                "rident" "wallets",
                "rval" "wallets",
                "ns" "UserAccount",
                "val" "owner"},
               "cardinality" "one"}],
             "rev" ["entries"],
             "ref" ["owner"],
             "data" ["id" "slug"],
             "defaults" {"slug" "default"},
             "ref_id" {"owner_id" "owner"}})]}
(fact "creates all keys"

  (!.js
    (sch/create-all-keys sample/Schema "Wallet"))
  => +all-wallet+

  (!.lua
    (sch/create-all-keys sample/Schema "Wallet"))
  => +all-wallet+

  (!.py
    (sch/create-all-keys sample/Schema "Wallet"))
  => +all-wallet+)

^{:refer xt.db.schema.base-schema/get-all-keys :added "4.0"
  :setup [(def +all-org+
            {"table"
             [{"ident" "id",
               "primary" true,
               "scope" "id",
               "order" 0,
               "type" "uuid",
               "cardinality" "one"}
              {"ident" "name",
               "unique" true,
               "scope" "data",
               "order" 1,
               "required" true,
               "type" "citext",
               "cardinality" "one"}
              {"ident" "title",
               "scope" "data",
               "order" 2,
               "required" true,
               "type" "text",
               "cardinality" "one"}
              {"ident" "description",
               "scope" "data",
               "order" 3,
               "type" "text",
               "cardinality" "one"}
              {"ident" "tags",
               "scope" "data",
               "order" 4,
               "type" "array",
               "cardinality" "one"}
              {"ident" "owner",
               "scope" "ref",
               "order" 5,
               "type" "ref",
               "ref"
               {"key" "owner",
                "rkey" "_owner",
                "link"
                {"lang" "postgres",
                 "id" "UserAccount",
                 "section" "code",
                 "module" "xt.db.helpers.seed-user-test"},
                "type" "forward",
                "rident" "organisations",
                "rval" "organisations",
                "ns" "UserAccount",
                "val" "owner"},
               "cardinality" "one"}],
             "rev" ["access"],
             "ref" ["owner"],
             "data" ["id" "name" "title" "description" "tags"],
             "defaults" {},
             "ref_id" {"owner_id" "owner"}})]}
(fact "get all keys"

  (!.js (sch/get-all-keys sample/Schema "Organisation"))
  => +all-org+

  (!.lua (sch/get-all-keys sample/Schema "Organisation"))
  => +all-org+

  (!.py (sch/get-all-keys sample/Schema "Organisation"))
  => +all-org+)

^{:refer xt.db.schema.base-schema/data-keys :added "4.0"}
(fact "gets data keys"

  (!.js (sch/data-keys sample/Schema "UserAccount"))
  => ["id" "nickname" "password_hash" "password_salt" "password_updated" "is_super" "is_suspended" "is_official"]

  (!.lua (sch/data-keys sample/Schema "UserAccount"))
  => ["id" "nickname" "password_hash" "password_salt" "password_updated" "is_super" "is_suspended" "is_official"]

  (!.py (sch/data-keys sample/Schema "UserAccount"))
  => ["id" "nickname" "password_hash" "password_salt" "password_updated" "is_super" "is_suspended" "is_official"])

^{:refer xt.db.schema.base-schema/ref-keys :added "4.0"}
(fact "gets ref keys"

  (!.js (sch/ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"]

  (!.lua (sch/ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"]

  (!.py (sch/ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"])

^{:refer xt.db.schema.base-schema/ref-id-keys :added "4.0"}
(fact "gets ref id keys"

  (!.js (sch/ref-id-keys sample/Schema "UserProfile"))
  => {"account_id" "account",
      "state_id" "state",
      "country_id" "country"}

  (!.lua (sch/ref-id-keys sample/Schema "UserProfile"))
  => {"account_id" "account",
      "state_id" "state",
      "country_id" "country"}

  (!.py (sch/ref-id-keys sample/Schema "UserProfile"))
  => {"account_id" "account",
      "state_id" "state",
      "country_id" "country"})

^{:refer xt.db.schema.base-schema/rev-keys :added "4.0"}
(fact "gets rev keys"

  (set (!.js (sch/rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"}

  (set (!.lua (sch/rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"}

  (set (!.py (sch/rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"})

^{:refer xt.db.schema.base-schema/table-defaults :added "4.0"}
(fact "gets the table defaults"

  (!.js
    (sch/table-defaults sample/Schema "Wallet"))
  => {"slug" "default"}

  (!.lua
    (sch/table-defaults sample/Schema "Wallet"))
  => {"slug" "default"}

  (!.py
    (sch/table-defaults sample/Schema "Wallet"))
  => {"slug" "default"})

^{:refer xt.db.schema.base-schema/table-entries :added "4.0"}
(fact "gets the table entries"

  (!.js
    (sch/table-entries sample/Schema "Wallet"))
  => [{"ident" "id",
       "primary" true,
       "scope" "id",
       "order" 0,
       "type" "uuid",
       "cardinality" "one"}
      {"ident" "slug",
       "scope" "data",
       "order" 1,
       "type" "citext",
       "cardinality" "one",
       "sql" {"default" "default"}}
      {"ident" "owner",
       "scope" "ref",
       "order" 2,
       "required" true,
       "type" "ref",
       "ref"
       {"key" "owner",
        "rkey" "_owner",
        "link"
        {"lang" "postgres",
         "id" "UserAccount",
         "section" "code",
         "module" "xt.db.helpers.seed-user-test"},
        "type" "forward",
        "rident" "wallets",
        "rval" "wallets",
        "ns" "UserAccount",
        "val" "owner"},
       "cardinality" "one"}]

  (!.lua
    (sch/table-entries sample/Schema "Wallet"))
  => [{"ident" "id",
       "primary" true,
       "scope" "id",
       "order" 0,
       "type" "uuid",
       "cardinality" "one"}
      {"ident" "slug",
       "scope" "data",
       "order" 1,
       "type" "citext",
       "cardinality" "one",
       "sql" {"default" "default"}}
      {"ident" "owner",
       "scope" "ref",
       "order" 2,
       "required" true,
       "type" "ref",
       "ref"
       {"key" "owner",
        "rkey" "_owner",
        "link"
        {"lang" "postgres",
         "id" "UserAccount",
         "section" "code",
         "module" "xt.db.helpers.seed-user-test"},
        "type" "forward",
        "rident" "wallets",
        "rval" "wallets",
        "ns" "UserAccount",
        "val" "owner"},
       "cardinality" "one"}]

  (!.py
    (sch/table-entries sample/Schema "Wallet"))
  => [{"ident" "id",
       "primary" true,
       "scope" "id",
       "order" 0,
       "type" "uuid",
       "cardinality" "one"}
      {"ident" "slug",
       "scope" "data",
       "order" 1,
       "type" "citext",
       "cardinality" "one",
       "sql" {"default" "default"}}
      {"ident" "owner",
       "scope" "ref",
       "order" 2,
       "required" true,
       "type" "ref",
       "ref"
       {"key" "owner",
        "rkey" "_owner",
        "link"
        {"lang" "postgres",
         "id" "UserAccount",
         "section" "code",
         "module" "xt.db.helpers.seed-user-test"},
        "type" "forward",
        "rident" "wallets",
        "rval" "wallets",
        "ns" "UserAccount",
        "val" "owner"},
       "cardinality" "one"}])

^{:refer xt.db.schema.base-schema/table-columns :added "4.0"
  :setup [(def +out+
            ["id" "account_id" "first_name" "last_name" "city"
             "state_id" "country_id" "about" "language" "detail"])]}
(fact "ges the table columns"

  (!.js (sch/table-columns sample/Schema "UserProfile"))
  => +out+

  (!.lua (sch/table-columns sample/Schema "UserProfile"))
  => +out+

  (!.py (sch/table-columns sample/Schema "UserProfile"))
  => +out+)

^{:refer xt.db.schema.base-schema/create-table-order :added "4.0"
  :setup [(def +ordered+
            ["UserAccount"
             "UserProfile"
             "UserNotification"
             "UserPrivilege"
             "Asset"
             "Wallet"
             "WalletAsset"
             "Organisation"
             "OrganisationAccess"
             "Currency"
             "RegionCountry"
             "RegionState"
             "RegionCity"])]}
(fact "creates the table order"

  (!.js
    (sch/create-table-order sample/SchemaLookup))
  => +ordered+

  (!.lua
    (sch/create-table-order sample/SchemaLookup))
  => +ordered+

  (!.py
    (sch/create-table-order sample/SchemaLookup))
  => +ordered+)

^{:refer xt.db.schema.base-schema/table-order :added "4.0"}
(fact "table order with caching"

  (!.js
    (sch/table-order  sample/SchemaLookup))
  => +ordered+

  (!.lua
    (sch/table-order  sample/SchemaLookup))
  => +ordered+

  (!.py
    (sch/table-order  sample/SchemaLookup))
  => +ordered+)

^{:refer xt.db.schema.base-schema/table-coerce :added "4.0"}
(fact "coerces output given schema and type functions"

  (!.js
    [(sch/table-coerce sample/Schema
                       "UserAccount"
                       {:is-super 1}
                       {:boolean ut/sqlite-to-boolean})
     (sch/table-coerce sample/Schema
                       "UserAccount"
                       {:is-super 1
                        :organisations [{:name "hello"}]}
                       {:boolean ut/sqlite-to-boolean})])
  => [{"is_super" true}
      {"organisations" [{"name" "hello"}], "is_super" true}]

  (!.lua
    [(sch/table-coerce sample/Schema
                       "UserAccount"
                       {:is-super 1}
                       {:boolean ut/sqlite-to-boolean})
     (sch/table-coerce sample/Schema
                       "UserAccount"
                       {:is-super 1
                        :organisations [{:name "hello"}]}
                       {:boolean ut/sqlite-to-boolean})])
  => [{"is_super" true}
      {"organisations" [{"name" "hello"}], "is_super" true}]

  (!.py
    [(sch/table-coerce sample/Schema
                       "UserAccount"
                       {:is-super 1}
                       {:boolean ut/sqlite-to-boolean})
     (sch/table-coerce sample/Schema
                       "UserAccount"
                       {:is-super 1
                        :organisations [{:name "hello"}]}
                       {:boolean ut/sqlite-to-boolean})])
  => [{"is_super" true}
      {"organisations" [{"name" "hello"}], "is_super" true}])

(comment

  (s/run ['xt.db.schema.base-schema])
  
  (s/seedgen-langadd 'xt.db.schema.base-schema {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.schema.base-schema {:lang [:lua :python] :write true}))
