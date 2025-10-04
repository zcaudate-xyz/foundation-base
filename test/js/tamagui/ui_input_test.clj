(ns js.tamagui.ui-input-test
  (:use code.test)
  (:require [js.tamagui.ui-input :refer :all]))

^{:refer js.tamagui.ui-input/getIconSize :added "4.0"}
(fact "gets the icon size")
