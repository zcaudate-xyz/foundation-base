(ns scratch.contains-test-2
  (:use code.test))

(fact "Verify contains output on failure with nested structure"
  {:a {:b 1 :c 2} :d 3}
  => (contains {:a {:b 2}}))
