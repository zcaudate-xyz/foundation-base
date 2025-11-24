(ns demo.contains-test
  (:use code.test))

(fact "Verify contains output on failure"
  {:a 1 :b 2 :c 3 :d 4}
  => (contains {:a 1 :b 3 :c 3}))
