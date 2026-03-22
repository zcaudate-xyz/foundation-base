(ns lib.lucene-test
  (:require [lib.lucene :refer :all])
  (:use code.test))

^{:refer lib.lucene/lucene :added "3.0"}
(fact "constructs a lucene engine"

  (lucene {:store :memory
           :template {:album {:analyzer {:type :standard}
                              :type  {:id {:stored false}}}}})
  => lib.lucene.LuceneSearch)
