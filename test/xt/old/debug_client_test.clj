(ns xt.lang.debug-client-test
  (:require [xt.lang.debug-client :refer :all])
  (:use code.test))

^{:refer xt.lang.debug-client/debug-client-basic :added "4.0"}
(fact "creates a basic client")

^{:refer xt.lang.debug-client/debug-client-ws :added "4.0"}
(fact "creates a basic websocket client")
