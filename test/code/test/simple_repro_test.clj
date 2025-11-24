(ns code.test.simple-repro-test
  (:use code.test))

(fact "simple correct contains"

  {:a 1}
  => (contains {:a 1}))

(fact "simple correct contains"

  {:a 1}
  => (contains {:a 2}))

(fact "simple correct"

  {:a 1}
  => {:a 1})

(fact "functions should work"

  1
  => odd?)

(fact "function forms should work"

  2
  => (fn [x]
       (= 2 x)))

(fact "contains fail"

  {:a 1}
  => (contains {:a 2}))

(fact "simple fail"

  {:a 1}
  => {:a 2})

(fact "nested check should fail"
  
  (let [a 1]
    a => 2)
  
  (do
    1 => 2))

(fact "nested checks should pass"

  (let [a 1]
    a => 1)

  (do
    1 => 1))
