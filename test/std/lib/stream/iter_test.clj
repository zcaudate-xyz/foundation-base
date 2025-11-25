(ns std.lib.stream.iter-test
  (:use code.test)
  (:require [std.lib.stream.iter :as i]))

(fact "iterator for map"
  ((i/i:map inc) (range 5))
  => '(1 2 3 4 5))

(fact "iterator for filter"
  ((i/i:filter odd?) (range 10))
  => '(1 3 5 7 9))

(fact "iterator for remove"
  ((i/i:remove odd?) (range 10))
  => '(0 2 4 6 8))

(fact "iterator for take"
  ((i/i:take 5) (range 10))
  => '(0 1 2 3 4))

(fact "iterator for butlast"
  ((i/i:butlast) (range 5))
  => '(0 1 2 3))

(fact "iterator for peek"
  (let [a (atom [])]
    (doall ((i/i:peek #(swap! a conj %)) (range 5)))
    @a)
  => [0 1 2 3 4])

(fact "iterator for mapcat"
  ((i/i:mapcat list) [1 2 3])
  => '(1 2 3))

(fact "iterator for dedupe"
  ((i/i:dedupe) [1 1 2 2 3 3])
  => '(1 2 3))

(fact "iterator for partition-all"
  ((i/i:partition-all 2) (range 5))
  => '((0 1) (2 3) (4)))

(fact "iterator for partition-by"
  ((i/i:partition-by odd?) (range 5))
  => '((0) (1) (2) (3) (4)))

(fact "iterator for sort"
  ((i/i:sort) [3 2 1 5 4])
  => '(1 2 3 4 5))

(fact "iterator for sort-by"
  ((i/i:sort-by :a) [{:a 3} {:a 1} {:a 2}])
  => '({:a 1} {:a 2} {:a 3}))

(fact "iterator for reductions"
  ((i/i:reductions +) (range 5))
  => '(0 1 3 6 10))
