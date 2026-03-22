(ns std.protocol.dispatch-test
  (:require [std.protocol.dispatch :refer :all])
  (:use code.test))

^{:refer std.protocol.dispatch/-create :added "3.0"}
(fact "creates an executor")
