(ns code.test.checker.logic-test
  (:require [code.test.checker.logic :refer :all])
  (:use [code.test :exclude [any all is-not]]))

^{:refer code.test.checker.logic/is-not :added "3.0" :class [:test/checker]}
(fact "checker that allows negative composition of checkers"

  (mapv (is-not even?)
        [1 2 3 4 5])
  => [true false true false true])

^{:refer code.test.checker.logic/any :added "3.0" :class [:test/checker]}
(fact "checker that allows `or` composition of checkers"

  (mapv (any even? 1)
        [1 2 3 4 5])
  => [true true false true false])

^{:refer code.test.checker.logic/all :added "3.0" :class [:test/checker]}
(fact "checker that allows `and` composition of checkers"

  (mapv (all even? #(< 3 %))
        [1 2 3 4 5])
  => [false false false true false])
