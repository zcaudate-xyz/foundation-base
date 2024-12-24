(ns xt.db.base-schema-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.json :as json]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.db.base-schema :as sch]
             [xt.db.sample-test :as sample]
             [xt.db.sql-util :as ut]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.db.base-schema :as sch]
             [xt.db.sample-test :as sample]
             [xt.db.sql-util :as ut]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.db.base-schema :as sch]
             [xt.db.sample-test :as sample]
             [xt.db.sql-util :as ut]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)
             (l/rt:scaffold :python)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.base-schema/get-ident-id :added "4.0"}
(fact "gets the ident id for a schema entry")

^{:refer xt.db.base-schema/list-tables :added "4.0"}
(fact "list tables"
  ^:hidden

  (def +tables+
    (!.js
     (sch/list-tables sample/Schema)))
  
  (!.lua
   (sch/list-tables sample/Schema))
  => +tables+
  
  (!.py
   (sch/list-tables sample/Schema))
  => +tables+)

^{:refer xt.db.base-schema/get-cached-schema :added "4.0"}
(fact "get lookup"
  ^:hidden
  
  (!.js (sch/get-cached-schema sample/Schema))
  => map?

  (!.lua (sch/get-cached-schema sample/Schema))
  => map?

  (!.py (sch/get-cached-schema sample/Schema))
  => map?)

^{:refer xt.db.base-schema/create-data-keys :added "4.0"}
(fact "creates data keys"
  ^:hidden
  
  (!.js
   (sch/create-data-keys sample/Schema "Currency"))
  => ["id" "type" "symbol" "native" "decimal" "name" "plural" "description"]
  
  (!.lua
   (sch/create-data-keys sample/Schema "Currency"))
  => ["id" "type" "symbol" "native" "decimal" "name" "plural" "description"]

  (!.py
   (sch/create-data-keys sample/Schema "Currency"))
  => ["id" "type" "symbol" "native" "decimal" "name" "plural" "description"])

^{:refer xt.db.base-schema/create-ref-keys :added "4.0"}
(fact "creates ref keys"
  ^:hidden
  
  (!.js (sch/create-ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"]

  (!.lua (sch/create-ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"]

  (!.py (sch/create-ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"])

^{:refer xt.db.base-schema/create-rev-keys :added "4.0"}
(fact "creates rev keys"
  ^:hidden

  (set (!.js
        (sch/create-rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"}
  

  (set (!.lua
        (sch/create-rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"}

  (set (!.py
       (sch/create-rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"})

^{:refer xt.db.base-schema/create-table-entries :added "4.0"}
(fact "creates the table keys")

^{:refer xt.db.base-schema/create-defaults :added "4.0"}
(fact "creates defaults from sql inputs")

^{:refer xt.db.base-schema/create-all-keys :added "4.0"
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
                 "module" "xt.db.sample-user-test"},
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
  ^:hidden

  (!.js
   (sch/create-all-keys sample/Schema "Wallet"))
  
  => +all-wallet+
  
  (!.lua
   (sch/create-all-keys sample/Schema "Wallet"))
  => +all-wallet+

  (!.py
   (sch/create-all-keys sample/Schema "Wallet"))
  => +all-wallet+)

^{:refer xt.db.base-schema/get-all-keys :added "4.0"
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
                 "module" "xt.db.sample-user-test"},
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
  ^:hidden
  
  (!.js (sch/get-all-keys sample/Schema "Organisation"))
  
  => +all-org+

  (!.lua (sch/get-all-keys sample/Schema "Organisation"))
  => +all-org+

  (!.py (sch/get-all-keys sample/Schema "Organisation"))
  => +all-org+)

^{:refer xt.db.base-schema/data-keys :added "4.0"}
(fact "gets data keys"
  ^:hidden
  
  (!.js (sch/data-keys sample/Schema "UserAccount"))
  => ["id" "nickname" "password_hash" "password_salt" "password_updated" "is_super" "is_suspended" "is_official"]

  (!.lua (sch/data-keys sample/Schema "UserAccount"))
  => ["id" "nickname" "password_hash" "password_salt" "password_updated" "is_super" "is_suspended" "is_official"]
  
  (!.py (sch/data-keys sample/Schema "UserAccount"))
  => ["id" "nickname" "password_hash" "password_salt" "password_updated" "is_super" "is_suspended" "is_official"])

^{:refer xt.db.base-schema/ref-keys :added "4.0"}
(fact "gets ref keys"
  ^:hidden
  
  (!.js (sch/ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"]
  
  (!.lua (sch/ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"]

  (!.py (sch/ref-keys sample/Schema "UserProfile"))
  => ["account" "state" "country"])

^{:refer xt.db.base-schema/ref-id-keys :added "4.0"}
(fact "gets ref id keys"
  ^:hidden
  
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

^{:refer xt.db.base-schema/rev-keys :added "4.0"}
(fact "gets rev keys"
  ^:hidden
  
  (set (!.js (sch/rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"}

  (set (!.lua (sch/rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"}

  (set (!.py (sch/rev-keys sample/Schema "UserAccount")))
  => #{"organisations" "profile" "privileges" "organisation_accesses" "wallets" "notification"})

^{:refer xt.db.base-schema/table-defaults :added "4.0"}
(fact "gets the table defaults")

^{:refer xt.db.base-schema/table-entries :added "4.0"}
(fact "gets the table entries")

^{:refer xt.db.base-schema/table-columns :added "4.0"
  :setup [(def +out+
            ["id" "account_id" "first_name" "last_name" "city"
             "state_id" "country_id" "about" "language" "detail"])]}
(fact "ges the table columns"
  ^:hidden

  (!.js (sch/table-columns sample/Schema "UserProfile"))
  => +out+
  

  (!.lua (sch/table-columns sample/Schema "UserProfile"))
  => +out+
  
  (!.py (sch/table-columns sample/Schema "UserProfile"))
  => +out+)

^{:refer xt.db.base-schema/create-table-order :added "4.0"
  :setup [(def +ordered+
            ["Currency"
             "RegionCountry"
             "RegionState"
             "RegionCity"
             "UserAccount"
             "UserProfile"
             "UserNotification"
             "UserPrivilege"
             "Asset"
             "Wallet"
             "WalletAsset"
             "Organisation"
             "OrganisationAccess"])]}
(fact "creates the table order"
  ^:hidden
  
  (!.js
   (sch/create-table-order sample/SchemaLookup))
  => +ordered+
 
  (!.lua
   (sch/create-table-order sample/SchemaLookup))
  => +ordered+

  (!.py
   (sch/create-table-order sample/SchemaLookup))
  => +ordered+)

^{:refer xt.db.base-schema/table-order :added "4.0"
  :setup [(def +order+
            ["Currency"
             "RegionCountry"
             "RegionState"
             "RegionCity"
             "UserAccount"
             "UserProfile"
             "UserNotification"
             "UserPrivilege"
             "Asset"
             "Wallet"
             "WalletAsset"
             "Organisation"
             "OrganisationAccess"])]}
(fact "table order with caching"
  ^:hidden
  
  (!.js
   (sch/table-order  sample/SchemaLookup))
  => +order+

  (!.lua
   (sch/table-order  sample/SchemaLookup))
  => +order+

  (!.py
   (sch/table-order  sample/SchemaLookup))
  => +order+)

^{:refer xt.db.base-schema/table-coerce :added "4.0"}
(fact "coerces output given schema and type functions"
  ^:hidden
  
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
  (./import)
  (./create-tests))
