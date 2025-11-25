(ns std.timeseries.common-test
  (:use code.test)
  (:require [std.timeseries.common :refer :all]
            [std.timeseries.range :as range]
            [std.math :as math]))

^{:refer std.timeseries.common/linspace :added "3.0"}
(fact "takes the linear space of n samples"

  (linspace (range 100) 10)
  => [0 11 22 33 44 55 66 77 88 99])

^{:refer std.timeseries.common/cluster :added "3.0"}
(fact "clusters a space with an aggregate function"

  (cluster (range 1 200) 10  math/mean)
  => [10 29 48 67 86 105 124 143 162 181])

^{:refer std.timeseries.common/make-empty :added "3.0"}
(fact "creates an empty entry given structure"

  (make-empty {:a 1 :b "hello" :c :world})
  => {:a 0, :b "", :c nil})

^{:refer std.timeseries.common/raw-template :added "3.0"}
(fact "creates a template for nest/flat mapping"

  (raw-template {:a {:b 1 :c "2"}})
  => {:nest {:a {:b "{{a.b}}",
                 :c "{{a.c}}"}},
      :flat {:a.b "{{a.b}}",
             :a.c "{{a.c}}"},
      :types {:a.b java.lang.Long,
              :a.c java.lang.String}
      :empty {:a.b 0, :a.c ""}})

^{:refer std.timeseries.common/flat-fn :added "3.0"}
(fact "creates a nested to flat transform"

  (def transform-fn (flat-fn (raw-template {:a {:b {:c 1}}})))

  (transform-fn {:a {:b {:c 10}}})
  => {:a.b.c 10})

^{:refer std.timeseries.common/nest-fn :added "3.0"}
(fact "creates a nested to flat transform"

  (def transform-fn (nest-fn (raw-template {:a {:b {:c 1}}})))

  (transform-fn {:a.b.c 10})
  => {:a {:b {:c 10}}})

^{:refer std.timeseries.common/create-template :added "3.0"}
(fact "creates a template for entry" ^:hidden
  (create-template  {:data {:in  ""
                            :out "ABC"}})
  => (contains {:raw {:empty {:data.in "", :data.out ""}
                      :nest {:data {:in "{{data.in}}", :out "{{data.out}}"}}
                      :flat {:data.in "{{data.in}}", :data.out "{{data.out}}"}
                      :types {:data.in java.lang.String, :data.out java.lang.String}}
                :nest fn?
                :flat fn?}))

^{:refer std.timeseries.common/order-flip :added "3.0"}
(fact "returns the opposite of the order"

  (order-flip :asc)
  => :desc)

^{:refer std.timeseries.common/order-fn :added "3.0"}
(fact "creates a function that returns or flips the input"

  (mapv (order-fn [:up :down])
        [:asc :desc]
        [false true])
  => [:up :up])

^{:refer std.timeseries.common/order-fns :added "3.0"}
(fact "returns commonly used functions"

  (order-fns :asc)
  => {:comp-fn <, :comp-eq-fn <=, :op-fn +})

^{:refer std.timeseries.common/from-ms :added "3.0"}
(fact "converts ms time into a given unit"

  (from-ms 100 :ns)
  => 100000000)

^{:refer std.timeseries.common/to-ms :added "3.0"}
(fact "converts a time to ms time"

  (to-ms 10000 :ns)
  => 0

  (to-ms 10000 :s)
  => 10000000)

^{:refer std.timeseries.common/from-ns :added "3.0"}
(fact "converts a time from ns"

  (from-ns 1000000 :ms)
  => 1)

^{:refer std.timeseries.common/duration :added "3.0"}
(fact "calculates the duration from array and options"

  (duration [1 2 3 4] {:key identity :unit :m})
  => 180000)

^{:refer std.timeseries.common/parse-time :added "3.0"}
(fact "parses a string or keyword time representation"

  (parse-time :0.1m :us)
  => 6000000

  (parse-time :0.5ms :us)
  => 500)

^{:refer std.timeseries.common/parse-time-expr :added "3.0"}
(fact "parses a time expression"

  (parse-time-expr {:interval "0.5ms"})
  => {:key identity, :unit :ms, :order :asc, :interval 0})

^{:refer std.timeseries.common/sampling-fn :added "3.0"}
(fact "extensible sampling function"

  (sampling-fn :unknown)
  => (throws))

^{:refer std.timeseries.common/sampling-parser :added "3.0"}
(fact "extensible parser function"

  (sampling-parser :unknown)
  => nil)

^{:refer std.timeseries.common/parse-sample-expr :added "3.0"}
(fact "parses a sample expression"

  (parse-sample-expr [10] {})
  => {:size 10, :strategy :linear})

^{:refer std.timeseries.common/process-sample :added "3.0"}
(fact "process sample given sampling function"

  (process-sample (range 100)
                  (parse-sample-expr [10 :random] {})
                  {:key identity :order :asc})
  ;; (4 20 39 51 51 60 70 72 89 96)
  => coll?

  ;; extended sampling function from range
  (process-sample (range 100)
                  (parse-sample-expr [[0 :10ms] :range] {:key identity :order :asc :unit :ms})
                  {:key identity :order :asc :unit :ms})
  => [0 1 2 3 4 5 6 7 8 9 10])

(comment
  (./scaffold)
  (./import))
