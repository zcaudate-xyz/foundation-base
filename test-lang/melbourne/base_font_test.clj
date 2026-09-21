(ns melbourne.base-font-test
  (:use code.test)
  (:require [melbourne.base-font :refer :all]))

^{:refer melbourne.base-font/getFontStyle :added "4.0"}
(fact "gets font style gives size")
