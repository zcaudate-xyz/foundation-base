(ns net.openapi.params
  (:require [clojure.string :as string])
  (:import (java.io File) (java.util Date TimeZone) (java.text SimpleDateFormat)))

(defn ^:private ^SimpleDateFormat make-date-format
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
  ([param]
  (param->str param nil))
  ([param date-format]
  (cond
    (instance? Date param) (format-date param date-format)
    (keyword? param) (name param)
    (symbol? param) (name param)
    (sequential? param) (string/join "," param)
    :else (str param))))

(declare normalize-param)

(defn encode-path-segment
  [value]
  (-> (java.net.URLEncoder/encode (str value) "UTF-8")
     (.replace "+" "%20")))

(defn normalize-array-param
  "Normalize array parameter according to :collection-format specified in the parameter's meta data.
  When the parameter contains File, a seq is returned so as to keep File parameters.
  For :multi collection format, a seq is returned which will be handled properly by clj-http.
  For other cases, a string is returned."
  {:added "4.0"}
  ([xs]
  (normalize-array-param xs nil))
  ([xs date-format]
  (if (some (partial instance? File) xs)
    (map #(normalize-param % date-format) xs)
    (case (-> (meta xs) :collection-format (or :csv))
      :csv (string/join "," (map #(normalize-param % date-format) xs))
      :ssv (string/join " " (map #(normalize-param % date-format) xs))
      :tsv (string/join "\t" (map #(normalize-param % date-format) xs))
      :pipes (string/join "|" (map #(normalize-param % date-format) xs))
      :multi (map #(normalize-param % date-format) xs)))))

(defn normalize-param
  "Normalize parameter value, handling three cases:
  for sequential value, apply `normalize-array-param` which handles collection format;
  for File value, use current value;
  otherwise, apply `param->str`."
  {:added "4.0"}
  ([param]
  (normalize-param param nil))
  ([param date-format]
  (cond
    (sequential? param) (normalize-array-param param date-format)
    (instance? File param) param
    :else (param->str param date-format))))

(defn normalize-params
  "Normalize parameters values: remove nils, format to string with `param->str`."
  {:added "4.0"}
  ([params]
  (normalize-params params nil))
  ([params date-format]
  (->> params
       (remove (comp nil? second))
       (map (fn [[k v]] [k (normalize-param v date-format)]))
       (into {}))))

(defn make-url
  "Make full URL by adding base URL and filling path parameters."
  {:added "4.0"}
  [base-url path path-params]
  (let [path (reduce (fn [p [k v]]
                      (let [k (cond
                                (keyword? k) (name k)
                                (symbol? k) (name k)
                                :else (str k))]
                        (string/replace p
                                        (re-pattern (str "\\{" (java.util.regex.Pattern/quote k) "\\}"))
                                        (encode-path-segment (normalize-param v)))))
                     path
                     path-params)]
   (str (or base-url "") path)))
