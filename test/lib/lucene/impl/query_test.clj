(ns lib.lucene.impl.query-test
  (:require [lib.lucene.impl.analyzer :as analyzer]
            [lib.lucene.impl.query :refer :all])
  (:use code.test)
  (:import (org.apache.lucene.util QueryBuilder)))

(def -analyzer- (analyzer/analyzer-standard {}))

^{:refer lib.lucene.impl.query/parse-form-query :added "3.0"}
(fact "parses a query"

  (parse-form-query :<query> nil)
  => :<query>)

^{:refer lib.lucene.impl.query/parse-form-seq :added "3.0"}
(fact "parses a sequential form"

  (parse-form-seq [{:id "hello"} {:id "world"}]
                  {:analyzer -analyzer-
                   :builder (QueryBuilder. -analyzer-)
                   :mode :query})
  ;; #object[org.apache.lucene.search.BooleanQuery 0x774d5068 "+(+id:hello) +(+id:world)"]
  => org.apache.lucene.search.BooleanQuery)

^{:refer lib.lucene.impl.query/parse-form-set :added "3.0"}
(fact "parses a set form"

  (parse-form-set #{{:id "hello"} {:id "world"}}
                  {:analyzer -analyzer-
                   :builder (QueryBuilder. -analyzer-)
                   :mode :query})
  ;; #object[org.apache.lucene.search.BooleanQuery 0x3dd55e15 "(+id:world) (+id:hello)"]
  => org.apache.lucene.search.BooleanQuery)

^{:refer lib.lucene.impl.query/parse-form-map :added "3.0"}
(fact "parses a map form"

  (parse-form-map {:id "hello" :dta "world"}
                  {:analyzer -analyzer-
                   :builder (QueryBuilder. -analyzer-)
                   :mode :query})
  ;; #object[org.apache.lucene.search.BooleanQuery 0x25efb5dc "+id:hello +dta:world"]
  => org.apache.lucene.search.BooleanQuery)

^{:refer lib.lucene.impl.query/parse-string :added "3.0"}
(fact "helper for `parse-form-string`"

  (parse-string -analyzer- "id" "hello")
  ;; #object[org.apache.lucene.search.TermQuery 0x487193be "id:hello"]
  => org.apache.lucene.search.TermQuery)

^{:refer lib.lucene.impl.query/parse-form-string :added "3.0"}
(fact "parses a string"

  (parse-form-string "hello"
                     {:builder (QueryBuilder. -analyzer-)
                      :mode :query
                      :key :id})
  ;; #object[org.apache.lucene.search.TermQuery 0x171ce796 "id:hello"]
  => org.apache.lucene.search.TermQuery)

^{:refer lib.lucene.impl.query/query :added "3.0"}
(fact "compiles a query"

  (query #{{:id "hello"} {:id "world"}})
  ;; #object[org.apache.lucene.search.BooleanQuery 0x3dd55e15 "(+id:world) (+id:hello)"]
  => org.apache.lucene.search.BooleanQuery)

