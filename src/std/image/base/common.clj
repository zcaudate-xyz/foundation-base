(ns std.image.base.common
  (:require [std.image.base.model :as model]
            [std.image.base.size :as size]
            [std.image.base.util :as util]
            [std.image.protocol :as protocol.image]
            [std.lib.foundation])
  (:refer-clojure :exclude [empty]))

(defn create-channels
  "creates channels based on the image
 
   (-> (create-channels (model/model :int-argb) 4)
       first
       vec)
   => [0 0 0 0]"
  {:added "3.0"}
  ([model length]
   (vec (mapv (fn [ch]
                (let [{:keys [array-fn]} (-> ch :type util/type-lookup)
                      to (array-fn (* (:span ch) length))]
                  to))
              (:meta model)))))

(defn empty
  "creates an empty image
 
   (empty [100 100] (model/model :int-argb))
   => (contains {:size {:width 100, :height 100}
                 :model map?
                 :data #(-> % count (= 10000))})"
  {:added "3.0"}
  ([size model]
   (let [length (size/length size)
         channels (create-channels model length)]
     {:size  (size/size->map size)
      :model model
      :data ((-> model :channel :inv) channels)})))

(defn copy
  "returns a copy of the image
 
   (-> (copy {:size [4 1]
              :model (model/model :ushort-555-rgb)
              :data (short-array [1 2 3 4])})
       :data
       vec)
   => [1 2 3 4]"
  {:added "3.0"}
  ([{:keys [model data size] :as image}]
   (let [channels  ((-> model :channel :fn) data)
         length    (size/length size)
         copy-fn   (fn [i ch]
                     (let [{:keys [array-fn]} (-> model
                                                  :meta
                                                  (get i)
                                                  :type
                                                  util/type-lookup)
                           to (array-fn length)]
                       (System/arraycopy ch 0 to 0 length)
                       to))
         copy      (vec (map-indexed copy-fn channels))]
     (assoc image :data ((-> model :channel :inv) channels)))))

(defn subimage
  "returns a subimage of an original image
 
   (-> (subimage {:model (model/model :standard-argb)
                  :size [2 2]
                  :data {:alpha (byte-array [255 255 255 255])
                         :red (byte-array [10 20 30 40])
                         :green (byte-array [50 60 70 80])
                         :blue (byte-array [90 100 110 120])}}
                 0 0 1 2)
       :data
      display-standard-data)
   => {:alpha [-1 -1], :red [10 30], :green [50 70], :blue [90 110]}"
  {:added "3.0"}
  ([{:keys [model data size] :as image} x y w h]
   (let [nsize {:width w :height h}
         pw   (protocol.image/-width size)
         ph   (protocol.image/-height size)
         pchannels ((-> model :channel :fn) data)
         nchannels (mapv (fn [{:keys [type span]} pchannel]
                           (let [{:keys [array-fn aset-fn]} (util/type-lookup type)
                                 ndata (array-fn (* span (size/length nsize)))]
                             (doall (for [i (range h)
                                          j (range w)
                                          k (range span)]
                                      (let [x (aget ^bytes pchannel (+ k (* span (+ j x (* pw (+ i y))))))]
                                        (aset-fn ndata
                                                 (+ k (* span (+ j (* w i))))
                                                 x))))
                             ndata))
                         (:meta model)
                         pchannels)]
     {:data ((-> model :channel :inv) nchannels)
      :model model
      :size nsize})))

(defn display-standard-data
  "converts array into vector
 
   (display-standard-data {:red   (byte-array [1 2 3])
                           :green (byte-array [4 5 6])
                           :blue  (byte-array [7 8 9])})
   => {:red [1 2 3], :green [4 5 6], :blue [7 8 9]}"
  {:added "3.0"}
  ([data]
   (zipmap (keys data)
           (map vec (vals data)))))

(defn standard-color-data->standard-gray
  "converts data of `standard-argb` to `standard-gray`
 
   (vec (standard-color-data->standard-gray {:alpha (byte-array [-1 -1])
                                             :red   (byte-array [1 2])
                                             :green (byte-array [3 4])
                                             :blue  (byte-array [5 6])}
                                            (byte-array 2)
                                            2))
   => [3 4]"
  {:added "3.0"}
  ([{:keys [^bytes alpha ^bytes red ^bytes green ^bytes blue]} to-bytes length]
   (let [a (if (nil? alpha) 255)]
     (dotimes [i length]

       (let [a (or a (bit-and 255 (aget alpha i)))
             r (bit-and 255 (aget red i))
             g (bit-and 255 (aget green i))
             b (bit-and 255 (aget blue i))]
         (aset-byte to-bytes
                    i
                    (unchecked-byte (/ (* a (+ r g b)) 255 3)))))
     to-bytes)))

(defn standard-color->standard-gray
  "converts data of `standard-argb` image `standard-gray`
 
   (-> {:model :standard-argb
        :size  [2 1]
        :data  {:alpha (byte-array [-1 -1])
                :red   (byte-array [1 2])
                :green (byte-array [3 4])
                :blue  (byte-array [5 6])}}
       standard-color->standard-gray
       :data :raw vec)
  => [3 4]"
  {:added "3.0"}
  ([{:keys [size data] :as image}]
   (let [length (size/length size)
         to (byte-array length)]
     (standard-color-data->standard-gray data to length)
     {:model (:gray model/*defaults*)
      :size  size
      :data  {:raw to}})))

(defn standard-gray->standard-color
  "converts data of `standard-gray` image to `standard-argb`
 
   (-> {:model :standard-gray
        :size  [2 1]
        :data  {:raw (byte-array [3 4])}}
       standard-gray->standard-color
       :data
       display-standard-data)
   => {:alpha [-1 -1], :red [3 4], :green [3 4], :blue [3 4]}"
  {:added "3.0"}
  ([{:keys [size data] :as image}]
   (let [length (size/length size)
         alpha (byte-array length)
         red   (byte-array length)
         green (byte-array length)
         blue  (byte-array length)]
     (do (System/arraycopy (:raw data) 0 red 0 length)
         (System/arraycopy (:raw data) 0 green 0 length)
         (System/arraycopy (:raw data) 0 blue 0 length)
         (java.util.Arrays/fill alpha (unchecked-byte 255)))
     {:model (:color model/*defaults*)
      :size  size
      :data  {:alpha alpha
              :red   red
              :green green
              :blue  blue}})))

(defn standard-type->standard-type
  "converts between standard types
 
   (-> {:model (model/model :standard-argb)
        :size  [2 1]
        :data  {:alpha (byte-array [-1 -1])
                :red   (byte-array [1 2])
                :green (byte-array [3 4])
                :blue  (byte-array [5 6])}}
       ;; Does not change
       (standard-type->standard-type :color)
      ;; Converts to gray
       (standard-type->standard-type :gray)
       :data
       display-standard-data)
   => {:raw [3 4]}"
  {:added "3.0"}
  ([{:keys [model] :as image} type]
   (case [(:type model) type]
     [:color :color] image
     [:gray :gray]   image
     [:color :gray]  (standard-color->standard-gray image)
     [:gray :color]  (standard-gray->standard-color image))))

(defn mask-value
  "gets a value out according to the mask applied
 
   (mask-value 20 ;;   010100
               2  ;;    |||
               7  ;;    111
 )  ;;    |||
   => 5           ;;    101"
  {:added "3.0"}
  ([v start mask]
   (-> (bit-shift-right v start)
       (bit-and mask))))

(defn shift-value
  "shifts the value according to the scaling
 
   (shift-value 4 2)
   => 16
 
   (shift-value 4 -2)
   => 1"
  {:added "3.0"}
  ([v scale]
   (cond (pos? scale)
         (bit-shift-left v scale)

         (neg? scale)
         (bit-shift-right v (- scale))

         :else v)))

(defn retrieve-single
  "returns the byte representation of a 
 
   (vec (retrieve-single (short-array [4024 8024])
                         (byte-array 2)
                         :raw
                         (model/model :ushort-gray)
                         2))
   => [15 31]"
  {:added "3.0"}
  ([from to name model length]
   (let [;; Channel Access
         {data-type    :type
          data-channel :channel
          data-index   :index
          data-access  :access :as data-model} (-> model :data name)
         _   (if-not (and data-type data-channel data-index)
               (throw (Exception. (str "No info available for " name))))
         from-data-size   (-> model :meta (get data-channel) :type util/type-lookup :size)
         from-channels ((-> model :channel :fn) from)
         from-selected (get from-channels data-channel)

        ;; Data Access
         [start size mask] (if data-access
                             (let [[start size] data-access]
                               [start size (dec (bit-shift-left 1 size))]))
         scale  (- (-> data-type util/type-lookup :size)
                   (or size from-data-size))
         span   (-> model :meta (get data-channel) :span)
         {:keys [unchecked-fn aset-fn]} (util/type-lookup data-type)]
     (dotimes [i length]
       (let [t (* i span)
             v (std.lib.foundation/aget from-selected (+ t data-index))
             v (if data-access
                 (mask-value v start mask)
                 v)
             v (shift-value v scale)]
         (aset-fn to i (unchecked-fn v))))
     to)))

(defn slice
  "given a key gets a slice of an image, putting it in a new one
 
   (-> (slice {:size [2 1]
               :model (model/model :3ch-byte-rgb)
               :data   [(byte-array [3 4])
                        (byte-array [4 8])
                        (byte-array [5 10])]}
              :red)
       :data
       vec)
  => [3 4]"
  {:added "3.0"}
  ([{:keys [size model data] :as image} name]
   (let [length (size/length size)
         array-fn (-> model :data name :type util/type-lookup :array-fn)
         to-array (array-fn length)]
     {:model (model/model :byte-gray)
      :size size
      :data (retrieve-single data to-array name model length)})))

(defn retrieve-all
  "returns the color properties of the image:
 
   (->> (retrieve-all {:type  :3ch-byte-rgb
                       :model (model/model :3ch-byte-rgb)
                       :data   [(byte-array [3 4])
                                (byte-array [4 8])
                                (byte-array [5 10])]}
                      2)
        display-standard-data)
   => {:red [3 4], :green [4 8], :blue [5 10]}"
  {:added "3.0"}
  ([{:keys [model data] :as image} length]
   (let [ks (-> model :data keys)]
     (->> (map (fn [k]
                 (let [array-fn (-> model :data k :type util/type-lookup :array-fn)
                       to-array (array-fn length)]
                   (retrieve-single data to-array k model length))) ks)
          (zipmap ks)))))

(defn color->standard-color
  "converts a color image to the standard color image
 
   (->> {:model  (model/model :3-byte-bgr)
         :size   [2 1]
         :data   (byte-array [3 4 5 6 8 10])}
        (color->standard-color)
        :data
        display-standard-data)
   => {:alpha [-1 -1] :red [5 10] :green [4 8] :blue [3 6]}"
  {:added "3.0"}
  ([{:keys [size model] :as image}]
   (cond (= (:type model)
            (:color model/*defaults*))
         image

         :else
         (let [length (size/length size)]
           (let [data (retrieve-all image length)
                 data (if-not (:alpha data)
                        (let [alpha (byte-array length)
                              _ (java.util.Arrays/fill alpha (unchecked-byte 255))]
                          (assoc data :alpha alpha))
                        data)]
             (assoc image
                    :model (model/*defaults* :color)
                    :data data))))))

(defn gray->standard-gray
  "converts a gray image to the standard gray image
 
   (->> {:model  (model/model :ushort-gray)
         :size   [2 1]
         :data   (short-array [256 (* 8 256)])}
        (gray->standard-gray)
        :data :raw vec)
   => [1 8]"
  {:added "3.0"}
  ([{:keys [size] :as image}]
   (cond (= (-> image :model :type)
            (:gray model/*defaults*))
         image

         :else
         (let [length (size/length size)
               data (retrieve-all image length)]
           (assoc image
                  :model (model/*defaults* :gray)
                  :data data)))))

(defn type->standard-type
  "converts a color image to the standard color image
 
   (->> {:model  (model/model :ushort-gray)
         :size   [2 1]
         :data   (short-array [256 (* 8 256)])}
        (type->standard-type)
        :data :raw vec)
   => [1 8]"
  {:added "3.0"}
  ([{:keys [model] :as image}]
   (case (:type model)
     :color (color->standard-color image)
     :gray  (gray->standard-gray image))))

(defn set-single-val
  "helper function to calculate single value from channel inputs
 
   (-> (set-single-val [1 2 3 4] {0 [0  8]
                                  1 [8  8]
                                  2 [16 8]
                                  3 [24 8]}
                       8 32)
       (util/>> [8 8 8 8]))
   => [4 3 2 1]"
  {:added "3.0"}
  ([data entries from-size to-size]
   (reduce (fn [out [i access]]
             (let [[start to-size] (or access [0 to-size])
                   d (get data i)
                   d (cond (= from-size to-size)
                           d

                           (< from-size to-size)
                           (bit-shift-left  d (- to-size from-size))

                           (> from-size to-size)
                           (bit-shift-right d (- from-size to-size)))
                   d (if (pos? start)
                       (bit-shift-left d start)
                       d)]
               (bit-or out d)))
           0
           entries)))

(defn set-single
  "sets a single channel given model and table
 
   (-> (set-single [(byte-array [10 20])]
                   (short-array 2)
                   (-> (model/model :ushort-gray)
                       (get-in [:meta 0]))
                   {0 {0 nil}}
                   2)
       vec)
   => [2560 5120]"
  {:added "3.0"}
  ([from-channels to-channel to-channel-model table length]
   (let [{:keys [aset-fn unchecked-fn size]}
         (util/type-lookup (:type to-channel-model))
         span (:span to-channel-model)]
     (dotimes [i length]
       (let [t    (* i span)
             data (mapv (fn [ch] (bit-and (std.lib.foundation/aget (from-channels ch) i)
                                          255))
                        (range (count from-channels)))]
         (doseq [[offset entries] table]
           (try
             (aset-fn to-channel
                      (+ t offset)
                      (unchecked-fn (set-single-val data
                                                    entries
                                                    8
                                                    size)))
             (catch Exception e
               (println "PROCESSING ERROR:" t i (count to-channel) (count (from-channels 0)))
               (throw e))))))
     to-channel)))

(defn set-all
  "sets all channels according to the given model
 
   (-> (set-all [(byte-array [255 255])
                 (byte-array [10 40])
                 (byte-array [20 50])
                 (byte-array [30 60])]
                [(byte-array 4)
                 (short-array 2)]
                {:type  :color
                 :label :custom
                 :meta [{:type Byte/TYPE, :span 2}
                        {:type Short/TYPE, :span 1}]
                 :channel {:count 2 :fn identity :inv identity}
                :data {:alpha {:type Byte/TYPE :channel 0 :index 0}
                        :red   {:type Byte/TYPE :channel 0 :index 1},
                        :green {:type Byte/TYPE :channel 1 :index 0 :access [8 8]},
                        :blue  {:type Byte/TYPE :channel 1 :index 0 :access [0 8]}}}
                2)
       ((fn [[barr iarr]]
          [(vec barr) (util/>> iarr [8 8])])))
   => [[-1 10 -1 40] [[20 30] [50 60]]]"
  {:added "3.0"}
  ([from-channels to-channels to-model length]
   (let [table (model/model-inv-table to-model)]
     (doall (for [i (range (count to-channels))]
              (set-single from-channels
                          (get to-channels i)
                          (get-in to-model [:meta i])
                          (get table i)
                          length)))
     to-channels)))

(defn standard-type->type
  "converts a standard-image to another of the same type (color->color), (gray->gray)
 
   (-> {:model (model/model :standard-gray)
        :size [2 1]
        :data {:raw (byte-array [10 20])}}
       (standard-gray->standard-color)
       (standard-type->type (model/model :ushort-555-rgb))
       :data
       (util/>> [5 5 5]))
   => [[1 1 1] [2 2 2]]"
  {:added "3.0"}
  ([{:keys [data size] :as standard} model]
   (cond (= (:model standard) model)
         standard

         :else
         (let [from-channels ((-> standard :model :channel :fn) data)
               length  (size/length size)
               to-channels  (create-channels model length)
               _  (set-all from-channels to-channels model length)
               to-data ((-> model :channel :inv) to-channels)]
           (assoc standard
                  :model model
                  :data to-data)))))

(defn convert-base
  "converts from one image to another, via standard
 
   (-> {:model (model/model :ushort-gray)
        :size [2 1]
        :data (short-array [2560 5120])}
       (convert-base (model/model :3ch-byte-rgb))
       :data
       (->> (mapv vec)))
   => [[10 20] [10 20] [10 20]]"
  {:added "3.0"}
  ([{:keys [data size] :as image} model]
   (cond (= (:model image) model)
         (copy image)

         :else
         (-> image
             (type->standard-type)
             (standard-type->standard-type (:type model))
             (standard-type->type model)))))

(extend-protocol protocol.image/IRepresentation
  clojure.lang.IPersistentMap
  (-size     [m] (:size m))
  (-model    [m] (:model m))
  (-data     [m] (:data m)))

(defn convert
  "converts from any image representation into a map representation
 
   (-> {:model (model/model :ushort-gray)
        :size [2 1]
        :data (short-array [2560 5120])}
       (convert (model/model :3ch-byte-rgb))
       :data
       (->> (mapv vec)))
   => [[10 20] [10 20] [10 20]]"
  {:added "3.0"}
  ([image]
   (convert image (protocol.image/-model image)))
  ([image to-model]
   (let [model (protocol.image/-model image)
         size  (protocol.image/-size image)
         data  (protocol.image/-data image)
         image  {:size size
                 :data data
                 :model model}]
     (cond (= (:label to-model) (:label model))
           image

           :else
           (convert-base image to-model)))))

(defn base-map
  "returns the base-map of an image
 
   (-> (awt/image {:size [2 1]
                   :model :byte-gray
                   :data (byte-array [10 20])})
       (base-map))
   => map?"
  {:added "3.0"}
  ([image]
   {:size  (protocol.image/-size image)
    :model (protocol.image/-model image)
    :data  (protocol.image/-data image)}))
