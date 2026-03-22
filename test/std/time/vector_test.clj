(ns std.time.vector-test
  (:require [std.protocol.time :as time]
            [std.time]
            [std.time.common :as common]
            [std.time.map :as map]
            [std.time.vector :refer :all])
  (:use code.test)
  (:import (java.util Date TimeZone) (java.time Instant Clock ZonedDateTime ZoneId)))

^{:refer std.time.vector/to-vector :added "3.0"  :class [:time/convert]}
(fact "converts an instant to an array representation"
  (to-vector 0 {:timezone "GMT"} :all)
  => [1970 1 1 0 0 0 0]

  (to-vector (Date. 0) {:timezone "GMT"} :day)
  => [1970 1 1]

  (to-vector (common/calendar (Date. 0)
                              (TimeZone/getTimeZone "EST"))
             {}
             [:month :day :year])
  => [12 31 1969]

  (to-vector (common/calendar (Date. 0)
                              (TimeZone/getTimeZone "EST"))
             {:timezone "GMT"}
             [:month :day :year])
  => [1 1 1970])

^{:refer std.protocol.time/-from-long :adopt true}
(fact "testing for v1.8 data structures"

  (to-vector (time/-from-long 0 {:type ZonedDateTime})
             {:timezone "GMT"} [:month :day :year])
  => [1 1 1970]

  (to-vector (time/-from-long 0 {:type ZonedDateTime})
             {:timezone "PST"}
             [:month :day :year])
  => [12 31 1969]

  (to-vector (time/-from-long 0 {:type Clock :timezone "EST"})
             {:timezone "GMT"}
             [:month :day :year])
  => [1 1 1970]

  (to-vector (time/-from-long 0 {:type Instant})
             {:timezone "PST"}
             [:month :day :year])
  => [12 31 1969])
