(ns lib.lucene.impl.analyzer-test
  (:require [lib.lucene.impl.analyzer :refer :all])
  (:use code.test))

^{:refer lib.lucene.impl.analyzer/analyzer-char-set :added "3.0"}
(fact "creates an analyzer char set"

  (analyzer-char-set [])
  => #{})

^{:refer lib.lucene.impl.analyzer/analyzer :added "3.0"}
(fact "creates an analyzer"

  (analyzer {:type :standard})
  => org.apache.lucene.analysis.standard.StandardAnalyzer)

^{:refer lib.lucene.impl.analyzer/analyzer-standard :added "3.0"}
(fact "creates a standard analyzer"

  (analyzer-standard {:stop-words ["and"]
                      :ignore-case false})
  => org.apache.lucene.analysis.standard.StandardAnalyzer)

^{:refer lib.lucene.impl.analyzer/analyzer-keyword :added "3.0"}
(fact "creates a keyword analyzer"

  (analyzer-keyword {})
  => org.apache.lucene.analysis.core.KeywordAnalyzer)

^{:refer lib.lucene.impl.analyzer/analyzer-field :added "3.0"}
(fact "creates a field analyzer"

  (analyzer-field {:type :field
                   :default {:type :standard
                             :stop-words []}
                   :fields {:time {:type :keyword}}})
  => org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper)
