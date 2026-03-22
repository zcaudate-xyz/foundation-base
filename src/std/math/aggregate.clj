(ns std.math.aggregate
  (:require [std.lib.collection :as collection]
            [std.math.common :as math]))

(defn max-fn
  "max taking an array as input
 
   (max-fn [1 2 3 4 5])
   => 5"
  {:added "3.0"}
  ([arr]
   (if-not (empty? arr)
     (apply max arr))))

(defn min-fn
  "min taking an array as input
 
   (min-fn [1 2 3 4 5])
   => 1"
  {:added "3.0"}
  ([arr]
   (if-not (empty? arr)
     (apply min arr))))

(defn range-fn
  "difference between max and min
 
   (range-fn [2 3 4 5])
   => 3"
  {:added "3.0"}
  ([arr]
   (if-not (empty? arr)
     (- (apply max arr) (apply min arr)))))

(defn middle-fn
  "finds the middle value in the array
 
   (middle-fn [3 4 5])
   => 4"
  {:added "3.0"}
  ([arr]
   (let [len (count arr)]
     (nth arr (long (/ len 2))))))

(defn wrap-not-nil
  "removes values that are nil"
  {:added "3.0"}
  ([f]
   (fn [arr]
     (->> (filter identity arr)
          (f)))))

(def +aggregations+
  (collection/map-vals wrap-not-nil
              {:first    first
               :last     last
               :middle   middle-fn
               :mean     (comp float math/mean)
               :sum      #(apply + %)
               :max      max-fn
               :min      min-fn
               :range    range-fn
               :stdev    math/stdev
               :mode     math/mode
               :median   math/median
               :skew     math/skew
               :variance math/variance
               :random   rand-nth}))

(defn aggregates
  "finds the aggregates of the array
 
   (aggregates [1 2 3 3 4 5])
   => (contains
       {:min 1, :mean 3.0, :stdev 1.4142135623730951,
        :skew 0.0, :mode [3], :variance 2, :median 3, :max 5,
        :random number?, :middle 3, :first 1, :last 5, :sum 18, :range 4})
 
   (aggregates [1 2 3 3 4 5] [:sum]
               {:product #(apply * %)})
   => {:sum 18, :product 360}"
  {:added "3.0"}
  ([arr]
   (collection/map-vals (fn [f] (f arr)) +aggregations+))
  ([arr ks]
   (collection/map-vals (fn [f] (f arr))
               (select-keys +aggregations+ ks)))
  ([arr ks m]
   (merge (aggregates arr ks)
          (collection/map-vals (fn [f] (f arr))
                      m))))
