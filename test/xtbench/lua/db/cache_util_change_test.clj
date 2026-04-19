(ns
 xtbench.lua.db.cache-util-change-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require
  [[xt.lang.common-repl :as repl]
   [xt.lang.common-data :as xtd]
   [xt.db.cache-util :as data]
   [xt.db.base-flatten :as f]
   [xt.db.sample-test :as sample]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})
