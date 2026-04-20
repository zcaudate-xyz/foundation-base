(ns
 xtbench.js.db.impl-sql-dart-test
 (:use code.test)
 (:require [rt.basic.type-common :as common] [std.lang :as l]))

(l/script- :js {:runtime :basic, :require []})

(def CANARY-DART (common/program-exists? "dart"))
