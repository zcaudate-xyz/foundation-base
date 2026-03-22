(ns std.image.awt.common
  (:require [std.image.base.common :as base]
            [std.image.base.model :as model]
            [std.image.base.size :deps true]
            [std.image.protocol :as protocol.image]
            [std.lib.collection]
            [std.object.query :as reflect]
            [std.string.case])
  (:import (java.awt.image BufferedImage)))

(defonce type-lookup
  (->> (reflect/query-class java.awt.image.BufferedImage [#"TYPE"])
       (map (juxt (comp keyword
                        #(subs % 5)
                        std.string.case/spear-case
                        :name)
                  #(% java.awt.image.BufferedImage)))
       (into {})))

(defonce name-lookup
  (std.lib.collection/transpose type-lookup))

(defn image-size
  "returns the size of the BufferedImage
 
   (-> (image {:size [2 2]
               :model :byte-gray
               :data (byte-array [10 20 30 40])})
       (image-size))
   => {:height 2, :width 2}"
  {:added "3.0"}
  ([^BufferedImage img]
   {:height (.getHeight img)
    :width (.getWidth img)}))

(defn image-model
  "returns the model associated with the BufferedImage
 
   (-> (image {:size [2 2]
               :model :byte-gray
               :data (byte-array [10 20 30 40])})
       (image-model))
   => (contains-in {:type :gray
                    :meta [{:type Byte/TYPE :span 1}],
                    :data {:raw {:type Byte/TYPE, :channel 0, :index 0}},
                    :label :byte-gray
                   :channel {:count 1}})"
  {:added "3.0"}
  ([^BufferedImage img]
   (-> (.getType img)
       name-lookup
       model/model)))

(defn image-data
  "returns the data contained within the BufferedImage
 
   (-> (image {:size [2 2]
               :model :byte-gray
               :data (byte-array [10 20 30 40])})
       (image-data)
       (vec))
   => [10 20 30 40]"
  {:added "3.0"}
  ([^BufferedImage img]
   (let [w (.getWidth img)
         h (.getHeight img)]
     (-> img
         (.getRaster)
         (.getDataElements 0 0 w h nil)))))

(defn image-channels
  "returns the channels for the image
 
   (->> (image {:size [2 2]
                :model :byte-gray
                :data (byte-array [10 20 30 40])})
        (image-channels)
        (mapv vec))
   => [[10 20 30 40]]"
  {:added "3.0"}
  ([img]
   [(image-data img)]))

(defn subimage
  "returns the subimage of the image
 
   (-> (image {:size  [4 4]
               :model :byte-gray
               :data  (byte-array [10 20 30 40
                                   10 20 30 40
                                   10 20 30 40
                                   10 20 30 40])})
       (subimage 1 1 2 2)
       (image-data)
      vec)
   => [20 30 20 30]"
  {:added "3.0"}
  ([^BufferedImage img x y w h]
   (.getSubimage img x y w h)))

(defn image-to-byte-gray
  "returns the data with data in gray
 
   (-> (image {:size  [4 4]
               :model :byte-gray
               :data  (byte-array [10 20 30 40
                                   10 20 30 40
                                   10 20 30 40
                                   10 20 30 40])})
       (image-to-byte-gray)
       (vec))
  => [10 20 30 40 10 20 30 40 10 20 30 40 10 20 30 40]"
  {:added "3.0"}
  ([img]
   (:data (base/convert img (model/model :byte-gray)))))

(defn image-to-int-argb
  "returns the data associated with argb
 
   (->> (image {:size  [2 2]
                :model :byte-gray
                :data  (byte-array [10 20
                                    30 40])})
        (image-to-int-argb)
        (mapv util/int->bytes))
   => [[255 10 10 10] [255 20 20 20]
       [255 30 30 30] [255 40 40 40]]"
  {:added "3.0"}
  ([img]
   (:data (base/convert img (model/model :int-argb)))))

(defn image
  "creates a `Bufferedimage` from a map
 
   (image {:size [2 2]
           :model :byte-gray
           :data (byte-array [10 20 30 40])})
   ;;=> #img[2 2]{:type java.awt.image.BufferedImage, :model :byte-gray}"
  {:added "3.0"}
  ([{:keys [size model data] :as m}]
   (image size model data))
  ([size model data]
   (let [w (protocol.image/-width size)
         h (protocol.image/-height size)
         model (model/model model)
         img (BufferedImage. w h (type-lookup (:label model)))
         raster (.getRaster img)]
     (if data (.setDataElements raster 0 0 w h data))
     img)))
