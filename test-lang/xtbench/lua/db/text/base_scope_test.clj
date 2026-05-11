(ns xtbench.lua.db.text.base-scope-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.text.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]
             [xt.db.text.sql-util :as ut]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.text.base-scope/get-data-columns.more :added "4.0" :adopt true}
(fact "classifies the link"

  (!.lua
    (xtd/arr-map (scope/get-data-columns sample/Schema
                                         "UserAccount"
                                         ["*/info" "password_updated"])
                 (fn:> [e] (. e ["ident"]))))
  => ["id" "nickname" "password_updated" "is_super" "is_suspended" "is_official"]

  (!.lua
    (xtd/arr-map (scope/get-data-columns sample/Schema
                                         "RegionState"
                                         ["*/info" "country_id"])
                 (fn:> [e] (. e ["ident"]))))
  => ["id" "country"])

^{:refer xt.db.text.base-scope/get-tree.more :added "4.0"
  :setup [(def +profile+
            ["UserProfile"
             {"custom" [],
              "where" [{"id" "zcaudate"}],
              "links" [],
              "data"
              ["id"
               "account_id"
               "first_name"
               "last_name"
               "city"
               "state_id"
               "country_id"
               "about"
               "language"]}])
                   (def +account+
                     ["UserAccount"
                      {"custom" [],
                       "where" [{"id" "zcaudate"}],
                       "links"
                       [["profile"
                         "reverse"
                         ["UserProfile"
                          {"custom" [],
                           "where" [{"account" ["eq" ["UserAccount.id"]]}],
                           "links" [],
                           "data"
                           ["id"
                            "account_id"
                            "first_name"
                            "last_name"
                            "city"
                            "state_id"
                            "country_id"
                            "about"
                            "language"]}]]],
                       "data"
                       ["id"
                        "nickname"
                        "password_updated"
                        "is_super"
                        "is_suspended"
                        "is_official"]}])] :adopt true}
(fact "MORE CHECKS"

  (!.lua
    (scope/get-tree sample/Schema
                    "UserProfile"
                    {:id "zcaudate"}
                    ["*/default"]
                    {}))
  => (l/as-lua +profile+)

  (!.lua
    (scope/get-tree sample/Schema
                    "UserAccount"
                    {:id "zcaudate"}
                    ["*/data"
                     ["profile" [{:name "hello"}] ["*/default"]]]
                    {}))
  => (l/as-lua +account+))

^{:refer xt.db.text.base-scope/get-link-standard.more :added "4.0" :adopt true}
(fact "classifies the link"

  (!.lua
    (xtd/arr-map (scope/get-link-columns sample/Schema
                                         "UserAccount"
                                         [["profile"
                                           {:id "1"}
                                           {:id "2"}
                                           {:id "3"}
                                           ["first_name"
                                            "last_name"]]])
                 (fn [out]
                   (var [e cols] out)
                   (return [(. e ["ident"]) cols]))))
  => [["profile" [{"id" "1"} {"id" "2"} {"id" "3"} ["first_name" "last_name"]]]])

^{:refer xt.db.text.base-scope/get-link-standard.more :added "4.0" :adopt true}
(fact "classifies the link"

  (!.lua
    (xtd/arr-map (scope/get-link-columns sample/Schema
                                         "UserAccount"
                                         [["profile"
                                           {:id "1"}
                                           {:id "2"}
                                           {:id "3"}
                                           ["first_name"
                                            "last_name"]]])
                 (fn [out]
                   (var [e cols] out)
                   (return [(. e ["ident"]) cols]))))
  => [["profile" [{"id" "1"} {"id" "2"} {"id" "3"} ["first_name" "last_name"]]]])

^{:refer xt.db.text.base-scope/merge-queries :added "4.0"}
(fact "merges query with clause"

  (!.lua
    [(scope/merge-queries [] [])
     (scope/merge-queries [{:a 1}] [{:a 2}])
     (scope/merge-queries [{:a 1}] [{:b 2}])
     (scope/merge-queries [{:a 1}] [{:b 2} {:c 3}])
     (scope/merge-queries [{:a 1} {:c 1}] [{:b 2} {:c 3}])])
  => [[]
      [{"a" 2}]
      [{"a" 1, "b" 2}]
      [{"a" 1, "b" 2} {"a" 1, "c" 3}]
      [{"a" 1, "b" 2} {"a" 1, "c" 3} {"b" 2, "c" 1} {"c" 3}]])

^{:refer xt.db.text.base-scope/filter-scope :added "4.0"}
(fact "filter scopes from keys"

  (!.lua  [(scope/filter-scope ["-/data"  "id"])
          (scope/filter-scope ["-/data" "-/key"])
          (scope/filter-scope ["*/data" "-/key"])
          (scope/filter-scope ["*/everything"])])
  => [{"-/data" true}
      {"-/data" true, "-/key" true}
      {"-/data" true, "-/key" true, "-/info" true, "-/id" true}
      {"-/system" true,
       "-/data" true,
       "-/key" true,
       "-/detail" true,
       "-/hidden" true,
       "-/ref" true,
       "-/info" true,
       "-/id" true}])

^{:refer xt.db.text.base-scope/filter-plain-key :added "4.0"}
(fact "converts _id tags to standard keys"

  (!.lua
    [(scope/filter-plain-key "hello")
     (scope/filter-plain-key "hello_id")])
  => ["hello" "hello"])

^{:refer xt.db.text.base-scope/filter-plain :added "4.0"}
(fact "filter ids keys from scope keys"

  (!.lua
    (scope/filter-plain  ["-/data"  "id"]))
  => {"id" true})

^{:refer xt.db.text.base-scope/get-data-columns :added "4.0"}
(fact "get columns for given keys"

  (!.lua
    (xtd/arr-map (scope/get-data-columns sample/Schema
                                         "UserAccount"
                                         ["*/data"])
                 (fn:> [e] (. e ["ident"]))))
  => ["id" "nickname" "password_updated" "is_super" "is_suspended" "is_official"]

  (!.lua
    (xtd/arr-map (scope/get-data-columns sample/Schema
                                         "UserProfile"
                                         ["*/standard"])
                 (fn:> [e] (. e ["ident"]))))
  => ["id" "account" "first_name" "last_name" "city" "state" "country" "about" "language" "detail"])

^{:refer xt.db.text.base-scope/get-link-standard :added "4.0"}
(fact "classifies the link"

  (!.lua
    (scope/get-link-standard ["hello" ["hello"]]))
  => ["hello" [{} ["hello"]]])

^{:refer xt.db.text.base-scope/get-query-tables :added "4.0"}
(fact "get columns for given query"

  (!.lua
    (scope/get-query-tables sample/Schema
                            "UserAccount"
                            {:profile {}}
                            {}))
  => {"UserProfile" true, "UserAccount" true})

^{:refer xt.db.text.base-scope/get-link-columns :added "4.0"}
(fact "get columns for given keys"

  (!.lua
    (xtd/arr-map (scope/get-link-columns sample/Schema
                                         "UserAccount"
                                         [["profile" ["first_name"
                                                      "last_name"]]])
                 (fn [out]
                   (var [e cols] out)
                   (return [(. e ["ident"]) cols]))))
  => [["profile" [{} ["first_name" "last_name"]]]])

^{:refer xt.db.text.base-scope/get-linked-tables-loop :added "4.1"}
(fact "accumulates linked tables into an existing map"

  (!.lua
    (scope/get-linked-tables-loop sample/Schema
                                  "UserAccount"
                                  [["profile"]
                                   ["wallets"
                                    [["entries"
                                      [["asset"]]]]]]
                                  {"Seed" true}))
  => {"Seed" true, "UserProfile" true, "Asset" true, "UserAccount" true, "WalletAsset" true, "Wallet" true})

^{:refer xt.db.text.base-scope/get-linked-tables :added "4.0"}
(fact "calculated linked tables given query"

  (!.lua
    (scope/get-linked-tables sample/Schema
                             "UserAccount"
                             [["profile"]
                              ["wallets"
                               [["entries"
                                 [["asset"]]]]]]))
  => {"UserProfile" true, "Asset" true, "UserAccount" true, "WalletAsset" true, "Wallet" true})

^{:refer xt.db.text.base-scope/as-where-input :added "4.0"}
(fact "when empty, returns an empty array"

  (!.lua
    [(scope/as-where-input [])
     (scope/as-where-input [{:id "zcaudate"}
                            {:id "z1"}])
     (scope/as-where-input {:id "zcaudate"})])
  => [[] [{"id" "zcaudate"} {"id" "z1"}] [{"id" "zcaudate"}]])

^{:refer xt.db.text.base-scope/get-tree :added "4.0"
  :setup [(def +account+
            ["UserAccount"
             {"custom" [],
              "where" [{"id" "zcaudate"} {"id" "z1"} {"id" "z3"}],
              "links"
              [["wallets"
                "reverse"
                ["Wallet"
                 {"custom" [],
                  "where"
                  [{"owner" ["eq" ["UserAccount.id"]], "id" "W1"}
                   {"owner" ["eq" ["UserAccount.id"]], "id" "W2"}
                   {"owner" ["eq" ["UserAccount.id"]], "id" "W3"}],
                  "links"
                  [["entries"
                    "reverse"
                    ["WalletAsset"
                     {"custom" [],
                      "where"
                      [{"wallet" ["eq" ["Wallet.id"]], "id" "E1"}
                       {"wallet" ["eq" ["Wallet.id"]], "id" "E2"}
                       {"wallet" ["eq" ["Wallet.id"]], "id" "E3"}],
                      "links" [],
                      "data" ["id"]}]]],
                  "data" []}]]],
              "data" []}])
                   (def +account-lua+
                     ["UserAccount"
                      {"custom" {},
                       "where" [{"id" "zcaudate"} {"id" "z1"} {"id" "z3"}],
                       "links"
                       [["wallets"
                         "reverse"
                         ["Wallet"
                          {"custom" {},
                           "where"
                           [{"owner" ["eq" ["UserAccount.id"]], "id" "W1"}
                            {"owner" ["eq" ["UserAccount.id"]], "id" "W2"}
                            {"owner" ["eq" ["UserAccount.id"]], "id" "W3"}],
                           "links"
                           [["entries"
                             "reverse"
                             ["WalletAsset"
                              {"custom" {},
                               "where"
                               [{"wallet" ["eq" ["Wallet.id"]], "id" "E1"}
                                {"wallet" ["eq" ["Wallet.id"]], "id" "E2"}
                                {"wallet" ["eq" ["Wallet.id"]], "id" "E3"}],
                               "links" {},
                               "data" ["id"]}]]],
                           "data" {}}]]],
                       "data" {}}])]}
(fact "calculated linked tree given query"

  (!.lua
    (scope/get-tree sample/Schema
                    "UserAccount"
                    [{:id "zcaudate"}
                     {:id "z1"}
                     {:id "z3"}]
                    [["wallets"
                      {:id "W1"}
                      {:id "W2"}
                      {:id "W3"}
                      [["entries"
                        {:id "E1"}
                        {:id "E2"}
                        {:id "E3"}]]]]
                    {}))
  => (l/as-lua +account+))

(comment
  (s/run ['xt.db.text.base-scope])
  (s/pedantic ['xt.db.text.base])
  (s/seedgen-benchadd '[xt.lang.spec] {:lang [:r] :write true})
  (s/seedgen-benchadd '[xt.db.text.base-scope] {:lang [:julia :dart] :write true})
  
  (s/seedgen-langadd 'xt.db.text.base-scope {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.text.base-scope {:lang [:lua :python] :write true}))
