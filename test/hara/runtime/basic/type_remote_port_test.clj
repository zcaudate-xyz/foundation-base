tahto/runtime/basic/type_remote_port_test.clj:1:(ns tahto.runtime.basic.type-remote-port-test
tahto/runtime/basic/type_remote_port_test.clj:2:  (:require [tahto.runtime.basic.type-common :as common]
tahto/runtime/basic/type_remote_port_test.clj:3:            [tahto.runtime.basic.type-remote-port :refer :all]
            [std.concurrent :as cc]
            [std.lib.component :as component]
            [std.lib.env :as env])
  (:use code.test))

tahto/runtime/basic/type_remote_port_test.clj:9:^{:refer tahto.runtime.basic.type-remote-port/start-remote-port :added "4.0"}
(fact "starts the connection to the remote port"
  (with-redefs [cc/relay (fn [_] :relay)]
    (start-remote-port {:port 1234})
    => (contains {:relay :relay})))

tahto/runtime/basic/type_remote_port_test.clj:15:^{:refer tahto.runtime.basic.type-remote-port/stop-remote-port :added "4.0"}
(fact "stops the connection to the remote port"
  (with-redefs [component/stop (fn [_] nil)]
    (stop-remote-port {:relay :relay})
    => (complement :relay)))

tahto/runtime/basic/type_remote_port_test.clj:21:^{:refer tahto.runtime.basic.type-remote-port/raw-eval-remote-port-relay :added "4.0"}
(fact "evaluates over the remote port"
  (with-redefs [cc/send (fn [_ _] (future {:output "{\"type\":\"data\",\"value\":1}"}))
                env/prn (fn [& _] nil)]
    (raw-eval-remote-port-relay {:relay {:socket nil} :encode nil} "1")
    => 1))

tahto/runtime/basic/type_remote_port_test.clj:28:^{:refer tahto.runtime.basic.type-remote-port/raw-eval-remote-port :added "4.0"}
(fact "evaluates over the remote port"
  (with-redefs [raw-eval-remote-port-relay (fn [& _] :ok)]
    (raw-eval-remote-port {} "1")
    => :ok))

tahto/runtime/basic/type_remote_port_test.clj:34:^{:refer tahto.runtime.basic.type-remote-port/invoke-ptr-remote-port :added "4.0"}
(fact "invokes over the remote port"
  ;; delegates to default-invoke-script
  )

tahto/runtime/basic/type_remote_port_test.clj:39:^{:refer tahto.runtime.basic.type-remote-port/rt-remote-port-string :added "4.0"}
(fact "gets the remote port string"
  (rt-remote-port-string {:lang :lua :port 1234})
  => string?)

tahto/runtime/basic/type_remote_port_test.clj:44:^{:refer tahto.runtime.basic.type-remote-port/rt-remote-port:create :added "4.0"}
(fact "creates the service"
  (with-redefs [common/get-options (fn [& _] {})
                env/prn (fn [& _] nil)]
    (rt-remote-port:create {:lang :lua}))
  => map?)

tahto/runtime/basic/type_remote_port_test.clj:51:^{:refer tahto.runtime.basic.type-remote-port/rt-remote-port :added "4.0"}
(fact "create and starts the service"
  (with-redefs [common/get-options (fn [& _] {})
                start-remote-port (fn [rt] (assoc rt :started true))
                env/prn (fn [& _] nil)]
    (rt-remote-port {:lang :lua :program :lua}))
  => (contains {:started true}))
