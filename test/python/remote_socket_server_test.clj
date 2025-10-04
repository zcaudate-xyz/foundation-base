(ns python.remote-socket-server-test
  (:use code.test)
  (:require [python.remote-socket-server :refer :all]))

^{:refer python.remote-socket-server/handle-connection :added "4.0"}
(fact "handles the connection")

^{:refer python.remote-socket-server/run-server :added "4.0"}
(fact "runs the server")

^{:refer python.remote-socket-server/start-async-loop :added "4.0"}
(fact "starts the async loop")

^{:refer python.remote-socket-server/start-async :added "4.0"}
(fact "starts the async server")
