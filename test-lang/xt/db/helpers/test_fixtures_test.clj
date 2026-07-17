(ns xt.db.helpers.test-fixtures-test
  (:use code.test)
  (:require [xt.db.helpers.test-fixtures :as fixtures]))

^{:refer xt.db.helpers.test-fixtures/+schema+ :added "4.1"}
(fact "exposes shared node and postgres fixtures"
  [(keys fixtures/+schema+)
   (keys fixtures/+lookup+)
   (keys fixtures/+task-tree+)]
  => [["Entry"]
      ["Entry"]
      ["Task"]])


^{:refer xt.db.helpers.test-fixtures/seed-entry-rows :added "4.1"}
(fact "TODO")