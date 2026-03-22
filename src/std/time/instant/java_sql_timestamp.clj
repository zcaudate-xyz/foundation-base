(ns std.time.instant.java-sql-timestamp
  (:require [std.protocol.time :as protocol.time]
            [std.time.coerce :as coerce]
            [std.time.common :as common])
  (:import (java.sql Timestamp) (java.text SimpleDateFormat) (java.util Calendar Date TimeZone)))

(def timestamp-meta
  {:type :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map   {:from {:proxy Calendar
                  :via (fn [^Calendar cal]
                         (Timestamp. (.getTime (.getTime cal))))}
           :to   {:proxy Calendar
                  :via (fn [^Timestamp t {:keys [timezone]}]
                         (common/calendar t (coerce/coerce-zone timezone {:type TimeZone})))}}})

(defmethod protocol.time/-time-meta Timestamp
  ([_]
   timestamp-meta))

(extend-type Timestamp
  protocol.time/IInstant
  (-to-long       [t] (.getTime t))
  (-has-timezone? [t] false)
  (-get-timezone  [t] nil)
  (-with-timezone [t _] t))

(defmethod protocol.time/-from-long Timestamp
  ([^Long long _]
   (Timestamp. long)))

(defmethod protocol.time/-now Timestamp
  ([_]
   (Timestamp. (.getTime (Date.)))))

