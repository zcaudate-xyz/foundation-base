(ns
 xtbench.dart.db.base-scope-test
 (:require [std.lang :as l])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.lang.spec-base :as xt]
   [xt.lang.common-data :as xtd]
   [xt.db.base-scope :as scope]
   [xt.db.sample-test :as sample]
   [xt.db.sql-util :as ut]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

(comment
 (!.dt (xt/x:get-path sample/Schema ["UserProfile" "account"]))
 (!.dt
  (scope/get-tree
   sample/Schema
   "UserProfile"
   {:id "zcaudate"}
   ["account"]
   {}))
 (!.dt
  (scope/get-tree
   sample/Schema
   "UserAccount"
   {:id "zcaudate"}
   [["profile" {:name "hello"}]]
   {}))
 (!.dt
  (scope/get-tree
   sample/Schema
   "UserProfile"
   {:first-name "hello"}
   [["account" {:is-official true}]]
   {}))
 (!.dt
  (scope/get-tree sample/Schema "UserAccount" {} [["profile"]] {}))
 (!.dt
  (scope/get-tree
   sample/Schema
   "UserProfile"
   {:first-name "hello"}
   [["account" {:is-official true}]]
   {})))
