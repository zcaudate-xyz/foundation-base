(ns std.lib.stream.iter-test
  (:use code.test)
  (:require [std.lib.stream.iter :refer :all]))

^{:refer std.lib.stream.iter/i:map :added "4.1"}
(fact "iterator for map"
  ((i:map inc) [1 2 3])
  => '(2 3 4)

  ((i:map + [4 5 6]) [1 2 3])
  => '(5 7 9))

^{:refer std.lib.stream.iter/i:map-indexed :added "4.1"}
(fact "iterator for map-indexed"
  ((i:map-indexed vector) [:a :b :c])
  => '([0 :a] [1 :b] [2 :c]))

^{:refer std.lib.stream.iter/i:filter :added "4.1"}
(fact "iterator for filter"
  ((i:filter odd?) [1 2 3 4])
  => '(1 3))

^{:refer std.lib.stream.iter/i:remove :added "4.1"}
(fact "iterator for remove"
  ((i:remove odd?) [1 2 3 4])
  => '(2 4))

^{:refer std.lib.stream.iter/i:keep :added "4.1"}
(fact "iterator for keep"
  ((i:keep #(if (odd? %) %)) [1 2 3 4])
  => '(1 3))

^{:refer std.lib.stream.iter/i:keep-indexed :added "4.1"}
(fact "iterator for keep-indexed"
  ((i:keep-indexed (fn [i v] (if (odd? i) v))) [:a :b :c :d])
  => '(:b :d))

^{:refer std.lib.stream.iter/i:take :added "4.1"}
(fact "iterator for take"
  ((i:take 2) [1 2 3 4])
  => '(1 2))

^{:refer std.lib.stream.iter/i:drop :added "4.1"}
(fact "iterator for drop"
  ((i:drop 2) [1 2 3 4])
  => '(3 4))

^{:refer std.lib.stream.iter/i:take-nth :added "4.1"}
(fact "iterator for take-nth"
  ((i:take-nth 2) [1 2 3 4 5])
  => '(1 3 5))

^{:refer std.lib.stream.iter/i:drop-last :added "4.1"}
(fact "iterator for drop-last"
  ((i:drop-last 1) [1 2 3])
  => '(1 2))

^{:refer std.lib.stream.iter/i:butlast :added "4.1"}
(fact "iterator for butlast"
  ((i:butlast) [1 2 3])
  => '(1 2))

^{:refer std.lib.stream.iter/i:peek :added "4.1"}
(fact "iterator for peek"
  (def out (atom []))
  (doall ((i:peek #(swap! out conj %)) [1 2 3]))
  @out
  => [1 2 3])

^{:refer std.lib.stream.iter/i:prn :added "4.1"}
(fact "iterator for prn"
  (with-out-str
    (doall ((i:prn identity) [1 2])))
  => "1\n2\n")

^{:refer std.lib.stream.iter/i:mapcat :added "4.1"}
(fact "iterator for mapcat"
  ((i:mapcat (fn [x] [x x])) [1 2 3])
  => '(1 1 2 2 3 3))

^{:refer std.lib.stream.iter/i:delay :added "4.1"}
(fact "iterator for delay"
  (let [start (System/currentTimeMillis)
        _ (doall ((i:delay 10) [1 2 3]))]
    (>= (- (System/currentTimeMillis) start) 30))
  => true)

^{:refer std.lib.stream.iter/i:dedupe :added "4.1"}
(fact "iterator for dedupe"
  ((i:dedupe) [1 1 2 3 3 3 4])
  => '(1 2 3 4))

^{:refer std.lib.stream.iter/i:partition-all :added "4.1"}
(fact "iterator for partition-all"
  ((i:partition-all 2) [1 2 3 4 5])
  => '((1 2) (3 4) (5)))

^{:refer std.lib.stream.iter/i:partition-by :added "4.1"}
(fact "iterator for partition-by"
  ((i:partition-by odd?) [1 1 2 2 3 3])
  => '((1 1) (2 2) (3 3)))

^{:refer std.lib.stream.iter/i:random-sample :added "4.1"}
(fact "iterator for random-sample"
  ((i:random-sample 1.0) [1 2 3])
  => '(1 2 3))

^{:refer std.lib.stream.iter/i:sort :added "4.1"}
(fact "iterator for sort"
  ((i:sort) [3 1 2])
  => '(1 2 3))

^{:refer std.lib.stream.iter/i:sort-by :added "4.1"}
(fact "iterator for sort-by"
  ((i:sort-by :a) [{:a 2} {:a 1}])
  => '({:a 1} {:a 2}))

^{:refer std.lib.stream.iter/i:reductions :added "4.1"}
(fact "iterator for reductions"
  ((i:reductions +) [1 2 3 4])
  => '(1 3 6 10))

^{:refer std.lib.stream.iter/i:some :added "4.1"}
(fact "iterator for some"
  ((i:some #{2}) [1 2 3])
  => 2)

^{:refer std.lib.stream.iter/i:count :added "4.1"}
(fact "iterator for count"
  ((i:count) [1 2 3])
  => 3)

^{:refer std.lib.stream.iter/i:reduce :added "4.1"}
(fact "iterator for reduce"
  ((i:reduce +) [1 2 3])
  => 6)

^{:refer std.lib.stream.iter/i:max :added "4.1"}
(fact "iterator for max"
  ((i:max) [1 3 2])
  => 3)

^{:refer std.lib.stream.iter/i:min :added "4.1"}
(fact "iterator for min"
  ((i:min) [1 3 2])
  => 1)

^{:refer std.lib.stream.iter/i:mean :added "4.1"}
(fact "iterator for mean"
  ((i:mean) [1 2 3 4])
  => 5/2)

^{:refer std.lib.stream.iter/i:stdev :added "4.1"}
(fact "iterator for stdev"
  ((i:stdev) [2 4 4 4 5 5 7 9])
  => 2.138089935299395)

^{:refer std.lib.stream.iter/i:last :added "4.1"}
(fact "iterator for last"
  ((i:last) [1 2 3])
  => 3)

^{:refer std.lib.stream.iter/i:str :added "4.1"}
(fact "iterator for str"
  ((i:str) [\a \b \c])
  => "abc")
