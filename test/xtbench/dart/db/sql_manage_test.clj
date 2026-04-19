(ns
 xtbench.dart.db.sql-manage-test
 (:require
  [std.lang :as l]
  [std.string.prose :as prose]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.db.base-schema :as sch]
   [xt.lang.common-spec :as xt]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-string :as str]
   [xt.db.sql-util :as ut]
   [xt.db.sql-manage :as manage]
   [xt.db.sample-test :as sample]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})
