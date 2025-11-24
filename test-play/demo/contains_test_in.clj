(ns demo.contains-test-in
  (:use code.test))

(fact "Verify contains-in output on failure"
  {:a {:b 2 :c 3} :d 4}
  => (contains-in {:a {:b 3}}))
