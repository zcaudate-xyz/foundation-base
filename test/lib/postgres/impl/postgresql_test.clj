(ns lib.postgres.impl.postgresql-test
  (:use code.test)
  (:require [lib.postgres.impl.postgresql :refer :all])
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

^{:refer lib.postgres.impl.postgresql/create-pool :added "4.1"}
(fact "creates a pooled connection function"
  create-pool
  => fn?)

^{:refer lib.postgres.impl.postgresql/execute-statement :added "4.1"}
(fact "executes a statement against a pooled connection"
  (execute-statement (mock-pooled-conn) :input (fn [_ input] input))
  => :input)
