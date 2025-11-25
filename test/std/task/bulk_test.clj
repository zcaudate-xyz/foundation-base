(ns std.task.bulk-test
  (:use code.test)
  (:require [std.task.bulk :refer :all]
            [std.lib.result :as res]))

(def +task+
  {:item {:display identity}
   :result {:keys [[:data identity]]
            :columns [{:key :data}]}})

(defn- bulk-test-fn
  ([input params lookup env & args]
   [input (res/result {:status :return :data (* 2 input)})]))

^{:refer std.task.bulk/bulk-display :added "3.0"}
(fact "constructs bulk display options"

  (bulk-display 10 20)
  => map?)

^{:refer std.task.bulk/bulk-process-item :added "3.0"}
(fact "bulk operation processing each item"

  (let [[k v] (bulk-process-item bulk-test-fn
                                 {:idx 0 :total 1 :input 1 :output (volatile! nil)
                                  :display-fn identity}
                                 {:print {}}
                                 {} {} {})]
    (and (= k 1)
         (= (:status v) :return)
         (= (:data v) 2)))
  => true)

^{:refer std.task.bulk/bulk-items-parallel :added "3.0"}
(fact "bulk operation processing in parallel"

  (let [results (bulk-items-parallel bulk-test-fn [1 2 3]
                                     {:idxs (range 3) :display-fn identity}
                                     {:print {}} {} {} {})]
    (and (= (count results) 3)
         (every? (fn [[k v]] (and (number? k) (= (:status v) :return))) results)))
  => true)

^{:refer std.task.bulk/bulk-items-single :added "3.0"}
(fact "bulk operation processing in single"

  (let [results (bulk-items-single bulk-test-fn [1 2 3]
                                   {:idxs (range 3) :display-fn identity}
                                   {:print {}} {} {} {})]
    (and (= (count results) 3)
         (every? (fn [[k v]] (and (number? k) (= (:status v) :return))) results)))
  => true)

^{:refer std.task.bulk/bulk-items :added "3.0"}
(fact "processes each item given a input"

  (let [results (bulk-items +task+ bulk-test-fn [1 2 3] {:print {}} {} {} {})]
    (and (= (count results) 3)
         (every? (fn [[k v]] (and (number? k) (= (:status v) :return))) results)))
  => true)

^{:refer std.task.bulk/bulk-warnings :added "3.0"}
(fact "outputs warnings that have been processed"

  (bulk-warnings {:print {}} [[1 {:status :warn}]])
  => [[1 {:status :warn}]])

^{:refer std.task.bulk/bulk-errors :added "3.0"}
(fact "outputs errors that have been processed"

  (bulk-errors {:print {}} [[1 {:status :error}]])
  => [[1 {:status :error}]])

^{:refer std.task.bulk/bulk-results :added "3.0"}
(fact "outputs results that have been processed"

  (bulk-results +task+ {:print {}} [[1 {:status :return :data 1}]])
  => (every-pred not-empty (partial every? map?)))

^{:refer std.task.bulk/bulk-summary :added "3.0"}
(fact "outputs summary of processed results"

  (bulk-summary +task+ {:print {}}
                [[1 {:time 10 :status :return :data 1}]]
                [{:data 1}]
                []
                []
                100)
  => map?)

^{:refer std.task.bulk/bulk-package :added "3.0"}
(fact "packages results for return"

  (bulk-package +task+ {:items [[1 {:data 1}]]
                        :results [{:key 1 :data 1}]
                        :warnings []
                        :errors []
                        :summary {}}
                :all :map)
  => (contains {:items {1 1} :results {1 1}}))

^{:refer std.task.bulk/bulk :added "3.0"}
(fact "process and output results for a group of inputs"

  (bulk +task+ bulk-test-fn [1 2 3] {:print {}} {} {} {})
  => map?)
