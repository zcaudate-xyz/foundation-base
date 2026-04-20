(ns
 xtbench.lua.db.impl-sql-dart-test
 (:use code.test)
 (:require [rt.basic.type-common :as common] [std.lang :as l]))

(l/script- :lua {:runtime :basic, :require []})

(def CANARY-DART (common/program-exists? "dart"))
