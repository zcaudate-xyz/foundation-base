(ns js.react-native.helper-mobile-test
  (:use code.test)
  (:require [js.react-native.helper-mobile :refer :all]))

^{:refer js.react-native.helper-mobile/isMobile :added "4.0" :unchecked true}
(fact "checks if agent is browser")
