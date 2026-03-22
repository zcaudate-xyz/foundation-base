(ns std.log.match
  (:require [std.lib.foundation]
            [std.log.common :as common]))

(defn filter-base
  "matches based on input and filter
 
   (filter-base  \"hello\"  #\"h.*\")
   => true"
  {:added "3.0"}
  ([value filter]
   (cond (coll? value)
         (->> (map #(filter-base % filter) value)
              (some true?)
              (boolean))

         (fn? filter)
         (try (filter value)
              (catch Throwable t
                false))

         :else
         (let [valstr (std.lib.foundation/strn value)]
           (cond (std.lib.foundation/regexp? filter)
                 (boolean (re-find filter valstr))

                 (string? filter)
                 (= valstr filter)

                 :else
                 (= filter value))))))

(defn filter-include
  "positive if filter is empty or one of the matches hit
 
   (filter-include :hello nil)
   => true"
  {:added "3.0"}
  ([value filters]
   (cond (nil? filters)
         true

         (empty? filters)
         false

         :else
         (->> (map (partial filter-base value) filters)
              (some true?)
              (boolean)))))

(defn filter-exclude
  "negative if one of the matches hit
 
   (filter-exclude :hello nil)
   => true"
  {:added "3.0"}
  ([value filters]
   (if (empty? filters)
     true
     (->> (map (partial filter-base value) filters)
          (some true?)
          not))))

(defn filter-value
  "filters based on exclude and include filters
 
   (filter-value  \"hello\"  nil)
   => true"
  {:added "3.0"}
  ([value {:keys [include exclude ignore]}]
   (let [selected (cond exclude
                        (and (or (filter-exclude value exclude)
                                 (and (not (empty? include))
                                      (filter-include value include))))

                        :else
                        (filter-include value include))]
     (and selected
          (filter-exclude value ignore)))))

(defn match-filter
  "helper for `match`
 
   (match-filter {:log/tags   {:include [\"hello\"]}}
                 {:log/tags #{:hello :world}})"
  {:added "3.0"}
  ([filter entry]
   (->> (map (fn [[k criteria]]
               (filter-value (get entry k) criteria))
             filter)
        (every? true?))))

(defn match
  "matches the logger with event information
 
   (match {:level     :debug}
     {:log/level :debug})
   => true"
  {:added "3.0"}
  ([{:keys [level namespace function filter] :as m} entry]
   (cond (if (set? level)
           (not (get level  (:log/level entry)))
           (< (get common/+levels+ (:log/level entry))
              (get common/+levels+ level)))
         false

         (and namespace
              (not (filter-value (:log/namespace entry) namespace)))
         false

         (and function
              (not (filter-value (:log/function entry) function)))
         false

         (and filter (not (match-filter filter entry)))
         false

         :else true)))
