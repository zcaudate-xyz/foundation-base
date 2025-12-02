(ns lib.jdbc.resultset-test
  (:use code.test)
  (:require [lib.jdbc.resultset :refer :all]
            [lib.jdbc.impl :as impl])
  (:import (java.sql ResultSet ResultSetMetaData Connection)))

(defn mock-result-set []
  (let [rs-meta (reify ResultSetMetaData
                  (getColumnCount [_] 1)
                  (getColumnLabel [_ _] "col"))
        state (atom 0)]
    (reify ResultSet
      (getMetaData [_] rs-meta)
      (next [_] (swap! state inc) (if (< @state 2) true false))
      (getObject [^ResultSet this ^int i] "val"))))

(defn mock-conn []
  (reify Connection))

^{:refer lib.jdbc.resultset/result-set->lazyseq :added "4.0"}
(fact
  "Function that wraps result in a lazy seq."
  (result-set->lazyseq (mock-conn) (mock-result-set) {})
  => [{:col "val"}])

^{:refer lib.jdbc.resultset/result-set->vector :added "4.0"}
(fact 
  "Function that evaluates a result into one clojure persistent vector."
  (result-set->vector (mock-conn) (mock-result-set) {})
  => [{:col "val"}])
