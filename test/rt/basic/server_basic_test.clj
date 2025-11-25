(ns rt.basic.server-basic-test
  (:use code.test)
  (:require [rt.basic.server-basic :refer :all]
            [std.lib :as h]
            [std.concurrent :as cc]))

^{:refer rt.basic.server-basic/get-port :added "4.0"}
(fact "gets the port given lang and id"
  (with-redefs [rt.basic.server-basic/*env* (atom {})]
    (start-server "test-port" :lua nil)
    (get-port {:lang :lua :id "test-port"})
    => integer?
    (stop-server "test-port" :lua)))

^{:refer rt.basic.server-basic/wait-ready :added "4.0"}
(fact "wait until server is ready"
  (with-redefs [rt.basic.server-basic/*env* (atom {})]
    (start-server "test-wait" :lua nil)
    ;; Ensure start-server populated the env
    (get-in @rt.basic.server-basic/*env* [:lua "test-wait"]) => map?
    (wait-ready :lua "test-wait")
    => true
    (stop-server "test-wait" :lua)))

^{:refer rt.basic.server-basic/run-basic-server :added "4.0"}
(fact "runs a basic socket server"
  (let [state (atom nil)
        ready (promise)
        server (run-basic-server {:port 0} state ready)]
    (:instance server) => #(instance? java.net.ServerSocket %)
    (.close (:instance server))))

^{:refer rt.basic.server-basic/get-encoding :added "4.0"}
(fact "gets the encoding to use"

  (get-encoding :json)
  => map?)

^{:refer rt.basic.server-basic/get-relay :added "4.0"}
(fact "gets the relay associated with the server"
  (with-redefs [rt.basic.server-basic/*env* (atom {})]
    (get-relay (start-server "test" :lua nil))
    => nil
    (stop-server "test" :lua)))

^{:refer rt.basic.server-basic/ping-relay :added "4.0"}
(fact "checks if the relay is still valid"
  (with-redefs [rt.basic.server-basic/*env* (atom {})]
    (ping-relay (start-server "test" :lua nil))
    => false
    (stop-server "test" :lua)))

^{:refer rt.basic.server-basic/raw-eval-basic-server :added "4.0"}
(fact "performs raw eval"
  (with-redefs [rt.basic.server-basic/*env* (atom {})]
    (start-server "test-eval" :lua nil)
    (raw-eval-basic-server (get-server "test-eval" :lua) "1 + 1")
    => {:status "not-connected"}
    (stop-server "test-eval" :lua)))

^{:refer rt.basic.server-basic/create-basic-server :added "4.0"}
(fact "creates a basic server"
  (with-redefs [rt.basic.server-basic/*env* (atom {})]
    (create-basic-server "test-create" :lua nil :json)
    => map?
    (stop-server "test-create" :lua)))

^{:refer rt.basic.server-basic/start-server :added "4.0"}
(fact "start server function"
  (with-redefs [rt.basic.server-basic/*env* (atom {})]
    (start-server "test-start" :lua nil)
    => map?
    (stop-server "test-start" :lua)))

^{:refer rt.basic.server-basic/get-server :added "4.0"}
(fact "gets a server given id"
  (with-redefs [rt.basic.server-basic/*env* (atom {})]
    (start-server "test-get" :lua nil)
    (get-server "test-get" :lua)
    => map?
    (stop-server "test-get" :lua)))

^{:refer rt.basic.server-basic/stop-server :added "4.0"}
(fact "stops a server"
  (with-redefs [rt.basic.server-basic/*env* (atom {})]
    (start-server "test-stop" :lua nil)
    (stop-server "test-stop" :lua)
    => map?))
