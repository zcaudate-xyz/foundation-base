(ns std.protocol.deps-test
  (:require [std.protocol.deps :refer :all])
  (:use code.test))

^{:refer std.protocol.deps/-create :added "3.0"}
(fact "creates a context")
