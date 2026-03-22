(ns std.time.instant.java-time-instant
  (:require [std.protocol.time :as protocol.time]
            [std.time.coerce :as coerce])
  (:import (java.time Clock Instant ZoneId ZonedDateTime) (java.time.format DateTimeFormatter)))

(defmethod protocol.time/-time-meta Instant
  ([_]
   {:base :instant
    :formatter {:type DateTimeFormatter}
    :parser    {:type DateTimeFormatter}
    :map  {:from {:proxy ZonedDateTime
                  :via (fn [^ZonedDateTime t]
                         (.toInstant t))}
           :to   {:proxy ZonedDateTime
                  :via (fn [^Instant t {:keys [timezone]}]
                         (ZonedDateTime/now
                          (Clock/fixed t
                                       (or (coerce/coerce-zone timezone {:type ZoneId})))))}}}))

(extend-type Instant
  protocol.time/IInstant
  (-to-long       [t] (.toEpochMilli t))
  (-has-timezone? [t] false)
  (-get-timezone  [t] nil)
  (-with-timezone [t _] t))

(defmethod protocol.time/-from-long Instant
  ([^Long long _]
   (Instant/ofEpochMilli long)))

(defmethod protocol.time/-now Instant
  ([_]
   (Instant/now)))
