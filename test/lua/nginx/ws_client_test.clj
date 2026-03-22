(ns lua.nginx.ws-client-test
  (:require [lua.nginx.ws-client :refer :all])
  (:use code.test))

^{:refer lua.nginx.ws-client/new :added "4.0"}
(fact "creates a new ws client")
