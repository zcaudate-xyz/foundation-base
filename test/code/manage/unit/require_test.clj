(ns code.manage.unit.require-test
  (:require [code.manage.unit.require :as require]
            [code.test :refer [fact]]))

(fact "require-file works"
  (require/require-file 'code.manage.unit.require {} nil nil)
  => '[require-file])


^{:refer code.manage.unit.require/require-file :added "4.0"}
(fact "TODO")