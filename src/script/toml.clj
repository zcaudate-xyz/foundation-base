(ns script.toml
  (:refer-clojure :exclude [read])
  (:import (com.moandjiezana.toml Toml) (java.util HashMap ArrayList)))


;;======================================================================================================================
;; Reader
;; =====================================================================================================================


(defn- java->clojure* [data {keywordize :keywordize :as params}]
  (cond
    ;; hashmap -> map
    (instance? java.util.HashMap data) (reduce (fn [res next] (assoc res (if (:keywordize params)
                                                                           (keyword next)
                                                                           next)
                                                                     (java->clojure* (get data next) params)))
                                               {}
                                               (into [] (.keySet ^java.util.HashMap data)))
    ;; array -> vec
    (instance? java.util.ArrayList data) (mapv #(java->clojure* % params) data)
    :else data))

(defn java->clojure
  "converts java object to map"
  {:added "3.0"}
  ([data & params]
   (java->clojure* data (reduce #(assoc %1 %2 true) {} params))))

(defn clojure->java
  "converts map to java object"
  {:added "3.0"}
  ([data]
   (cond
    ;; map -> HashMap
     (map? data) (let [res (HashMap.)]
                   (doseq [[key val] data]
                     (.put res
                           (if (keyword? key) (name key) key)
                           (clojure->java val)))
                   res)
    ;; seq -> ArrayList
     (sequential? data) (let [res (ArrayList.)]
                          (doseq [val data]
                            (.add res (clojure->java val)))
                          res)
     :else data)))

(defn read
  "reads toml to map"
  {:added "3.0"}
  ([^String str & params]
   (let [toml (Toml.)
         java-result (.toMap (.read toml str))]
     (apply java->clojure (cons java-result params)))))

;;======================================================================================================================
;; Writer
;; =====================================================================================================================
(defn- print-name [name ^StringBuffer sb]
  (when (seq name)
    (.append sb "\n[")
    (.append sb name)
    (.append sb "]\n")))

(defn- compose-deep-name [deep-name nested-name]
  (let [deep-name (name deep-name)
        nested-name (name nested-name)]
    (if (seq deep-name)
      (str deep-name "." nested-name)
      nested-name)))

(defn write
  "writes map to toml"
  {:added "3.0"}
  ([data] (write data "" (StringBuffer.)))
  ([data deep-name ^StringBuffer sb]
   (let [simple-vals (filter (fn [[k v]] (not (map? v))) data)
         complex-vals (filter (fn [[k v]] (map? v)) data)]
     (when (seq simple-vals)
       (print-name deep-name sb)
       (doseq [[k v] simple-vals]
         (.append sb (name k))
         (.append sb " = ")
         (.append sb (pr-str v))
         (.append sb "\n")))
     (doseq [[dp v] complex-vals]
       (write v (compose-deep-name deep-name dp) sb))
     (.toString sb))))
