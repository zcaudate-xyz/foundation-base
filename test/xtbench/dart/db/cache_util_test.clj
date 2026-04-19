(ns
 xtbench.dart.db.cache-util-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.lang.common-repl :as repl]
   [xt.lang.common-lib :as k]
   [xt.lang.common-data :as xtd]
   [xt.db.cache-util :as data]
   [xt.db.base-flatten :as f]
   [xt.db.sample-test :as sample]]})

(fact:global
 {:setup
  [(l/rt:restart)
   (def
    +flattened+
    (!.dt (f/flatten sample/Schema "UserAccount" sample/RootUser {})))
   (def
    +flattened-full+
    (!.dt
     (f/flatten sample/Schema "UserAccount" sample/RootUserFull {})))],
  :teardown [(l/rt:stop)]})

(comment (./create-tests) (./import))
