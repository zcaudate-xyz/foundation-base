(ns rt.basic.type-remote-port-test
  (:use code.test)
  (:require [rt.basic.type-remote-port :refer :all]
            [std.concurrent :as cc]
            [std.lib :as h]
            [rt.basic.type-common :as common]))

^{:refer rt.basic.type-remote-port/start-remote-port :added "4.0"}
(fact "starts the connection to the remote port"
  (with-redefs [cc/relay (fn [_] :relay)]
    (start-remote-port {:port 1234})
    => (contains {:relay :relay})))

^{:refer rt.basic.type-remote-port/stop-remote-port :added "4.0"}
(fact "stops the connection to the remote port"
  (with-redefs [h/stop (fn [_] nil)]
    (stop-remote-port {:relay :relay})
    => (complement :relay)))

^{:refer rt.basic.type-remote-port/raw-eval-remote-port-relay :added "4.0"}
(fact "evaluates over the remote port"
  (with-redefs [cc/send (fn [_ _] (future {:output "{\"type\":\"data\",\"value\":1}"}))
                h/prn (fn [& _] nil)]
    (raw-eval-remote-port-relay {:relay {:socket nil} :encode nil} "1")
    => 1))

^{:refer rt.basic.type-remote-port/raw-eval-remote-port :added "4.0"}
(fact "evaluates over the remote port"
  (with-redefs [raw-eval-remote-port-relay (fn [& _] :ok)]
    (raw-eval-remote-port {} "1")
    => :ok))

^{:refer rt.basic.type-remote-port/invoke-ptr-remote-port :added "4.0"}
(fact "invokes over the remote port"
  ;; delegates to default-invoke-script
  )

^{:refer rt.basic.type-remote-port/rt-remote-port-string :added "4.0"}
(fact "gets the remote port string"
  (rt-remote-port-string {:lang :lua :port 1234})
  => string?)

^{:refer rt.basic.type-remote-port/rt-remote-port:create :added "4.0"}
(fact "creates the service"
  (with-redefs [common/get-options (fn [& _] {})
                h/prn (fn [& _] nil)]
    (rt-remote-port:create {:lang :lua}))
  => map?)

^{:refer rt.basic.type-remote-port/rt-remote-port :added "4.0"}
(fact "create and starts the service"
  (with-redefs [common/get-options (fn [& _] {})
                start-remote-port (fn [rt] (assoc rt :started true))
                h/prn (fn [& _] nil)]
    (rt-remote-port {:lang :lua :program :lua}))
  => (contains {:started true}))
