(ns std.protocol.state-test
  (:require [std.protocol.state :refer :all])
  (:use code.test))

^{:refer std.protocol.state/-create-state :added "3.0"}
(fact "creates a state object")

^{:refer std.protocol.state/-container-state :added "3.0"}
(fact "returns a type for a label")
