(ns std.time.instant.java-time-zoneddatetime
  (:require [std.protocol.string :as protocol.string]
            [std.protocol.time :as protocol.time]
            [std.time.coerce :as coerce]
            [std.time.zone.java-time-zoneid])
  (:import (java.time Clock ZoneId ZonedDateTime) (java.time.format DateTimeFormatter)))

(defonce +ms->ns+ 1000000)

(defn from-map
  "creates a ZonedDateTime object from a map
 
   (tz/from-map  (t/epoch {:timezone \"GMT\"}))
   => java.time.ZonedDateTime"
  {:added "3.0"}
  ([{:keys [millisecond second minute hour day month year timezone] :as rep}]
   (ZonedDateTime/of year month day hour minute second (* millisecond +ms->ns+)
                     (coerce/coerce-zone timezone {:type ZoneId}))))

(def zoneddatetime-meta
  {:base :instant
   :formatter {:type DateTimeFormatter}
   :parser    {:type DateTimeFormatter}
   :map {:from  {:fn from-map}}})

(defmethod protocol.time/-time-meta ZonedDateTime
  ([_]
   zoneddatetime-meta))

(extend-type ZonedDateTime
  protocol.time/IInstant
  (-to-long       [t] (.toEpochMilli (.toInstant t)))
  (-has-timezone? [t] true)
  (-get-timezone  [t] (protocol.string/-to-string (.getZone t)))
  (-with-timezone [t tz] (.withZoneSameInstant
                          t
                          ^ZoneId (coerce/coerce-zone tz {:type ZoneId})))

  protocol.time/IRepresentation
  (-millisecond  [t _] (/ (.getNano t) +ms->ns+))
  (-second       [t _] (.getSecond t))
  (-minute       [t _] (.getMinute t))
  (-hour         [t _] (.getHour t))
  (-day          [t _] (.getDayOfMonth t))
  (-day-of-week  [t _] (.getValue (.getDayOfWeek t)))
  (-month        [t _] (.getValue (.getMonth t)))
  (-year         [t _] (.getYear t)))

(defmethod protocol.time/-from-long ZonedDateTime
  ([^Long long opts]
   (ZonedDateTime/now ^Clock (protocol.time/-from-long long (assoc opts :type Clock)))))

(defmethod protocol.time/-now ZonedDateTime
  ([opts]
   (ZonedDateTime/now ^Clock (protocol.time/-now (assoc opts :type Clock)))))
