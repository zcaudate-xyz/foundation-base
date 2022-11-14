(ns code.test.base.print-test
  (:use code.test)
  (:require [code.test.base.print :refer :all]))

^{:refer code.test.base.print/print-success :added "3.0"}
(comment "outputs the description for a successful test")

^{:refer code.test.base.print/print-failure :added "3.0"}
(comment "outputs the description for a failed test")

^{:refer code.test.base.print/print-thrown :added "3.0"}
(comment "outputs the description for a form that throws an exception")

^{:refer code.test.base.print/print-fact :added "3.0"}
(comment "outputs the description for a fact form that contains many statements")

^{:refer code.test.base.print/print-summary :added "3.0"}
(comment "outputs the description for an entire test run")

(comment
  (code.manage/import))
