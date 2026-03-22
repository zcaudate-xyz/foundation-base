(ns std.time.format.java-time-format-datetimeformatter
  (:require [std.protocol.time :as protocol.time]
            [std.time.coerce :as coerce]
            [std.time.zone :as zone])
  (:import (java.time Clock Instant LocalDate LocalDateTime LocalTime ZoneId ZonedDateTime) (java.time.format DateTimeFormatter ResolverStyle)))

(defmethod protocol.time/-formatter DateTimeFormatter
  ([pattern {:keys [timezone] :as opts}]
   (DateTimeFormatter/ofPattern pattern)))

(defmethod protocol.time/-format [DateTimeFormatter Instant]
  ([^DateTimeFormatter formatter ^Instant t {:keys [timezone]}]
   (let [tz  (if timezone
               (coerce/coerce-zone timezone {:type ZoneId})
               (ZoneId/systemDefault))]
     (.format formatter (LocalDateTime/ofInstant t tz)))))

(defmethod protocol.time/-format [DateTimeFormatter Clock]
  ([^DateTimeFormatter formatter ^Clock t {:keys [timezone]}]
   (.format formatter
            (LocalDateTime/ofInstant (.instant t)
                                     (if timezone
                                       (coerce/coerce-zone timezone {:type ZoneId})
                                       (.getZone t))))))

(defmethod protocol.time/-format [DateTimeFormatter ZonedDateTime]
  ([^DateTimeFormatter formatter ^ZonedDateTime t {:keys [timezone]}]
   (.format formatter (if timezone
                        (.withZoneSameInstant t ^ZoneId (coerce/coerce-zone timezone {:type ZoneId}))
                        t))))

(defmethod protocol.time/-parser DateTimeFormatter
  ([pattern {:keys [timezone] :as opts}]
   (DateTimeFormatter/ofPattern pattern)))

(defmethod protocol.time/-parse [DateTimeFormatter Instant]
  ([^DateTimeFormatter parser s opts]
   (Instant/from (.parse parser s))))

(defmethod protocol.time/-parse [DateTimeFormatter ZonedDateTime]
  ([^DateTimeFormatter parser s opts]
   (let [t (ZonedDateTime/from (.parse parser s))
         offset (.getId (.getZone t))
         ^ZoneId tz (coerce/coerce-zone (-> offset
                                            zone/by-string-offset
                                            zone/by-offset)
                                        {:type ZoneId})]
     (.withZoneSameLocal t tz))))

(defmethod protocol.time/-parse [DateTimeFormatter Clock]
  ([^DateTimeFormatter parser s opts]
   (let [^ZonedDateTime dt (protocol.time/-parse parser s (assoc opts :type ZonedDateTime))]
     (Clock/fixed (.toInstant dt)
                  (.getZone dt)))))

