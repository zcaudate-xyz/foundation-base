(ns indigo.server.test-runner-test
  (:require [indigo.server.dispatch :as dispatch]
            [indigo.server.test-runner :refer :all]
            [std.lib.signal :as signal])
  (:use code.test))

^{:refer indigo.server.test-runner/browser-test-listener :added "4.1"}
(fact "broadcasts a test-result message"
  (let [captured (atom nil)]
    (with-redefs [dispatch/broadcast! (fn [msg] (reset! captured msg))]
      (browser-test-listener {:result {:status :success :data true :meta {:path "x" :function 'foo :ns 'bar :line 1}}}))
    (:type @captured))
  => "test-result")

^{:refer indigo.server.test-runner/install-browser-listener :added "4.1"}
(fact "installs a signal handler without throwing"
  (with-redefs [signal/*manager* (atom (signal/manager))]
    (install-browser-listener))
  => some?)

^{:refer indigo.server.test-runner/run-test :added "4.1"}
(fact "is a function"
  (fn? run-test) => true)

^{:refer indigo.server.test-runner/run-ns-tests :added "4.1"}
(fact "runs namespace tests without throwing"
  (with-redefs [signal/*manager* (atom (signal/manager))]
    (run-ns-tests "indigo.server.dispatch-test"))
  => map?)