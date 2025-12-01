(ns js.react-native.ui-frame-basic-test
  (:use code.test)
  (:require [js.react-native.ui-frame-basic :refer :all]))

^{:refer js.react-native.ui-frame-basic/FramePane :added "4.0" :unchecked true}
(fact "creates a frame pane")

^{:refer js.react-native.ui-frame-basic/Frame :added "4.0" :unchecked true}
(fact "creates a frame")
