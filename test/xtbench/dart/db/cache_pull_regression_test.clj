(ns
 xtbench.dart.db.cache-pull-regression-test
 (:use code.test)
 (:require [std.lang :as l]))

(l/script-
 :dart
 {:runtime :twostep,
  :require [[xt.db.base-flatten :as f] [xt.db.sample-test :as sample]]})

(def +flattened-full+ nil)

(fact:global
 {:setup
  [(l/rt:restart)
   (def
    +flattened-full+
    (!.dt
     (f/flatten sample/Schema "UserAccount" sample/RootUserFull {})))],
  :teardown [(l/rt:stop)]})
