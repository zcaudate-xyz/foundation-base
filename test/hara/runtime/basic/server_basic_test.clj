tahto/runtime/basic/server_basic_test.clj:1:(ns tahto.runtime.basic.server-basic-test
tahto/runtime/basic/server_basic_test.clj:2:  (:require [tahto.runtime.basic.server-basic :refer :all]
            [std.concurrent :as cc])
  (:use code.test))

tahto/runtime/basic/server_basic_test.clj:6:^{:refer tahto.runtime.basic.server-basic/get-port :added "4.0"}
(fact "gets the port given lang and id"
tahto/runtime/basic/server_basic_test.clj:8:  (with-redefs [tahto.runtime.basic.server-basic/*env* (atom {})]
    (start-server "test-port" :lua nil)
    (get-port {:lang :lua :id "test-port"})
    => integer?
    (stop-server "test-port" :lua)))

tahto/runtime/basic/server_basic_test.clj:14:^{:refer tahto.runtime.basic.server-basic/wait-ready :added "4.0"}
(fact "wait until server is ready"
tahto/runtime/basic/server_basic_test.clj:16:  (with-redefs [tahto.runtime.basic.server-basic/*env* (atom {})]
    (start-server "test-wait" :lua nil)
    ;; Ensure start-server populated the env
tahto/runtime/basic/server_basic_test.clj:19:    (get-in @tahto.runtime.basic.server-basic/*env* [:lua "test-wait"]) => map?
    
    (future
      (Thread/sleep 100)
      (let [port (get-port {:lang :lua :id "test-wait"})]
        (try (java.net.Socket. "localhost" port)
             (catch Exception e))))
    
    (wait-ready :lua "test-wait")
    => true
    (stop-server "test-wait" :lua)))

tahto/runtime/basic/server_basic_test.clj:31:^{:refer tahto.runtime.basic.server-basic/run-basic-server :added "4.0"}
(fact "runs a basic socket server"
  (let [state (atom nil)
        ready (promise)
        server (run-basic-server {:port 0} state ready)]
    (:instance server) => #(instance? java.net.ServerSocket %)
    (.close (:instance server))))

tahto/runtime/basic/server_basic_test.clj:39:^{:refer tahto.runtime.basic.server-basic/get-encoding :added "4.0"}
(fact "gets the encoding to use"

  (get-encoding :json)
  => map?)

tahto/runtime/basic/server_basic_test.clj:45:^{:refer tahto.runtime.basic.server-basic/get-relay :added "4.0"}
(fact "gets the relay associated with the server"
tahto/runtime/basic/server_basic_test.clj:47:  (with-redefs [tahto.runtime.basic.server-basic/*env* (atom {})]
    (get-relay (start-server "test" :lua nil))
    => nil
    (stop-server "test" :lua)))

tahto/runtime/basic/server_basic_test.clj:52:^{:refer tahto.runtime.basic.server-basic/ping-relay :added "4.0"}
(fact "checks if the relay is still valid"
tahto/runtime/basic/server_basic_test.clj:54:  (with-redefs [tahto.runtime.basic.server-basic/*env* (atom {})]
    (ping-relay (start-server "test" :lua nil))
    => false
    (stop-server "test" :lua)))

tahto/runtime/basic/server_basic_test.clj:59:^{:refer tahto.runtime.basic.server-basic/raw-eval-basic-server :added "4.0"}
(fact "performs raw eval"
tahto/runtime/basic/server_basic_test.clj:61:  (with-redefs [tahto.runtime.basic.server-basic/*env* (atom {})]
    (start-server "test-eval" :lua nil)
    (raw-eval-basic-server (get-server "test-eval" :lua) "1 + 1")
    => {:status "not-connected"}
    (stop-server "test-eval" :lua)))

tahto/runtime/basic/server_basic_test.clj:67:^{:refer tahto.runtime.basic.server-basic/create-basic-server :added "4.0"}
(fact "creates a basic server"
tahto/runtime/basic/server_basic_test.clj:69:  (with-redefs [tahto.runtime.basic.server-basic/*env* (atom {})]
    (create-basic-server "test-create" :lua nil :json)
    => map?
    (stop-server "test-create" :lua)))

tahto/runtime/basic/server_basic_test.clj:74:^{:refer tahto.runtime.basic.server-basic/start-server :added "4.0"}
(fact "start server function"
tahto/runtime/basic/server_basic_test.clj:76:  (with-redefs [tahto.runtime.basic.server-basic/*env* (atom {})]
    (start-server "test-start" :lua nil)
    => map?
    (stop-server "test-start" :lua)))

tahto/runtime/basic/server_basic_test.clj:81:^{:refer tahto.runtime.basic.server-basic/get-server :added "4.0"}
(fact "gets a server given id"
tahto/runtime/basic/server_basic_test.clj:83:  (with-redefs [tahto.runtime.basic.server-basic/*env* (atom {})]
    (start-server "test-get" :lua nil)
    (get-server "test-get" :lua)
    => map?
    (stop-server "test-get" :lua)))

tahto/runtime/basic/server_basic_test.clj:89:^{:refer tahto.runtime.basic.server-basic/stop-server :added "4.0"}
(fact "stops a server"
tahto/runtime/basic/server_basic_test.clj:91:  (with-redefs [tahto.runtime.basic.server-basic/*env* (atom {})]
    (start-server "test-stop" :lua nil)
    (stop-server "test-stop" :lua)
    => map?))
