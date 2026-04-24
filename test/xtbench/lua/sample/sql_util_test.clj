(ns
 xtbench.lua.sample.sql-util-test
 (:require
  [std.lang :as l]
  [xt.lang.spec-base :as xt]
  [xt.db.sql-util :as ut])
 (:use code.test))

(l/script- :lua {:runtime :basic})

(fact
 "encodes a value to sql"
 (!.lua (string.format "%0.f" 100000000000000000))
 (!.lua (ut/encode-value 100000000000000000))
 =>
 "'100000000000000000'")
