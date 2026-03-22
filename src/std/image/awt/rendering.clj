(ns std.image.awt.rendering
  (:require [std.object.query :as reflect]
            [std.string.case :as case])
  (:import (java.awt Graphics2D RenderingHints)))

(def hint-lookup
  {"FRACTIONALMETRICS" :fractionalmetrics,
   "ANTIALIAS" :antialiasing,
   "INTERPOLATION" :interpolation,
   "TEXT_ANTIALIAS" :text-antialiasing,
   "STROKE" :stroke-control,
   "DITHER" :dithering,
   "ALPHA_INTERPOLATION" :alpha-interpolation,
   "COLOR_RENDER" :color-rendering,
   "RENDER" :rendering
   "RESOLUTION_VARIANT" :resolution-variant})

(def hint-keys
  (let [elems (->> (reflect/query-class RenderingHints [:field])
                   (filter #(.startsWith ^String (:name %) "KEY")))
        ks    (map (fn [elem] (-> (:name elem)
                                  (subs 4)
                                  (.toLowerCase)
                                  (case/spear-case)
                                  (keyword)))
                   elems)]
    (zipmap ks elems)))

(def hint-values
  (let [elems (->> (reflect/query-class RenderingHints [:field])
                   (filter #(.startsWith ^String (:name %) "VALUE")))
        groups (group-by (fn [elem]
                           (first (filter
                                   (fn [[v k]] (.startsWith ^String (:name elem)
                                                            (str "VALUE_" v)))
                                   hint-lookup)))
                         elems)]
    (reduce-kv (fn [out [n k] elems]
                 (assoc out k (reduce (fn [out elem]
                                        (let [vk (-> (:name elem)
                                                     (subs (+ 7 (count n)))
                                                     (.toLowerCase)
                                                     (case/spear-case)
                                                     (keyword))]
                                          (assoc out vk elem)))
                                      {}
                                      elems)))
               {}
               groups)))

(defn hint-options
  "show the options for hints
 
   (hint-options)
   => (contains {:color-rendering [:default :quality :speed],
                 :interpolation [:bicubic :bilinear :nearest-neighbor],
                 :antialiasing [:default :off :on],
                 :alpha-interpolation [:default :quality :speed],
                 :dithering [:default :disable :enable],
                 :rendering [:default :quality :speed],
                 :stroke-control [:default :normalize :pure],
                :text-antialiasing [:default :gasp :lcd-hbgr :lcd-hrgb
                                     :lcd-vbgr :lcd-vrgb :off :on],
                 :fractionalmetrics [:default :off :on]})"
  {:added "3.0"}
  ([]
   (zipmap (keys hint-values)
           (map (comp vec keys) (vals hint-values)))))

(defn hints
  "creates a map to be used in `setRenderingHints` on `Graphics2d` objects
 
   (hints {:antialiasing :on})
   => {RenderingHints/KEY_ANTIALIASING
       RenderingHints/VALUE_ANTIALIAS_ON}"
  {:added "3.0"}
  ([m]
   (reduce-kv (fn [out k v]
                (try
                  (assoc out
                         ((hint-keys k) RenderingHints)
                         ((get-in hint-values [k v]) RenderingHints))
                  (catch NullPointerException e
                    (throw (Exception. (str "Invalid Pair: " [k v]))))))
              {}
              m)))
