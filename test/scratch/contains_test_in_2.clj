(ns scratch.contains-test-in-2
  (:use code.test))

(fact "Verify contains-in output on failure with nested structure"
  {:a {:b 1 :c 2} :d 3}
  => (contains-in {:a {:b 2}}))
