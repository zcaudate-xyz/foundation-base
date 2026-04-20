(ns
 xtbench.python.db.base-view-test
 (:require
  [rt.postgres :as pg]
  [std.lang :as l]
  [xt.db.gen-bind :as bind]
  [xt.db.sample-data-test :as data]
  [xt.db.sample-user-test :as user])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.lang.common-data :as xtd]
   [xt.db.base-view :as v]
   [xt.db.base-util :as ut]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

(def
 +views+
 (mapv
  (comp pg/bind-view deref resolve second)
  (concat
   (pg/list-view 'xt.db.sample-data-test :select)
   (pg/list-view 'xt.db.sample-data-test :return)
   (pg/list-view 'xt.db.sample-user-test :select)
   (pg/list-view 'xt.db.sample-user-test :return))))

^{:refer xt.db.base-view/all-overview, :added "4.0"}
(fact
 "gets an overview of the views"
 ^{:hidden true}
 (!.py (v/all-overview (ut/collect-views (@! +views+))))
 =>
 {"RegionCity"
  {"return" ["with_access" "default" "info"],
   "select" ["by_country" "by_state"]},
  "Organisation"
  {"return" ["view_membership" "view_default"],
   "select" ["by_name" "all_as_owner" "all_as_admin" "all_as_member"]},
  "RegionState" {"return" ["default" "info"], "select" ["by_country"]},
  "UserAccount" {"return" ["info"], "select" ["by_organisation"]},
  "Currency"
  {"return" ["default" "info"],
   "select" ["all" "all_fiat" "all_crypto" "by_type" "by_country"]},
  "RegionCountry"
  {"return" ["with_access" "default" "info"],
   "select" ["by_name" "all"]}})

^{:refer xt.db.base-view/all-keys, :added "4.0"}
(fact
 "gets all table keys for a view"
 ^{:hidden true}
 (set
  (!.py
   (v/all-keys (ut/collect-views (@! +views+)) "Currency" "select")))
 =>
 #{"by_country" "all_fiat" "all" "all_crypto" "by_type"})

^{:refer xt.db.base-view/all-methods,
  :added "4.0",
  :setup
  [(def
    +methods+
    '(["Currency" "return" "default"]
      ["Currency" "return" "info"]
      ["Currency" "select" "all"]
      ["Currency" "select" "all_crypto"]
      ["Currency" "select" "all_fiat"]
      ["Currency" "select" "by_country"]
      ["Currency" "select" "by_type"]
      ["Organisation" "return" "view_default"]
      ["Organisation" "return" "view_membership"]
      ["Organisation" "select" "all_as_admin"]
      ["Organisation" "select" "all_as_member"]
      ["Organisation" "select" "all_as_owner"]
      ["Organisation" "select" "by_name"]
      ["RegionCity" "return" "default"]
      ["RegionCity" "return" "info"]
      ["RegionCity" "return" "with_access"]
      ["RegionCity" "select" "by_country"]
      ["RegionCity" "select" "by_state"]
      ["RegionCountry" "return" "default"]
      ["RegionCountry" "return" "info"]
      ["RegionCountry" "return" "with_access"]
      ["RegionCountry" "select" "all"]
      ["RegionCountry" "select" "by_name"]
      ["RegionState" "return" "default"]
      ["RegionState" "return" "info"]
      ["RegionState" "select" "by_country"]
      ["UserAccount" "return" "info"]
      ["UserAccount" "select" "by_organisation"]))]}
(fact
 "gets all methods for views"
 ^{:hidden true}
 (sort (!.py (v/all-methods (ut/collect-views (@! +views+)))))
 =>
 +methods+)
