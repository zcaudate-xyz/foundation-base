(ns xt.db.base-scope-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]
             [xt.db.sql-util :as ut]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]
             [xt.db.sql-util :as ut]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]
             [xt.db.sql-util :as ut]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)
             (l/rt:scaffold :python)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.base-scope/get-data-columns.more :adopt true :added "4.0"}
(fact "classifies the link"

  (!.js
   (k/arr-map (scope/get-data-columns sample/Schema
                                      "UserAccount"
                                      ["*/info" "password_updated"])
              (fn:> [e] (. e ["ident"]))))
  => ["id" "nickname" "password_updated" "is_super" "is_suspended" "is_official"]

  (!.js
   (k/arr-map (scope/get-data-columns sample/Schema
                                      "RegionState"
                                      ["*/info" "country"])
              (fn:> [e] (. e ["ident"]))))
  
  (!.js
   (k/arr-map (scope/get-data-columns sample/Schema
                                      "RegionState"
                                      ["*/info" "country_id"])
              (fn:> [e] (. e ["ident"]))))
  => ["id" "country"])

^{:refer xt.db.base-scope/get-tree.more :adopt true :added "4.0"}
(fact "MORE CHECKS"

  (!.js
   (scope/get-tree sample/Schema
                   "UserProfile"
                   {:id "zcaudate"}
                   ["*/default"]
                   {}))
  => ["UserProfile"
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
        "language"]}]
 
  (!.js
   (scope/get-tree sample/Schema
                      "UserAccount"
                      {:id "zcaudate"}
                      ["*/data"
                       ["profile" [{:name "hello"}] ["*/default"]]]
                      {}))
  => ["UserAccount"
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
     "is_official"]}]
  
  
  (!.py
   (scope/get-tree sample/Schema
                   "UserAccount"
                   {:id "zcaudate"}
                   [["wallets"
                     ["*/data"
                      ["entries"
                       ["*/data"
                        ["asset"]]]]]]
                   (ut/postgres-opts sample/SchemaLookup)))
  => ["UserAccount"
      {"custom" [],
       "where" [{"id" "zcaudate"}],
       "links"
       [["wallets"
         "reverse"
         ["Wallet"
          {"custom" [],
           "where"
           [{"owner"
             ["eq"
              ["\"scratch/xt.db.sample-user-test\".\"UserAccount\".\"id\""]]}],
           "links"
           [["entries"
             "reverse"
             ["WalletAsset"
              {"custom" [],
               "where"
               [{"wallet"
                 ["eq"
                  ["\"scratch/xt.db.sample-user-test\".\"Wallet\".\"id\""]]}],
               "links"
               [["asset"
                 "forward"
                 ["Asset"
                  {"custom" [],
                   "where"
                   [{"id"
                     ["eq"
                      ["\"scratch/xt.db.sample-user-test\".\"WalletAsset\".\"asset_id\""]]}],
                   "links" [],
                   "data" ["id"]}]]],
               "data" ["id"]}]]],
           "data" ["id" "slug"]}]]],
       "data" []}]
  
  (!.py
   (scope/get-tree sample/Schema
                   "UserAccount"
                   {:id "zcaudate"}
                   [["wallets"
                     ["*/data"
                      ["entries"
                       ["*/data"
                        ["asset"]]]]]]
                   (ut/sqlite-opts nil)))
  => ["UserAccount"
      {"custom" [],
       "where" [{"id" "zcaudate"}],
       "links"
       [["wallets"
         "reverse"
         ["Wallet"
          {"custom" [],
           "where" [{"owner" ["eq" ["\"UserAccount\".\"id\""]]}],
           "links"
           [["entries"
             "reverse"
             ["WalletAsset"
              {"custom" [],
               "where" [{"wallet" ["eq" ["\"Wallet\".\"id\""]]}],
               "links"
               [["asset"
                 "forward"
                 ["Asset"
                  {"custom" [],
                   "where"
                   [{"id" ["eq" ["\"WalletAsset\".\"asset_id\""]]}],
                   "links" [],
                   "data" ["id"]}]]],
               "data" ["id"]}]]],
           "data" ["id" "slug"]}]]],
       "data" []}])

^{:refer xt.db.base-scope/get-link-standard.more :adopt true :added "4.0"}
(fact "classifies the link"
  ^:hidden
  
  (!.js
   (scope/get-link-standard ["hello" {} {} ["hello"]]))
  => ["hello" [{} {} ["hello"]]])

^{:refer xt.db.base-scope/get-link-standard.more :adopt true :added "4.0"}
(fact "classifies the link"
  ^:hidden


  (!.js
   (k/arr-map (scope/get-link-columns sample/Schema
                                      "UserAccount"
                                      [["profile"
                                        {:id "1"}
                                        {:id "2"}
                                        {:id "3"}
                                        ["first_name"
                                         "last_name"]]])
              (fn [[e cols]] (return [e.ident cols]))))
  => [["profile" [{"id" "1"} {"id" "2"} {"id" "3"} ["first_name" "last_name"]]]])

^{:refer xt.db.base-scope/merge-queries :added "4.0"}
(fact "merges query with clause"
  ^:hidden
  
  (!.js
   [(scope/merge-queries [] [])
    (scope/merge-queries [{:a 1}] [{:a 2}])
    (scope/merge-queries [{:a 1}] [{:b 2}])
    (scope/merge-queries [{:a 1}] [{:b 2} {:c 3}])
    (scope/merge-queries [{:a 1} {:c 1}] [{:b 2} {:c 3}])])
  => [[]
      [{"a" 2}]
      [{"a" 1, "b" 2}]
      [{"a" 1, "b" 2} {"a" 1, "c" 3}]
      [{"a" 1, "b" 2} {"a" 1, "c" 3} {"b" 2, "c" 1} {"c" 3}]]

  (!.lua
   [(scope/merge-queries [] [])
    (scope/merge-queries [{:a 1}] [{:a 2}])
    (scope/merge-queries [{:a 1}] [{:b 2}])
    (scope/merge-queries [{:a 1}] [{:b 2} {:c 3}])
    (scope/merge-queries [{:a 1} {:c 1}] [{:b 2} {:c 3}])])
  => [{}
      [{"a" 2}]
      [{"a" 1, "b" 2}]
      [{"a" 1, "b" 2} {"a" 1, "c" 3}]
      [{"a" 1, "b" 2} {"a" 1, "c" 3} {"b" 2, "c" 1} {"c" 3}]]

  (!.py
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

^{:refer xt.db.base-scope/filter-scope :added "4.0"}
(fact "filter scopes from keys"
  ^:hidden

  (!.js  [(scope/filter-scope ["-/data"  "id"])
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
       "-/id" true}]

  (!.lua [(scope/filter-scope ["-/data"  "id"])
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
       "-/id" true}]

  (!.py [(scope/filter-scope ["-/data"  "id"])
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

^{:refer xt.db.base-scope/filter-plain-key :added "4.0"}
(fact  "converts _id tags to standard keys"
  ^:hidden

  (!.js
   [(scope/filter-plain-key "hello")
    (scope/filter-plain-key "hello_id")])
  => ["hello" "hello"]

  (!.lua
   [(scope/filter-plain-key "hello")
    (scope/filter-plain-key "hello_id")])
  => ["hello" "hello"]

  (!.py
   [(scope/filter-plain-key "hello")
    (scope/filter-plain-key "hello_id")])
  => ["hello" "hello"])

^{:refer xt.db.base-scope/filter-plain :added "4.0"}
(fact "filter ids keys from scope keys"
  ^:hidden

  (!.js 
   (scope/filter-plain  ["-/data"  "id"]))
  => {"id" true}

  (!.lua
   (scope/filter-plain  ["-/data"  "id"]))
  => {"id" true}

  (!.py
   (scope/filter-plain  ["-/data"  "id"]))
  => {"id" true})

^{:refer xt.db.base-scope/get-data-columns :added "4.0"
  :setup [(def +out+
            ["id" "account" "first_name" "last_name" "city"
             "state" "country" "about" "language" "detail"])]}
(fact "get columns for given keys"
  ^:hidden
  
  (!.js
   (k/arr-map (scope/get-data-columns sample/Schema
                                "UserAccount"
                                ["*/data"])
          (fn:> [e] (. e ["ident"]))))
  => ["id" "nickname" "password_updated" "is_super" "is_suspended" "is_official"]

  (!.js
   (k/arr-map (scope/get-data-columns sample/Schema
                                "UserProfile"
                                ["*/standard"])
          (fn:> [e] (. e ["ident"]))))
  => +out+


  (!.lua
   (k/arr-map (scope/get-data-columns sample/Schema
                                      "UserAccount"
                                      ["*/data"])
              (fn:> [e] (. e ["ident"]))))
  => ["id" "nickname" "password_updated" "is_super" "is_suspended" "is_official"]
  
  (!.lua
   (k/arr-map (scope/get-data-columns sample/Schema
                                      "UserProfile"
                                      ["*/standard"])
          (fn:> [e] (. e ["ident"]))))
  => +out+

  (!.py
   (k/arr-map (scope/get-data-columns sample/Schema
                                      "UserAccount"
                                      ["*/data"])
              (fn:> [e] (. e ["ident"]))))
  => ["id" "nickname" "password_updated" "is_super" "is_suspended" "is_official"]
  
  (!.py
   (k/arr-map (scope/get-data-columns sample/Schema
                                      "UserProfile"
                                      ["*/standard"])
          (fn:> [e] (. e ["ident"]))))
  =>  +out+)

^{:refer xt.db.base-scope/get-link-standard :added "4.0"}
(fact "classifies the link"
  ^:hidden
  
  (!.js
   (scope/get-link-standard ["hello" ["hello"]]))
  => ["hello" [{} ["hello"]]]

  (!.lua
   (scope/get-link-standard ["hello" ["hello"]]))
  => ["hello" [{} ["hello"]]]

  (!.py
   (scope/get-link-standard ["hello" ["hello"]]))
  => ["hello" [{} ["hello"]]])

^{:refer xt.db.base-scope/get-query-tables :added "4.0"}
(fact "get columns for given query"
  ^:hidden
  
  (!.js
   (scope/get-query-tables sample/Schema
                           "UserAccount"
                           {:profile {}}
                           {}))
  => {"UserProfile" true, "UserAccount" true}


  (!.lua
   (scope/get-query-tables sample/Schema
                           "UserAccount"
                           {:profile {}}
                           {}))
  => {"UserProfile" true, "UserAccount" true}


  (!.py
   (scope/get-query-tables sample/Schema
                           "UserAccount"
                           {:profile {}}
                           {}))
  => {"UserProfile" true, "UserAccount" true})

^{:refer xt.db.base-scope/get-link-columns :added "4.0"}
(fact "get columns for given keys"
  ^:hidden
  
  (!.js
   (k/arr-map (scope/get-link-columns sample/Schema
                                      "UserAccount"
                                      [["profile" ["first_name"
                                                   "last_name"]]])
              (fn [[e cols]] (return [e.ident cols]))))
  => [["profile" [{} ["first_name" "last_name"]]]]
  
  (!.lua
   (k/arr-map (scope/get-link-columns sample/Schema
                                      "UserAccount"
                                      [["profile" ["first_name"
                                                   "last_name"]]])
              (fn [e] (return [(k/get-key (k/first e)
                                          "ident")
                               (k/second e)]))))
  => [["profile" [{} ["first_name" "last_name"]]]]

  (!.py
   (k/arr-map (scope/get-link-columns sample/Schema
                                      "UserAccount"
                                      [["profile" ["first_name"
                                                   "last_name"]]])
              (fn [e] (return [(k/get-key (k/first e)
                                          "ident")
                               (k/second e)]))))
  => [["profile" [{} ["first_name" "last_name"]]]])

^{:refer xt.db.base-scope/get-linked-tables :added "4.0"}
(fact "calculated linked tables given query"
  ^:hidden
  
  (!.js
   (scope/get-linked-tables sample/Schema
                                "UserAccount"
                                [["profile"]
                                 ["wallets"
                                  [["entries"
                                    [["asset"]]]]]]))
  => {"UserProfile" true, "Asset" true, "UserAccount" true, "WalletAsset" true, "Wallet" true}

  (!.lua
   (scope/get-linked-tables sample/Schema
                            "UserAccount"
                            [["profile"]
                             ["wallets"
                              [["entries"
                                [["asset"]]]]]]))
  => {"UserProfile" true, "Asset" true, "UserAccount" true, "WalletAsset" true, "Wallet" true}

  (!.py
   (scope/get-linked-tables sample/Schema
                            "UserAccount"
                            [["profile"]
                             ["wallets"
                              [["entries"
                                [["asset"]]]]]]))
  => {"UserProfile" true, "Asset" true, "UserAccount" true, "WalletAsset" true, "Wallet" true})

^{:refer xt.db.base-scope/as-where-input :added "4.0"}
(fact "when empty, returns an empty array")

^{:refer xt.db.base-scope/get-tree :added "4.0"}
(fact "calculated linked tree given query"
  ^:hidden
  
  (!.js
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
  => ["UserAccount"
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

(comment
  (!.py
   (k/get-path sample/Schema ["UserProfile" "account"]))
  
  (!.py
   (scope/get-tree sample/Schema
                   "UserProfile"
                   {:id "zcaudate"}
                   ["account"]
                   {}))
  
  
  (!.py
   (scope/get-tree sample/Schema
                   "UserAccount"
                   {:id "zcaudate"}
                   [["profile" {:name "hello"}]]
                   {}))
  
  
  (!.js
   (scope/get-tree sample/Schema
                   "UserProfile"
                   {:first-name "hello"}
                   [["account" {:is-official true}]]
                   {}))

  (!.js
   (scope/get-tree sample/Schema
                   "UserAccount"
                   {}
                   [["profile"]]
                   {}))
  
  (!.js
   (scope/get-tree sample/Schema
                   "UserProfile"
                   {:first-name "hello"}
                   [["account" {:is-official true}]]
                   {})))
