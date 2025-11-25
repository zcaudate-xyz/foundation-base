(ns std.timeseries.range-test
  (:use code.test)
  (:require [std.timeseries.range :refer :all])
  (:import (java.util Date)))

^{:refer std.timeseries.range/parse-range-unit :added "3.0"}
(fact "categorising the unit range"

  (parse-range-unit :start {})
  => [:array 0]

  (parse-range-unit :end {})
  => [:array -1]

  (parse-range-unit :1m {:unit :ms})
  => [:time 60000]

  (parse-range-unit :-1m {:unit :ms})
  => [:time -60000]

  (parse-range-unit 0.44 {})
  => [:ratio 0.44])

^{:refer std.timeseries.range/parse-range-expr :added "3.0"}
(fact "parsing a range expression"

  (parse-range-expr [:20s 0.8] {:unit :ms}) ^:hidden
  => {:type :to, :start [:time 20000], :end [:ratio 0.8]}

  (parse-range-expr [0 :for :1m] {:unit :ms})
  => {:type :for, :start [:array 0], :end [:time 60000]}

  (parse-range-expr [:-2m (Date. 0)] {:unit :ms})
  => {:type :end, :start [:time -120000], :end [:absolute 0]})

^{:refer std.timeseries.range/range-wrap :added "3.0"}
(fact "function wrapper for range-start and range-end functions"

  ((range-wrap range-start true) [1 2 3] {} [:array 1] {:order :asc :key identity})
  => [1 [2 3]])

^{:refer std.timeseries.range/range-start :added "3.0"}
(fact "chooses the start of the array"

  (range-start [1 2 3 4] {} [:array 2] {:order :asc :key identity})
  => [2 [3 4]]

  (range-start [-2 -1 0 1 2] {} [:time 2] {:order :asc :key identity})
  => [2 [0 1 2]]

  (range-start [-2 -1 0 1 2] {} [:absolute 0] {:order :asc :key identity})
  => [2 [0 1 2]])

^{:refer std.timeseries.range/range-end-for :added "3.0"}
(fact "ends the array range given :for option"

  (range-end-for [2 3 4] {} [:array 2] {:order :asc :key identity})
  => [2 3])

^{:refer std.timeseries.range/range-end-to :added "3.0"}
(fact "ends the array range given :to option"

  (range-end-to [2 3 4] {:dropped 2} [:array 4] {:order :asc :key identity})
  => [2 3])

^{:refer std.timeseries.range/range-op :added "3.0"}
(fact "standardises units for negative inputs")

^{:refer std.timeseries.range/select-range-standard :added "3.0"}
(fact "helper for standard range select")

^{:refer std.timeseries.range/select-range-end :added "3.0"}
(fact "helper for range select when a date value is at the end")

^{:refer std.timeseries.range/select-range :added "3.0"}
(fact "selects the range"

  (select-range [1 2 3 4 5]
                {:type :for :start [:array 1] :end [:array 3]}
                {:order :asc :key identity})
  => [2 3 4] ^:hidden

  (select-range [1 2 3 4 5]
                {:type :for :start [:time 1] :end [:time 3]}
                {:order :asc :key identity})
  => [2 3 4 5]

  (select-range [1 2 3 4 5 6 7 8]
                {:type :end :start [:array -3] :end [:absolute 7]}
                {:order :asc :key identity})
  => [5 6 7]

  (select-range [1 2 3 4 5 6 7 8]
                {:type :end :start [:time -3] :end [:absolute 7]}
                {:order :asc :key identity})
  => [4 5 6 7])

^{:refer std.timeseries.range/process-filter :added "3.0"}
(fact "processes items given a filter"

  (process-filter  [1 2 3 4 5 6 7 8]
                   even?
                   {})
  => [2 4 6 8] ^:hidden

  (process-filter  [{:id 1 :val 1}
                    {:id 2 :val 2}
                    {:id 3 :val 3}
                    {:id 4 :val 1}
                    {:id 5 :val 4}
                    {:id 6 :val 1}]
                   {:val 1}
                   {})
  => [{:id 1, :val 1} {:id 4, :val 1} {:id 6, :val 1}])

^{:refer std.timeseries.range/process-range :added "3.0"}
(fact "range stage in the process pipeline"

  (process-range (range 10000)
                 {:time    {:key identity :unit :s :order :asc}
                  :range   [:1m :5m]
                  :sample  [10 :linear]})
  => [60 86 112 138 164 190 216 242 268 300]

  (process-range (range 10000)
                 {:time    {:key identity :unit :ms :order :asc}
                  :range   [:-1s (Date. 7000)]
                  :sample  [10 :linear]})
  => [6000 6111 6222 6333 6444 6555 6666 6777 6888 7000])

(comment
  (./import)

  (require '[std.timeseries.process :as process])

  (count (process/process (clojure.core/range 30 8000 10)
                          {:time  {:interval :50ms :order :asc}
                           :range [:0m :4s]
                           :transform {:resolution :50ms
                                       :skip true
                                       :time {:aggregate identity :sample [3]}}}))

  (h/parse-ms "0m")
  (range (clojure.core/range 100)
         (parse-range [0.1 :for :0.08ms] (assoc (:time +default+)
                                                :unit :us)))

  (range (clojure.core/range 100)
         (parse-range [:3us :0.08ms] (assoc (:time +default+)
                                            :unit :us)))

  (range (clojure.core/range 100)
         (parse-range [0.1 :for 0.11] (:time +default+))))

(comment
  (parse-time :0m (:time +default+))
  (sort-by identity < [1 2 3 4])

  (sample (reverse (clojure.core/range 101))
          {:size 10 :strategy :random}
          {:order :desc :key identity}))

(comment
  (parse-range [:1m :to 0.8])
  (parse-range [:1m :for 0.2])

  (sample (clojure.core/range 101) {:size 10 :strategy :even})

  (sample (clojure.core/range 101) {:size 10 :strategy :random})

  (sample (clojure.core/range 101) {:size 10 :strategy :random}))

(comment
  (sample (range 30 3000 10)
          {:time  {:interval :50ms :order :asc}
           :range [:0m :5m]})

  :group {:interval :1s
          :sample [4 :random]}
  :indicators {:lag-norm  '(/ :bench.stats.lag [:stats/max :bench.stats.lag])}

  (parse-sample 10)

  (range)

  (process -dataseries-
           {:time     {:key :start :unit :ms :interval 200 :order :desc}
            :interval [:1m :5m]
            :sample   [1000 :even]
            :group    {:cluster "1s"
                       :sample 4
                       :aggregate :max}
            :indicators {:lag-norm  (/ :bench.stats.lag [:stats/max :bench.stats.lag])}}))
