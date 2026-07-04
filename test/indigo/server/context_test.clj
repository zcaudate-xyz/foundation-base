(ns indigo.server.context-test
  (:require [indigo.server.context :refer :all]
            [org.httpkit.server :as http])
  (:use code.test))

^{:refer indigo.server.context/broadcast! :added "4.1"}
(fact "broadcast! sends a message to all repl clients"
  (let [sent (atom [])]
    (with-redefs [repl-clients (atom #{:ch1 :ch2})
                  http/send! (fn [ch msg] (swap! sent conj [ch msg]))]
      (broadcast! "hello")
      (set @sent)
      => #{[:ch1 "hello"] [:ch2 "hello"]})))
