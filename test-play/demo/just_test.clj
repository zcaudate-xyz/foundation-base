(ns scratch.just-test
  (:use code.test))

(fact "Verify just output on failure"
  {:a 1 :b 2 :c 3}
  => (just {:a 1 :b 3 :c 3}))
