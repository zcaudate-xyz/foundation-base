(ns xt.db.base-flatten-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.db.base-flatten :as f]
             [xt.db.base-schema :as sch]
             [xt.db.sample-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.db.base-flatten :as f]
             [xt.db.base-schema :as sch]
             [xt.db.sample-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.db.base-flatten :as f]
             [xt.db.base-schema :as sch]
             [xt.db.sample-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.base-flatten/flatten-get-links :added "4.0"}
(fact "flatten links"
  ^:hidden
  
  (!.js
    (f/flatten-get-links {:currencies [{:id "hello"}]}))
  => {"currencies" {"hello" true}}

  (!.lua
   (f/flatten-get-links {:currencies [{:id "hello"}]}))
  => {"currencies" {"hello" true}}

  (!.py
   (f/flatten-get-links {:currencies [{:id "hello"}]}))
  => {"currencies" {"hello" true}})

^{:refer xt.db.base-flatten/flatten-merge :added "4.0"}
(fact "flatten data"
  ^:hidden
  
  (!.js
   (f/flatten-merge {}
                    {:id "hello"}
                    {:profile {"profile-id" true}}
                    {}))
  => {"hello" {"ref_links" {"profile" {"profile-id" true}},
               "id" "hello",
               "rev_links" {},
               "data" {"id" "hello"}}}

  (!.lua
   (f/flatten-merge {}
                    {:id "hello"}
                    {:profile {"profile-id" true}}
                    {}))
  => {"hello" {"ref_links" {"profile" {"profile-id" true}},
               "id" "hello",
               "rev_links" {},
               "data" {"id" "hello"}}}

  (!.py
   (f/flatten-merge {}
                    {:id "hello"}
                    {:profile {"profile-id" true}}
                    {}))
  => {"hello" {"ref_links" {"profile" {"profile-id" true}},
               "id" "hello",
               "rev_links" {},
               "data" {"id" "hello"}}})

^{:refer xt.db.base-flatten/flatten-node :added "4.0"}
(fact "flatten node"
  ^:hidden

  (set (!.js
           (var out (f/flatten-node sample/Schema
                                    "UserAccount"
                                    sample/RootUserFull
                                    {}
                                    {}))
           (k/obj-keys out)))
  => #{"table_map" "data_obj" "ref_obj" "rev_obj"}

  (set (!.lua
       (var out (f/flatten-node sample/Schema
                                "UserAccount"
                                sample/RootUserFull
                                {}
                                {}))
       (k/obj-keys out)))
  => #{"table_map" "data_obj" "ref_obj" "rev_obj"}

  (set (!.py
        (var out (f/flatten-node sample/Schema
                                 "UserAccount"
                                 sample/RootUserFull
                                 {}
                                 {}))
        (k/obj-keys out)))
  => #{"table_map" "data_obj" "ref_obj" "rev_obj"})

^{:refer xt.db.base-flatten/flatten-node.account :adopt true :added "4.0"
  :setup [(def +table-map-account+
            (contains-in {"00000000-0000-0000-0000-000000000000"
                          {"ref_links" {},
                           "id" "00000000-0000-0000-0000-000000000000",
                           "rev_links"
                           {"organisations" {"ec088f52-310b-491b-a034-d4efc222fd00" true},
                            "profile" {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true},
                            "wallets" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                            "notification" {"d0adc63a-0bfa-41fe-b054-f4fb0cb354bd" true}},
                           "data"
                           {"is_official" false,
                            "nickname" "root",
                            "id" "00000000-0000-0000-0000-000000000000",
                            "is_suspended" false,
                            "password_updated" number?
                            "is_super" true}}}))]}
(fact "flatten node example for user account"
  ^:hidden

  (!.js
   (-> (f/flatten-node sample/Schema
                       "UserAccount"
                       sample/RootUserFull
                       {}
                       {})
       (k/get-key "table_map")))
  => +table-map-account+

  (!.lua
   (-> (f/flatten-node sample/Schema
                       "UserAccount"
                       sample/RootUserFull
                       {}
                       {})
       (k/get-key "table_map")))
  
  
  => +table-map-account+

  (!.py
   (-> (f/flatten-node sample/Schema
                       "UserAccount"
                       sample/RootUserFull
                       {}
                       {})
       (k/get-key "table_map")))
  => +table-map-account+)

^{:refer xt.db.base-flatten/flatten-node.profile :adopt true :added "4.0"
  :setup [(def +table-map-profile+
            {"table_map"
             {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
              {"ref_links" {"account" {"hello" true}},
               "rev_links" {},
               "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
               "data" {"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                       "last_name" "User",
                       "first_name" "Root",
                       "language" "en"}}},
             "data_obj" {"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                         "last_name" "User",
                         "first_name" "Root",
                         "language" "en"},
             "ref_obj" {"account" ["hello"]},
             "rev_obj" {}})]}
(fact "flatten node example for user account"
  ^:hidden

  (!.js
   (f/flatten-node sample/Schema
                       "UserProfile"
                       {"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                        "account" ["hello"]
                          "last_name" "User",
                        "first_name" "Root",
                        "language" "en"}
                       {}
                       {}))
  => +table-map-profile+

  (!.lua
   (f/flatten-node sample/Schema
                       "UserProfile"
                       {"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                        "account" ["hello"]
                          "last_name" "User",
                        "first_name" "Root",
                        "language" "en"}
                       {}
                       {}))
  => +table-map-profile+

  (!.py
   (f/flatten-node sample/Schema
                       "UserProfile"
                       {"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                        "account" ["hello"]
                          "last_name" "User",
                        "first_name" "Root",
                        "language" "en"}
                       {}
                       {}))
  => +table-map-profile+)

^{:refer xt.db.base-flatten/flatten-linked :added "4.0"
  :setup [(def +table-linked+
            {"UserProfile"
             {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
              {"ref_links" {"account" {"hello" true}},
               "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
               "rev_links" {},
               "data"
               {"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                "last_name" "User",
                "first_name" "Root",
                "language" "en"}}},
             "Organisation"
             {"ec088f52-310b-491b-a034-d4efc222fd00"
              {"ref_links" {"owner" {"hello" true}},
               "id" "ec088f52-310b-491b-a034-d4efc222fd00",
               "rev_links" {},
               "data"
               {"id" "ec088f52-310b-491b-a034-d4efc222fd00",
                "name" "root"}}}})]}
(fact "flatten linked for schema"
  ^:hidden

  (!.js
   (f/flatten-linked sample/Schema
                     "UserAccount"
                     {"profile" [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                                  "last_name" "User", "first_name" "Root", "picture" {"url" "static/user.jpg",
                                                                                      "type" "base"},
                                  "language" "en"}]
                      "organisations" [{"id" "ec088f52-310b-491b-a034-d4efc222fd00",
                                        "name" "root"}],}
                     "hello"
                     {}
                     f/flatten-obj))
  => +table-linked+

  (!.lua
   (f/flatten-linked sample/Schema
                     "UserAccount"
                     {"profile" [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                                  "last_name" "User", "first_name" "Root", "picture" {"url" "static/user.jpg",
                                                                                      "type" "base"},
                                  "language" "en"}]
                      "organisations" [{"id" "ec088f52-310b-491b-a034-d4efc222fd00",
                                        "name" "root"}],}
                     "hello"
                     {}
                     f/flatten-obj))
  => +table-linked+

  (!.py
   (f/flatten-linked sample/Schema
                     "UserAccount"
                     {"profile" [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                                  "last_name" "User", "first_name" "Root", "picture" {"url" "static/user.jpg",
                                                                                      "type" "base"},
                                  "language" "en"}]
                      "organisations" [{"id" "ec088f52-310b-491b-a034-d4efc222fd00",
                                        "name" "root"}],}
                     "hello"
                     {}
                     f/flatten-obj))
  => +table-linked+)

^{:refer xt.db.base-flatten/flatten-obj :added "4.0"
  :setup [(def +user-full+
            (contains-in
             {"UserProfile"
              {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
               {"ref_links"
                {"account" {"00000000-0000-0000-0000-000000000000" true}},
                "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                "rev_links" {},
                "data"
                {"city" nil,
                 "about" nil,
                 "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                 "last_name" "User",
                 "first_name" "Root",
                 "language" "en"}}},
              "Asset"
              {"9e576e3e-c73e-4d18-92b4-f975c1bed3d4"
               {"ref_links" {"currency" {"USD" true}},
                "id" "9e576e3e-c73e-4d18-92b4-f975c1bed3d4",
                "rev_links"
                {"linked_wallet" {"38889fdc-de34-4161-bb37-f8844d67ee5a" true}},
                "data" {"id" "9e576e3e-c73e-4d18-92b4-f975c1bed3d4"}},
               "9261d072-b7f5-41df-935a-c36fe13acf14"
               {"ref_links" {"currency" {"XLM.T" true}},
                "id" "9261d072-b7f5-41df-935a-c36fe13acf14",
                "rev_links"
                {"linked_wallet" {"2b3d4318-8cea-4420-a31c-f110d8198654" true}},
                "data" {"id" "9261d072-b7f5-41df-935a-c36fe13acf14"}},
               "63acfd25-4b1b-4de4-aa82-909019c95591"
               {"ref_links" {"currency" {"STATS" true}},
                "id" "63acfd25-4b1b-4de4-aa82-909019c95591",
                "rev_links"
                {"linked_wallet" {"6eb2fa48-c753-41c6-abda-c680828da1d2" true}},
                "data" {"id" "63acfd25-4b1b-4de4-aa82-909019c95591"}},
               "222de282-ca29-4d04-81dd-86ec3f9189cf"
               {"ref_links" {"currency" {"XLM" true}},
                "id" "222de282-ca29-4d04-81dd-86ec3f9189cf",
                "rev_links"
                {"linked_wallet" {"4b146b40-947a-42a5-b116-2ad8816c4078" true}},
                "data" {"id" "222de282-ca29-4d04-81dd-86ec3f9189cf"}}},
              "Organisation"
              {"ec088f52-310b-491b-a034-d4efc222fd00"
               {"ref_links"
                {"owner" {"00000000-0000-0000-0000-000000000000" true}},
                "id" "ec088f52-310b-491b-a034-d4efc222fd00",
                "rev_links" {},
                "data"
                {"id" "ec088f52-310b-491b-a034-d4efc222fd00",
                 "name" "root",
                 "title" "",
                 "description" nil}}},
              "UserNotification"
              {"d0adc63a-0bfa-41fe-b054-f4fb0cb354bd"
               {"ref_links"
                {"account" {"00000000-0000-0000-0000-000000000000" true}},
                "id" "d0adc63a-0bfa-41fe-b054-f4fb0cb354bd",
                "rev_links" {},
                "data"
                {"id" "d0adc63a-0bfa-41fe-b054-f4fb0cb354bd",
                 "trading" {},
                 "general" {},
                 "funding" {}}}},
              "UserAccount"
              {"00000000-0000-0000-0000-000000000000"
               {"ref_links" {},
                "id" "00000000-0000-0000-0000-000000000000",
                "rev_links"
                {"organisations" {"ec088f52-310b-491b-a034-d4efc222fd00" true},
                 "profile" {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true},
                 "wallets" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                 "notification" {"d0adc63a-0bfa-41fe-b054-f4fb0cb354bd" true}},
                "data"
                {"is_official" false,
                 "nickname" "root",
                 "id" "00000000-0000-0000-0000-000000000000",
                 "is_suspended" false,
                 "password_updated" number?
                 "is_super" true}}},
              "WalletAsset"
              {"6eb2fa48-c753-41c6-abda-c680828da1d2"
               {"ref_links"
                {"wallet" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                 "asset" {"63acfd25-4b1b-4de4-aa82-909019c95591" true}},
                "id" "6eb2fa48-c753-41c6-abda-c680828da1d2",
                "rev_links" {},
                "data" {"id" "6eb2fa48-c753-41c6-abda-c680828da1d2"}},
               "38889fdc-de34-4161-bb37-f8844d67ee5a"
               {"ref_links"
                {"wallet" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                 "asset" {"9e576e3e-c73e-4d18-92b4-f975c1bed3d4" true}},
                "id" "38889fdc-de34-4161-bb37-f8844d67ee5a",
                "rev_links" {},
                "data" {"id" "38889fdc-de34-4161-bb37-f8844d67ee5a"}},
               "2b3d4318-8cea-4420-a31c-f110d8198654"
               {"ref_links"
                {"wallet" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                 "asset" {"9261d072-b7f5-41df-935a-c36fe13acf14" true}},
                "id" "2b3d4318-8cea-4420-a31c-f110d8198654",
                "rev_links" {},
                "data" {"id" "2b3d4318-8cea-4420-a31c-f110d8198654"}},
               "4b146b40-947a-42a5-b116-2ad8816c4078"
               {"ref_links"
                {"wallet" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                 "asset" {"222de282-ca29-4d04-81dd-86ec3f9189cf" true}},
                "id" "4b146b40-947a-42a5-b116-2ad8816c4078",
                "rev_links" {},
                "data" {"id" "4b146b40-947a-42a5-b116-2ad8816c4078"}}},
              "Wallet"
              {"531f3edb-b9d4-4c8e-8419-22edfe715b15"
               {"ref_links"
                {"owner" {"00000000-0000-0000-0000-000000000000" true}},
                "id" "531f3edb-b9d4-4c8e-8419-22edfe715b15",
                "rev_links"
                {"entries"
                 {"6eb2fa48-c753-41c6-abda-c680828da1d2" true,
                  "38889fdc-de34-4161-bb37-f8844d67ee5a" true,
                  "2b3d4318-8cea-4420-a31c-f110d8198654" true,
                  "4b146b40-947a-42a5-b116-2ad8816c4078" true}},
                "data" {"id" "531f3edb-b9d4-4c8e-8419-22edfe715b15"}}},
              "Currency"
              {"XLM.T"
               {"ref_links" {},
                "id" "XLM.T",
                "rev_links"
                {"assets" {"9261d072-b7f5-41df-935a-c36fe13acf14" true}},
                "data" {"id" "XLM.T"}},
               "XLM"
               {"ref_links" {},
                "id" "XLM",
                "rev_links"
                {"assets" {"222de282-ca29-4d04-81dd-86ec3f9189cf" true}},
                "data" {"id" "XLM"}},
               "STATS"
               {"ref_links" {},
                "id" "STATS",
                "rev_links"
                {"assets" {"63acfd25-4b1b-4de4-aa82-909019c95591" true}},
                "data" {"id" "STATS"}},
               "USD"
               {"ref_links" {},
                "id" "USD",
                "rev_links"
                {"assets" {"9e576e3e-c73e-4d18-92b4-f975c1bed3d4" true}},
                "data" {"id" "USD"}}}}))]}
(fact "flatten data for schema"
  ^:hidden
  
  (!.js
   (f/flatten-obj sample/Schema
                  "UserAccount"
                  sample/RootUserFull
                  {}
                  {}))
  => +user-full+

  (!.lua
   (f/flatten-obj sample/Schema
                  "UserAccount"
                  sample/RootUserFull
                  {}
                  {}))
  => +user-full+

  (!.py
   (f/flatten-obj sample/Schema
                  "UserAccount"
                  sample/RootUserFull
                  {}
                  {}))
  => +user-full+)

^{:refer xt.db.base-flatten/flatten :added "4.0"}
(fact "flattens data schema"
  ^:hidden
  
  (!.js
   (f/flatten sample/Schema
              "UserAccount"
              sample/RootUserFull
              nil))
  => +user-full+

  (!.lua
   (f/flatten sample/Schema
              "UserAccount"
              sample/RootUserFull
              nil))
  => +user-full+

  (!.py
   (f/flatten sample/Schema
              "UserAccount"
              sample/RootUserFull
              nil))
  => +user-full+)

^{:refer xt.db.base-flatten/flatten-bulk :added "4.0"}
(fact "flattens bulk data"
  ^:hidden

  (!.js
   (f/flatten-bulk
    sample/Schema
    {"UserAccount" [sample/RootUserFull]}))
  => +user-full+

  (!.lua
   (f/flatten-bulk
    sample/Schema
    {"UserAccount" [sample/RootUserFull]}))
  => +user-full+

  (!.py
   (f/flatten-bulk
    sample/Schema
    {"UserAccount" [sample/RootUserFull]}))
  => +user-full+)

(comment
  (./import)
  (./create-tests)
  )


 
