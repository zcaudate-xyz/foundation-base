(ns xt.lang.base-client-test
  (:require [xt.lang.base-client :refer :all])
  (:use code.test))

^{:refer xt.lang.base-client/client-basic :added "4.0"}
(fact "creates a basic client")

^{:refer xt.lang.base-client/client-ws :added "4.0"}
(fact "creates a basic websocket client")
