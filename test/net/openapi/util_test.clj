(ns net.openapi.util-test
  (:use code.test)
  (:require [net.openapi.util :refer :all]))

^{:refer net.openapi.util/call-api :added "4.0"}
(fact "Call an API by making HTTP request and return its response.")
