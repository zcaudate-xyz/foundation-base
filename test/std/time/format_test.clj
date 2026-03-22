(ns std.time.format-test
  (:require [std.protocol.time :as time]
            [std.time.common :as common]
            [std.time.format :as f]
            [std.time.map :as map])
  (:use code.test)
  (:import (java.util Date Calendar TimeZone) (java.sql Timestamp) (java.time Instant Clock ZonedDateTime)))

^{:refer std.time.format/cache :added "3.0"}
(comment "helper function to access formatters by keyword"

  (f/cache f/+format-cache+
           nil
           [java.text.SimpleDateFormat "HH MM dd Z"]
           true))

^{:refer std.time.format/format :added "3.0" :class [:time/convert]}
(fact "converts a date into a string"

  (f/format (Date. 0) "HH MM dd Z" {:timezone "GMT" :cached true})
  => "00 01 01 +0000"

  (f/format (common/calendar (Date. 0)
                             (TimeZone/getTimeZone "GMT"))
            "HH MM dd Z"
            {})
  => "00 01 01 +0000"

  (f/format (Timestamp. 0)
            "HH MM dd Z"
            {:timezone "PST"})
  => "16 12 31 -0800"

  (f/format (Date. 0) "HH MM dd Z")
  => string?)

^{:refer std.time.format/parse :added "3.0" :class [:time/convert]}
(fact "converts a string into a date"
  (f/parse "00 00 01 01 01 1989 +0000"
           "ss mm HH dd MM yyyy Z"
           {:type Date :timezone "GMT"})
  => #inst "1989-01-01T01:00:00.000-00:00"

  (-> (f/parse "00 00 01 01 01 1989 -0800"
               "ss mm HH dd MM yyyy Z"
               {:type Calendar})
      (map/to-map {:timezone "GMT"}
                  common/+default-keys+))
  => {:type java.util.GregorianCalendar,
      :timezone "GMT",
      :long 599648400000,
      :year 1989,
      :month 1, :day 1,
      :hour 9, :minute 0,
      :second 0, :millisecond 0}

  (-> (f/parse "00 00 01 01 01 1989 +0000"
               "ss mm HH dd MM yyyy Z"
               {:type Timestamp})
      (map/to-map {:timezone "Asia/Kolkata"}
                  common/+default-keys+))
  => {:type java.sql.Timestamp,
      :timezone "Asia/Kolkata",
      :long 599619600000,
      :year 1989,
      :month 1, :day 1,
      :hour 6, :minute 30,
      :second 0, :millisecond 0}

  ^:hidden
  (f/parse "00 00 01 01 01 1989 +0000"
           "ss mm HH dd MM yyyy Z")
  => map?)

^{:refer std.time.format/parse}
(fact "testing format for java.time datastructures"
  (-> (f/parse "00 00 01 01 01 1989 +0000" "ss mm HH dd MM yyyy Z"
               {:type Clock})
      (map/to-map {} common/+default-keys+))
  => {:type java.time.Clock$FixedClock,
      :timezone "Etc/GMT"
      :long 599619600000,
      :year 1989, :month 1, :day 1,
      :hour 1, :minute 0, :second 0, :millisecond 0}

  (-> (f/parse "00 00 01 01 01 1989 -1000" "ss mm HH dd MM yyyy Z"
               {:type Clock})
      (map/to-map {} common/+default-keys+))
  => {:type java.time.Clock$FixedClock, :timezone "Etc/GMT-10", :long 599583600000,
      :year 1989, :month 1, :day 1, :hour 1, :minute 0, :second 0, :millisecond 0}

  (-> (f/parse "00 00 01 01 01 1989 +1000" "ss mm HH dd MM yyyy Z"
               {:type Clock})
      (map/to-map {} common/+default-keys+))
  => {:type java.time.Clock$FixedClock, :timezone "Etc/GMT+10", :long 599655600000,
      :year 1989, :month 1, :day 1, :hour 1, :minute 0, :second 0, :millisecond 0})

(comment
  (code.manage/import))
