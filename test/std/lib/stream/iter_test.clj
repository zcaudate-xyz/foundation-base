(ns std.lib.stream.iter-test
  (:use code.test)
  (:require [std.lib.stream.iter :refer :all]))

^{:refer std.lib.stream.iter/i:map :added "4.1"}
(fact "iterator for map"
  ^:hidden
  
  ((i:map inc) [1 2 3])
  => '(2 3 4)

  ((i:map + [4 5 6]) [1 2 3])
  => '(5 7 9))

^{:refer std.lib.stream.iter/i:map-indexed :added "4.1"}
(fact "iterator for map-indexed"
  ^:hidden
  
  ((i:map-indexed vector) [:a :b :c])
  => '([0 :a] [1 :b] [2 :c]))

^{:refer std.lib.stream.iter/i:filter :added "4.1"}
(fact "iterator for filter"
  ^:hidden
  
  ((i:filter odd?) [1 2 3 4])
  => '(1 3))

^{:refer std.lib.stream.iter/i:remove :added "4.1"}
(fact "iterator for remove"
  ^:hidden
  
  ((i:remove odd?) [1 2 3 4])
  => '(2 4))

^{:refer std.lib.stream.iter/i:keep :added "4.1"}
(fact "iterator for keep"
  ^:hidden
  
  ((i:keep #(if (odd? %) %)) [1 2 3 4])
  => '(1 3))

^{:refer std.lib.stream.iter/i:keep-indexed :added "4.1"}
(fact "iterator for keep-indexed"
  ((i:keep-indexed (fn [i v] (if (odd? i) v))) [:a :b :c :d])
  => '(:b :d))

^{:refer std.lib.stream.iter/i:take :added "4.1"}
(fact "iterator for take"
  ^:hidden
  
  ((i:take 2) [1 2 3 4])
  => '(1 2))

^{:refer std.lib.stream.iter/i:drop :added "4.1"}
(fact "iterator for drop"
  ^:hidden
  
  ((i:drop 2) [1 2 3 4])
  => '(3 4))

^{:refer std.lib.stream.iter/i:take-nth :added "4.1"}
(fact "iterator for take-nth"
  ^:hidden
  
  ((i:take-nth 2) [1 2 3 4 5])
  => '(1 3 5))

^{:refer std.lib.stream.iter/i:drop-last :added "4.1"}
(fact "iterator for drop-last"
  ^:hidden
  
  ((i:drop-last 1) [1 2 3])
  => '(1 2))

^{:refer std.lib.stream.iter/i:butlast :added "4.1"}
(fact "iterator for butlast"
  ^:hidden
  
  ((i:butlast) [1 2 3])
  => '(1 2))

^{:refer std.lib.stream.iter/i:peek :added "4.1"}
(fact "iterator for peek"
  ^:hidden
  
  (def out (atom []))
  (doall ((i:peek #(swap! out conj %)) [1 2 3]))
  @out
  => [1 2 3])

^{:refer std.lib.stream.iter/i:prn :added "4.1"}
(fact "iterator for prn"
  ^:hidden
  
  (with-out-str
    (doall ((i:prn identity) [1 2])))
  => "1\n2\n")

^{:refer std.lib.stream.iter/i:mapcat :added "4.1"}
(fact "iterator for mapcat"
  ^:hidden
  
  ((i:mapcat (fn [x] [x x])) [1 2 3])
  => '(1 1 2 2 3 3))

^{:refer std.lib.stream.iter/i:delay :added "4.1"}
(fact "iterator for delay"
  ^:hidden
  
  (let [start (System/currentTimeMillis)
        _ (doall ((i:delay 10) [1 2 3]))]
    (>= (- (System/currentTimeMillis) start) 30))
  => true)

^{:refer std.lib.stream.iter/i:dedupe :added "4.1"}
(fact "iterator for dedupe"
 ^:hidden
  
   ((i:dedupe) [1 1 2 3 3 3 4])
  => '(1 2 3 4))

^{:refer std.lib.stream.iter/i:partition-all :added "4.1"}
(fact "iterator for partition-all"
  ^:hidden
  
  ((i:partition-all 2) [1 2 3 4 5])
  => '((1 2) (3 4) (5)))

^{:refer std.lib.stream.iter/i:partition-by :added "4.1"}
(fact "iterator for partition-by"
  ^:hidden
  
  ((i:partition-by odd?) [1 1 2 2 3 3])
  => '((1 1) (2 2) (3 3)))

^{:refer std.lib.stream.iter/i:random-sample :added "4.1"}
(fact "iterator for random-sample"
  ^:hidden
  
  ((i:random-sample 1.0) [1 2 3])
  => '(1 2 3))

^{:refer std.lib.stream.iter/i:sort :added "4.1"}
(fact "iterator for sort"
  ^:hidden
  
  ((i:sort) [3 1 2])
  => '(1 2 3))

^{:refer std.lib.stream.iter/i:sort-by :added "4.1"}
(fact "iterator for sort-by"
  ^:hidden
  
  ((i:sort-by :a) [{:a 2} {:a 1}])
  => '({:a 1} {:a 2}))

^{:refer std.lib.stream.iter/i:reductions :added "4.1"}
(fact "iterator for reductions"
  ^:hidden
  
  ((i:reductions +) [1 2 3 4])
  => '(1 3 6 10))

^{:refer std.lib.stream.iter/i:some :added "4.1"}
(fact "iterator for some"
  ^:hidden
  
  ((i:some #{2}) [1 2 3])
  => 2)

^{:refer std.lib.stream.iter/i:count :added "4.1"}
(fact "iterator for count"
  ^:hidden
  
  ((i:count) [1 2 3])
  => 3)

^{:refer std.lib.stream.iter/i:reduce :added "4.1"}
(fact "iterator for reduce"
  ^:hidden
  
  ((i:reduce +) [1 2 3])
  => 6)

^{:refer std.lib.stream.iter/i:max :added "4.1"}
(fact "iterator for max"
  ^:hidden
  
  ((i:max) [1 3 2])
  => 3)

^{:refer std.lib.stream.iter/i:min :added "4.1"}
(fact "iterator for min"
  ^:hidden
  
  ((i:min) [1 3 2])
  => 1)

^{:refer std.lib.stream.iter/i:mean :added "4.1"}
(fact "iterator for mean"
  ^:hidden
  
  ((i:mean) [1 2 3 4])
  => 5/2)

^{:refer std.lib.stream.iter/i:stdev :added "4.1"}
(fact "iterator for stdev"
  ^:hidden
  
  ((i:stdev) [2 4 4 4 5 5 7 9])
  => 2.138089935299395)

^{:refer std.lib.stream.iter/i:last :added "4.1"}
(fact "iterator for last"
  ^:hidden
  
  ((i:last) [1 2 3])
  => 3)

^{:refer std.lib.stream.iter/i:str :added "4.1"}
(fact "iterator for str"
  ^:hidden
  
  ((i:str) [\a \b \c])
  => "abc")
