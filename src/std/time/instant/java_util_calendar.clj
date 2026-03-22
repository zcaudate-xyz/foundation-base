(ns std.time.instant.java-util-calendar
  (:require [std.protocol.string :as protocol.string]
            [std.protocol.time :as protocol.time]
            [std.time.coerce :as coerce]
            [std.time.common :as common])
  (:import (java.text SimpleDateFormat) (java.util Calendar Date GregorianCalendar TimeZone)))

(defn from-map
  "creates a Calendar object from a map
 
   (cal/from-map (t/epoch {:timezone \"GMT\"}))
   => java.util.Calendar"
  {:added "3.0"}
  ([{:keys [millisecond second minute hour day month year timezone]}]
   (let [cal (doto (Calendar/getInstance ^TimeZone
                    (coerce/coerce-zone timezone {:type TimeZone}))
               (.set year (dec month) day hour minute second))
         _   (if (or (nil? millisecond) (zero? millisecond))
               (.set cal Calendar/MILLISECOND 0)
               (.set cal Calendar/MILLISECOND millisecond))]
     cal)))

(def calendar-meta
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map       {:from  {:fn from-map}}})

(defn with-timezone
  "adds the timezone to a Calendar object
 
   (-> (cal/from-map (t/epoch {:timezone \"EST\"}))
       (cal/with-timezone \"GMT\"))
   => java.util.Calendar ;;#inst \"1970-01-01T00:00:00.000+00:00\""
  {:added "3.0"}
  ([^Calendar t tz]
   (cond (= (protocol.time/-get-timezone t)
            (protocol.string/-to-string tz))
         t

         :else
         (common/calendar (.getTime t)
                          (coerce/coerce-zone tz {:type TimeZone})))))

(defmethod protocol.time/-time-meta GregorianCalendar
  ([_]
   calendar-meta))

(extend-type GregorianCalendar
  protocol.time/IInstant
  (-to-long       [t] (.getTime (.getTime t)))
  (-has-timezone? [t] true)
  (-get-timezone  [t] (protocol.string/-to-string (.getTimeZone t)))
  (-with-timezone [t tz] (with-timezone t tz))

  protocol.time/IRepresentation
  (-millisecond  [t _] (.get t Calendar/MILLISECOND))
  (-second       [t _] (.get t Calendar/SECOND))
  (-minute       [t _] (.get t Calendar/MINUTE))
  (-hour         [t _] (.get t Calendar/HOUR_OF_DAY))
  (-day          [t _] (.get t Calendar/DAY_OF_MONTH))
  (-day-of-week  [t _] (rem (dec (.get t Calendar/DAY_OF_WEEK)) 7))
  (-month        [t _] (inc (.get t Calendar/MONTH)))
  (-year         [t _] (.get t Calendar/YEAR)))

(defmethod protocol.time/-time-meta Calendar
  ([_]
   calendar-meta))

(extend-type Calendar
  protocol.time/IInstant
  (-to-long       [t] (.getTime (.getTime t)))
  (-has-timezone? [t] true)
  (-get-timezone  [t] (protocol.string/-to-string (.getTimeZone t)))
  (-with-timezone [t tz] (with-timezone t tz)) protocol.time/IRepresentation
  (-millisecond  [t _] (.get t Calendar/MILLISECOND))
  (-second       [t _] (.get t Calendar/SECOND))
  (-minute       [t _] (.get t Calendar/MINUTE))
  (-hour         [t _] (.get t Calendar/HOUR_OF_DAY))
  (-day          [t _] (.get t Calendar/DAY_OF_MONTH))
  (-day-of-week  [t _] (rem (dec (.get t Calendar/DAY_OF_WEEK)) 7))
  (-month        [t _] (inc (.get t Calendar/MONTH)))
  (-year         [t _] (.get t Calendar/YEAR)))

(defmethod protocol.time/-from-long Calendar
  ([^Long long {:keys [timezone] :as opts}]
   (common/calendar (Date. long)
                    (coerce/coerce-zone timezone {:type TimeZone}))))

(defmethod protocol.time/-now Calendar
  ([{:keys [timezone]}]
   (common/calendar (Date.)
                    (coerce/coerce-zone timezone {:type TimeZone}))))
