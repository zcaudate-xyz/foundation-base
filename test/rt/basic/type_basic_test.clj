(ns rt.basic.type-basic-test
  (:use code.test)
  (:require [rt.basic.type-basic :refer :all]
            [rt.basic.server-basic :as server]
            [rt.basic.impl.process-lua :as lua]
            [rt.basic.impl.process-js :as js]
            [rt.basic.type-bench :as bench]
            [rt.basic.type-container :as container]
            [std.lib :as h]))

^{:refer rt.basic.type-basic/start-basic :added "4.0"}
(fact "starts the basic rt"
  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& _] {})
                bench/start-bench (fn [& _] {})
                rt.basic.type-common/get-options (fn [& _] {})]
    (start-basic (rt-basic:create {:lang :test :id "test-start" :program nil :make nil :exec nil})))
  => map?)

^{:refer rt.basic.type-basic/stop-basic :added "4.0"}
(fact "stops the basic rt"
  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                server/stop-server (fn [& _] {})
                container/stop-container (fn [& _] {})
                bench/stop-bench (fn [& _] {})
                bench/get-bench (fn [& _] {})
                rt.basic.type-common/get-options (fn [& _] {})]
    (start-basic (rt-basic:create {:lang :test :id "test-stop" :program nil :make nil :exec nil}))
    (stop-basic {:id "test-stop" :lang :test}))
  => map?)

^{:refer rt.basic.type-basic/raw-eval-basic :added "4.0"}
(fact "raw eval for basic rt"
  (with-redefs [server/get-server (fn [& _] {:raw-eval (fn [_ _ _] :ok)})]
    (raw-eval-basic {:id "test-eval" :lang :test} "1 + 1"))
  => :ok)

^{:refer rt.basic.type-basic/invoke-ptr-basic :added "4.0"}
(fact "invoke for basic rt"
  ;; delegates to default-invoke-script
  )

^{:refer rt.basic.type-basic/rt-basic-string :added "4.0"}
(fact "string for basic rt"
  (with-redefs [server/get-server (fn [& _] {:port 1234 :type :server :count (atom 1)})]
    (rt-basic-string {:id "test-string" :lang :test}))
  => string?)

^{:refer rt.basic.type-basic/rt-basic-port :added "4.0"}
(fact "return the basic port of the rt"
  (with-redefs [server/get-server (fn [& _] {:port 1234})]
    (rt-basic-port {:id "test-port" :lang :test}))
  => 1234)

^{:refer rt.basic.type-basic/rt-basic:create :added "4.0"}
(fact "creates a basic rt"
  (with-redefs [rt.basic.type-common/get-options (fn [& _] {})]
    (rt-basic:create {:lang :test}))
  => map?)

^{:refer rt.basic.type-basic/rt-basic :added "4.0"}
(fact "creates and starts a basic rt"
  (with-redefs [server/start-server (fn [& _] {:port 1234})
                server/wait-ready (fn [& _] true)
                container/start-container (fn [& _] {})
                bench/start-bench (fn [& _] {})
                server/stop-server (fn [& _] {})]
    (def +rt+ (rt-basic {:lang :test
                         :program nil
                         :make nil
                         :exec nil}))

    (h/stop +rt+)))
