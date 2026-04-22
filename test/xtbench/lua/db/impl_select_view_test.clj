(ns
 xtbench.lua.db.impl-select-view-test
 (:use code.test)
 (:require
  [std.lang :as l]
  [std.string.prose :as prose]
  [xt.lang.common-notify :as notify]))

(l/script- :lua {:runtime :basic, :require []})
