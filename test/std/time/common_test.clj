(ns std.time.common-test
  (:require [std.protocol.time :as time]
            [std.time.common :refer :all])
  (:use code.test)
  (:import [java.util Date TimeZone Calendar]))

^{:refer std.time.common/calendar :added "3.0" :class [:time/general]}
(fact "creates a calendar to be used by the base date classes"
  (-> ^Calendar (calendar (Date. 0) (TimeZone/getTimeZone "GMT"))
      (.getTime))
  => #inst "1970-01-01T00:00:00.000-00:00")

^{:refer std.time.common/default-type :added "3.0" :class [:time/general]}
(comment "accesses the default type for datetime"

  (default-type) ;; getter
  => clojure.lang.PersistentArrayMap

  (default-type Long) ;; setter
  => java.lang.Long)

^{:refer std.time.common/local-timezone :added "3.0" :class [:time/zone]}
(comment "returns the current timezone as a string"

  (local-timezone)
  => "Asia/Ho_Chi_Minh")

^{:refer std.time.common/default-timezone :added "3.0" :class [:time/zone]}
(comment "accesses the default timezone as a string"

  (default-timezone)  ;; getter
  => "Asia/Ho_Chi_Minh"

  (default-timezone "GMT")  ;; setter
  => "GMT")
