(ns lib.lucene.protocol-test
  (:use code.test)
  (:require [lib.lucene.protocol :refer :all]))

^{:refer lib.lucene.protocol/-create :added "4.0"}
(fact "creates a lucene object"
  (defmethod -create :test [_] :created)
  (-create {:type :test}) => :created)
