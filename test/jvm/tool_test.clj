(ns jvm.tool-test
  (:require [jvm.tool :refer :all])
  (:use code.test))

^{:refer jvm.tool/hotkey-set :added "4.0"}
(fact "set the hotkey function")
