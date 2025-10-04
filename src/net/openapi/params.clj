(ns net.openapi.params
  (:require [std.string :as str])
  (:import (java.io File)
           (java.util Date TimeZone)
           (java.text SimpleDateFormat)))

(defn- ^SimpleDateFormat make-date-format
  ([^String format-str] (make-date-format format-str nil))
  ([^String format-str ^String time-zone]
   (let [date-format (SimpleDateFormat. format-str)]
     (when time-zone
       (.setTimeZone date-format (TimeZone/getTimeZone time-zone)))
     date-format)))

(defn format-date
  "Format the given Date object with the :date-format defined in *api-options*.
   NOTE: The UTC time zone is used."
  {:added "4.0"}
  [^Date date date-format]
  (-> (make-date-format date-format "UTC")
      (.format date)))

(defn parse-date
  "Parse the given string to a Date object with the :date-format defined in *api-options*.
   NOTE: The UTC time zone is used."
  {:added "4.0"}
  [^String s date-format]
  (-> (make-date-format date-format "UTC")
      (.parse s)))

(defn param->str
  "Format the given parameter value to string."
  {:added "4.0"}
  [param date-format]
  (cond
    (instance? Date param) (format-date param date-format)
    (sequential? param) (str/join "," param)
    :else (str param)))

(declare normalize-param)

(defn normalize-array-param
  "Normalize array parameter according to :collection-format specified in the parameter's meta data.
   When the parameter contains File, a seq is returned so as to keep File parameters.
   For :multi collection format, a seq is returned which will be handled properly by clj-http.
   For other cases, a string is returned."
  {:added "4.0"}
  [xs]
  (if (some (partial instance? File) xs)
    (map normalize-param xs)
    (case (-> (meta xs) :collection-format (or :csv))
      :csv (str/join "," (map normalize-param xs))
      :ssv (str/join " " (map normalize-param xs))
      :tsv (str/join "\t" (map normalize-param xs))
      :pipes (str/join "|" (map normalize-param xs))
      :multi (map normalize-param xs))))

(defn normalize-param
  "Normalize parameter value, handling three cases:
   for sequential value, apply `normalize-array-param` which handles collection format;
   for File value, use current value;
   otherwise, apply `param->str`."
  {:added "4.0"}
  [param]
  (cond
    (sequential? param) (normalize-array-param param)
    (instance? File param) param
    :else (param->str param)))

(defn normalize-params
  "Normalize parameters values: remove nils, format to string with `param->str`."
  {:added "4.0"}
  [params]
  (->> params
       (remove (comp nil? second))
       (map (fn [[k v]] [k (normalize-param v)]))
       (into {})))

(defn make-url
  "Make full URL by adding base URL and filling path parameters."
  {:added "4.0"}
  [base-url path path-params]
  (let [path (reduce (fn [p [k v]]
                       (str/replace p (re-pattern (str "\\{" k "\\}")) (normalize-param v)))
                     path
                     path-params)]
    (str base-url path)))
