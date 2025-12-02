(ns lib.jdbc-test
  (:use code.test)
  (:require [lib.jdbc :refer :all]
            [lib.jdbc.protocol :as protocol]
            [lib.jdbc.types :as types])
  (:import (java.sql Connection PreparedStatement ResultSet)))

(defn mock-conn []
  (reify Connection
    (setReadOnly [_ _])
    (setTransactionIsolation [_ _])
    (setSchema [_ _])))

(defn mock-pstmt []
  (reify PreparedStatement
    (executeQuery [_] (reify ResultSet
                        (next [_] false)
                        (getMetaData [_] nil)
                        (close [_])))))

^{:refer lib.jdbc/connection :added "4.0"}
(fact
  "Creates a connection to a database."
  (with-redefs [protocol/-connection (constantly (mock-conn))]
    (connection {}))
  => #(satisfies? protocol/IConnection %))

^{:refer lib.jdbc/prepared-statement? :added "4.0"}
(fact "Check if specified object is prepared statement."
  (prepared-statement? (mock-pstmt)) => true)

^{:refer lib.jdbc/prepared-statement :added "4.0"}
(fact 
  "Given a string or parametrized sql in sqlvec format
  return an instance of prepared statement."
  (with-redefs [protocol/-connection (constantly (mock-conn))
                protocol/-prepared-statement (constantly (mock-pstmt))]
    (prepared-statement (mock-conn) "sql"))
  => (partial instance? PreparedStatement))

^{:refer lib.jdbc/execute :added "4.0"}
(fact 
  "Execute a query and return a number of rows affected."
  (with-redefs [protocol/-connection (constantly (mock-conn))
                protocol/-execute (constantly 1)]
    (execute (mock-conn) "sql"))
  => 1)

^{:refer lib.jdbc/fetch :added "4.0"}
(fact 
  "Fetch eagerly results executing a query."
  (with-redefs [protocol/-connection (constantly (mock-conn))
                protocol/-fetch (constantly [])]
    (fetch (mock-conn) "sql"))
  => [])

^{:refer lib.jdbc/fetch-one :added "4.0"}
(fact "Fetch eagerly one restult executing a query."
  (with-redefs [protocol/-connection (constantly (mock-conn))
                protocol/-fetch (constantly [1])]
    (fetch-one (mock-conn) "sql"))
  => 1)

^{:refer lib.jdbc/fetch-lazy :added "4.0"}
(fact 
  "Fetch lazily results executing a query."
  (with-redefs [protocol/-connection (constantly (mock-conn))
                protocol/-prepared-statement (constantly (mock-pstmt))]
    (fetch-lazy (mock-conn) "sql"))
  => (partial instance? lib.jdbc.types.Cursor))

^{:refer lib.jdbc/cursor->lazyseq :added "4.0"}
(fact 
  "Transform a cursor in a lazyseq."
  (with-redefs [lib.jdbc.impl/cursor->lazyseq (constantly [])]
    (cursor->lazyseq :cursor))
  => [])
