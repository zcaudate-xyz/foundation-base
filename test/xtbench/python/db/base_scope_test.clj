(ns
 xtbench.python.db.base-scope-test
 (:require [std.lang :as l])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.lang.common-spec :as xt]
   [xt.lang.common-data :as xtd]
   [xt.db.base-scope :as scope]
   [xt.db.sample-test :as sample]
   [xt.db.sql-util :as ut]]})

(fact:global
 {:setup [(l/rt:restart) (l/rt:scaffold :python)],
  :teardown [(l/rt:stop)]})

^{:refer xt.db.base-scope/get-data-columns.more,
  :adopt true,
  :added "4.0"}
(fact
 "classifies the link"
 (!.py
  (xtd/arr-map
   (scope/get-data-columns
    sample/Schema
    "UserAccount"
    ["*/info" "password_updated"])
   (fn:> [e] (. e ["ident"]))))
 =>
 ["id"
  "nickname"
  "password_updated"
  "is_super"
  "is_suspended"
  "is_official"]
 (!.py
  (xtd/arr-map
   (scope/get-data-columns
    sample/Schema
    "RegionState"
    ["*/info" "country_id"])
   (fn:> [e] (. e ["ident"]))))
 =>
 ["id" "country"])

^{:refer xt.db.base-scope/get-tree.more, :adopt true, :added "4.0"}
(fact
 "MORE CHECKS"
 (!.py
  (scope/get-tree
   sample/Schema
   "UserAccount"
   {:id "zcaudate"}
   [["wallets" ["*/data" ["entries" ["*/data" ["asset"]]]]]]
   (ut/postgres-opts sample/SchemaLookup)))
 =>
 ["UserAccount"
  {"custom" [],
   "where" [{"id" "zcaudate"}],
   "links"
   [["wallets"
     "reverse"
     ["Wallet"
      {"custom" [],
       "where"
       [{"owner"
         ["eq" ["\"scratch-sample-db\".\"UserAccount\".\"id\""]]}],
       "links"
       [["entries"
         "reverse"
         ["WalletAsset"
          {"custom" [],
           "where"
           [{"wallet"
             ["eq" ["\"scratch-sample-db\".\"Wallet\".\"id\""]]}],
           "links"
           [["asset"
             "forward"
             ["Asset"
              {"custom" [],
               "where"
               [{"id"
                 ["eq"
                  ["\"scratch-sample-db\".\"WalletAsset\".\"asset_id\""]]}],
               "links" [],
               "data" ["id"]}]]],
           "data" ["id"]}]]],
       "data" ["id" "slug"]}]]],
   "data" []}]
 (!.py
  (scope/get-tree
   sample/Schema
   "UserAccount"
   {:id "zcaudate"}
   [["wallets" ["*/data" ["entries" ["*/data" ["asset"]]]]]]
   (ut/sqlite-opts nil)))
 =>
 ["UserAccount"
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

^{:refer xt.db.base-scope/get-link-standard.more,
  :adopt true,
  :added "4.0"}
(fact
 "classifies the link"
 ^{:hidden true}
 (!.py (scope/get-link-standard ["hello" {} {} ["hello"]]))
 =>
 ["hello" [{} {} ["hello"]]])

^{:refer xt.db.base-scope/get-link-standard.more,
  :adopt true,
  :added "4.0"}
(fact
 "classifies the link"
 ^{:hidden true}
 (!.py
  (xtd/arr-map
   (scope/get-link-columns
    sample/Schema
    "UserAccount"
    [["profile"
      {:id "1"}
      {:id "2"}
      {:id "3"}
      ["first_name" "last_name"]]])
   (fn
    [e]
    (return [(xt/x:get-key (xtd/first e) "ident") (xtd/second e)]))))
 =>
 [["profile"
   [{"id" "1"} {"id" "2"} {"id" "3"} ["first_name" "last_name"]]]])

^{:refer xt.db.base-scope/merge-queries, :added "4.0"}
(fact
 "merges query with clause"
 ^{:hidden true}
 (!.py
  [(scope/merge-queries [] [])
   (scope/merge-queries [{:a 1}] [{:a 2}])
   (scope/merge-queries [{:a 1}] [{:b 2}])
   (scope/merge-queries [{:a 1}] [{:b 2} {:c 3}])
   (scope/merge-queries [{:a 1} {:c 1}] [{:b 2} {:c 3}])])
 =>
 [[]
  [{"a" 2}]
  [{"a" 1, "b" 2}]
  [{"a" 1, "b" 2} {"a" 1, "c" 3}]
  [{"a" 1, "b" 2} {"a" 1, "c" 3} {"b" 2, "c" 1} {"c" 3}]])

^{:refer xt.db.base-scope/filter-scope, :added "4.0"}
(fact
 "filter scopes from keys"
 ^{:hidden true}
 (!.py
  [(scope/filter-scope ["-/data" "id"])
   (scope/filter-scope ["-/data" "-/key"])
   (scope/filter-scope ["*/data" "-/key"])
   (scope/filter-scope ["*/everything"])])
 =>
 [{"-/data" true}
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

^{:refer xt.db.base-scope/filter-plain-key, :added "4.0"}
(fact
 "converts _id tags to standard keys"
 ^{:hidden true}
 (!.py
  [(scope/filter-plain-key "hello")
   (scope/filter-plain-key "hello_id")])
 =>
 ["hello" "hello"])

^{:refer xt.db.base-scope/filter-plain, :added "4.0"}
(fact
 "filter ids keys from scope keys"
 ^{:hidden true}
 (!.py (scope/filter-plain ["-/data" "id"]))
 =>
 {"id" true})

^{:refer xt.db.base-scope/get-data-columns,
  :added "4.0",
  :setup
  [(def
    +out+
    ["id"
     "account"
     "first_name"
     "last_name"
     "city"
     "state"
     "country"
     "about"
     "language"
     "detail"])]}
(fact
 "get columns for given keys"
 ^{:hidden true}
 (!.py
  (xtd/arr-map
   (scope/get-data-columns sample/Schema "UserAccount" ["*/data"])
   (fn:> [e] (. e ["ident"]))))
 =>
 ["id"
  "nickname"
  "password_updated"
  "is_super"
  "is_suspended"
  "is_official"]
 (!.py
  (xtd/arr-map
   (scope/get-data-columns sample/Schema "UserProfile" ["*/standard"])
   (fn:> [e] (. e ["ident"]))))
 =>
 +out+)

^{:refer xt.db.base-scope/get-link-standard, :added "4.0"}
(fact
 "classifies the link"
 ^{:hidden true}
 (!.py (scope/get-link-standard ["hello" ["hello"]]))
 =>
 ["hello" [{} ["hello"]]])

^{:refer xt.db.base-scope/get-query-tables, :added "4.0"}
(fact
 "get columns for given query"
 ^{:hidden true}
 (!.py
  (scope/get-query-tables
   sample/Schema
   "UserAccount"
   {:profile {}}
   {}))
 =>
 {"UserProfile" true, "UserAccount" true})

^{:refer xt.db.base-scope/get-link-columns, :added "4.0"}
(fact
 "get columns for given keys"
 ^{:hidden true}
 (!.py
  (xtd/arr-map
   (scope/get-link-columns
    sample/Schema
    "UserAccount"
    [["profile" ["first_name" "last_name"]]])
   (fn
    [e]
    (return [(xt/x:get-key (xtd/first e) "ident") (xtd/second e)]))))
 =>
 [["profile" [{} ["first_name" "last_name"]]]])

^{:refer xt.db.base-scope/get-linked-tables, :added "4.0"}
(fact
 "calculated linked tables given query"
 ^{:hidden true}
 (!.py
  (scope/get-linked-tables
   sample/Schema
   "UserAccount"
   [["profile"] ["wallets" [["entries" [["asset"]]]]]]))
 =>
 {"UserProfile" true,
  "Asset" true,
  "UserAccount" true,
  "WalletAsset" true,
  "Wallet" true})

^{:refer xt.db.base-scope/get-tree, :added "4.0"}
(fact
 "calculated linked tree given query"
 ^{:hidden true}
 (!.py
  (scope/get-tree
   sample/Schema
   "UserAccount"
   [{:id "zcaudate"} {:id "z1"} {:id "z3"}]
   [["wallets"
     {:id "W1"}
     {:id "W2"}
     {:id "W3"}
     [["entries" {:id "E1"} {:id "E2"} {:id "E3"}]]]]
   {}))
 =>
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

(comment
 (!.py (xt/x:get-path sample/Schema ["UserProfile" "account"]))
 (!.py
  (scope/get-tree
   sample/Schema
   "UserProfile"
   {:id "zcaudate"}
   ["account"]
   {}))
 (!.py
  (scope/get-tree
   sample/Schema
   "UserAccount"
   {:id "zcaudate"}
   [["profile" {:name "hello"}]]
   {}))
 (!.py
  (scope/get-tree
   sample/Schema
   "UserProfile"
   {:first-name "hello"}
   [["account" {:is-official true}]]
   {}))
 (!.py
  (scope/get-tree sample/Schema "UserAccount" {} [["profile"]] {}))
 (!.py
  (scope/get-tree
   sample/Schema
   "UserProfile"
   {:first-name "hello"}
   [["account" {:is-official true}]]
   {})))
