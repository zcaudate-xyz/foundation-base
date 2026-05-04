(ns xtbench.dart.db.schema.base-schema-test
  (:use code.test)
  (:require [std.json :as json]
            [hara.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.db.text.base-schema :as sch]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.text.base-schema/get-ident-id :added "4.0"}
(fact "gets the ident id for a schema entry"

  (!.dt
    (sch/get-ident-id {"ident" "name"
                       "type" "text"}))
  => "name"

  (!.dt
    (sch/get-ident-id {"ident" "owner"
                       "type" "ref"}))
  => "owner_id")

^{:refer xt.db.text.base-schema/list-tables :added "4.0"}
(fact "list schema tables"

  (!.dt
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

^{:refer xt.db.text.base-schema/get-cached-schema :added "4.0"}
(fact "get lookup"

  (!.dt (sch/get-cached-schema sample/Schema))
  => map?)

^{:refer xt.db.text.base-schema/create-data-keys :added "4.0"}
(fact "creates data keys"

  (!.dt
    (sch/create-data-keys sample/SchemaCurrency "Currency"))
  => ["id" "type" "symbol" "native" "decimal" "name" "plural" "description"])

^{:refer xt.db.text.base-schema/create-ref-keys :added "4.0"}
(fact "creates ref keys"

  (!.dt (sch/create-ref-keys sample/Schema "UserProfile"))
  => (just ["account" "state" "country"]  :in-any-order))

^{:refer xt.db.text.base-schema/create-rev-keys :added "4.0"}
(fact "creates rev keys"

  (!.dt
    (sch/create-rev-keys sample/Schema "UserAccount"))
  => (just ["organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"]
           :in-any-order))

^{:refer xt.db.text.base-schema/create-table-entries :added "4.0"}
(fact "creates the table keys"

  (!.dt
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

^{:refer xt.db.text.base-schema/create-defaults :added "4.0"}
(fact "creates defaults from sql inputs"

  (!.dt
    (sch/create-defaults sample/Schema "Wallet"))
  => {"slug" "default"})

^{:refer xt.db.text.base-schema/create-all-keys :added "4.0"
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

  (!.dt
    (sch/create-all-keys sample/Schema "Wallet"))
  => +all-wallet+)

^{:refer xt.db.text.base-schema/get-all-keys :added "4.0"
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

  (!.dt (sch/get-all-keys sample/Schema "Organisation"))
  => +all-org+)

^{:refer xt.db.text.base-schema/data-keys :added "4.0"}
(fact "gets data keys"

  (!.dt (sch/data-keys sample/Schema "UserAccount"))
  => ["id" "nickname" "password_hash" "password_salt" "password_updated" "is_super" "is_suspended" "is_official"])

^{:refer xt.db.text.base-schema/ref-keys :added "4.0"}
(fact "gets ref keys"

  (!.dt (sch/ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"])

^{:refer xt.db.text.base-schema/ref-id-keys :added "4.0"}
(fact "gets ref id keys"

  (!.dt (sch/ref-id-keys sample/Schema "UserProfile"))
  => {"account_id" "account",
      "state_id" "state",
      "country_id" "country"})

^{:refer xt.db.text.base-schema/rev-keys :added "4.0"}
(fact "gets rev keys"

  (set (!.dt (sch/rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"})

^{:refer xt.db.text.base-schema/table-defaults :added "4.0"}
(fact "gets the table defaults"

  (!.dt
    (sch/table-defaults sample/Schema "Wallet"))
  => {"slug" "default"})

^{:refer xt.db.text.base-schema/table-entries :added "4.0"}
(fact "gets the table entries"

  (!.dt
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

^{:refer xt.db.text.base-schema/table-columns :added "4.0"
  :setup [(def +out+
            ["id" "account_id" "first_name" "last_name" "city"
             "state_id" "country_id" "about" "language" "detail"])]}
(fact "ges the table columns"

  (!.dt (sch/table-columns sample/Schema "UserProfile"))
  => +out+)

^{:refer xt.db.text.base-schema/create-table-order :added "4.0"
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

  (!.dt
    (sch/create-table-order sample/SchemaLookup))
  => +ordered+)

^{:refer xt.db.text.base-schema/table-order :added "4.0"}
(fact "table order with caching"

  (!.dt
    (sch/table-order  sample/SchemaLookup))
  => +ordered+)

^{:refer xt.db.text.base-schema/table-coerce :added "4.0"}
(fact "coerces output given schema and type functions"

  (!.dt
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

  (s/run ['xt.db.text.base-schema])
  
  (s/seedgen-langadd 'xt.db.text.base-schema {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.text.base-schema {:lang [:lua :python] :write true}))
