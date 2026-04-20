(ns
 xtbench.lua.db.cache-pull-test
 (:require [net.http :as http] [std.json :as json] [std.lang :as l])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require
  [[xt.lang.common-repl :as repl]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-trace :as trace]
   [xt.db.cache-util :as data]
   [xt.db.cache-pull :as q]
   [xt.db.base-flatten :as f]
   [xt.db.sql-util :as ut]
   [xt.db.sample-test :as sample]]})

(def +flattened+ nil)

(def +flattened-full+ nil)

(fact:global
 {:setup
  [(l/rt:restart)
   (l/rt:scaffold :lua)
   (def
    +flattened+
    (!.lua (f/flatten sample/Schema "UserAccount" sample/RootUser {})))
   (def
    +flattened-full+
    (!.lua
     (f/flatten sample/Schema "UserAccount" sample/RootUserFull {})))],
  :teardown [(l/rt:stop)]})

^{:refer xt.db.cache-pull/pull.control, :adopt true, :added "4.0"}
(fact
 "gets a currency graph"
 ^{:hidden true}
 (!.lua
  (trace/trace-log-clear)
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull
   rows
   sample/Schema
   "Currency"
   {:returning ["id"], :limit 2, :offset 2, :order-by ["id"]}))
 =>
 [{"id" "XLM"} {"id" "XLM.T"}]
 (!.lua
  (trace/trace-log-clear)
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull
   rows
   sample/Schema
   "Currency"
   {:returning ["id"], :limit 2, :order-by ["id"]}))
 =>
 [{"id" "STATS"} {"id" "USD"}]
 (!.lua
  (trace/trace-log-clear)
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull
   rows
   sample/Schema
   "Currency"
   {:returning ["id"],
    :limit 2,
    :order-by ["id"],
    :order-sort "desc"}))
 =>
 [{"id" "XLM.T"} {"id" "XLM"}])

^{:refer xt.db.cache-pull/pull.currency, :adopt true, :added "4.0"}
(fact
 "gets a currency graph"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull
   rows
   sample/Schema
   "Wallet"
   {:returning [["entries" [["asset" ["-/data" ["currency"]]]]]]}))
 =>
 vector?)

^{:refer xt.db.cache-pull/pull.currency, :adopt true, :added "4.0"}
(fact
 "gets a currency graph"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull
   rows
   sample/Schema
   "UserAccount"
   {:returning
    ["id"
     "nickname"
     ["profile" ["first_name" "last_name"]]
     ["emails"]
     ["wallets" [["entries" [["asset" ["-/data" ["currency"]]]]]]]]}))
 =>
 (contains-in
  [{"nickname" "root",
    "profile" [{"last_name" "User", "first_name" "Root"}],
    "id" "00000000-0000-0000-0000-000000000000",
    "wallets"
    [{"entries"
      (contains
       [{"asset" [{"currency" [{"id" "XLM"}]}]}
        {"asset" [{"currency" [{"id" "STATS"}]}]}
        {"asset" [{"currency" [{"id" "USD"}]}]}
        {"asset" [{"currency" [{"id" "XLM.T"}]}]}]
       :in-any-order)}]}]))

^{:refer xt.db.cache-pull/check-like-clause, :added "4.0"}
(fact
 "emulates the sql `like` clause"
 ^{:hidden true}
 (!.lua (q/check-like-clause "abc" "a%"))
 =>
 true)

^{:refer xt.db.cache-pull/check-clause-value, :added "4.0"}
(fact
 "checks the clause within a record"
 ^{:hidden true}
 (!.lua
  (q/check-clause-value {:data {:name "abc"}} "data" "name" "abc"))
 =>
 true)

^{:refer xt.db.cache-pull/check-clause-function, :added "4.0"}
(fact
 "checks the clause for a function within a record"
 ^{:hidden true}
 (!.lua
  (q/check-clause-function
   {:data {:name "abc"}}
   "data"
   "name"
   q/check-like-clause
   ["a%"]))
 =>
 true)

^{:refer xt.db.cache-pull/pull-where-clause, :added "4.0"}
(fact
 "pull where clause"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull-where-clause
   rows
   sample/Schema
   "UserAccount"
   (xtd/get-in
    rows
    ["UserAccount" "00000000-0000-0000-0000-000000000000" "record"])
   q/pull-where
   "id"
   (fn:> [x] true)))
 =>
 true)

^{:refer xt.db.cache-pull/pull-where, :added "4.0"}
(fact
 "clause for where construct"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  [(q/pull-where
    rows
    sample/Schema
    "UserAccount"
    (fn:> [record table-key] true)
    {})
   (q/pull-where
    rows
    sample/Schema
    "UserAccount"
    {:nickname "hello"}
    {})
   (q/pull-where
    rows
    sample/Schema
    "UserAccount"
    {:nickname "hello"}
    {:data {:nickname "hello"}})])
 =>
 [true false true])

^{:refer xt.db.cache-pull/pull-return-clause, :added "4.0"}
(fact
 "pull return clause"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull-return-clause
   rows
   sample/Schema
   (xtd/get-in
    rows
    ["UserAccount" "00000000-0000-0000-0000-000000000000" "record"])
   q/pull-where
   q/pull-return
   {"ident" "profile",
    "type" "ref",
    "ref"
    {"key" "_account",
     "rkey" "account",
     "type" "reverse",
     "rident" "account",
     "rval" "account",
     "ns" "UserProfile",
     "val" "profile"},
    "cardinality" "many"}
   [{} ["*/data"]]))
 =>
 ["profile"
  [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
    "last_name" "User",
    "first_name" "Root",
    "language" "en"}]]
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull-return-clause
   rows
   sample/Schema
   (xtd/get-in
    rows
    ["UserAccount" "00000000-0000-0000-0000-000000000000" "record"])
   q/pull-where
   q/pull-return
   {"ident" "profile",
    "type" "ref",
    "ref"
    {"key" "_account",
     "rkey" "account",
     "type" "reverse",
     "rident" "account",
     "rval" "account",
     "ns" "UserProfile",
     "val" "profile"},
    "cardinality" "many"}
   [{:id "missing"} ["*/data"]]))
 =>
 ["profile" nil])

^{:refer xt.db.cache-pull/pull-return-clause, :added "4.0"}
(fact
 "pull return clause python profile match"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull-return-clause
   rows
   sample/Schema
   (xtd/get-in
    rows
    ["UserAccount" "00000000-0000-0000-0000-000000000000" "record"])
   q/pull-where
   q/pull-return
   {"ident" "profile",
    "type" "ref",
    "ref"
    {"key" "_account",
     "rkey" "account",
     "type" "reverse",
     "rident" "account",
     "rval" "account",
     "ns" "UserProfile",
     "val" "profile"},
    "cardinality" "many"}
   [{} ["*/data"]]))
 =>
 ["profile"
  [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
    "last_name" "User",
    "first_name" "Root",
    "language" "en"}]])

^{:refer xt.db.cache-pull/pull-return-clause, :added "4.0"}
(fact
 "pull return clause python profile miss"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull-return-clause
   rows
   sample/Schema
   (xtd/get-in
    rows
    ["UserAccount" "00000000-0000-0000-0000-000000000000" "record"])
   q/pull-where
   q/pull-return
   {"ident" "profile",
    "type" "ref",
    "ref"
    {"key" "_account",
     "rkey" "account",
     "type" "reverse",
     "rident" "account",
     "rval" "account",
     "ns" "UserProfile",
     "val" "profile"},
    "cardinality" "many"}
   [{:id "missing"} ["*/data"]]))
 =>
 ["profile" nil])

^{:refer xt.db.cache-pull/pull-return, :added "4.0"}
(fact
 "return construct"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (q/pull-return
   rows
   sample/Schema
   "UserAccount"
   ["id" "nickname" ["profile" ["first_name" "last_name"]]]
   (xtd/get-in
    rows
    ["UserAccount" "00000000-0000-0000-0000-000000000000" "record"])))
 =>
 {"nickname" "root",
  "profile" [{"last_name" "User", "first_name" "Root"}],
  "id" "00000000-0000-0000-0000-000000000000"})

^{:refer xt.db.cache-pull/pull, :added "4.0"}
(fact
 "pull data from database"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened+) nil)
  [(q/pull
    rows
    sample/Schema
    "UserAccount"
    {:returning
     ["id" "nickname" ["profile" ["first_name" "last_name"]]]})
   (q/pull
    rows
    sample/Schema
    "UserAccount"
    {:returning ["id" "nickname" ["profile" ["*/data"]]]})])
 =>
 (contains-in
  [[{"nickname" "root",
     "profile" [{"last_name" "User", "first_name" "Root"}],
     "id" "00000000-0000-0000-0000-000000000000"}]
   [{"nickname" "root",
     "profile"
     [(contains
       {"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
        "last_name" "User",
        "first_name" "Root",
        "language" "en"})],
     "id" "00000000-0000-0000-0000-000000000000"}]]))

^{:refer xt.db.cache-pull/pull, :added "4.0"}
(fact
 "pull data from database python profile match"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened+) nil)
  (q/pull
   rows
   sample/Schema
   "UserAccount"
   {:returning
    ["id" "nickname" ["profile" ["first_name" "last_name"]]]}))
 =>
 [{"nickname" "root",
   "profile" [{"last_name" "User", "first_name" "Root"}],
   "id" "00000000-0000-0000-0000-000000000000"}])

^{:refer xt.db.cache-pull/pull, :added "4.0"}
(fact
 "pull data from database python profile full"
 ^{:hidden true}
 (!.lua
  (var rows {})
  (data/merge-bulk rows (@! +flattened+) nil)
  (q/pull
   rows
   sample/Schema
   "UserAccount"
   {:returning ["id" "nickname" ["profile" ["*/data"]]]}))
 =>
 [{"nickname" "root",
   "profile"
   [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
     "last_name" "User",
     "first_name" "Root",
     "language" "en"}],
   "id" "00000000-0000-0000-0000-000000000000"}])

(comment
 (q/pull
  rows
  sample/Schema
  "UserAccount"
  {:returning
   ["id"
    "nickname"
    ["profile" ["first_name" "last_name"]]
    ["emails"]
    ["wallets" [["entries" [["asset" ["-/data" ["currency"]]]]]]]]})
 =>
 [{"nickname" "root",
   "profile" [{"last_name" "User", "first_name" "Root"}],
   "id" "00000000-0000-0000-0000-000000000000",
   "wallets"
   [{"entries"
     [{"asset"
       [{"balance" 10000000,
         "currency" [{"id" "STATS"}],
         "time_updated" 1630408722786926,
         "escrow" 0,
         "time_created" 1630408722786926}]}
      {"asset"
       [{"balance" 0,
         "currency" [{"id" "USD"}],
         "time_updated" 1630408722786926,
         "escrow" 0,
         "time_created" 1630408722786926}]}
      {"asset"
       [{"balance" 0,
         "currency" [{"id" "XLM.T"}],
         "time_updated" 1630408722786926,
         "escrow" 0,
         "time_created" 1630408722786926}]}
      {"asset"
       [{"balance" 0,
         "currency" [{"id" "XLM"}],
         "time_updated" 1630408722786926,
         "escrow" 0,
         "time_created" 1630408722786926}]}]}],
   "emails"
   [{"time_updated" 1630408722786926,
     "is_active" true,
     "is_verified" true,
     "id" "db0898be-4630-43f5-96f3-fac1663267c8",
     "is_public" false,
     "value" "root@statstrade.io",
     "is_primary" true,
     "time_created" 1630408722786926}]}]
 (comment
  (q/pull
   rows
   sample/Schema
   "Organisation"
   {:where {:owner {:nickname "root"}}, :returning ["id"]})))
