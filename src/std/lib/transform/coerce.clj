(ns std.lib.transform.coerce
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h])
  (:import (java.text ParseException)))

(def ^:dynamic *handler* nil)

(def ^java.text.SimpleDateFormat date-format-json
  (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss'Z'"))

(def ^java.text.SimpleDateFormat date-format-js
  (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

(defn assoc-set
  "associates a set as keys to a map
   (assoc-set {} #{:a :b :c} 1)
   => {:a 1, :b 1, :c 1}"
  {:added "3.0"}
  ([m s v]
   (if (set? s)
     (apply assoc m (interleave s (repeat v)))
     (assoc m s v)))
  ([m s v & more]
   (let [out (assoc-set m s v)]
     (if more
       (apply assoc-set out more)
       out))))

(defn hash-mapset
  "constructs a hashmap from a set of keys
 
   (hash-mapset #{:a :b :c} 1 #{:d :e} 2)
   => {:c 1, :b 1, :a 1, :e 2, :d 2}"
  {:added "3.0"}
  ([s v]
   (assoc-set {} s v))
  ([s v & more]
   (apply assoc-set {} s v more)))

(defn read-enum
  "helper function for enums
 
   (read-enum \":enum\") => :enum
 
   (read-enum \"12334\") => 12334
 
   (read-enum \"{}\") => (throws)"
  {:added "3.0"}
  ([s]
   (let [v (read-string s)]
     (cond (or (keyword? v)
               (integer? v))
           v
           :else
           (h/error  (str "READ_ENUM: " v " is not a proper :enum value"))))))

(defn read-ref
  "helper function for refs
 
   (read-ref \"{:a 1}\") => {:a 1}
 
   (read-ref \"_\") => (throws)"
  {:added "3.0"}
  ([s]
   (let [v (read-string s)]
     (cond (or (integer? v)
               (coll/hash-map? v))
           v
           :else
           (h/error  (str "READ_REF: " v " is not a proper :ref value"))))))

(def Numbers
  #{java.lang.Integer java.lang.Long java.lang.Float java.lang.Double clojure.lang.Ratio clojure.lang.BigInt java.math.BigDecimal})

(def Strings
  #{java.util.UUID java.net.URI})

(defn parse-date
  "converts string to date
 
   (parse-date \"2017-05-25T17:29:46.000Z\")
   ;;#inst \"2017-05-25T09:29:46.000-00:00\"
   => java.util.Date"
  {:added "3.0"}
  ([^String s]
   (case (count s)
     20 (.parse date-format-json s)
     24 (.parse date-format-js s)
     (if-let [parse-fn (:date-string *handler*)]
       (parse-fn s)
       (h/error "Cannot parse string to date" {:input s})))))

(def from-string-chart
  {:keyword (fn [v] (keyword v))
   :bigint  (fn [v] (BigInteger. ^String v))
   :bigdec  (fn [v] (BigDecimal. ^String v))
   :long    (fn [v] (Long/parseLong v))
   :float   (fn [v] (Float/parseFloat v))
   :double  (fn [v] (Double/parseDouble v))
   :instant (fn [v] (parse-date v))
   :uuid    (fn [v] (h/uuid v))
   :uri     (fn [v] (h/uri v))
   :enum    (fn [v] (read-enum v))
   :ref     (fn [v] (read-ref v))})

(def default-coerce-to-string-chart
  (->> from-string-chart
       (map (fn [[k f]] [k {java.lang.String f}]))
       (into {})))

(def default-coerce-chart
  (coll/merge-nested
   default-coerce-to-string-chart
   {:keyword
    (hash-mapset Strings           (fn [v] (keyword (str v)))
                 Numbers           (fn [v] (keyword (str v))))
    :string
    (hash-mapset java.util.Date (fn [^String v] (.format date-format-js v))
                 clojure.lang.Keyword (fn [v] (name v))
                 Strings (fn [v] (str v))
                 Numbers (fn [v] (str v)))
    :bigint
    (hash-mapset Numbers (fn [v] (bigint v)))
    :bigdec
    (hash-mapset Numbers (fn [v] (bigdec v)))
    :long
    (hash-mapset Numbers (fn [v] (long v))
                 java.util.Date (fn [^java.util.Date v] (.getTime v)))
    :float
    (hash-mapset Numbers (fn [v] (float v)))
    :double
    (hash-mapset Numbers (fn [v] (double v)))
    :instant
    (hash-mapset Numbers (fn [v] (java.util.Date. (long v))))
    :enum
    (hash-mapset Numbers (fn [v] (long v)))}))

(def ^:dynamic *coerce-chart* default-coerce-chart)

(defn coerce
  "associates a set as keys to a map
   (coerce 1 :string)
   => \"1\"
 
   (coerce  \"2017-05-25T17:29:46.000Z\" :instant)
   => (java.util.Date. 117 4 25 17 29 46)
 
   (coerce \"2017-05-25T17:29:46Z\" :instant)
   => (java.util.Date. 117 4 25 17 29 46)
 
   (coerce \"oeuoe\" :keyword)
   => :oeuoe"
  {:added "3.0"}
  ([v t]
   (coerce v t nil))
  ([v t chart]
   (if-let [c-fn (get-in (coll/merge-nested *coerce-chart* chart) [t (type v)])]
     (c-fn v)
     (h/error "Not coercible" {:type {:from (type v) :to t}
                               :input v}))))
