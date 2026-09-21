(ns melbourne.addon-debug-test
  (:use code.test)
  (:require [melbourne.addon-debug :refer :all]))

^{:refer melbourne.addon-debug/addonCounter :added "4.0"}
(fact "creates a debug counter")
