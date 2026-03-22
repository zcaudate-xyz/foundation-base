(ns std.time.instant.java-time-clock
  (:require [std.protocol.string :as protocol.string]
            [std.protocol.time :as protocol.time]
            [std.time.coerce :as coerce]
            [std.time.zone.java-time-zoneid])
  (:import (java.time Clock Clock$FixedClock Instant ZoneId ZonedDateTime) (java.time.format DateTimeFormatter)))

(def clock-meta
  {:base :instant
   :formatter {:type DateTimeFormatter}
   :parser    {:type DateTimeFormatter}
   :map  {:from {:proxy ZonedDateTime
                 :via (fn [^ZonedDateTime t]
                        (Clock/fixed (.toInstant t)
                                     (.getZone t)))}
          :to   {:proxy ZonedDateTime
                 :via (fn [^Clock t opts]
                        (ZonedDateTime/now t))}}})

(defmethod protocol.time/-time-meta Clock
  ([_]
   clock-meta))

(defmethod protocol.time/-time-meta Clock$FixedClock
  ([_]
   clock-meta))

(extend-type Clock
  protocol.time/IInstant
  (-to-long       [t] (.millis t))
  (-has-timezone? [t] true)
  (-get-timezone  [t] (protocol.string/-to-string (.getZone t)))
  (-with-timezone [t tz] (.withZone t (coerce/coerce-zone tz {:type ZoneId}))))

(defmethod protocol.time/-from-long Clock
  ([^Long long {:keys [timezone]}]
   (Clock/fixed (Instant/ofEpochMilli long)
                (coerce/coerce-zone timezone {:type ZoneId}))))

(defmethod protocol.time/-now Clock
  ([{:keys [timezone]}]
   (Clock/fixed (Instant/now)
                (coerce/coerce-zone timezone {:type ZoneId}))))
