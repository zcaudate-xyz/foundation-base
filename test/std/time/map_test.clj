(ns std.time.map-test
  (:require [std.lib.version :as version]
            [std.protocol.time :as time]
            [std.time.common :as common]
            [std.time.map :refer :all])
  (:use code.test)
  (:import (java.util Date Calendar TimeZone) (java.time Instant Clock ZonedDateTime ZoneId)))

^{:refer std.time.map/to-map :added "3.0"}
(fact "converts an instant to a map"
  (to-map 0 {:timezone "GMT"} common/+default-keys+)
  => {:type java.lang.Long, :timezone "GMT", :long 0
      :year 1970, :month 1, :day 1,
      :hour 0, :minute 0 :second 0 :millisecond 0}

  (to-map (Date. 0) {:timezone "EST"}
          [:year :day :month])
  => {:type java.util.Date, :timezone "EST", :long 0
      :year 1969, :day 31, :month 12}

  (to-map {:type java.lang.Long, :timezone "GMT", :long 0
           :year 1970, :month 1, :day 1,
           :hour 0, :minute 0 :second 0 :millisecond 0}
          {:timezone "EST"}
          common/+default-keys+))

^{:refer std.time.map/from-map :added "3.0"}
(fact "converts a map back to an instant type"
  (from-map {:type java.lang.Long
             :year 1970, :month 1, :day 1,
             :hour 0, :minute 0 :second 0 :millisecond 0
             :timezone "GMT"}
            {:timezone "Asia/Kolkata"}
            {})
  => 0

  (-> (from-map {:type java.util.Calendar
                 :year 1970, :month 1, :day 1,
                 :hour 0, :minute 0 :second 0 :millisecond 0
                 :timezone "GMT"}
                {:timezone "Asia/Kolkata"}
                {})
      (to-map {} common/+default-keys+))
  => {:type java.util.GregorianCalendar, :timezone "Asia/Kolkata", :long 0
      :year 1970, :month 1, :day 1,
      :hour 5, :minute 30 :second 0 :millisecond 0}

  (to-map (common/calendar (Date. 0)
                           (TimeZone/getTimeZone "EST"))
          {:timezone "GMT"} [:month :day :year])
  => {:type java.util.GregorianCalendar, :timezone "GMT", :long 0,
      :year 1970 :month 1, :day 1})

^{:refer std.time.map/with-timezone :added "3.0"}
(fact "adds the timezone to a Calendar object"

  (-> (to-map 0 {:timezone "EST"})
      (with-timezone "GMT")
      (dissoc :type))
  => (-> (to-map 0 {:timezone "GMT"})
         (dissoc :type)))

^{:refer std.protocol.time/-from-long :adopt true}
(fact "testing for v1.8 data structures"

  (to-map (time/-from-long 0 {:type ZonedDateTime})
          {:timezone "GMT"}
          [:month :day :year])
  => {:type java.time.ZonedDateTime, :long 0,
      :timezone "GMT", :year 1970 :month 1, :day 1}

  (to-map (time/-from-long 0 {:type ZonedDateTime})
          {:timezone "PST"}
          [:month :day :year])
  => {:type java.time.ZonedDateTime, :long 0,
      :timezone "America/Los_Angeles",
      :year 1969, :month 12, :day 31}

  (to-map (time/-from-long 0 {:type Clock :timezone "EST"})
          {:timezone "GMT"}
          [:month :day :year])
  => {:type java.time.Clock$FixedClock, :timezone "GMT", :long 0,
      :year 1970, :month 1, :day 1})

(comment
  (code.manage/import))
