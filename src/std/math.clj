(ns std.math
  (:require [std.lib.foundation :as f]
            [std.math.aggregate :as aggregate]
            [std.math.common :as common]
            [std.math.random :as random])
  (:refer-clojure :exclude [abs rand rand-int rand-nth]))

(f/intern-in common/abs
             common/ceil
             common/factorial
             common/floor
             common/combinatorial
             common/log
             common/loge
             common/log10
             common/mean
             common/median
             common/mode
             common/variance
             common/stdev
             common/skew
             common/kurtosis
             common/histogram

             aggregate/aggregates

             random/rand-seed!
             random/rand
             random/rand-int
             random/rand-nth
             random/rand-digits
             random/rand-sample)
