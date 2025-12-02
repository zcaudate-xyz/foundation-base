(ns lib.jdbc.types-test
  (:use code.test)
  (:require [lib.jdbc.types :refer :all]
            [lib.jdbc.protocol :as protocol])
  (:import (java.sql Connection PreparedStatement)))

(defn mock-conn []
  (reify Connection
    (close [_])))

(defn mock-stmt []
  (reify PreparedStatement
    (close [_])
    (getConnection [_] (mock-conn))))

^{:refer lib.jdbc.types/->connection :added "4.0"}
(fact 
  "Create a connection wrapper."
  (->connection (mock-conn))
  => #(satisfies? protocol/IConnection %))

^{:refer lib.jdbc.types/->cursor :added "4.0"}
(fact "creates a cursor from prepared statement"
  (->cursor (mock-stmt))
  => #(satisfies? protocol/IConnection %))
