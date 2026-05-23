(ns xt.db.helpers.test-fixtures-test
  (:require [xt.db.helpers.test-fixtures :as fixtures])
  (:use code.test))

(fact "provides the shared task and postgres fixture roots"
  [(keys fixtures/+schema+)
   (keys fixtures/+lookup+)
   (keys fixtures/+task-tree+)
   (keys fixtures/+model-spec+)]
  => [["Entry"]
      ["Entry"]
      ["Task"]
      ["views"]])
