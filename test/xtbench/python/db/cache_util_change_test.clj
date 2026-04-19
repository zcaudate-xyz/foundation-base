(ns
 xtbench.python.db.cache-util-change-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.lang.common-repl :as repl]
   [xt.lang.common-data :as xtd]
   [xt.db.cache-util :as data]
   [xt.db.base-flatten :as f]
   [xt.db.sample-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart) (l/rt:scaffold :js)], :teardown [(l/rt:stop)]})

^{:refer xt.db.cache-util/add-single-link,
  :adopt true,
  :added "4.0",
  :setup
  [(def
    +account+
    (!.js
     (f/flatten
      sample/Schema
      "UserAccount"
      (xtd/obj-omit sample/RootUser ["emails" "profile"])
      {})))
   (def
    +profile+
    (dissoc
     (!.js (f/flatten sample/Schema "UserAccount" sample/RootUser {}))
     "UserAccount"))]}
(fact
 "adds single link"
 ^{:hidden true}
 (!.py
  (var rows {})
  (data/merge-bulk rows (@! +profile+) nil)
  (data/add-single-link
   rows
   sample/Schema
   "UserProfile"
   "c4643895-b0ce-44cc-b07b-2386bf18d43b"
   "country"
   "US")
  (data/add-single-link
   rows
   sample/Schema
   "UserProfile"
   "c4643895-b0ce-44cc-b07b-2386bf18d43b"
   "country"
   "UK")
  (data/merge-bulk rows (@! +account+) nil)
  (data/add-single-link
   rows
   sample/Schema
   "UserProfile"
   "c4643895-b0ce-44cc-b07b-2386bf18d43b"
   "account"
   "00000000-0000-0000-0000-000000000000")
  rows)
 =>
 (contains-in
  {"UserProfile"
   {"c4643895-b0ce-44cc-b07b-2386bf18d43b"
    {"record"
     {"ref_links"
      {"country" {"UK" true},
       "account" {"00000000-0000-0000-0000-000000000000" true}},
      "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
      "rev_links" {},
      "data"
      {"detail" {"hello" "world"},
       "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
       "last_name" "User",
       "first_name" "Root",
       "language" "en"}}}},
   "UserAccount"
   {"00000000-0000-0000-0000-000000000000"
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
       "password_updated" 1630408723423619,
       "is_super" true}}}}}))
