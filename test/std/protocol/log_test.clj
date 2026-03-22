(ns std.protocol.log-test
  (:require [std.protocol.log :refer :all])
  (:use code.test))

^{:refer std.protocol.log/-create :added "3.0"}
(fact "creates a logger")
