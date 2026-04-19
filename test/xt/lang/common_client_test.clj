(ns xt.lang.common-client-test
  (:require [xt.lang.common-client :refer :all])
  (:use code.test))

^{:refer xt.lang.common-client/client-basic :added "4.0"}
(fact "creates a basic client")

^{:refer xt.lang.common-client/client-ws :added "4.0"}
(fact "creates a basic websocket client")
