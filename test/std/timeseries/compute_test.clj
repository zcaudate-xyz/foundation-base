(ns std.timeseries.compute-test
  (:use code.test)
  (:require [std.timeseries.compute :refer :all])
  (:refer-clojure :exclude [compile]))

^{:refer std.timeseries.compute/max-fn :added "3.0"}
(fact "max function accepting array"

  (max-fn [1 2 3])
  => 3)

^{:refer std.timeseries.compute/min-fn :added "3.0"}
(fact "min function accepting array"

  (min-fn [1 2 3])
  => 1)

^{:refer std.timeseries.compute/range-fn :added "3.0"}
(fact "range function accepting array"

  (range-fn [1 2 3])
  => 2)

^{:refer std.timeseries.compute/middle-fn :added "3.0"}
(fact "middling function"

  (middle-fn [1 2 3])
  => 2)

^{:refer std.timeseries.compute/wrap-not-nil :added "3.0"}
(fact "ensures no null values"

  ((wrap-not-nil +) 1 nil)
  => 1)

^{:refer std.timeseries.compute/template? :added "3.0"}
(fact "checks if vector is a template"

  (template? [:s/norm 1])
  => true)

^{:refer std.timeseries.compute/apply-template :added "3.0"}
(fact "applies the template"

  (= (apply-template [:s/norm :bench.stats.lag])
     (list `/ :bench.stats.lag [:s/max :bench.stats.lag]))
  => true)
*e

^{:refer std.timeseries.compute/key-references :added "3.0"}
(fact "finds all references for the expr map"

  (key-references {:t            '(* :start 100000000)
                   :t0           [:s/adj :t]
                   :t1           [:s/inv :t0]
                   :output.norm  [:s/norm :output.value]})
  => {:t #{}, :t0 #{:t}, :t1 #{:t0}, :output.norm #{}})

^{:refer std.timeseries.compute/key-order :added "3.0"}
(fact ""

  (key-order {:output.norm  [:s/norm :output.value]
              :t1           [:s/inv :t0]
              :t0           [:s/adj :t]
              :t            '(* :start 100000000)})
  => [:t :t0 :t1 :output.norm])

^{:refer std.timeseries.compute/compile-keyword :added "3.0"}
(fact "compiles a single keyword"

  (compile-keyword :bench.stats.lag)
  => `(get ~'output (keyword "bench.stats.lag")))

^{:refer std.timeseries.compute/compile-aggregates :added "3.0"}
(fact "compiles the aggregates"

  (compile-aggregates '{v1 [:s/max :bench.stats.lag]})
  => seq?)

^{:refer std.timeseries.compute/compile-form :added "3.0"}
(fact "compiles the entire form"

  (compile-form '(- [:s/norm :bench.stats.time] [:s/norm :bench.stats.lag]))
  => seq?)

^{:refer std.timeseries.compute/compile-single :added "3.0"}
(fact "complise a single fn form"

  (eval (compile-single '(- [:s/norm :bench.stats.time] [:s/norm :bench.stats.lag])))
  => fn?)

^{:refer std.timeseries.compute/compile :added "3.0"}
(fact "complies a map of expressions"

  (compile '{:diff (- [:s/norm :bench.stats.time]
                      [:s/norm :bench.stats.lag])})
  => map?)

^{:refer std.timeseries.compute/compute :added "3.0"}
(fact "computes additional values given array"

  (-> (compute [{:start 10 :output.value 1}
                {:start 11 :output.value 4}
                {:start 12 :output.value 1}
                {:start 13 :output.value 6}
                {:start 14 :output.value 10.1}]
               {:t            '(* :start 100000000)
                :t0           [:s/adj :t]
                :t1           [:s/inv :t0]
                :output.norm  [:s/norm :output.value]})
      first)
  => {:start 10, :output.value 1, :t 1000000000, :t0 0, :t1 400000000, :output.norm 0.09900990099009901})

(comment
  (-> (-jnl- :sample    :1m
             :compute  {:t1           '(quot [:s/adj :start] 100000000)
                        :t0           '(quot [:s/inv :start] 100000000)
                        :output.norm  [:s/norm :output.value]}
             :series   [{:data :t1}
                        {:data :t0}])
      (gp/plot {:autoscale true :terminal :qt})))
