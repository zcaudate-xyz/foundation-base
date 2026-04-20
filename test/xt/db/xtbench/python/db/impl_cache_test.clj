(ns
 xtbench.python.db.impl-cache-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.db.impl-cache :as impl-cache]
   [xt.lang.common-lib :as k]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-repl :as repl]
   [xt.db.base-flatten :as f]
   [xt.db.cache-util :as ut]
   [xt.db.sample-test :as sample]]})

(fact:global
 {:setup
  [(l/rt:restart)
   true
   true
   (do
    (!.py (:= (!:G INSTANCE) {:rows {}}))
    (!.py (:= (!:G INSTANCE) {:rows {}})))],
  :teardown [(l/rt:stop)]})

^{:refer xt.db.impl-cache/cache-process-event-remove.lua,
  :adopt true,
  :added "4.0",
  :setup
  [(!.py
    (impl-cache/cache-process-event-sync
     INSTANCE
     "add"
     {"UserAccount" [sample/RootUser]}
     sample/Schema
     sample/SchemaLookup
     nil))]}
(fact
 "removes data from database"
 ^{:hidden true}
 (!.py
  (impl-cache/cache-pull-sync
   INSTANCE
   sample/Schema
   ["UserAccount" ["nickname" ["profile" ["first_name"]]]]
   nil))
 =>
 [{"nickname" "root", "profile" [{"first_name" "Root"}]}]
 (!.py
  (impl-cache/cache-process-event-remove
   INSTANCE
   "input"
   {"UserAccount" [sample/RootUser]}
   sample/Schema
   sample/SchemaLookup
   nil))
 =>
 [["UserAccount" ["00000000-0000-0000-0000-000000000000"]]
  ["UserProfile" ["c4643895-b0ce-44cc-b07b-2386bf18d43b"]]]
 (sort
  (!.py
   (impl-cache/cache-process-event-remove
    INSTANCE
    "remove"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil)))
 =>
 ["UserAccount" "UserProfile"]
 (!.py
  (impl-cache/cache-pull-sync
   INSTANCE
   sample/Schema
   ["UserAccount" ["nickname" ["profile" ["first_name"]]]]
   nil))
 =>
 empty?)

^{:refer xt.db.impl-cache/cache-process-event-remove,
  :added "4.0",
  :setup
  [(!.py
    (xtd/arr-sort
     (impl-cache/cache-process-event-sync
      INSTANCE
      "add"
      {"UserAccount" [sample/RootUser]}
      sample/Schema
      sample/SchemaLookup
      nil)
     k/identity
     k/lt))]}
(fact
 "removes data from database"
 ^{:hidden true}
 (!.py
  (impl-cache/cache-pull-sync
   INSTANCE
   sample/Schema
   ["UserAccount" ["nickname" ["profile" ["first_name"]]]]
   nil))
 =>
 [{"nickname" "root", "profile" [{"first_name" "Root"}]}]
 (!.py
  (impl-cache/cache-process-event-remove
   INSTANCE
   "input"
   {"UserAccount" [sample/RootUser]}
   sample/Schema
   sample/SchemaLookup
   nil))
 =>
 [["UserAccount" ["00000000-0000-0000-0000-000000000000"]]
  ["UserProfile" ["c4643895-b0ce-44cc-b07b-2386bf18d43b"]]]
 (sort
  (!.py
   (impl-cache/cache-process-event-remove
    INSTANCE
    "remove"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil)))
 =>
 ["UserAccount" "UserProfile"]
 (!.py
  (impl-cache/cache-pull-sync
   INSTANCE
   sample/Schema
   ["UserAccount" ["nickname" ["profile" ["first_name"]]]]
   nil))
 =>
 empty?)

^{:refer xt.db.impl-cache/cache-pull-sync,
  :added "4.0",
  :setup
  [(def
    +account+
    (contains-in
     [{"is_official" false,
       "nickname" "root",
       "profile"
       [{"id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
         "last_name" "User",
         "first_name" "Root",
         "language" "en"}],
       "id" "00000000-0000-0000-0000-000000000000",
       "is_suspended" false,
       "password_updated" number?,
       "is_super" true}]))]}
(fact
 "runs a pull statement"
 ^{:hidden true}
 [(set
   (!.py
    (impl-cache/cache-process-event-sync
     INSTANCE
     "add"
     {"Currency" (@! sample/+currency+)}
     sample/Schema
     sample/SchemaLookup
     nil)
    (impl-cache/cache-pull-sync
     INSTANCE
     sample/Schema
     ["Currency" ["id"]]
     nil)))
  (!.py
   (impl-cache/cache-process-event-sync
    INSTANCE
    "add"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    nil)
   (impl-cache/cache-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount" ["*/data" ["profile"]]]
    nil))]
 =>
 (contains
  [#{{"id" "USD"} {"id" "XLM.T"} {"id" "STATS"} {"id" "XLM"}}
   +account+]))

{:refer xt.db.impl-cache/cache-pull-sync, :added "4.0"}

(fact
 "sample of different types of pull"
 ^{:hidden true}
 (!.py
  (impl-cache/cache-pull-sync
   INSTANCE
   sample/Schema
   ["Currency" {:id ["like" "ST%"]} ["id"]]
   nil))
 =>
 [{"id" "STATS"}]
 (!.py
  (impl-cache/cache-pull-sync
   INSTANCE
   sample/Schema
   ["Currency" {:symbol "XLM", :id "XLM", :decimal ["lt" 0]} ["id"]]
   nil))
 =>
 [{"id" "XLM"}]
 (!.py
  (impl-cache/cache-pull-sync
   INSTANCE
   sample/Schema
   ["UserAccount"
    {:profile "c4643895-b0ce-44cc-b07b-2386bf18d43b"}
    ["id"]]
   nil))
 =>
 [{"id" "00000000-0000-0000-0000-000000000000"}]
 (!.py
  (impl-cache/cache-pull-sync
   INSTANCE
   sample/Schema
   ["UserAccount"
    {:profile "c4643895-b0ce-44cc-b07b-2386bf18d43b"}
    ["*/data" ["profile" ["first_name"]]]]
   nil))
 =>
 [{"is_official" false,
   "nickname" "root",
   "profile" [{"first_name" "Root"}],
   "id" "00000000-0000-0000-0000-000000000000",
   "is_suspended" false,
   "password_updated" 1630408723423619,
   "is_super" true}])
