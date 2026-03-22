(ns js.tamagui.ui-input-test
  (:require [js.tamagui.ui-input :refer :all])
  (:use code.test))

^{:refer js.tamagui.ui-input/getIconSize :added "4.0" :unchecked true}
(fact "gets the icon size")
