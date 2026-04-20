(ns
 xtbench.python.db.cache-pull-regression-test
 (:use code.test)
 (:require [std.lang :as l]))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.lang.common-data :as xtd]
   [xt.db.cache-util :as data]
   [xt.db.cache-pull :as q]
   [xt.db.base-flatten :as f]
   [xt.db.sample-test :as sample]]})

(def +flattened-full+ nil)

(fact:global
 {:setup
  [(l/rt:restart)
   (l/rt:scaffold :python)
   (def
    +flattened-full+
    (!.py
     (f/flatten sample/Schema "UserAccount" sample/RootUserFull {})))],
  :teardown [(l/rt:stop)]})

^{:refer xt.db.cache-util/merge-bulk, :added "4.0"}
(fact
 "merges the full cache fixture in isolation"
 ^{:hidden true}
 (!.py (var rows {}) (data/merge-bulk rows (@! +flattened-full+) nil))
 =>
 map?)

^{:refer xt.db.cache-pull/pull-return-clause, :added "4.0"}
(fact
 "pull-return-clause works for the profile data path"
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
 "pull-return-clause missing-profile path does not reproduce in minimal isolation"
 ^{:hidden true}
 (!.py
  (try
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
    [{:id "missing"} ["*/data"]])
   (return "NO_ERROR")
   (catch Exception (return "ERROR"))))
 =>
 "NO_ERROR")

^{:refer xt.db.cache-util/merge-bulk, :added "4.0"}
(fact
 "reusing the same flattened fixture across python calls stays valid"
 ^{:hidden true}
 (!.py
  (var rows-a {})
  (var rows-b {})
  (var fdata (@! +flattened-full+))
  (data/merge-bulk rows-a fdata nil)
  (data/merge-bulk rows-b fdata nil)
  "OK")
 =>
 "OK")

^{:refer xt.db.cache-pull/pull-return-clause, :added "4.0"}
(fact
 "running the successful profile clause before the missing-profile clause stays valid"
 ^{:hidden true}
 (!.py
  (try
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
    [{} ["*/data"]])
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
    [{:id "missing"} ["*/data"]])
   (return "NO_ERROR")
   (catch Exception (return "ERROR"))))
 =>
 "NO_ERROR")

^{:refer xt.db.cache-util/merge-bulk, :added "4.0"}
(fact
 "two standalone python evaluations can reuse the full fixture"
 ^{:hidden true}
 (!.py
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  "OK")
 =>
 "OK"
 (!.py
  (var rows {})
  (data/merge-bulk rows (@! +flattened-full+) nil)
  "OK")
 =>
 "OK")
