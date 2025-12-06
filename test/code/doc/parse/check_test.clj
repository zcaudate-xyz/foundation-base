(ns code.doc.parse.check-test
  (:use code.test)
  (:require [code.doc.parse.check :refer :all]
            [code.query :as query]
            [std.block.parse :as p]
            [std.block.navigate :as nav]))

^{:refer code.doc.parse.check/wrap-meta :added "3.0"}
(fact "helper function for navigating `^:meta` tags"

  ((wrap-meta query/match) (nav/parse-string "^:hello ()") list?)
  => true)

^{:refer code.doc.parse.check/directive? :added "3.0"}
(fact "check if element is a directive"

  (directive? (nav/parse-string "[[:chapter {:title \"hello\"}]]"))
  => true)

^{:refer code.doc.parse.check/attribute? :added "3.0"}
(fact "check if element is an attribute"

  (attribute? (nav/parse-string "[[{:title \"hello\"}]]"))
  => true)

^{:refer code.doc.parse.check/code-directive? :added "3.0"}
(fact "check if element is a code directive"

  (code-directive? (nav/parse-string "[[:code {:lang \"python\"} \"1 + 1\"]]"))
  => true)

^{:refer code.doc.parse.check/ns? :added "3.0"}
(fact "check if element is a `ns` form"

  (ns? (nav/parse-string "(ns code.manage)"))
  => true)

^{:refer code.doc.parse.check/fact? :added "3.0"}
(fact "check if element is a `fact` form"

  (fact? (nav/parse-string "(fact 1 => 1)"))
  => true)

^{:refer code.doc.parse.check/facts? :added "3.0"}
(fact "check if element is a `facts` form"

  (facts? (nav/parse-string "(facts 1 => 1)"))
  => true)

^{:refer code.doc.parse.check/comment? :added "3.0"}
(fact "check if element is a `comment` form"

  (comment? (nav/parse-string "(comment 1 => 1)"))
  => true)

^{:refer code.doc.parse.check/deftest? :added "3.0"}
(fact "check if element is a `deftest` form"

  (deftest? (nav/parse-string "(deftest ...)"))
  => true)

^{:refer code.doc.parse.check/is? :added "3.0"}
(fact "check if element is an `is` form"

  (is? (nav/parse-string "(is ...)"))
  => true)

^{:refer code.doc.parse.check/paragraph? :added "3.0"}
(fact "check if element is a paragraph (string)"

  (paragraph? (nav/parse-string "\"hello world\""))
  => true)

^{:refer code.doc.parse.check/whitespace? :added "3.0"}
(fact "check if element is whitespace"

  (whitespace? (nav/parse-string " "))
  => true)
