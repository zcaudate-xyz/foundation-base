(ns indigo.server.dispatch-test
  (:require [indigo.server.dispatch :refer :all]
            [org.httpkit.server :as http])
  (:use code.test))

^{:refer indigo.server.dispatch/register! :added "4.1"}
(fact "register! adds a channel to *clients*"
  (binding [*clients* (atom #{})]
    (register! :ch1)
    (contains? @*clients* :ch1)
    => true))

^{:refer indigo.server.dispatch/unregister! :added "4.1"}
(fact "unregister! removes a channel from *clients*"
  (binding [*clients* (atom #{:ch1 :ch2})]
    (unregister! :ch1)
    (contains? @*clients* :ch1)
    => false
    (contains? @*clients* :ch2)
    => true))

^{:refer indigo.server.dispatch/send! :added "4.1"}
(fact "send! delivers a string message to a channel"
  (let [sent (atom nil)]
    (with-redefs [http/send! (fn [ch msg] (reset! sent {:ch ch :msg msg}))]
      (send! :ch1 "hello")
      @sent
      => {:ch :ch1 :msg "hello"})))

^{:refer indigo.server.dispatch/broadcast! :added "4.1"}
(fact "broadcast! delivers a string message to all channels"
  (let [sent (atom [])]
    (binding [*clients* (atom #{:ch1 :ch2})]
      (with-redefs [http/send! (fn [ch msg] (swap! sent conj [ch msg]))]
        (broadcast! "hello")
        (set @sent)
        => #{[:ch1 "hello"] [:ch2 "hello"]}))))
