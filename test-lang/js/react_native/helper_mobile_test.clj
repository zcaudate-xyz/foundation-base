(ns js.react-native.helper-mobile-test
  (:require [js.react-native.helper-mobile :refer :all])
  (:use code.test))

^{:refer js.react-native.helper-mobile/isMobile :added "4.0" :unchecked true}
(fact "checks if agent is browser")
