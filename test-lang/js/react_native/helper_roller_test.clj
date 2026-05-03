(ns js.react-native.helper-roller-test
  (:require [js.react-native.helper-roller :refer :all])
  (:use code.test))

^{:refer js.react-native.helper-roller/useRoller :added "4.0" :unchecked true}
(fact "roller model for slider and spinner")
