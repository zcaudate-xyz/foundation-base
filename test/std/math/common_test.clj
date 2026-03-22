(ns std.math.common-test
  (:require [std.math.common :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [abs]))

^{:refer std.math.common/abs :added "3.0"}
(fact "returns the absolute value of `x`"

  (abs -7) => 7
  (abs 7) => 7)

^{:refer std.math.common/square :added "3.0"}
(fact "calculates the square of a number"

  (square 5)
  => 25)

^{:refer std.math.common/sqrt :added "3.0"}
(fact "calculates the square root of a number"

  (sqrt 25)
  => 5.0)

^{:refer std.math.common/ceil :added "3.0"}
(fact "finds the ceiling of a number"

  (ceil 0.1)
  => 1)

^{:refer std.math.common/floor :added "3.0"}
(fact "finds the floor of a number"

  (floor 0.1)
  => 0)

^{:refer std.math.common/factorial :added "3.0"}
(fact "calculates the factorial of `n`"

  (factorial 4) => 24

  (factorial 10) => 3628800)

^{:refer std.math.common/combinatorial :added "3.0"}
(fact "calculates the result of `n` choose `i`"

  (combinatorial 4 2) => 6

  (combinatorial 4 3) => 4)

^{:refer std.math.common/log :added "3.0"}
(fact "calculates the logarithm base `b` of `x`"

  (log 2 2) => 1.0

  (log 4 16) => 2.0)

^{:refer std.math.common/loge :added "3.0"}
(fact "calculates the natural log of `x`"

  (loge 2) => 0.6931471805599453

  (loge 10) => 2.302585092994046)

^{:refer std.math.common/log10 :added "3.0"}
(fact "calculates the log base 10 of `x`"

  (log10 2) => 0.3010299956639812

  (log10 10) => 1.0)

^{:refer std.math.common/mean :added "3.0"}
(fact "calculates the average value of a set of data"

  (mean [1 2 3 4 5])
  => 3

  (mean [1 1.6 7.4 10])
  => 5.0)

^{:refer std.math.common/mode :added "3.0"}
(fact "calculates the most frequent value of a set of data"

  (mode [:alan :bob :alan :greg])
  => [:alan]

  (mode [:smith :carpenter :doe :smith :doe])
  => [:smith :doe])

^{:refer std.math.common/median :added "3.0"}
(fact "calculates the middle value of a set of data"

  (median [5 2 4 1 3])
  => 3

  (median [7 0 2 3])
  => 5/2)

^{:refer std.math.common/percentile :added "3.0"}
(fact "calculates the value within the population given a ratio"

  (percentile [1 9 9 9 9] 0.5)
  => 9)

^{:refer std.math.common/quantile :added "3.0"}
(fact "splits the total population into equal quantiles"

  (quantile [1 2 4 4 4 4 8 9] 4)
  => [3.5 4.0 5.0])

^{:refer std.math.common/variance :added "3.0"}
(fact "calculates the average of the squared differences from the mean"

  (variance [4 5 2 9 5 7 4 5 4])
  => 4)

^{:refer std.math.common/stdev :added "3.0"}
(fact "calculates the standard deviation for a set of data"

  (stdev [4 5 2 9 5 7 4 5 4])
  => 2.0)

^{:refer std.math.common/skew :added "3.0"}
(fact "calculates the skewedness of the data"

  (skew [4 5 2 9 5 7 4 5 4])
  => (approx 0.5833))

^{:refer std.math.common/kurtosis :added "3.0"}
(fact "calculates the kurtosis of the data"

  (kurtosis [4 5 2 9 5 7 4 5 4])
  => (approx 2.4722))

^{:refer std.math.common/histogram :added "3.0"}
(fact "creates a histogram of values"

  (histogram [1 2 3 3 5 5 7 7 8 8 9] 4)
  => [4 2 2 3])
