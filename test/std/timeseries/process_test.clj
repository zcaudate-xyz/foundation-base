(ns std.timeseries.process-test
  (:use code.test)
  (:require [std.timeseries.process :refer :all]))

^{:refer std.timeseries.process/prep-merge :added "3.0"}
(fact "prepares the merge functions and options"

  (prep-merge {:sample [[0 :1m] :range]} {:unit :ms})
  => (contains-in [fn? {:size {:type :to, :start [:array 0],
                               :end [:time 60000]},
                        :strategy :range}]))

^{:refer std.timeseries.process/create-merge-fn :added "3.0"}
(fact "creates a merge function"

  (def merge-fn (create-merge-fn {:sample 9} {:key identity :order :asc}))

  (long (merge-fn (range 90)))
  => 44)

^{:refer std.timeseries.process/create-custom-fns :added "3.0"}
(fact "create custom functions"

  (create-custom-fns [{:keys [:bench.stats.lag :bench.stats.time]
                       :sample 10
                       :aggregate :max}]
                     {:order :desc :key :start})
  => (contains {:bench.stats.lag fn?
                :bench.stats.time fn?}))

^{:refer std.timeseries.process/map-merge-fn :added "3.0"}
(fact "creates a map merge function"

  (map-merge-fn {:time    {:aggregate :first}
                 :default {:sample 4}
                 :custom  [{:keys [:bench.stats.lag]
                            :sample 10
                            :aggregate :max}
                           {:keys [:bench.stats.time]
                            :sample 4
                            :aggregate :mean}]}
                {:order :desc :key :start})
  => fn?)

^{:refer std.timeseries.process/time-merge-fn :added "3.0"}
(fact "creates a merge function for time"

  (time-merge-fn {:aggregate :first} {:key identity})
  => fn?)

^{:refer std.timeseries.process/parse-transform-expr :added "3.0"}
(fact "parses the transform expr"

  (parse-transform-expr {:interval "1m"
                         :time    {:aggregate :first}}
                        :map
                        {:order :desc :key :start :unit :ms})
  => map?)

^{:refer std.timeseries.process/transform-interval :added "3.0"}
(fact "transform array based on resolution"

  (transform-interval (mapv (fn [i] {:time i}) (range 100))
                      {:interval [:time 10]
                       :merge-fn (fn [arr] (map :time arr))}
                      :map
                      {:key :time :order :asc})
  => coll?)

^{:refer std.timeseries.process/process-transform :added "3.0"}
(fact "processes the transform stage"

  (process-transform (vec (range 100))
                     {:time {:key identity :order :asc :unit :ms}
                      :transform {:interval 10}})
  => coll?)

^{:refer std.timeseries.process/process-compute :added "3.0"}
(fact "processes the indicator stage"

  (->> (process-compute [{:start 0
                          :bench.stats.lag 0.7
                          :bench.stats.time 0.3}
                         {:start 2
                          :bench.stats.lag 0.3
                          :bench.stats.time 0.5}
                         {:start 3
                          :bench.stats.lag 0.4
                          :bench.stats.time 0.3}
                         {:start 4
                          :bench.stats.lag 0.5
                          :bench.stats.time 0.3}]
                        {:compute {:lag  [:s/norm :bench.stats.lag]
                                   :time [:s/norm :bench.stats.time]}})
       (map (juxt :lag :time)))
  => [[1.0 0.6] [0.4285714285714286 1.0] [0.5714285714285715 0.6] [0.7142857142857143 0.6]])

^{:refer std.timeseries.process/process :added "3.0"}
(comment "processes time series" ^:hidden

  (process (->> @(first (vals (:active @(:runtime (:collector @*instance*)))))
                :results
                (mapv (fn [{:keys [output] :as m}]
                        (merge (dissoc m :output) output))))

           {:time  {:key :start :unit :ns}
            :range   [0 :end]
            :sample  :100ms
            :transform {:interval :20ms
                        :sample 2
                        :time    {:aggregate :first}
                        :default {}
                        :custom  [{:keys [:bench.stats.lag]
                                   :sample 10
                                   :aggregate :max}
                                  {:keys [:executor.type
                                          :executor.running
                                          :bench.call.duration.last]
                                   :aggregate :last}
                                  {:keys [:bench.stats.time]
                                   :sample 10
                                   :aggregate :max}]}
            :compute {:lag-norm [:s/norm :bench.stats.lag]
                      :lag :bench.stats.lag}}))

(comment
  (./import)

  (range/parse-range-unit "1s" (:time helper/+default+))
  => [:time 1000]

  (range/parse-range-unit 10 (:time helper/+default+))
  => [:number 10]

  (process-transform [] {:time   (common/parse-time-expr {:key :start :unit :ms :interval 200 :order :desc})
                         :transform    {:resolution "1s"
                                        :time    {:aggregate :first}
                                        :default {:sample 4}
                                        :custom  [{:keys [:bench.stats.lag]
                                                   :sample 10
                                                   :aggregate :max}
                                                  {:keys [:bench.stats.time]
                                                   :sample 10
                                                   :aggregate :max}]}})

  (defn world []
    (std.log/section
     (do [+ 1 2 3]
         (std.log/track
          (do [+ 1 2 3]
              (std.log/spy [1 3]))))))

  (defn hello []
    (std.log/track {:log/indent true}
                   (do (world)
                       [+ 1 2 3])))

  (std.log/trace {:log/message "COUNT"
                  :fn/console count}
                 (hello))

  (comment
    (std.log/section
     (->> (process (->> @(first (vals (:active @(:runtime (:collector @hara.shell.gnuplat-impl-test/*instance*)))))
                        :results
                           ;;count
                        (mapv (fn [{:keys [output] :as m}]
                                (merge (dissoc m :output) output))))
                   {:time   {:key :start :unit :ns :interval 200 :order :desc}
                    :range  [0 :end]
                       ;;:sample 1000
                    :transform  {:resolution 20
                                 :sample 2
                                 :time    {:aggregate :first}
                                    ;; :default {}
                                 :custom  [{:keys [:bench.stats.lag]
                                               ;;:sample 10
                                            :aggregate :max}
                                           {:keys [:executor.type
                                                   :executor.running
                                                   :bench.call.duration.last]
                                            :aggregate :last}
                                           {:keys [:bench.stats.time]
                                               ;;:sample 10
                                            :aggregate :max}]}
                    :indicators {:lag-norm '(/ :bench.stats.lag [:s/max :bench.stats.lag])
                                    ;;:lag-norm :bench.stats.lag 
                                 }})
          (map/map-vals count))))

  (process -dataseries-
           {:time   {:key :start :unit :ms :interval 200 :order :desc}
            :interval [:1m :5m]
            :sample   1000
            :transform    {:resolution "1s"
                           :time    {:aggregate :first}
                           :default {:sample 4 :aggregate :mean}
                           :custom  [{:keys [:bench.stats.lag]
                                      :sample 10
                                      :aggregate :max}
                                     {:keys [:bench.stats.time]
                                      :sample 10
                                      :aggregate :max}]}
            :indicators {:lag-norm  (/ :bench.stats.lag [:stats/max :bench.stats.lag])}})

  [:stats/normalised :bench.stats.lag])
