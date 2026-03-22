(ns std.math.aggregate-test
  (:require [std.math.aggregate :refer :all])
  (:use code.test))

^{:refer std.math.aggregate/max-fn :added "3.0"}
(fact "max taking an array as input"

  (max-fn [1 2 3 4 5])
  => 5)

^{:refer std.math.aggregate/min-fn :added "3.0"}
(fact "min taking an array as input"

  (min-fn [1 2 3 4 5])
  => 1)

^{:refer std.math.aggregate/range-fn :added "3.0"}
(fact "difference between max and min"

  (range-fn [2 3 4 5])
  => 3)

^{:refer std.math.aggregate/middle-fn :added "3.0"}
(fact "finds the middle value in the array"

  (middle-fn [3 4 5])
  => 4)

^{:refer std.math.aggregate/wrap-not-nil :added "3.0"}
(fact "removes values that are nil")

^{:refer std.math.aggregate/aggregates :added "3.0"}
(fact "finds the aggregates of the array"

  (aggregates [1 2 3 3 4 5])
  => (contains
      {:min 1, :mean 3.0, :stdev 1.4142135623730951,
       :skew 0.0, :mode [3], :variance 2, :median 3, :max 5,
       :random number?, :middle 3, :first 1, :last 5, :sum 18, :range 4})

  (aggregates [1 2 3 3 4 5] [:sum]
              {:product #(apply * %)})
  => {:sum 18, :product 360})
