(ns std.lib.system.topology
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.lib.sort :as sort]))

(defn long-form-imports
  "converts short form imports to long form
 
   (long-form-imports [:db [:file {:as :fs}]])
   => {:db   {:type :single, :as :db},
       :file {:type :single, :as :fs}}
 
   (long-form-imports [[:ids {:type :element :as :id}]])
   => {:ids {:type :element, :as :id}}"
  {:added "3.0"}
  ([args]
   (->> args
        (map (fn [x]
               (cond (keyword? x)
                     [x {:type :single :as x}]
                     (vector? x)
                     [(first x) (merge {:type :single} (second x))])))
        (into {}))))

(defn long-form-entry
  "converts short form entry into long form"
  {:added "3.0"}
  ([[desc & args]]
   (let [dependencies  (keep (fn [x] (if (vector? x)
                                       (if-not (:nocheck (second x))
                                         (first x))
                                       x))
                             args)
         [desc form] (if (vector? desc)
                       [(first desc) {:compile :array}]
                       [desc {:compile :single}])
         desc (cond (or (fn? desc)
                        (instance? clojure.lang.MultiFn desc))
                    {:type :build :constructor desc}

                    (:type desc) desc

                    (:expose desc)
                    (-> desc
                        (dissoc :expose)
                        (assoc :type :expose :in (first dependencies) :function (:expose desc)))

                    :else
                    (assoc desc :type :build))]
     (cond-> (merge form desc)
       (= :build (:type desc))
       (assoc :import (long-form-imports args))

       :finally
       (assoc :dependencies dependencies)))))

(defn long-form
  "converts entire topology to long form"
  {:added "3.0"}
  ([topology]
   (coll/map-vals long-form-entry topology)))

(defn get-dependencies
  "get dependencies for long form"
  {:added "3.0"}
  ([full-topology]
   (coll/map-vals (comp set :dependencies) full-topology)))

(defn get-exposed
  "get exposed keys for long form"
  {:added "3.0"}
  ([full-topology]
   (reduce-kv (fn [arr k v]
                (if (= :expose (:type v))
                  (conj arr k)
                  arr))
              [] full-topology)))

(defn all-dependencies
  "gets all dependencies for long form"
  {:added "3.0"}
  ([m]
   (let [order (sort/topological-sort m)]
     (reduce (fn [out key]
               (let [inputs (set (get m key))
                     result (set (concat inputs (mapcat out inputs)))]
                 (assoc out
                        key
                        result)))
             {}
             order))))
