(ns code.query.match.impl-test
  (:use code.test)
  (:require [code.query.match.impl :refer :all]))

^{:refer code.query.match.impl/actual-pattern :added "3.0"}
(fact "constructs a pattern used for direct comparison"

  (:expression (actual-pattern '_))
  => '_

  (:expression (actual-pattern #{1 2 3}))
  => #{1 2 3})

^{:refer code.query.match.impl/actual-pattern? :added "3.0"}
(fact "checks if input is an actual pattern"

  (actual-pattern? '_) => false

  (-> (actual-pattern '_)
      actual-pattern?)
  => true)

^{:refer code.query.match.impl/eval-pattern :added "3.0"}
(fact "constructs a pattern that is evaluated before comparison"

  (:expression (eval-pattern '(keyword "a")))
  => '(keyword "a")

  (:expression (eval-pattern 'symbol?))
  => 'symbol?)

^{:refer code.query.match.impl/eval-pattern? :added "3.0"}
(fact "checks if input is an eval pattern"

  (-> (eval-pattern 'symbol?)
      eval-pattern?)
  => true)

^{:refer code.query.match.impl/match-inner :added "3.0"}
(fact "matches the inner contents of a array"

  (match-inner [number? {:a {:b #'symbol?}} '& '_]
               [1 {:a {:b 'o}} 5 67 89 100])
  => true)

(comment
  (code.manage/import 'code.query.match.impl))
