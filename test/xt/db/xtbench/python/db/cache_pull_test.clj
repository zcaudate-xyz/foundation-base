(ns
 xtbench.python.db.cache-pull-test
 (:require [net.http :as http] [std.json :as json] [std.lang :as l])
 (:use code.test))

(l/script-
 :python
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
   (l/rt:scaffold :python)
   (def
    +flattened+
    (!.py (f/flatten sample/Schema "UserAccount" sample/RootUser {})))
   (def
    +flattened-full+
    (!.py
     (f/flatten sample/Schema "UserAccount" sample/RootUserFull {})))],
  :teardown [(l/rt:stop)]})

^{:refer xt.db.cache-pull/pull.control, :adopt true, :added "4.0"}
(fact
 "gets a currency graph"
 ^{:hidden true}
 (!.py
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
 (!.py
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
 (!.py
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
 (!.py
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (return
   (q/pull
    rows
    sample/Schema
    "Wallet"
    {:returning [["entries" [["asset" ["-/data" ["currency"]]]]]]})))
 =>
 [{"entries"
   [{"asset" [{"currency" [{"id" "STATS"}]}]}
    {"asset" [{"currency" [{"id" "USD"}]}]}
    {"asset" [{"currency" [{"id" "XLM.T"}]}]}
    {"asset" [{"currency" [{"id" "XLM"}]}]}]}])

^{:refer xt.db.cache-pull/pull.currency, :adopt true, :added "4.0"}
(fact
 "gets a currency graph"
 ^{:hidden true}
 (!.py
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  (var
   out
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
  [(. out [0] ["id"])
   (. out [0] ["nickname"])
   (. out [0] ["profile"] [0] ["first_name"])
   (. out [0] ["profile"] [0] ["last_name"])
   (xtd/arr-sort
    (xtd/arr-map
     (. out [0] ["wallets"] [0] ["entries"])
     (fn
      [entry]
      (return (. entry ["asset"] [0] ["currency"] [0] ["id"]))))
    (fn [x] (return x))
    (fn [a b] (return (< a b))))])
 =>
 ["00000000-0000-0000-0000-000000000000"
  "root"
  "Root"
  "User"
  ["STATS" "USD" "XLM" "XLM.T"]])

^{:refer xt.db.cache-pull/check-like-clause, :added "4.0"}
(fact
 "emulates the sql `like` clause"
 ^{:hidden true}
 (!.py (q/check-like-clause "abc" "a%"))
 =>
 true)

^{:refer xt.db.cache-pull/check-clause-value, :added "4.0"}
(fact
 "checks the clause within a record"
 ^{:hidden true}
 (!.py
  (q/check-clause-value {:data {:name "abc"}} "data" "name" "abc"))
 =>
 true)

^{:refer xt.db.cache-pull/check-clause-function, :added "4.0"}
(fact
 "checks the clause for a function within a record"
 ^{:hidden true}
 (!.py
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
 (!.py
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
 (!.py
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
 (!.py
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
    "city" nil,
    "about" nil,
    "last_name" "User",
    "first_name" "Root",
    "language" "en"}]]
 (!.py
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
 (!.py
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
    "city" nil,
    "about" nil,
    "last_name" "User",
    "first_name" "Root",
    "language" "en"}]])

^{:refer xt.db.cache-pull/pull-return-clause, :added "4.0"}
(fact
 "pull return clause python profile miss"
 ^{:hidden true}
 (!.py
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
 (!.py
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
 (!.py
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
 [[{"nickname" "root",
    "profile" [{"last_name" "User", "first_name" "Root"}],
    "id" "00000000-0000-0000-0000-000000000000"}]
  [{"nickname" "root",
    "profile"
    [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
      "city" nil,
      "about" nil,
      "last_name" "User",
      "first_name" "Root",
      "language" "en"}],
    "id" "00000000-0000-0000-0000-000000000000"}]])

^{:refer xt.db.cache-pull/pull, :added "4.0"}
(fact
 "pull data from database python profile match"
 ^{:hidden true}
 (!.py
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
 (!.py
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
     "city" nil,
     "about" nil,
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
