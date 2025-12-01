(ns js.react-native.helper-debug-test
  (:use code.test)
  (:require [js.react-native.helper-debug :refer :all]))

^{:refer js.react-native.helper-debug/create-client :added "4.0" :unchecked true}
(fact "creates the debug client ws")

^{:refer js.react-native.helper-debug/DebugClient :added "4.0" :unchecked true}
(fact "creates the debug client ui")
