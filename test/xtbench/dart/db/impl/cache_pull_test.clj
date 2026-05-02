(ns xtbench.dart.db.impl.cache-pull-test
  (:use code.test)
  (:require [net.http :as http]
            [std.json :as json]
            [std.lang :as l]
            [xt.db.helpers.data-main-test :as sample]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-trace :as trace]
             [xt.db.impl.cache-util :as data]
             [xt.db.impl.cache-pull :as q]
             [xt.db.schema.base-flatten :as f]
             [xt.db.schema.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +flattened+
  {"UserProfile"
   {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
    {"ref_links"
     {"account" {"00000000-0000-0000-0000-000000000000" true}},
     "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
     "rev_links" {},
     "data"
     {"detail" {"hello" "world"},
      "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
      "last_name" "User",
      "first_name" "Root",
      "language" "en"}}},
   "UserAccount"
   {"00000000-0000-0000-0000-000000000000"
    {"ref_links" {},
     "id" "00000000-0000-0000-0000-000000000000",
     "rev_links"
     {"profile" {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}},
     "data"
     {"is_official" false,
      "nickname" "root",
      "id" "00000000-0000-0000-0000-000000000000",
      "is_suspended" false,
      "is_super" true}}}})

(def +flattened-full+
  {"UserProfile"
   {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
    {"ref_links"
     {"account" {"00000000-0000-0000-0000-000000000000" true}},
     "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
     "rev_links" {},
     "data"
     {"detail" {"hello" "world"},
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
      "title" ""}}},
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
     "data" {"id" "USD"}}}})

^{:refer xt.db.impl.cache-pull/pull.control :added "4.0" :adopt true}
(fact "gets a currency graph"

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened-full+) nil)
    (q/pull rows sample/Schema "Currency"
            {:returning ["id"]
             :limit 2
             :offset 2
             :order-by ["id"]}))
  => [{"id" "XLM"} {"id" "XLM.T"}]

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened-full+) nil)
    (q/pull rows sample/Schema "Currency"
            {:returning ["id"]
             :limit 2
             :order-by ["id"]}))
  => [{"id" "STATS"} {"id" "USD"}]

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened-full+) nil)
    (q/pull rows sample/Schema "Currency"
            {:returning ["id"]
             :limit 2
             :order-by ["id"]
             :order-sort "desc"}))
  => [{"id" "XLM.T"} {"id" "XLM"}])

^{:refer xt.db.impl.cache-pull/pull.currency :added "4.0"
  :setup [(def +check-pull-currency+
            (contains-in
             [{"entries"
               (contains
                [(contains-in {"asset" [{"currency" [{"id" "STATS"}]}]})
                 (contains-in {"asset" [{"currency" [{"id" "USD"}]}]})
                 (contains-in {"asset" [{"currency" [{"id" "XLM.T"}]}]})
                 (contains-in {"asset" [{"currency" [{"id" "XLM"}]}]})]
                :in-any-order)}]))] :adopt true}
(fact "gets a currency graph"

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened-full+) nil)
    (q/pull rows
            sample/Schema
            "Wallet"
            {:returning [["entries"
                          [["asset" ["-/data"
                                     ["currency"]]]]]]}))
  => +check-pull-currency+)

^{:refer xt.db.impl.cache-pull/check-in-clause :added "4.0"}
(fact "emulates the sql `in` clause"

  (!.dt
    (q/check-in-clause "a" [["a" "b"]]))
  => true)

^{:refer xt.db.impl.cache-pull/like-char-at :added "4.1"}
(fact "gets the single char from a string"

  (!.dt
    (q/like-char-at "abc" 1))
  => "b")

^{:refer xt.db.impl.cache-pull/check-like-clause :added "4.0"}
(fact "emulates the sql `like` clause"

  (!.dt
    [(q/check-like-clause "abc" "a%")
     (q/check-like-clause "abc" "%c")
     (q/check-like-clause "abc" "a_c")
     (q/check-like-clause "abc" "a%d")
     (q/check-like-clause "abc" "%%a%c%%")
     (q/check-like-clause "a%b" "a\\%b")
     (q/check-like-clause "a_b" "a\\_b")
     (q/check-like-clause "a\\b" "a\\\\b")
     (q/check-like-clause "%" "%_")
     (q/check-like-clause nil "a%")
     (q/check-like-clause "abc" nil)])
  => [true true true false true true true true true false false])

^{:refer xt.db.impl.cache-pull/check-clause-value :added "4.0"}
(fact "checks the clause within a record"

  (!.dt
    (q/check-clause-value {:data {:name "abc"}}
                          "data"
                          "name"
                          "abc"))
  => true)

^{:refer xt.db.impl.cache-pull/check-clause-function :added "4.0"}
(fact "checks the clause for a function within a record"

  (!.dt
    (q/check-clause-function {:data {:name "abc"}}
                             "data"
                             "name"
                             q/check-like-clause
                             ["a%"]))
  => true)

^{:refer xt.db.impl.cache-pull/pull-where-clause :added "4.0"}
(fact "pull where clause"

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened-full+) nil)
    (q/pull-where-clause rows
                         sample/Schema
                         "UserAccount"
                         (xtd/get-in rows ["UserAccount"
                                           "00000000-0000-0000-0000-000000000000"
                                           "record"])
                         q/pull-where
                         "id"
                         (fn:> [x] true))))

^{:refer xt.db.impl.cache-pull/pull-where :added "4.0"}
(fact "clause for where construct"

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened-full+) nil)
    [(q/pull-where rows sample/Schema "UserAccount"
                   (fn:> [record table-key] true) {})
     (q/pull-where rows sample/Schema  "UserAccount"
                   {:nickname "hello"}
                   {})
     (q/pull-where rows sample/Schema  "UserAccount"
                   {:nickname "hello"}
                   {:data {:nickname "hello"}})])
  => [true false true])

^{:refer xt.db.impl.cache-pull/pull-return-clause :added "4.0"}
(fact "pull return clause"

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened-full+) nil)
    (q/pull-return-clause rows
                          sample/Schema
                          (xtd/get-in rows ["UserAccount"
                                            "00000000-0000-0000-0000-000000000000"
                                            "record"])
                          q/pull-where
                          q/pull-return
                          {"ident" "profile",
                           "type" "ref",
                           "ref" {"key" "_account",
                                  "rkey" "account",
                                  "type" "reverse",
                                  "rident" "account",
                                  "rval" "account",
                                  "ns" "UserProfile",
                                  "val" "profile"},
                           "cardinality" "many"}
                          [{} ["*/data"]]))
  => (contains-in
      ["profile" [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                   "last_name" "User",
                   "first_name" "Root",
                   "language" "en"}]])

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened-full+) nil)
    (q/pull-return-clause rows
                          sample/Schema
                          (xtd/get-in rows ["UserAccount"
                                            "00000000-0000-0000-0000-000000000000"
                                            "record"])
                          q/pull-where
                          q/pull-return
                          {"ident" "profile",
                           "type" "ref",
                           "ref" {"key" "_account",
                                  "rkey" "account",
                                  "type" "reverse",
                                  "rident" "account",
                                  "rval" "account",
                                  "ns" "UserProfile",
                                  "val" "profile"},
                           "cardinality" "many"}
                          [{:id "missing"} ["*/data"]]))
  => (contains ["profile"]))

^{:refer xt.db.impl.cache-pull/pull-return :added "4.0"}
(fact "return construct"

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened-full+) nil)
    (q/pull-return rows
                   sample/Schema
                   "UserAccount"
                   ["id" "nickname"
                    ["profile"
                     ["first_name" "last_name"]]]
                   (xtd/get-in rows ["UserAccount"
                                     "00000000-0000-0000-0000-000000000000"
                                     "record"])))
  => {"nickname" "root",
      "profile" [{"last_name" "User", "first_name" "Root"}],
      "id" "00000000-0000-0000-0000-000000000000"})

^{:refer xt.db.impl.cache-pull/pull :added "4.0"}
(fact "pull data from database python profile full"

  (!.dt
    (var rows {})
    (data/merge-bulk rows (@! +flattened+) nil)
    (q/pull rows sample/Schema "UserAccount"
            {:returning ["id" "nickname"
                         ["profile"
                          ["*/data"]]]}))
  => (contains-in
      [{"nickname" "root",
        "profile"
        [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
          "last_name" "User",
          "first_name" "Root",
          "language" "en"}],
        "id" "00000000-0000-0000-0000-000000000000"}]))

(comment
  (s/pedantic '[xt.db.impl.cache-pull])
  (s/run '[xt.db.impl.cache-pull])
  
  (s/seedgen-benchadd '[xt.db] {:lang [:dart :julia] :write true})
  (s/seedgen-langadd '[xt.db.impl.cache-pull] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.db.impl.cache-pull] {:lang [:lua :python] :write true}))
