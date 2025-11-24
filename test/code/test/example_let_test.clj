(ns code.test.example-let-test
  (:use code.test))

(fact "nested checks should work"

  (let [a 1]
    a => 1)

  (let [a 1]
    a => 2)
  
  (do
    1 => 1))
