(ns std.time-test
  (:require [std.time :as t]
            [std.time.common :as common]
            [std.time.map :as map])
  (:use code.test)
  (:import [java.util Date TimeZone Calendar]))

^{:refer std.time/representation? :added "3.0" :class [:time/general]}
(fact "checks if an object implements the representation protocol"
  (t/representation? 0) => false

  (t/representation? (common/calendar (Date. 0) (TimeZone/getTimeZone "GMT")))
  => true)

^{:refer std.time/duration? :added "3.0" :class [:time/general]}
(fact "checks if an object implements the duration protocol"
  (t/duration? 0) => true

  (t/duration? {:weeks 1})
  => true)

^{:refer std.time/instant? :added "3.0" :class [:time/general]}
(fact "checks if an object implements the instant protocol"
  (t/instant? 0) => true

  (t/instant? (Date.)) => true)

^{:refer std.time/has-timezone? :added "3.0" :class [:time/zone]}
(fact "checks if the instance contains a timezone"
  (t/has-timezone? 0) => false

  (t/has-timezone? (common/calendar (Date. 0)
                                    (TimeZone/getDefault)))
  => true)

^{:refer std.time/get-timezone :added "3.0" :class [:time/zone]}
(fact "returns the contained timezone if exists"
  (t/get-timezone 0) => nil

  (t/get-timezone (common/calendar (Date. 0)
                                   (TimeZone/getTimeZone "EST")))
  => "EST")

^{:refer std.time/with-timezone :added "3.0" :class [:time/zone]}
(fact "returns the same instance in a different timezone"
  (t/with-timezone 0 "EST") => 0
  (t/to-map (t/with-timezone (common/calendar (Date. 0)
                                              (TimeZone/getTimeZone "GMT"))
              "EST"))
  => {:type java.util.GregorianCalendar,
      :timezone "EST", :long 0,
      :year 1969, :month 12, :day 31, :hour 19,
      :minute 0, :second 0, :millisecond 0})

^{:refer std.time/time-meta :added "3.0" :class [:time/general]}
(fact "retrieves the meta-data for the time object"
  (t/time-meta TimeZone)
  => {:base :zone}
  (t/time-meta Date)
  => (contains {:base :instant,
                :map (contains {:from (contains {:proxy java.util.Calendar,
                                                 :via fn?}),
                                :to (contains {:proxy java.util.Calendar,
                                               :via fn?})})}))

^{:refer std.time/to-long :added "3.0" :class [:time/convert]}
(fact "gets the long representation for the instant"
  (t/to-long #inst "1970-01-01T00:00:10.000-00:00")
  => 10000)

^{:refer std.time/from-long :added "3.0" :class [:time/convert]}
(fact "creates an instant from a long"
  (-> (t/from-long 0 {:timezone "Asia/Kolkata"
                      :type Calendar})
      (t/to-map))

  => {:type java.util.GregorianCalendar,
      :timezone "Asia/Kolkata", :long 0
      :year 1970, :month 1, :day 1,
      :hour 5, :minute 30 :second 0, :millisecond 0})

^{:refer std.time/to-map :added "3.0" :class [:time/convert]}
(fact "creates an map from an instant"
  (-> (t/from-long 0 {:timezone "Asia/Kolkata"
                      :type Date})
      (t/to-map {:timezone "GMT"} [:year :month :day]))
  => {:type java.util.Date, :timezone "GMT", :long 0,
      :year 1970, :month 1, :day 1})

^{:refer std.time/from-map :added "3.0" :class [:time/convert]}
(fact "creates an map from an instant"
  (t/from-map {:type java.util.GregorianCalendar,
               :timezone "Asia/Kolkata", :long 0
               :year 1970, :month 1, :day 1,
               :hour 5, :minute 30 :second 0, :millisecond 0}

              {:timezone "Asia/Kolkata"
               :type Date})
  => #inst "1970-01-01T00:00:00.000-00:00"
  (t/from-map {:type Long,
               :timezone "Asia/Kolkata", :long 0
               :year 1970, :month 1, :day 1,
               :hour 5, :minute 30 :second 0, :millisecond 0})
  => 0
  (t/from-map {:type Long
               :timezone "Asia/Kolkata",
               :year 1970, :month 1, :day 1,
               :hour 5, :minute 30 :second 0, :millisecond 0}
              {:type clojure.lang.PersistentHashMap
               :timezone "GMT"})
  => {:type clojure.lang.PersistentHashMap
      :timezone "GMT", :long 0
      :year 1970, :month 1, :day 1,
      :hour 0, :minute 0 :second 0, :millisecond 0})

^{:refer std.time/to-length :added "3.0" :class [:time/op]}
(fact "converts a object implementing IDuration to a long"
  (t/to-length {:days 1})
  => 86400000)

^{:refer std.time/wrap-proxy :added "3.0"}
(comment
  "converts one representation to another via a third object,
  used by java.sql.Timestamp and others")

^{:refer std.time/year :added "3.0" :class [:time/access]}
(fact "accesses the year representated by the instant"
  (t/year 0 {:timezone "GMT"}) => 1970

  (t/year (Date. 0) {:timezone "EST"}) => 1969)

^{:refer std.time/month :added "3.0" :class [:time/access]}
(fact "accesses the month representated by the instant"
  (t/month 0 {:timezone "GMT"}) => 1
  (t/month (Date. 0) {:timezone "EST"}) => 12)

^{:refer std.time/day :added "3.0" :class [:time/access]}
(fact "accesses the day representated by the instant"
  (t/day 0 {:timezone "GMT"}) => 1

  (t/day (Date. 0) {:timezone "EST"}) => 31)

^{:refer std.time/day-of-week :added "3.0" :class [:time/access]}
(fact "accesses the day of week representated by the instant"
  (t/day-of-week 0 {:timezone "GMT"}) => 4

  (t/day-of-week (Date. 0) {:timezone "EST"}) => 3)

^{:refer std.time/hour :added "3.0" :class [:time/access]}
(fact "accesses the hour representated by the instant"
  (t/hour 0 {:timezone "GMT"}) => 0

  (t/hour (Date. 0) {:timezone "Asia/Kolkata"}) => 5)

^{:refer std.time/minute :added "3.0" :class [:time/access]}
(fact "accesses the minute representated by the instant"
  (t/minute 0 {:timezone "GMT"}) => 0

  (t/minute (Date. 0) {:timezone "Asia/Kolkata"}) => 30)

^{:refer std.time/second :added "3.0" :class [:time/access]}
(fact "accesses the second representated by the instant"
  (t/second 1000 {:timezone "GMT"}) => 1)

^{:refer std.time/millisecond :added "3.0" :class [:time/access]}
(fact "accesses the millisecond representated by the instant"
  (t/millisecond 1010 {:timezone "GMT"}) => 10)

^{:refer std.time/now :added "3.0" :class [:time/general]}
(fact "returns the current datetime"
  (t/now)
  ;; => #(instance? (t/default-type) %)

  (t/now {:type Date})
  => #(instance? Date %)

  (t/now {:type Calendar})
  => #(instance? Calendar %))

^{:refer std.time/epoch :added "3.0" :class [:time/general]}
(fact "returns the beginning of unix epoch"
  (t/epoch {:type Date})
  => #inst "1970-01-01T00:00:00.000-00:00"
  (t/epoch {:type clojure.lang.PersistentArrayMap :timezone "GMT"})
  {:type clojure.lang.PersistentArrayMap,
   :timezone "GMT", :long 0,
   :year 1970, :month 1, :day 1, :hour 0, :minute 0, :second 0, :millisecond 0})

^{:refer std.time/equal :added "3.0" :class [:time/op]}
(fact "compares dates, retruns true if all inputs are the same"
  (t/equal 1
           (Date. 1)
           (common/calendar
            (Date. 1)
            (TimeZone/getTimeZone "GMT")))
  => true)

^{:refer std.time/before :added "3.0" :class [:time/op]}
(fact "compare dates, returns true if t1 is before t2, etc"
  (t/before 0
            (Date. 1)
            (common/calendar
             (Date. 2)
             (TimeZone/getTimeZone "GMT")))
  => true)

^{:refer std.time/after :added "3.0" :class [:time/op]}
(fact "compare dates, returns true if t1 is after t2, etc"
  (t/after 2
           (Date. 1)
           (common/calendar
            (Date. 0)
            (TimeZone/getTimeZone "GMT")))
  => true)

^{:refer std.time/plus :added "3.0" :class [:time/op]}
(fact "adds a duration to the time"
  (t/plus (Date. 0) {:weeks 2})
  => #inst "1970-01-15T00:00:00.000-00:00"

  (t/plus (Date. 0) 1000)
  => #inst "1970-01-01T00:00:01.000-00:00"

  (t/plus (java.util.Date. 0)
          {:years 10 :months 1 :weeks 4 :days 2})
  => #inst "1980-03-02T00:00:00.000-00:00")

^{:refer std.time/minus :added "3.0" :class [:time/op]}
(fact "substracts a duration from the time"
  (t/minus (Date. 0) {:years 1})
  => #inst "1969-01-01T00:00:00.000-00:00"

  (-> (t/from-map {:type java.time.ZonedDateTime
                   :timezone "GMT",
                   :year 1970, :month 1, :day 1,
                   :hour 0, :minute 0, :second 0, :millisecond 0})
      (t/minus    {:years 10 :months 1 :weeks 4 :days 2})
      (t/to-map {:timezone "GMT"}))
  => {:type java.time.ZonedDateTime, :timezone "GMT",
      :long -320803200000
      :year 1959, :month 11, :day 2,
      :hour 0, :minute 0, :second 0, :millisecond 0})

^{:refer std.time/adjust :added "3.0" :class [:time/op]}
(fact "adjust fields of a particular time"
  (t/adjust (Date. 0) {:year 2000 :second 10} {:timezone "GMT"})
  => #inst "2000-01-01T00:00:10.000-00:00"
  (t/adjust {:year 1970, :month 1 :day 1, :day-of-week 4,
             :hour 0 :minute 0 :second 0 :millisecond 0,
             :timezone "GMT"}
            {:year 1999})
  => {:type clojure.lang.PersistentHashMap,
      :timezone "GMT", :long 915148800000,
      :year 1999, :month 1, :day 1, :hour 0, :minute 0 :second 0, :millisecond 0})

^{:refer std.time/truncate :added "3.0" :class [:time/op]}
(fact "truncates the time to a particular field"
  (t/truncate #inst "1989-12-28T12:34:00.000-00:00"
              :hour {:timezone "GMT"})
  => #inst "1989-12-28T12:00:00.000-00:00"

  (t/truncate #inst "1989-12-28T12:34:00.000-00:00"
              :year {:timezone "GMT"})
  => #inst "1989-01-01T00:00:00.000-00:00"
  (t/truncate (t/to-map #inst "1989-12-28T12:34:00.000-00:00" {:timezone "GMT"})
              :hour)
  => {:type clojure.lang.PersistentHashMap, :timezone "GMT", :long 630849600000,
      :year 1989, :month 12, :day 28,
      :hour 12, :minute 0, :second 0, :millisecond 0})

^{:refer std.time/coerce :added "3.0" :class [:time/convert]}
(fact "adjust fields of a particular time"
  (t/coerce 0 {:type Date})
  => #inst "1970-01-01T00:00:00.000-00:00"

  (t/coerce {:type clojure.lang.PersistentHashMap,
             :timezone "PST", :long 915148800000,
             :year 1999, :month 1, :day 1, :hour 0, :minute 0 :second 0, :millisecond 0}
            {:type Date})
  => #inst "1999-01-01T08:00:00.000-00:00")

^{:refer std.time/latest :added "3.0" :class [:time/op]}
(fact "returns the latest date out of a range of inputs"
  (t/latest (Date. 0) (Date. 1000) (Date. 20000))
  => #inst "1970-01-01T00:00:20.000-00:00")

^{:refer std.time/earliest :added "3.0" :class [:time/op]}
(fact "returns the earliest date out of a range of inputs"
  (t/earliest (Date. 0) (Date. 1000) (Date. 20000))
  => #inst "1970-01-01T00:00:00.000-00:00")

(comment
  (code.manage/import)

  (t/format (t/now) "HH MM dd"))
