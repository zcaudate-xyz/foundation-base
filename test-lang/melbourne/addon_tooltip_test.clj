(ns melbourne.addon-tooltip-test
  (:use code.test)
  (:require [melbourne.addon-tooltip :refer :all]))

^{:refer melbourne.addon-tooltip/addonTooltip :added "4.0"}
(fact "creates an addon tooltip")
