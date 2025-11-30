(ns std.lib.stream.iter-test
  (:use code.test)
  (:require [std.lib.stream.iter :as i]))

(fact "iterator for map"
  ((i/i:map inc) (range 5))
  => '(1 2 3 4 5))

(fact "iterator for map-indexed"
  ((i/i:map-indexed vector) [:a :b :c])
  => '([0 :a] [1 :b] [2 :c]))

(fact "iterator for filter"
  ((i/i:filter odd?) (range 10))
  => '(1 3 5 7 9))

(fact "iterator for remove"
  ((i/i:remove odd?) (range 10))
  => '(0 2 4 6 8))

(fact "iterator for keep"
  ((i/i:keep #(if (odd? %) %)) (range 10))
  => '(1 3 5 7 9))

(fact "iterator for keep-indexed"
  ((i/i:keep-indexed #(if (odd? %1) %2)) [:a :b :c :d])
  => '(:b :d))

(fact "iterator for take"
  ((i/i:take 5) (range 10))
  => '(0 1 2 3 4))

(fact "iterator for drop"
  ((i/i:drop 5) (range 10))
  => '(5 6 7 8 9))

(fact "iterator for take-nth"
  ((i/i:take-nth 2) (range 10))
  => '(0 2 4 6 8))

(fact "iterator for drop-last"
  ((i/i:drop-last 2) (range 5))
  => '(0 1 2))

(fact "iterator for butlast"
  ((i/i:butlast) (range 5))
  => '(0 1 2 3))

(fact "iterator for peek"
  (let [a (atom [])]
    (doall ((i/i:peek #(swap! a conj %)) (range 5)))
    @a)
  => [0 1 2 3 4])

(fact "iterator for prn"
  (with-out-str
    (doall ((i/i:prn identity) [1 2 3])))
  => "1\n2\n3\n")

(fact "iterator for mapcat"
  ((i/i:mapcat list) [1 2 3])
  => '(1 2 3))

(fact "iterator for delay"
  (let [start (System/currentTimeMillis)
        _ (doall ((i/i:delay 10) (range 5)))
        end (System/currentTimeMillis)]
    (>= (- end start) 50))
  => true)

(fact "iterator for dedupe"
  ((i/i:dedupe) [1 1 2 2 3 3])
  => '(1 2 3))

(fact "iterator for partition-all"
  ((i/i:partition-all 2) (range 5))
  => '((0 1) (2 3) (4)))

(fact "iterator for partition-by"
  ((i/i:partition-by odd?) (range 5))
  => '((0) (1) (2) (3) (4)))

(fact "iterator for random-sample"
  (count ((i/i:random-sample 0.5) (range 100)))
  => (fn [x] (<= 20 x 80)))

(fact "iterator for sort"
  ((i/i:sort) [3 2 1 5 4])
  => '(1 2 3 4 5))

(fact "iterator for sort-by"
  ((i/i:sort-by :a) [{:a 3} {:a 1} {:a 2}])
  => '({:a 1} {:a 2} {:a 3}))

(fact "iterator for reductions"
  ((i/i:reductions +) (range 5))
  => '(0 1 3 6 10))

(fact "iterator for some"
  ((i/i:some #{3}) (range 5))
  => 3)

(fact "iterator for count"
  ((i/i:count) (range 5))
  => 5)

(fact "iterator for reduce"
  ((i/i:reduce +) (range 5))
  => 10)

(fact "iterator for max"
  ((i/i:max) [1 3 5 2 4])
  => 5)

(fact "iterator for min"
  ((i/i:min) [1 3 5 2 4])
  => 1)

(fact "iterator for mean"
  ((i/i:mean) [1 2 3 4 5])
  => 3)

(fact "iterator for stdev"
  (let [res ((i/i:stdev) [1 2 3 4 5])]
    (if res
      (< (Math/abs (- res 1.5811388300841898)) 0.0000001)
      false))
  => true)

(fact "iterator for last"
  ((i/i:last) (range 5))
  => 4)

(fact "iterator for str"
  ((i/i:str) [\a \b \c])
  => "abc")
