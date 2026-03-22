(ns std.time.format.java-text-simpledateformat
  (:require [std.protocol.time :as protocol.time]
            [std.time.coerce :as coerce]
            [std.time.map :as map]
            [std.time.zone :as zone])
  (:import (clojure.lang PersistentArrayMap PersistentHashMap) (java.sql Timestamp) (java.text SimpleDateFormat) (java.util Calendar Date TimeZone)))

(defmethod protocol.time/-formatter SimpleDateFormat
  ([s {:keys [timezone] :as opts}]
   (let [fmt (SimpleDateFormat. s)
         _ (if timezone
             (.setTimeZone fmt (coerce/coerce-zone timezone
                                                   {:type TimeZone})))]
     fmt)))

(defmethod protocol.time/-format [SimpleDateFormat Date]
  ([^SimpleDateFormat formatter ^Date t {:keys [timezone]}]
   (if timezone
     (.setTimeZone formatter (coerce/coerce-zone timezone
                                                 {:type TimeZone}))) (.format formatter t)))

(defmethod protocol.time/-format [SimpleDateFormat Timestamp]
  ([^SimpleDateFormat formatter ^Timestamp t {:keys [timezone]}]
   (if timezone
     (.setTimeZone formatter (coerce/coerce-zone timezone
                                                 {:type TimeZone}))) (.format formatter t)))

(defmethod protocol.time/-format [SimpleDateFormat Calendar]
  ([^SimpleDateFormat formatter ^Calendar t {:keys [timezone]}]
   (let [timezone (or timezone
                      (.getTimeZone t))
         _ (.setTimeZone formatter (coerce/coerce-zone timezone
                                                       {:type TimeZone}))
         t (.getTime t)]
     (.format formatter t))))

(defmethod protocol.time/-format [SimpleDateFormat PersistentArrayMap]
  ([^SimpleDateFormat formatter ^PersistentHashMap m {:keys [timezone] :as opts}]
   (protocol.time/-format formatter (map/from-map m {:type Calendar} opts) opts)))

(defmethod protocol.time/-format [SimpleDateFormat PersistentHashMap]
  ([^SimpleDateFormat formatter ^PersistentHashMap m {:keys [timezone] :as opts}]
   (protocol.time/-format formatter (map/from-map m {:type Calendar} opts) opts)))

(defmethod protocol.time/-parser SimpleDateFormat
  ([s {:keys [timezone] :as opts}]
   (SimpleDateFormat. s)))

(defmethod protocol.time/-parse [SimpleDateFormat Date]
  ([^SimpleDateFormat parser s opts]
   (.parse parser s)))

(defmethod protocol.time/-parse [SimpleDateFormat Calendar]
  ([^SimpleDateFormat parser s opts]
   (let [_ (.parse parser s)
         cal (.getCalendar parser)
         offset (.get cal Calendar/ZONE_OFFSET)
         tz     (get zone/by-offset offset)
         _      (.setTimeZone cal (coerce/coerce-zone tz {:type TimeZone}))]
     cal)))

(defmethod protocol.time/-parse [SimpleDateFormat Timestamp]
  ([^SimpleDateFormat parser s opts]
   (let [_ (.parse parser s)]
     (-> (.getCalendar parser)
         (.getTime)
         (.getTime)
         (Timestamp.)))))

(defmethod protocol.time/-parse [SimpleDateFormat PersistentArrayMap]
  ([^SimpleDateFormat parser s opts]
   (-> (protocol.time/-parse parser s (assoc opts :type Calendar))
       (map/to-map opts))))

(defmethod protocol.time/-parse [SimpleDateFormat PersistentHashMap]
  ([^SimpleDateFormat parser s opts]
   (-> (protocol.time/-parse parser s (assoc opts :type Calendar))
       (map/to-map opts))))
