(ns std.print.format.time-test
  (:require [std.print.format.time :refer :all])
  (:use code.test))

^{:refer std.print.format.time/t:time :added "3.0"}
(fact "only returns the time (not day) of an instant"

  (t:time (System/currentTimeMillis)) ^:hidden
  ;; "01:21:10.336"
  => string?)

^{:refer std.print.format.time/t:ms :added "3.0"}
(fact "converts an ms time into readable"

  (t:ms 1000) ^:hidden
  => "1.0s"

  (t:ms 10000000)
  => #(.endsWith ^String % ":46:40h"))

^{:refer std.print.format.time/t:ns :added "3.0"}
(fact "creates humanised time for nanoseconds"

  (t:ns 10000 3) ^:hidden
  => "10.0us")

^{:refer std.print.format.time/t:text :added "3.0"}
(fact "formats ns to string"

  (t:text 1) ^:hidden
  => "1ns"

  (t:text 100000000000000)
  => #"2d \d\d:46:40h")

^{:refer std.print.format.time/t:style :added "3.0"}
(fact "sets the color for a time"

  (t:style 10000) ^:hidden
  => [:normal :cyan]

  (t:style 100000000)
  => [:bold :yellow])

^{:refer std.print.format.time/t :added "3.0"}
(fact "formats ns to string"

  (t 1) ^:hidden
  => "[22;36m1ns[0m"

  (t 100000000000000)
  => string?)
