(ns code.manage.unit.require-test
  (:require [code.manage.unit.require :as require]
            [code.test :refer [fact]]))

(fact "require-file works"
  (require/require-file 'code.manage.unit.require {} nil nil)
  => '[require-file])
