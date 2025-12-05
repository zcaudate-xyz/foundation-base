(ns code.test.mock-test
  (:use code.test)
  (:require [code.test.mock :as mock]))

(defn my-fn [x]
  (inc x))

(fact "mocks a function"
  (mock/with [my-fn (mock/return 10)]
    (my-fn 1))
  => 10)

(fact "verifies call"
  (mock/with [my-fn (mock/return 10)]
    (my-fn 1)
    (mock/verify-called 'my-fn))
  => true)

(fact "verifies call with args"
  (mock/with [my-fn (mock/return 10)]
    (my-fn 1)
    (mock/verify-called 'my-fn [1]))
  => true)

(fact "verifies call count"
  (mock/with [my-fn (mock/return 10)]
    (my-fn 1)
    (my-fn 2)
    (mock/verify-call-count 'my-fn 2))
  => true)

(fact "fails verification"
  (mock/with [my-fn (mock/return 10)]
    (mock/verify-called 'my-fn))
  => false)
