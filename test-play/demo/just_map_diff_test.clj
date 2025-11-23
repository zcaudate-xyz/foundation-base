(ns scratch.just-map-diff-test
  (:use code.test))

(fact "Verify just-map diff output"
  {:a 1 :b 2}
  => (just {:a 1 :b 3}))
