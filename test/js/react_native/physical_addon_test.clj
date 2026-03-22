(ns js.react-native.physical-addon-test
  (:require [js.react-native.physical-addon :refer :all])
  (:use code.test))

^{:refer js.react-native.physical-addon/tagBase :added "4.0" :unchecked true}
(fact "base for tag single and tag all")

^{:refer js.react-native.physical-addon/tagSingle :added "4.0" :unchecked true}
(fact "display a single indicator")

^{:refer js.react-native.physical-addon/tagAll :added "4.0" :unchecked true}
(fact "display all indicators")
