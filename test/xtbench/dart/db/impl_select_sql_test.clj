(ns
 xtbench.dart.db.impl-select-sql-test
 (:use code.test)
 (:require [std.lang :as l] [xt.lang.common-notify :as notify]))

(l/script- :dart {:runtime :twostep, :require []})
