(ns std.image.base.common-test
  (:require [std.image.awt]
            [std.image.awt.common :as awt]
            [std.image.base]
            [std.image.base.common :refer :all]
            [std.image.base.model :as model]
            [std.image.base.util :as util])
  (:use code.test)
  (:refer-clojure :exclude [empty]))

^{:refer std.image.base.common/create-channels :added "3.0"}
(fact "creates channels based on the image"

  (-> (create-channels (model/model :int-argb) 4)
      first
      vec)
  => [0 0 0 0])

^{:refer std.image.base.common/empty :added "3.0"}
(fact "creates an empty image"

  (empty [100 100] (model/model :int-argb))
  => (contains {:size {:width 100, :height 100}
                :model map?
                :data #(-> % count (= 10000))}))

^{:refer std.image.base.common/copy :added "3.0"}
(fact "returns a copy of the image"

  (-> (copy {:size [4 1]
             :model (model/model :ushort-555-rgb)
             :data (short-array [1 2 3 4])})
      :data
      vec)
  => [1 2 3 4])

^{:refer std.image.base.common/subimage :added "3.0"}
(fact "returns a subimage of an original image"

  (-> (subimage {:model (model/model :standard-argb)
                 :size [2 2]
                 :data {:alpha (byte-array [255 255 255 255])
                        :red (byte-array [10 20 30 40])
                        :green (byte-array [50 60 70 80])
                        :blue (byte-array [90 100 110 120])}}
                0 0 1 2)
      :data
      display-standard-data)
  => {:alpha [-1 -1], :red [10 30], :green [50 70], :blue [90 110]})

^{:refer std.image.base.common/display-standard-data :added "3.0"}
(fact "converts array into vector"

  (display-standard-data {:red   (byte-array [1 2 3])
                          :green (byte-array [4 5 6])
                          :blue  (byte-array [7 8 9])})
  => {:red [1 2 3], :green [4 5 6], :blue [7 8 9]})

^{:refer std.image.base.common/standard-color-data->standard-gray :added "3.0"}
(fact "converts data of `standard-argb` to `standard-gray`"

  (vec (standard-color-data->standard-gray {:alpha (byte-array [-1 -1])
                                            :red   (byte-array [1 2])
                                            :green (byte-array [3 4])
                                            :blue  (byte-array [5 6])}
                                           (byte-array 2)
                                           2))
  => [3 4])

^{:refer std.image.base.common/standard-color->standard-gray :added "3.0"}
(fact "converts data of `standard-argb` image `standard-gray`"

  (-> {:model :standard-argb
       :size  [2 1]
       :data  {:alpha (byte-array [-1 -1])
               :red   (byte-array [1 2])
               :green (byte-array [3 4])
               :blue  (byte-array [5 6])}}
      standard-color->standard-gray
      :data :raw vec)
  => [3 4])

^{:refer std.image.base.common/standard-gray->standard-color :added "3.0"}
(fact "converts data of `standard-gray` image to `standard-argb`"

  (-> {:model :standard-gray
       :size  [2 1]
       :data  {:raw (byte-array [3 4])}}
      standard-gray->standard-color
      :data
      display-standard-data)
  => {:alpha [-1 -1], :red [3 4], :green [3 4], :blue [3 4]})

^{:refer std.image.base.common/standard-type->standard-type :added "3.0"}
(fact "converts between standard types"

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
  => {:raw [3 4]})

^{:refer std.image.base.common/mask-value :added "3.0"}
(fact "gets a value out according to the mask applied"

  (mask-value 20 ;;   010100
              2  ;;    |||
              7  ;;    111
)  ;;    |||
  => 5           ;;    101
  )

^{:refer std.image.base.common/shift-value :added "3.0"}
(fact "shifts the value according to the scaling"

  (shift-value 4 2)
  => 16

  (shift-value 4 -2)
  => 1)

^{:refer std.image.base.common/retrieve-single :added "3.0"}
(fact "returns the byte representation of a "

  (vec (retrieve-single (short-array [4024 8024])
                        (byte-array 2)
                        :raw
                        (model/model :ushort-gray)
                        2))
  => [15 31]

  ^:hidden
  (defn argb->int [arr]
    (int-array (mapv (fn [[a r g b]]
                       (unchecked-int (+ (bit-shift-left a 24)
                                         (bit-shift-left r 16)
                                         (bit-shift-left g 8)
                                         b)))
                     arr)))
  (vec (retrieve-single [[1 2 3 4] [4 3 2 1]]
                        (byte-array 2)
                        :alpha
                        (assoc-in (model/model :int-argb)
                                  [:channel :fn]
                                  (fn [v] [(argb->int v)]))
                        2))
  => [1 4]

  (defn rgb555->short [arr]
    (short-array (mapv (fn [[r g b]]
                         (unchecked-short (+ (bit-shift-left r 10)
                                             (bit-shift-left g 5)
                                             b)))
                       arr)))
  (vec (retrieve-single [[3 4 5] [6 8 10]]
                        (byte-array 2)
                        :red
                        (assoc-in (model/model :ushort-555-rgb)
                                  [:channel :fn]
                                  (fn [v] [(rgb555->short v)]))
                        2))
  => [24 48]

  (vec (retrieve-single [(byte-array [3 4])
                         (byte-array [4 8])
                         (byte-array [5 10])]
                        (byte-array 2)
                        :green
                        (model/model :3ch-byte-rgb)
                        2))
  => [4 8])

^{:refer std.image.base.common/slice :added "3.0"}
(fact "given a key gets a slice of an image, putting it in a new one"

  (-> (slice {:size [2 1]
              :model (model/model :3ch-byte-rgb)
              :data   [(byte-array [3 4])
                       (byte-array [4 8])
                       (byte-array [5 10])]}
             :red)
      :data
      vec)
  => [3 4])

^{:refer std.image.base.common/retrieve-all :added "3.0"}
(fact "returns the color properties of the image:"

  (->> (retrieve-all {:type  :3ch-byte-rgb
                      :model (model/model :3ch-byte-rgb)
                      :data   [(byte-array [3 4])
                               (byte-array [4 8])
                               (byte-array [5 10])]}
                     2)
       display-standard-data)
  => {:red [3 4], :green [4 8], :blue [5 10]}

  ^:hidden
  (->> (retrieve-all {:type  :ushort-gray
                      :model (model/model :ushort-gray)
                      :data   (short-array [256 (* 8 256) (* 16 256)])}
                     3)
       display-standard-data)
  => {:raw [1 8 16]}

  (->> (retrieve-all {:type  :3-byte-bgr
                      :model (model/model :3-byte-bgr)
                      :data   (byte-array [3 4 5 6 8 10])}
                     2)
       display-standard-data)
  => {:red [5 10], :green [4 8], :blue [3 6]})

^{:refer std.image.base.common/color->standard-color :added "3.0"}
(fact "converts a color image to the standard color image"

  (->> {:model  (model/model :3-byte-bgr)
        :size   [2 1]
        :data   (byte-array [3 4 5 6 8 10])}
       (color->standard-color)
       :data
       display-standard-data)
  => {:alpha [-1 -1] :red [5 10] :green [4 8] :blue [3 6]})

^{:refer std.image.base.common/gray->standard-gray :added "3.0"}
(fact "converts a gray image to the standard gray image"

  (->> {:model  (model/model :ushort-gray)
        :size   [2 1]
        :data   (short-array [256 (* 8 256)])}
       (gray->standard-gray)
       :data :raw vec)
  => [1 8])

^{:refer std.image.base.common/type->standard-type :added "3.0"}
(fact "converts a color image to the standard color image"

  (->> {:model  (model/model :ushort-gray)
        :size   [2 1]
        :data   (short-array [256 (* 8 256)])}
       (type->standard-type)
       :data :raw vec)
  => [1 8]

  ^:hidden
  (->> {:model  (model/model :3-byte-bgr)
        :size   [2 1]
        :data   (byte-array [3 4 5 6 8 10])}
       type->standard-type
       :data
       display-standard-data)
  => {:alpha [-1 -1] :red [5 10] :green [4 8] :blue [3 6]})

^{:refer std.image.base.common/set-single-val :added "3.0"}
(fact "helper function to calculate single value from channel inputs"

  (-> (set-single-val [1 2 3 4] {0 [0  8]
                                 1 [8  8]
                                 2 [16 8]
                                 3 [24 8]}
                      8 32)
      (util/>> [8 8 8 8]))
  => [4 3 2 1]
  ^:hidden
  (-> (set-single-val [0 63 127 255] {1 [0  5]
                                      2 [5  5]
                                      3 [10 5]}
                      8 16)
      (util/>> [5 5 5]))
  => [31 15 7]

  (set-single-val [24] {0 nil}
                  8 16)
  => 6144

  (set-single-val [10 20 30 40] {1 nil}
                  8 8)
  => 20)

^{:refer std.image.base.common/set-single :added "3.0"}
(fact "sets a single channel given model and table"

  (-> (set-single [(byte-array [10 20])]
                  (short-array 2)
                  (-> (model/model :ushort-gray)
                      (get-in [:meta 0]))
                  {0 {0 nil}}
                  2)
      vec)
  => [2560 5120]
  ^:hidden
  (-> (set-single [(byte-array [255 255])
                   (byte-array [10 40])
                   (byte-array [20 50])
                   (byte-array [30 60])]
                  (int-array 2)
                  (-> (model/model :int-argb)
                      (get-in [:meta 0]))
                  {0 {0 [24 8]
                      1 [16 8]
                      2 [8  8]
                      3 [0  8]}}
                  2)
      (util/>> [8 8 8 8]))
  => [[255 10 20 30] [255 40 50 60]]

  (-> (set-single [(byte-array [255 255])
                   (byte-array [10 40])
                   (byte-array [20 50])
                   (byte-array [30 60])]
                  (short-array 2)
                  (-> (model/model :ushort-555-rgb)
                      (get-in [:meta 0]))
                  {0 {1 [10 5]
                      2 [5  5]
                      3 [0  5]}}
                  2)
      (util/>> [5 5 5]))
  => [[1 2 3] [5 6 7]])

^{:refer std.image.base.common/set-all :added "3.0"}
(fact "sets all channels according to the given model"

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
  => [[-1 10 -1 40] [[20 30] [50 60]]]
  ^:hidden
  (-> (set-all [(byte-array [255 255])
                (byte-array [10 40])
                (byte-array [20 50])
                (byte-array [30 60])]
               [(byte-array 4)
                (int-array 2)]
               {:type :color
                :label :custom
                :meta [{:type Byte/TYPE, :span 2}
                       {:type Integer/TYPE, :span 1}]
                :channel {:count 2 :fn identity :inv identity}
                :data {:alpha {:type Byte/TYPE :channel 0 :index 0}
                       :red   {:type Byte/TYPE :channel 0 :index 1},
                       :green {:type Byte/TYPE :channel 1 :index 0 :access [16 16]},
                       :blue  {:type Byte/TYPE :channel 1 :index 0 :access [0  16]}}}
               2)
      ((fn [[barr iarr]]
         [(vec barr) (util/>> iarr [16 16])])))
  => [[-1 10 -1 40]
      [[5120 7680] [12800 15360]]])

^{:refer std.image.base.common/standard-type->type :added "3.0"}
(fact "converts a standard-image to another of the same type (color->color), (gray->gray)"

  (-> {:model (model/model :standard-gray)
       :size [2 1]
       :data {:raw (byte-array [10 20])}}
      (standard-gray->standard-color)
      (standard-type->type (model/model :ushort-555-rgb))
      :data
      (util/>> [5 5 5]))
  => [[1 1 1] [2 2 2]]
  ^:hidden
  (-> {:model (model/model :standard-gray)
       :size [2 1]
       :data {:raw (short-array [10 20])}}
      (standard-type->type (model/model :ushort-gray))
      :data
      vec)
  => [2560 5120])

^{:refer std.image.base.common/convert-base :added "3.0"}
(fact "converts from one image to another, via standard"

  (-> {:model (model/model :ushort-gray)
       :size [2 1]
       :data (short-array [2560 5120])}
      (convert-base (model/model :3ch-byte-rgb))
      :data
      (->> (mapv vec)))
  => [[10 20] [10 20] [10 20]]
  ^:hidden
  (-> {:model (model/model :ushort-gray)
       :size [2 1]
       :data (short-array [2560 5120])}
      (convert-base (model/model :3ch-byte-rgb))
      (convert-base (model/model :ushort-gray))
      :data
      vec)
  => [2560 5120])

^{:refer std.image.base.common/convert :added "3.0"}
(fact "converts from any image representation into a map representation"

  (-> {:model (model/model :ushort-gray)
       :size [2 1]
       :data (short-array [2560 5120])}
      (convert (model/model :3ch-byte-rgb))
      :data
      (->> (mapv vec)))
  => [[10 20] [10 20] [10 20]])

^{:refer std.image.base.common/base-map :added "3.0"}
(fact "returns the base-map of an image"

  (-> (awt/image {:size [2 1]
                  :model :byte-gray
                  :data (byte-array [10 20])})
      (base-map))
  => map?)

(comment
  (./import)
  (./scaffold))
