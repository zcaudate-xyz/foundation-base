(ns js.react-native.ui-frame-basic-test
  (:require [js.react-native.ui-frame-basic :refer :all])
  (:use code.test))

^{:refer js.react-native.ui-frame-basic/FramePane :added "4.0" :unchecked true}
(fact "creates a frame pane")

^{:refer js.react-native.ui-frame-basic/Frame :added "4.0" :unchecked true}
(fact "creates a frame")
