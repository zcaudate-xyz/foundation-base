(ns js.react-native.animate-transition-test
  (:use code.test)
  (:require [js.react-native.animate-transition :refer :all]))

^{:refer js.react-native.animate-transition/unit :added "4.0" :unchecked true}
(fact "gets the unit transform")

^{:refer js.react-native.animate-transition/LinearFn :added "4.0" :unchecked true}
(fact "linear transition function")
