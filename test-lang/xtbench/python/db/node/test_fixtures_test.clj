(ns xtbench.python.db.node.test-fixtures-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.db.node.test-fixtures :as fixtures]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.test-fixtures/InstallOpts :added "4.1"}
(fact "provides reusable schema, model, and seed fixtures"

  (!.py
   {"schema-id" (xtd/get-in fixtures/InstallOpts ["schema" "Order" "id" "ident"])
    "views" (xt/x:obj-keys (. fixtures/ModelSpec ["views"]))
    "seed-status" (xtd/get-in fixtures/Seed ["Order" 0 "status"])})
  => {"schema-id" "id"
      "views" ["main" "open"]
      "seed-status" "open"})
