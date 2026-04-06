(ns lib.postgres.impl.impossibl-test
  (:use code.test)
  (:require [lib.postgres.impl.impossibl :refer :all])
  (:import (javax.sql PooledConnection)))

(defn mock-conn []
  (reify java.sql.Connection
    (close [_])))

(defn mock-pooled-conn []
  (reify PooledConnection
    (close [_])
    (getConnection [_] (mock-conn))
    (addConnectionEventListener [_ _])
    (removeConnectionEventListener [_ _])))

^{:refer lib.postgres.impl.impossibl/create-pool :added "4.1"}
(fact "creates a pooled connection function"
  create-pool
  => fn?)

^{:refer lib.postgres.impl.impossibl/execute-statement :added "4.1"}
(fact "executes a statement against a pooled connection"
  (execute-statement (mock-pooled-conn) :input (fn [_ input] input))
  => :input)

^{:refer lib.postgres.impl.impossibl/notify-listener :added "4.1"}
(fact "creates a notification listener"
  (let [events (atom [])]
    (doto (notify-listener {:on-notify (fn [& xs] (swap! events conj xs))
                            :on-close  (fn [] (swap! events conj :closed))})
      (.notification 1 "ch" "payload")
      (.closed))
    @events)
  => [[1 "ch" "payload"] :closed])

^{:refer lib.postgres.impl.impossibl/create-notify :added "4.1"}
(fact "creates a notify channel function"
  create-notify
  => fn?)
