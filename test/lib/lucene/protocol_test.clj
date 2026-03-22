(ns lib.lucene.protocol-test
  (:require [lib.lucene.protocol :refer :all])
  (:use code.test))

^{:refer lib.lucene.protocol/-create :added "4.0"}
(fact "creates a lucene object"
  (defmethod -create :test [_] :created)
  (-create {:type :test}) => :created)
