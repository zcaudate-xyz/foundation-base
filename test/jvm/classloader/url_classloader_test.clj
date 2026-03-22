(ns jvm.classloader.url-classloader-test
  (:require [jvm.classloader.url-classloader :refer :all])
  (:use code.test))

^{:refer jvm.classloader.url-classloader/ucp-remove-url :added "4.0"}
(fact "removes the url")
