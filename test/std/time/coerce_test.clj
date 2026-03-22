(ns std.time.coerce-test
  (:require [std.protocol.string :as string]
            [std.protocol.time :as time]
            [std.time]
            [std.time.coerce :refer :all]
            [std.time.common :as common]
            [std.time.map :as map])
  (:use code.test)
  (:import (java.util Date Calendar TimeZone) (java.time Instant Clock ZonedDateTime)))

^{:refer std.time.coerce/coerce-zone :added "3.0"}
(fact "coercion of one zone object to another"
  (-> (coerce-zone "Asia/Kolkata" {:type TimeZone})
      (string/-to-string))
  => "Asia/Kolkata"

  (-> (coerce-zone nil {:type TimeZone})
      (string/-to-string))
  => (-> (TimeZone/getDefault)
         (string/-to-string)))

^{:refer std.time.coerce/coerce-instant :added "3.0"}
(fact "coercion of one instant object to another"
  (-> ^Calendar (coerce-instant 0 {:type Calendar})
      (.getTime)
      (.getTime))
  => 0)

^{:refer std.time.coerce/coerce-instant}
(fact "coerce-instant for java.time datastructures"
  (coerce-instant 0 {:type Long
                     :timezone "GMT"})
  => 0

  (-> (coerce-instant 0 {:type ZonedDateTime
                         :timezone "GMT"})
      (map/to-map {} common/+default-keys+))
  => {:type ZonedDateTime
      :timezone "GMT", :long 0,
      :year 1970, :month 1, :day 1, :hour 0,
      :minute 0, :second 0 :millisecond 0}

  (-> (time/-from-long 0 {:type ZonedDateTime
                          :timezone "GMT"})
      (coerce-instant {:type Clock
                       :timezone "Asia/Kolkata"})
      (map/to-map {} common/+default-keys+))
  => {:type java.time.Clock$FixedClock,
      :timezone "Asia/Kolkata", :long 0,
      :year 1970, :month 1, :day 1, :hour 5,
      :minute 30, :second 0 :millisecond 0}

  (-> (time/-from-long 0 {:type Clock
                          :timezone "GMT"})
      (coerce-instant {:type Calendar
                       :timezone "Asia/Kolkata"})
      (map/to-map {} common/+default-keys+))
  => {:type java.util.GregorianCalendar
      :timezone "Asia/Kolkata", :long 0,
      :year 1970, :month 1, :day 1, :hour 5,
      :minute 30, :second 0 :millisecond 0})
