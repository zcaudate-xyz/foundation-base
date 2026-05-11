(ns xtbench.python.db.runtime.cache-util-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.db.runtime.cache-util :as data]
             [xt.db.text.base-flatten :as f]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.cache-util/has-entry :added "4.0"}
(fact "checks if entry exists"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {}) nil)
    (data/has-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => true)

^{:refer xt.db.runtime.cache-util/get-entry :added "4.0"
  :setup [(def +account-get-entry-check+
            (just-in
             {"record"
              {"ref_links" {},
               "id" "00000000-0000-0000-0000-000000000000",
               "rev_links"
               {"profile" {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
               "data"
               {"is_official" false,
                "nickname" "root",
                "id" "00000000-0000-0000-0000-000000000000",
                "is_suspended" false,
                "password_updated" number?
                "is_super" true}},
              "t" number?}))]}
(fact "gets entry by id"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (data/get-entry rows "UserAccount" "00000000-0000-0000-0000-000000000000"))
  => +account-get-entry-check+)

^{:refer xt.db.runtime.cache-util/swap-if-entry :added "4.0"}
(fact "modifies entry if exists"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (-> (data/swap-if-entry rows
                            "UserAccount" "00000000-0000-0000-0000-000000000000"
                            (fn [record]
                              (return (xtd/set-in record ["data" "foo"] "hello"))))
        (xtd/get-in ["record" "data" "foo"])))
  => "hello")

^{:refer xt.db.runtime.cache-util/merge-single :added "4.0"}
(fact "merges a single entry"

  (!.py
    (data/merge-single {}
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000001"
                       {:id "00000000-0000-0000-0000-000000000001"
                        :data {}
                        :ref-links {}
                        :rev-links {}}
                       k/identity))
  => (just {"record" {"ref_links" {}, "id" "00000000-0000-0000-0000-000000000001", "rev_links" {}, "data" {}},
            "t" number?}))

^{:refer xt.db.runtime.cache-util/merge-bulk :added "4.0"
  :setup [(def +account-merge-bulk-check+
            (just-in
             [{"UserProfile"
               {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
                {"record"
                 {"ref_links"
                  {"account"
                   {"00000000-0000-0000-0000-000000000000" true}},
                  "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                  "rev_links" {},
                  "data"
                  {"detail" {"hello" "world"},
                   "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                   "last_name" "User",
                   "first_name" "Root",
                   "language" "en"}},
                 "t" number?}},
               "UserAccount"
               {"00000000-0000-0000-0000-000000000000"
                {"record"
                 {"ref_links" {},
                  "id" "00000000-0000-0000-0000-000000000000",
                  "rev_links"
                  {"profile"
                   {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
                  "data"
                  {"is_official" false,
                   "nickname" "root",
                   "id" "00000000-0000-0000-0000-000000000000",
                   "is_suspended" false,
                   "password_updated" number?
                   "is_super" true}},
                 "t" number?}}}
              ["00000000-0000-0000-0000-000000000000"]]))]}
(fact "merges flattened data into the database"

  (!.py
    (var rows {})
    [(data/merge-bulk rows (f/flatten sample/Schema
                                      "UserAccount"
                                      sample/RootUser
                                      {})
                      nil)
     (data/get-ids rows "UserAccount")])
  => +account-merge-bulk-check+)

^{:refer xt.db.runtime.cache-util/merge-bulk.1 :added "4.0"
  :setup [(def +account-min-check+
            (just-in
             {"UserAccount"
              {"00000000-0000-0000-0000-000000000000"
               {"record"
                {"ref_links" {},
                 "id" "00000000-0000-0000-0000-000000000000",
                 "rev_links" {},
                 "data"
                 {"nickname" "root",
                  "id" "00000000-0000-0000-0000-000000000000"}},
                "t" number?}}}))
                   (def +account-profile-check+
                     (just-in
                      {"UserProfile"
                       {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
                        {"record"
                         {"ref_links"
                          {"account" {"00000000-0000-0000-0000-000000000000" true}},
                          "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                          "rev_links" {},
                          "data"
                          {"detail" {"hello" "world"},
                           "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                           "last_name" "User",
                           "first_name" "Root",
                           "language" "en"}},
                         "t" number?}},
                       "UserAccount"
                       {"00000000-0000-0000-0000-000000000000"
                        {"record"
                         {"ref_links" {},
                          "id" "00000000-0000-0000-0000-000000000000",
                          "rev_links"
                          {"profile" {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
                          "data"
                          {"nickname" "root",
                           "id" "00000000-0000-0000-0000-000000000000"}},
                         "t" number?}}}))
                   (def +account-org-notification-check+
                     (just-in
                      {"Organisation"
                       {"ec088f52-310b-491b-a034-d4efc222fd00"
                        {"record"
                         {"ref_links"
                          {"owner"
                           {"00000000-0000-0000-0000-000000000000" true}},
                          "id" "ec088f52-310b-491b-a034-d4efc222fd00",
                          "rev_links" {},
                          "data"
                          {"id" "ec088f52-310b-491b-a034-d4efc222fd00",
                           "name" "root",
                           "title" ""}},
                         "t" number?}},
                       "UserNotification"
                       {"d0adc63a-0bfa-41fe-b054-f4fb0cb354bd"
                        {"record"
                         {"ref_links"
                          {"account"
                           {"00000000-0000-0000-0000-000000000000" true}},
                          "id" "d0adc63a-0bfa-41fe-b054-f4fb0cb354bd",
                          "rev_links" {},
                          "data"
                          {"id" "d0adc63a-0bfa-41fe-b054-f4fb0cb354bd",
                           "trading" {},
                           "general" {},
                           "funding" {}}},
                         "t" number?}},
                       "UserAccount"
                       {"00000000-0000-0000-0000-000000000000"
                        {"record"
                         {"ref_links" {},
                          "id" "00000000-0000-0000-0000-000000000000",
                          "rev_links"
                          {"organisations"
                           {"ec088f52-310b-491b-a034-d4efc222fd00" true},
                           "notification"
                           {"d0adc63a-0bfa-41fe-b054-f4fb0cb354bd" true}},
                          "data"
                          {"nickname" "root",
                           "id" "00000000-0000-0000-0000-000000000000"}},
                         "t" number?}}}))]}
(fact "merges the full cache fixture step by step in python"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-pick sample/RootUserFull
                                                   ["id" "nickname" "is_active"])
                                     {}) nil))
  => +account-min-check+

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-pick sample/RootUserFull
                                                   ["id" "nickname" "is_active" "profile"])
                                     {}) nil))
  => +account-profile-check+

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-pick sample/RootUserFull
                                                   ["id" "nickname" "is_active" "organisations" "notification"])
                                     {}) nil))
  => +account-org-notification-check+)

^{:refer xt.db.runtime.cache-util/merge-bulk.2 :added "4.0"
  :setup [(def +account-merge-bulk-check+
            (just-in {"UserProfile"
                      {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
                       {"record"
                        {"ref_links"
                         {"account" {"00000000-0000-0000-0000-000000000000" true}},
                         "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                         "rev_links" {},
                         "data"
                         {"detail" {"hello" "world"},
                          "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                          "last_name" "User",
                          "first_name" "Root",
                          "language" "en"}},
                        "t" number?}},
                      "Asset"
                      {"9e576e3e-c73e-4d18-92b4-f975c1bed3d4"
                       {"record"
                        {"ref_links" {"currency" {"USD" true}},
                         "id" "9e576e3e-c73e-4d18-92b4-f975c1bed3d4",
                         "rev_links"
                         {"linked_wallet"
                          {"38889fdc-de34-4161-bb37-f8844d67ee5a" true}},
                         "data" {"id" "9e576e3e-c73e-4d18-92b4-f975c1bed3d4"}},
                        "t" number?},
                       "9261d072-b7f5-41df-935a-c36fe13acf14"
                       {"record"
                        {"ref_links" {"currency" {"XLM.T" true}},
                         "id" "9261d072-b7f5-41df-935a-c36fe13acf14",
                         "rev_links"
                         {"linked_wallet"
                          {"2b3d4318-8cea-4420-a31c-f110d8198654" true}},
                         "data" {"id" "9261d072-b7f5-41df-935a-c36fe13acf14"}},
                        "t" number?},
                       "63acfd25-4b1b-4de4-aa82-909019c95591"
                       {"record"
                        {"ref_links" {"currency" {"STATS" true}},
                         "id" "63acfd25-4b1b-4de4-aa82-909019c95591",
                         "rev_links"
                         {"linked_wallet"
                          {"6eb2fa48-c753-41c6-abda-c680828da1d2" true}},
                         "data" {"id" "63acfd25-4b1b-4de4-aa82-909019c95591"}},
                        "t" number?},
                       "222de282-ca29-4d04-81dd-86ec3f9189cf"
                       {"record"
                        {"ref_links" {"currency" {"XLM" true}},
                         "id" "222de282-ca29-4d04-81dd-86ec3f9189cf",
                         "rev_links"
                         {"linked_wallet"
                          {"4b146b40-947a-42a5-b116-2ad8816c4078" true}},
                         "data" {"id" "222de282-ca29-4d04-81dd-86ec3f9189cf"}},
                        "t" number?}},
                      "Organisation"
                      {"ec088f52-310b-491b-a034-d4efc222fd00"
                       {"record"
                        {"ref_links"
                         {"owner" {"00000000-0000-0000-0000-000000000000" true}},
                         "id" "ec088f52-310b-491b-a034-d4efc222fd00",
                         "rev_links" {},
                         "data"
                         {"id" "ec088f52-310b-491b-a034-d4efc222fd00",
                          "name" "root",
                          "title" ""}},
                        "t" number?}},
                      "UserNotification"
                      {"d0adc63a-0bfa-41fe-b054-f4fb0cb354bd"
                       {"record"
                        {"ref_links"
                         {"account" {"00000000-0000-0000-0000-000000000000" true}},
                         "id" "d0adc63a-0bfa-41fe-b054-f4fb0cb354bd",
                         "rev_links" {},
                         "data"
                         {"id" "d0adc63a-0bfa-41fe-b054-f4fb0cb354bd",
                          "trading" {},
                          "general" {},
                          "funding" {}}},
                        "t" number?}},
                      "UserAccount"
                      {"00000000-0000-0000-0000-000000000000"
                       {"record"
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
                          "is_super" true}},
                        "t" number?}},
                      "WalletAsset"
                      {"6eb2fa48-c753-41c6-abda-c680828da1d2"
                       {"record"
                        {"ref_links"
                         {"wallet" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                          "asset" {"63acfd25-4b1b-4de4-aa82-909019c95591" true}},
                         "id" "6eb2fa48-c753-41c6-abda-c680828da1d2",
                         "rev_links" {},
                         "data" {"id" "6eb2fa48-c753-41c6-abda-c680828da1d2"}},
                        "t" number?},
                       "38889fdc-de34-4161-bb37-f8844d67ee5a"
                       {"record"
                        {"ref_links"
                         {"wallet" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                          "asset" {"9e576e3e-c73e-4d18-92b4-f975c1bed3d4" true}},
                         "id" "38889fdc-de34-4161-bb37-f8844d67ee5a",
                         "rev_links" {},
                         "data" {"id" "38889fdc-de34-4161-bb37-f8844d67ee5a"}},
                        "t" number?},
                       "2b3d4318-8cea-4420-a31c-f110d8198654"
                       {"record"
                        {"ref_links"
                         {"wallet" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                          "asset" {"9261d072-b7f5-41df-935a-c36fe13acf14" true}},
                         "id" "2b3d4318-8cea-4420-a31c-f110d8198654",
                         "rev_links" {},
                         "data" {"id" "2b3d4318-8cea-4420-a31c-f110d8198654"}},
                        "t" number?},
                       "4b146b40-947a-42a5-b116-2ad8816c4078"
                       {"record"
                        {"ref_links"
                         {"wallet" {"531f3edb-b9d4-4c8e-8419-22edfe715b15" true},
                          "asset" {"222de282-ca29-4d04-81dd-86ec3f9189cf" true}},
                         "id" "4b146b40-947a-42a5-b116-2ad8816c4078",
                         "rev_links" {},
                         "data" {"id" "4b146b40-947a-42a5-b116-2ad8816c4078"}},
                        "t" number?}},
                      "Wallet"
                      {"531f3edb-b9d4-4c8e-8419-22edfe715b15"
                       {"record"
                        {"ref_links"
                         {"owner" {"00000000-0000-0000-0000-000000000000" true}},
                         "id" "531f3edb-b9d4-4c8e-8419-22edfe715b15",
                         "rev_links"
                         {"entries"
                          {"6eb2fa48-c753-41c6-abda-c680828da1d2" true,
                           "38889fdc-de34-4161-bb37-f8844d67ee5a" true,
                           "2b3d4318-8cea-4420-a31c-f110d8198654" true,
                           "4b146b40-947a-42a5-b116-2ad8816c4078" true}},
                         "data" {"id" "531f3edb-b9d4-4c8e-8419-22edfe715b15"}},
                        "t" number?}},
                      "Currency"
                      {"XLM.T"
                       {"record"
                        {"ref_links" {},
                         "id" "XLM.T",
                         "rev_links"
                         {"assets" {"9261d072-b7f5-41df-935a-c36fe13acf14" true}},
                         "data" {"id" "XLM.T"}},
                        "t" number?},
                       "XLM"
                       {"record"
                        {"ref_links" {},
                         "id" "XLM",
                         "rev_links"
                         {"assets" {"222de282-ca29-4d04-81dd-86ec3f9189cf" true}},
                         "data" {"id" "XLM"}},
                        "t" number?},
                       "STATS"
                       {"record"
                        {"ref_links" {},
                         "id" "STATS",
                         "rev_links"
                         {"assets" {"63acfd25-4b1b-4de4-aa82-909019c95591" true}},
                         "data" {"id" "STATS"}},
                        "t" number?},
                       "USD"
                       {"record"
                        {"ref_links" {},
                         "id" "USD",
                         "rev_links"
                         {"assets" {"9e576e3e-c73e-4d18-92b4-f975c1bed3d4" true}},
                         "data" {"id" "USD"}},
                        "t" number?}}}))]}
(fact "merges the combined full cache fixture in python"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUserFull
                                     {}) nil))
  => +account-merge-bulk-check+)

^{:refer xt.db.runtime.cache-util/get-ids :added "4.0"}
(fact "get ids for table-key"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (data/get-ids rows "UserAccount"))
  => ["00000000-0000-0000-0000-000000000000"])

^{:refer xt.db.runtime.cache-util/all-records :added "4.0"
  :setup [(def +account-all-records-check+
            (just-in 
             {"00000000-0000-0000-0000-000000000000"
              {"ref_links" {},
               "id" "00000000-0000-0000-0000-000000000000",
               "rev_links"
               {"profile"
                {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
               "data"
               {"is_official" false,
                "nickname" "root",
                "id" "00000000-0000-0000-0000-000000000000",
                "is_suspended" false,
                "password_updated" number?
                "is_super" true}}}))]}
(fact "returns all records"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {}) nil)
    (data/all-records rows "UserAccount"))
  => +account-all-records-check+)

^{:refer xt.db.runtime.cache-util/get-changed-single :added "4.0"}
(fact "gets changed record"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (var changed (-> (data/get-entry rows  "UserAccount" "00000000-0000-0000-0000-000000000000")
                     (. ["record"])
                     (xtd/clone-nested)
                     (xtd/set-in ["data" "nickname"] "hello")))
            
    (data/get-changed-single rows
                             "UserAccount" "00000000-0000-0000-0000-000000000000"
                             changed))
  => {"data" {"nickname" "hello"}})

^{:refer xt.db.runtime.cache-util/has-changed-single :added "4.0"}
(fact "checks if record has changed"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (var changed (-> (data/get-entry rows  "UserAccount" "00000000-0000-0000-0000-000000000000")
                     (. ["record"])
                     (xtd/clone-nested)
                     (xtd/set-in ["data" "nickname"] "hello")))
    (data/has-changed-single rows "UserAccount" "00000000-0000-0000-0000-000000000000"
                             changed))
  => true)

^{:refer xt.db.runtime.cache-util/get-link-attrs :added "4.0"
  :setup [(def +get-link-attrs-check+
            {"table_link" "rev_links",
             "inverse_link" "ref_links",
             "table_key" "UserAccount",
             "table_field" "profile",
             "inverse_key" "UserProfile",
             "inverse_field" "account"})]}
(fact "find link attributes"

  (!.py (data/get-link-attrs sample/Schema "UserAccount" "profile"))
  => +get-link-attrs-check+)

^{:refer xt.db.runtime.cache-util/remove-single-link-entry :added "4.0"}
(fact "removes single link for entry"

  (!.py
    (var rows {})
    (var removed {"id" nil})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    [(-> (data/remove-single-link-entry rows
                                        "UserAccount"
                                        "00000000-0000-0000-0000-000000000000"
                                         "rev_links"
                                         "profile"
                                         "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                                         (fn [link-id]
                                           (xtd/set-in removed ["id"] link-id)))
          (xtd/get-in ["record" "rev_links"]))
     (. removed ["id"])])
  => [{} "c4643895-b0ce-44cc-b07b-2386bf18d43b"])

^{:refer xt.db.runtime.cache-util/remove-single-link :added "4.0"}
(fact "removes single link"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (data/remove-single-link rows
                             sample/Schema
                             "UserAccount"
                             "00000000-0000-0000-0000-000000000000"
                             "profile"
                             "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true])

^{:refer xt.db.runtime.cache-util/remove-single :added "4.0"
  :setup [(def +account-remove-single-check+
            (contains-in
             [{"record"
               {"ref_links" {},
                "id" "00000000-0000-0000-0000-000000000000",
                "rev_links"
                {"profile" {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
                "data"
                {"is_official" false,
                 "nickname" "root",
                 "id" "00000000-0000-0000-0000-000000000000",
                 "is_suspended" false,
                 "password_updated" number?
                 "is_super" true}},
               "t" number?}]))]}
(fact "removes a single entry"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (data/remove-single rows
                        sample/Schema
                        "UserAccount"
                        "00000000-0000-0000-0000-000000000000"))
  => +account-remove-single-check+)

^{:refer xt.db.runtime.cache-util/remove-bulk :added "4.0"}
(fact "removes bulk data"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {})
                     nil)
    (var removed (data/remove-bulk rows
                                   sample/Schema
                                   "UserAccount"
                                   ["00000000-0000-0000-0000-000000000000"]))
    [(xtd/get-in (xtd/first removed) ["record" "id"])
     (data/get-ids rows "UserAccount")])
  => (just ["00000000-0000-0000-0000-000000000000"
            empty?]))

^{:refer xt.db.runtime.cache-util/add-single-link-entry :added "4.0"}
(fact "adds single link entry for one side"

  (!.py
    (var rows {})
    (var added {"id" nil})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-omit sample/RootUser ["profile"])
                                     {})
                     nil)
    [(-> (data/add-single-link-entry rows
                                     "UserAccount"
                                     "00000000-0000-0000-0000-000000000000"
                                      "rev_links"
                                      "profile"
                                      "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                                      (fn [link-id]
                                        (xtd/set-in added ["id"] link-id))
                                      "UserProfile"
                                      "account")
          (xtd/get-in ["record" "rev_links" "profile"]))
     (. added ["id"])])
  => [{"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}
      "c4643895-b0ce-44cc-b07b-2386bf18d43b"])

^{:refer xt.db.runtime.cache-util/add-single-link :added "4.0"}
(fact "adds single link"

  (!.py
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-omit sample/RootUser ["emails" "profile"])
                                     {})
                     nil)
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     sample/RootUser
                                     {}) nil)
    (data/add-single-link rows
                          sample/Schema
                          "UserAccount"
                          "00000000-0000-0000-0000-000000000000"
                          "profile"
                          "c4643895-b0ce-44cc-b07b-2386bf18d43b"))
  => [true true])

^{:refer xt.db.runtime.cache-util/add-bulk-links :added "4.0"
  :setup [(def +account-add-bulk-links-check+
            (just
             [(just [{"table" "UserAccount",
                      "id" "00000000-0000-0000-0000-000000000000",
                     "field" "profile",
                      "link_id" "c4643895-b0ce-44cc-b07b-2386bf18d43b"}
                     {"table" "UserProfile",
                      "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                      "field" "account",
                      "link_id" "00000000-0000-0000-0000-000000000000"}]
                    :in-any-order)
              {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}]))]}
(fact "adding bulk links from external data (to be doubly sure)"

  (!.py
    (var flat (f/flatten sample/Schema
                         "UserAccount"
                         sample/RootUser
                         {}))
    (var rows {})
    (data/merge-bulk rows (f/flatten sample/Schema
                                     "UserAccount"
                                     (xtd/obj-omit sample/RootUser ["emails" "profile"])
                                     {})
                     nil)
    (data/merge-bulk rows (xtd/obj-omit flat ["UserAccount"]) nil)
    [(data/add-bulk-links rows sample/Schema flat)
     (xtd/get-in rows ["UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "rev_links"
                       "profile"])])
  => +account-add-bulk-links-check+)

(comment
  (./create-tests)
  (./import)
  
  (s/pedantic '[xt.db.runtime.cache-util])
  (s/run '[xt.db.runtime.cache-util])

  (s/seedgen-benchadd '[xt.db] {:lang [:dart] :write true})
  (s/seedgen-benchadd 'xt.db.runtime.cache-util {:lang [:dart] :write true})
  (s/seedgen-langadd 'xt.db.runtime.cache-util {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.runtime.cache-util {:lang [:lua :python] :write true}))

(comment

  (def +full-org-notify+
    (!.js
      (f/flatten sample/Schema
                 "UserAccount"
                 (xtd/obj-omit sample/RootUserFull
                               ["wallets" "portfolios" "identities"])
                 {})))
  (def +full-wallets+
    (!.js
      (f/flatten sample/Schema
                 "UserAccount"
                 (xtd/obj-omit sample/RootUserFull
                               ["notification" "organisations" "emails"
                                "portfolios" "identities" "profile"])
                 {})))
  (def +full-no-wallets+
    (!.js
      (f/flatten sample/Schema
                 "UserAccount"
                 (xtd/obj-omit sample/RootUserFull
                               ["wallets"])
                 {})))
  
  :setup [(def +full-core+
            (!.js
              (f/flatten sample/Schema
                         "UserAccount"
                         (xtd/obj-pick sample/RootUserFull
                                       ["id" "nickname" "is_active"])
                         {})))
          (def +full-contact+
            (!.js
              (f/flatten sample/Schema
                         "UserAccount"
                         (xtd/obj-omit sample/RootUserFull
                                       ["notification" "organisations" "wallets"
                                        "portfolios" "identities"])
                         {})))
          (def +full-org-notify+
            (!.js
              (f/flatten sample/Schema
                         "UserAccount"
                         (xtd/obj-omit sample/RootUserFull
                                       ["wallets" "portfolios" "identities"])
                         {})))
          (def +full-wallets+
            (!.js
              (f/flatten sample/Schema
                         "UserAccount"
                         (xtd/obj-omit sample/RootUserFull
                                       ["notification" "organisations" "emails"
                                        "portfolios" "identities" "profile"])
                         {})))
          (def +full-no-wallets+
            (!.js
              (f/flatten sample/Schema
                         "UserAccount"
                         (xtd/obj-omit sample/RootUserFull
                                       ["wallets"])
                         {})))]
  

  (!.py
    (var rows {})
    (data/merge-bulk rows (@! +full-core+) nil)
    (data/merge-bulk rows (@! +full-contact+) nil))
  => map?

  (!.py
    (var rows {})
    (data/merge-bulk rows (@! +full-contact+) nil)
    (data/merge-bulk rows (@! +full-org-notify+) nil))
  => map?

  (!.py
    (var rows {})
    (data/merge-bulk rows (@! +full-no-wallets+) nil)
    (data/merge-bulk rows (@! +full-wallets+) nil))
  => map?)
