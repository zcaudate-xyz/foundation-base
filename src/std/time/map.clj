(ns std.time.map
  (:require [std.protocol.time :as protocol.time]
            [std.time.common :as common])
  (:import (clojure.lang PersistentArrayMap PersistentHashMap) (java.text SimpleDateFormat) (java.util Calendar Date TimeZone)))

(defn to-map
  "converts an instant to a map
   (to-map 0 {:timezone \"GMT\"} common/+default-keys+)
   => {:type java.lang.Long, :timezone \"GMT\", :long 0
       :year 1970, :month 1, :day 1,
       :hour 0, :minute 0 :second 0 :millisecond 0}
 
   (to-map (Date. 0) {:timezone \"EST\"}
           [:year :day :month])
   => {:type java.util.Date, :timezone \"EST\", :long 0
       :year 1969, :day 31, :month 12}
 
   (to-map {:type java.lang.Long, :timezone \"GMT\", :long 0
            :year 1970, :month 1, :day 1,
            :hour 0, :minute 0 :second 0 :millisecond 0}
           {:timezone \"EST\"}
           common/+default-keys+)"
  {:added "3.0"}
  ([t opts]
   (to-map t opts common/+default-keys+))
  ([t {:keys [timezone] :as opts} ks]
   (let [tmeta (protocol.time/-time-meta (class t))
         [p pmeta] (let [{:keys [proxy via]} (-> tmeta :map :to)]
                     (if (and proxy via)
                       [(via t opts) (protocol.time/-time-meta proxy)]
                       [t tmeta]))
         p         (if timezone
                     (protocol.time/-with-timezone p timezone)
                     p)
         fns       (select-keys common/+default-fns+ ks)
         output    (reduce-kv (fn [out k t-fn]
                                (assoc out k (t-fn p opts)))
                              {}
                              fns)]
     (-> output
         (assoc :timezone (protocol.time/-get-timezone p)
                :type     (class t)
                :long     (protocol.time/-to-long t))))))

(defn from-map
  "converts a map back to an instant type
   (from-map {:type java.lang.Long
              :year 1970, :month 1, :day 1,
              :hour 0, :minute 0 :second 0 :millisecond 0
              :timezone \"GMT\"}
             {:timezone \"Asia/Kolkata\"}
             {})
   => 0
 
   (-> (from-map {:type java.util.Calendar
                  :year 1970, :month 1, :day 1,
                  :hour 0, :minute 0 :second 0 :millisecond 0
                  :timezone \"GMT\"}
                 {:timezone \"Asia/Kolkata\"}
                 {})
       (to-map {} common/+default-keys+))
   => {:type java.util.GregorianCalendar, :timezone \"Asia/Kolkata\", :long 0
       :year 1970, :month 1, :day 1,
       :hour 5, :minute 30 :second 0 :millisecond 0}
 
   (to-map (common/calendar (Date. 0)
                            (TimeZone/getTimeZone \"EST\"))
           {:timezone \"GMT\"} [:month :day :year])
   => {:type java.util.GregorianCalendar, :timezone \"GMT\", :long 0,
       :year 1970 :month 1, :day 1}"
  {:added "3.0"}
  ([m opts fill]
   (let [m    (merge fill m)
         type (or (:type opts)
                  (:type m))
         {:keys [proxy via] :as tmeta} (get-in (protocol.time/-time-meta type)
                                               [:map :from])
         output (cond proxy
                      (-> (assoc m :type proxy)
                          (from-map {} {})
                          (via))

                      :else
                      ((:fn tmeta) m))]
     (if-let [tz (:timezone opts)]
       (protocol.time/-with-timezone output tz)
       output))))

(defn with-timezone
  "adds the timezone to a Calendar object
 
   (-> (to-map 0 {:timezone \"EST\"})
       (with-timezone \"GMT\")
       (dissoc :type))
   => (-> (to-map 0 {:timezone \"GMT\"})
          (dissoc :type))"
  {:added "3.0"}
  ([{:keys [long timezone] :as m} tz]
   (cond (= timezone tz)
         m

         long
         (protocol.time/-from-long long {:type PersistentArrayMap
                                         :timezone tz})

         :else
         (-> m
             (from-map {:type Calendar :timezone tz} {})
             (to-map {} common/+default-keys+)))))

(def arraymap-meta
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map       {:from {:fn (fn [m] (assoc m :type PersistentArrayMap))}}})

(defmethod protocol.time/-time-meta PersistentArrayMap
  ([_]
   arraymap-meta))

(extend-type PersistentArrayMap
  protocol.time/IInstant
  (-to-long [{:keys [long] :as m}]
    (from-map m {:type Long} {}))
  (-has-timezone? [m] (not (nil? (:timezone m))))
  (-get-timezone  [m] (:timezone m))
  (-with-timezone [m tz]
    (assoc (with-timezone m tz) :type PersistentArrayMap))

  protocol.time/IRepresentation
  (-millisecond  [t _] (:millisecond t))
  (-second       [t _] (:second t))
  (-minute       [t _] (:minute t))
  (-hour         [t _] (:hour t))
  (-day          [t _] (:day t))
  (-day-of-week  [t _] (:day-of-week t))
  (-month        [t _] (:month t))
  (-year         [t _] (:year t)))

(defmethod protocol.time/-from-long PersistentArrayMap
  ([long opts]
   (-> (to-map long opts common/+default-keys+)
       (assoc :type PersistentArrayMap))))

(defmethod protocol.time/-now PersistentArrayMap
  ([opts]
   (-> (to-map (protocol.time/-now (assoc opts :type Calendar))
               opts
               (or (:keys opts)
                   common/+default-keys+))
       (assoc :type PersistentArrayMap))))

(def hashmap-meta
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map       {:from {:fn (fn [m] (assoc m :type PersistentHashMap))}}})

(defmethod protocol.time/-time-meta PersistentHashMap
  ([_]
   hashmap-meta))

(defmethod protocol.time/-from-long PersistentHashMap
  ([long opts]
   (-> (to-map long opts common/+default-keys+)
       (assoc :type PersistentHashMap))))

(extend-type PersistentHashMap
  protocol.time/IInstant
  (-to-long [{:keys [long] :as m}]
    (from-map m {:type Long} {}))
  (-has-timezone? [m] (not (nil? (:timezone m))))
  (-get-timezone  [m] (:timezone m))
  (-with-timezone [m tz] (assoc (with-timezone m tz) :type PersistentHashMap))

  protocol.time/IRepresentation
  (-millisecond  [t _] (:millisecond t))
  (-second       [t _] (:second t))
  (-minute       [t _] (:minute t))
  (-hour         [t _] (:hour t))
  (-day          [t _] (:day t))
  (-day-of-week  [t _] (:day-of-week t))
  (-month        [t _] (:month t))
  (-year         [t _] (:year t)))

(defmethod protocol.time/-now PersistentHashMap
  ([opts]
   (-> (to-map (protocol.time/-now (assoc opts :type Calendar))
               opts
               (or (:keys opts)
                   common/+default-keys+))
       (assoc :type PersistentHashMap))))
