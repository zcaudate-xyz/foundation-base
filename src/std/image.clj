(ns std.image
  (:require [std.image.awt]
            [std.image.base]
            [std.image.base.common :as common]
            [std.image.base.model :as model]
            [std.image.protocol :as protocol.image]
            [std.lib.invoke :as invoke])
  (:import (java.awt.image BufferedImage))
  (:refer-clojure :exclude [read]))

(invoke/definvoke default-type
  "displays and sets the default type
 
   (default-type std.image.base.Image)
 
   (default-type java.awt.image.BufferedImage)
 
   (default-type)
   => java.awt.image.BufferedImage"
  {:added "3.0"}
  [:dynamic {:val BufferedImage}])

(invoke/definvoke default-model
  "displays and set the default model
 
   (:label (default-model))
   => :int-argb"
  {:added "3.0"}
  [:dynamic {:val (model/model :int-argb)}])

(invoke/definvoke default-view
  "displays and set the default view
 
   (default-view)
   => :awt"
  {:added "3.0"}
  [:dynamic {:val :awt}])

(defn image?
  "checks whether object is an image
 
   (image? (image/read \"test-data/std.image/circle-30.png\"))
   => true"
  {:added "3.0"}
  ([obj]
   (extends? protocol.image/IRepresentation (type obj))))

(defn image-channels
  "returns the channels of an image
 
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (image-channels))
   ;;[#object[\"[B\" 0x76926cfc \"[B@76926cfc\"]]"
  {:added "3.0"}
  ([image]
   (protocol.image/-channels image)))

(defn image-size
  "returns the size of the image
 
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (image-size))
   => {:height 30, :width 30}"
  {:added "3.0"}
  ([image]
   (protocol.image/-size image)))

(defn image-model
  "returns the model associated with the image
 
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (image-model))
   => (model/model :4-byte-abgr)"
  {:added "3.0"}
  ([image]
   (protocol.image/-model image)))

(defn image-data
  "returns the raw data associated with the image
 
   (->> (image/read \"test-data/std.image/circle-30.png\")
        (image-data)
        (take 10))
   => [-1 -1 -1 -1 -1 -1 -1 -1 -1 -1]"
  {:added "3.0"}
  ([image]
   (protocol.image/-data image)))

(defn size?
  "checks whether object is a size
 
   (size? [10 10]) => true
 
   (size? {:height 10 :width 10}) => true"
  {:added "3.0"}
  ([obj]
   (cond (map? obj)
         (-> (keys obj) set (= #{:height :width}))

         (vector? obj)
         (-> (count obj) (= 2))

         :else
         (extends? protocol.image/ISize (type obj)))))

(defn height
  "returns the height of an image
 
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (image/height))
   => 30"
  {:added "3.0"}
  ([obj]
   (cond (image? obj)
         (protocol.image/-height (image-size obj))

         :else
         (protocol.image/-height obj))))

(defn width
  "returns the width of an image
 
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (image/width))
   => 30"
  {:added "3.0"}
  ([obj]
   (cond (image? obj)
         (protocol.image/-width (image-size obj))

         :else
         (protocol.image/-width obj))))

(defn subimage
  "returns a subimage given image and bounds
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (subimage 10 10 10 10)
       (image-size))
   ;; #image.awt[10 10]{:model :4-byte-abgr}
   => {:height 10, :width 10}"
  {:added "3.0"}
  ([image x y w h]
   (protocol.image/-subimage image x y w h)))

(defn blank
  "returns a blank image
 
   (image/blank [10 10])
   ;;#image.awt[10 10]{:model :int-argb}
   => java.awt.image.BufferedImage"
  {:added "3.0"}
  ([size]
   (blank size *default-model* *default-type*))
  ([size model]
   (blank size model *default-type*))
  ([size model type]
   (protocol.image/-blank size model type)))

(defn read
  "reads an image from file
 
   (image/read \"test-data/std.image/circle-30.png\"
               (model/model :byte-gray)
               :base)
   ;;#image.base[30 30]{:model :byte-gray}
   => std.image.base.Image"
  {:added "3.0"}
  ([source]
   (read source *default-model* *default-type*))
  ([source model]
   (read source model *default-type*))
  ([source model type]
   (protocol.image/-read source model type)))

(defn to-byte-gray
  "converts an image :byte-gray representation
 
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (to-byte-gray))
   ;;#object[\"[B\" 0x2c1a4f3 \"[B@2c1a4f3\"]"
  {:added "3.0"}
  ([image]
   (protocol.image/-to-byte-gray image)))

(defn to-int-argb
  "converts an image :int-argb representation
 
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (to-int-argb))
   ;;#object[\"[I\" 0x3514a0dc \"[I@3514a0dc\"]"
  {:added "3.0"}
  ([image]
   (protocol.image/-to-int-argb image)))

(defn write
  "writes an image to file
 
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (image/coerce (model/model :3-byte-bgr) :base)
      (image/write \"test-data/std.image/circle-30.jpg\" {:format \"JPG\"}))"
  {:added "3.0"}
  ([image sink]
   (write image sink {:format "png"}))
  ([image sink opts]
   (protocol.image/-write image sink opts)))

(defn image
  "creates an image given parameters
 
   (image {:size [2 2]
           :model :byte-gray
           :data (byte-array [1 2 3 4])})
   ;;#image.awt[2 2]{:model :byte-gray}
   => *default-type*"
  {:added "3.0"}
  ([{:keys [size model data type]}]
   (protocol.image/-image size
                          (or model *default-model*)
                          data
                          (or type *default-type*)))
  ([size model data]
   (protocol.image/-image size model data *default-type*))
  ([size model data type]
   (protocol.image/-image size model data type)))

(defn coerce
  "coerces one image to another
 
   (-> (image/read \"test-data/std.image/circle-30.png\")
       (image/coerce :base))
   ;; #image.base[30 30]{:model :4-byte-abgr}
   => std.image.base.Image"
  {:added "3.0"}
  ([image type]
   (let [size  (image-size image)
         model (image-model image)
         data  (image-data image)]
     (protocol.image/-image size model data type)))
  ([image to-model type]
   (let [base (common/convert image to-model)]
     (coerce base type))))

(defn display-class
  "shows which types can be displayed
 
   (display-class :base)
   => #{std.image.base.Image}
 
   (display-class :awt)
   => #{java.awt.image.BufferedImage}"
  {:added "3.0"}
  ([type]
   (protocol.image/-display-class type)))

(defn display
  "displays an image
 
   (-> (image/read \"test-data/std.image/circle-10.png\")
       (display {:channel :red} :base))"
  {:added "3.0"}
  ([image]
   (display image {}))
  ([image opts]
   (display image opts *default-view*))
  ([image opts type]
   (let [types (display-class type)
         image (if (some #(instance? % image) types)
                 image
                 (coerce image type))]
     (protocol.image/-display image opts type))))

(comment

  (./code:import)
  (./code:arrange))
