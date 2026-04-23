(ns
 xtbench.js.sample.sql-util-test
 (:require
  [std.lang :as l]
  [xt.lang.spec-base :as xt]
  [xt.db.sql-util :as ut])
 (:use code.test))

(l/script- :js {:runtime :basic})

(fact
 "encodes a value to sql"
 ^{:hidden true}
 (!.js (xt/x:json-encode 100000000000000000))
 (!.js (ut/encode-value 100000000000000000))
 =>
 "'100000000000000000'")
