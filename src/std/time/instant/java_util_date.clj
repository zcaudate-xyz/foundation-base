(ns std.time.instant.java-util-date
  (:require [std.protocol.time :as protocol.time]
            [std.time.coerce :as coerce]
            [std.time.common :as common])
  (:import (java.text SimpleDateFormat) (java.util Calendar Date TimeZone)))

(def date-meta
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map  {:from {:proxy Calendar
                 :via (fn [^Calendar cal]
                        (.getTime cal))}
          :to   {:proxy Calendar
                 :via (fn [^Date t {:keys [timezone]}]
                        (common/calendar t
                                         (coerce/coerce-zone timezone {:type TimeZone})))}}})

(defmethod protocol.time/-time-meta Date
  ([_]
   date-meta))

(extend-type Date
  protocol.time/IInstant
  (-to-long       [t] (.getTime t))
  (-has-timezone? [t] false)
  (-get-timezone  [t] nil)
  (-with-timezone [t _] t))

(defmethod protocol.time/-from-long Date
  ([^Long long _]
   (Date. long)))

(defmethod protocol.time/-now Date
  ([_]
   (Date.)))
