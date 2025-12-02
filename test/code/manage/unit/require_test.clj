(ns code.manage.unit.require-test
  (:use code.test)
  (:require [code.manage.unit.require :as require]
            [code.project :as project]))

^{:refer code.manage.unit.require/require-file :added "3.0"}
(fact "requires the file and returns public vars"

  (require/require-file 'code.manage.unit.require {} nil nil)
  => (contains '[require-file]))
