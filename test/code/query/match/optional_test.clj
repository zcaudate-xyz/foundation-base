(ns code.query.match.optional-test
  (:require [code.query.match.optional :refer :all])
  (:use code.test))

^{:refer code.query.match.optional/tag-meta :added "3.0"}
(fact "increment a counter everytime a `:?` element is seen"
  (let [out (atom {:? -1})]
    (tag-meta ^:? () out)
    @out)
  => {:? 0})

^{:refer code.query.match.optional/pattern-seq :added "3.0"}
(fact "generate a sequence of possible matches"
  (pattern-seq '(+ ^:? (1) ^:? (^:? + 2)))
  => '((+)
       (+ (1))
       (+ (2))
       (+ (1) (2))
       (+ (+ 2))
       (+ (1) (+ 2))))
