(ns std.image.awt.common-test
  (:require [std.image.awt.common :refer :all]
            [std.image.base.util :as util])
  (:use code.test))

^{:refer std.image.awt.common/image-size :added "3.0"}
(fact "returns the size of the BufferedImage"

  (-> (image {:size [2 2]
              :model :byte-gray
              :data (byte-array [10 20 30 40])})
      (image-size))
  => {:height 2, :width 2})

^{:refer std.image.awt.common/image-model :added "3.0"}
(fact "returns the model associated with the BufferedImage"

  (-> (image {:size [2 2]
              :model :byte-gray
              :data (byte-array [10 20 30 40])})
      (image-model))
  => (contains-in {:type :gray
                   :meta [{:type Byte/TYPE :span 1}],
                   :data {:raw {:type Byte/TYPE, :channel 0, :index 0}},
                   :label :byte-gray
                   :channel {:count 1}}))

^{:refer std.image.awt.common/image-data :added "3.0"}
(fact "returns the data contained within the BufferedImage"

  (-> (image {:size [2 2]
              :model :byte-gray
              :data (byte-array [10 20 30 40])})
      (image-data)
      (vec))
  => [10 20 30 40])

^{:refer std.image.awt.common/image-channels :added "3.0"}
(fact "returns the channels for the image"

  (->> (image {:size [2 2]
               :model :byte-gray
               :data (byte-array [10 20 30 40])})
       (image-channels)
       (mapv vec))
  => [[10 20 30 40]])

^{:refer std.image.awt.common/subimage :added "3.0"}
(fact "returns the subimage of the image"

  (-> (image {:size  [4 4]
              :model :byte-gray
              :data  (byte-array [10 20 30 40
                                  10 20 30 40
                                  10 20 30 40
                                  10 20 30 40])})
      (subimage 1 1 2 2)
      (image-data)
      vec)
  => [20 30 20 30])

^{:refer std.image.awt.common/image-to-byte-gray :added "3.0"}
(fact "returns the data with data in gray"

  (-> (image {:size  [4 4]
              :model :byte-gray
              :data  (byte-array [10 20 30 40
                                  10 20 30 40
                                  10 20 30 40
                                  10 20 30 40])})
      (image-to-byte-gray)
      (vec))
  => [10 20 30 40 10 20 30 40 10 20 30 40 10 20 30 40])

^{:refer std.image.awt.common/image-to-int-argb :added "3.0"}
(fact "returns the data associated with argb"

  (->> (image {:size  [2 2]
               :model :byte-gray
               :data  (byte-array [10 20
                                   30 40])})
       (image-to-int-argb)
       (mapv util/int->bytes))
  => [[255 10 10 10] [255 20 20 20]
      [255 30 30 30] [255 40 40 40]])

^{:refer std.image.awt.common/image :added "3.0"}
(fact "creates a `Bufferedimage` from a map"

  (image {:size [2 2]
          :model :byte-gray
          :data (byte-array [10 20 30 40])})
  => java.awt.image.BufferedImage)

(comment
  (./import))
