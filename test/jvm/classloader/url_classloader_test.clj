(ns jvm.classloader.url-classloader-test
  (:use code.test)
  (:require [jvm.classloader.url-classloader :refer :all]))

^{:refer jvm.classloader.url-classloader/ucp-remove-url :added "4.0"}
(fact "removes the url")
