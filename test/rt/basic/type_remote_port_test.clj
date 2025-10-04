(ns rt.basic.type-remote-port-test
  (:use code.test)
  (:require [rt.basic.type-remote-port :refer :all]))

^{:refer rt.basic.type-remote-port/start-remote-port :added "4.0"}
(fact "starts the connection to the remote port")

^{:refer rt.basic.type-remote-port/stop-remote-port :added "4.0"}
(fact "stops the connection to the remote port")

^{:refer rt.basic.type-remote-port/raw-eval-remote-port-relay :added "4.0"}
(fact "evaluates over the remote port")

^{:refer rt.basic.type-remote-port/raw-eval-remote-port :added "4.0"}
(fact "evaluates over the remote port")

^{:refer rt.basic.type-remote-port/invoke-ptr-remote-port :added "4.0"}
(fact "invokes over the remote port")

^{:refer rt.basic.type-remote-port/rt-remote-port-string :added "4.0"}
(fact "gets the remote port string")

^{:refer rt.basic.type-remote-port/rt-remote-port:create :added "4.0"}
(fact "creates the service")

^{:refer rt.basic.type-remote-port/rt-remote-port :added "4.0"}
(fact "create and starts the service")
