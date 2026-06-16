(ns js.cell.runtime.browser-test
  (:use code.test)
  (:require [js.cell.runtime.browser :refer :all]))

^{:refer js.cell.runtime.browser/make-webworker-cell :added "4.1"}
(fact "TODO")

^{:refer js.cell.runtime.browser/make-sharedworker-cell :added "4.1"}
(fact "TODO")