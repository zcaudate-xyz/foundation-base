(ns std.image.base.model-test
  (:require [std.image.base.model :refer :all])
  (:use code.test))

^{:refer std.image.base.model/create-model :added "3.0"}
(fact "creates a predefined image model given a label"

  (create-model :ushort-555-rgb)
  => {:type :color
      :label :ushort-555-rgb
      :channel {:count 1 :fn vector :inv first}
      :meta [{:type Short/TYPE :span 1}]
      :data  {:red {:type Byte/TYPE
                    :channel 0
                    :index 0
                    :access [10 5]}
              :green {:type Byte/TYPE
                      :channel 0
                      :index 0
                      :access [5 5]}
              :blue {:type Byte/TYPE
                     :channel 0
                     :index 0
                     :access [0 5]}}})

^{:refer std.image.base.model/model-inv-table :added "3.0"}
(fact "creates a inverse access table for setting data within an image"

  ;; channel 0, 0th index = (i1>>3)<<11 + (i2>>2)<<6 + i3>>3 
  (model-inv-table (model :ushort-565-rgb))
  => {0 {0 {1 [11 5],
            2 [5 6],
            3 [0 5]}}}

  ^:hidden
  ;; channel 0, 0th index = i3, 1st index = i2, 2nd index = i1
  (model-inv-table (model :3-byte-bgr))
  => {0 {2 {1 nil},
         1 {2 nil},
         0 {3 nil}}}

  ;; channel 0, 0th index = i0
  (model-inv-table (model :ushort-gray))
  => {0 {0 {0 nil}}}

  ;; channel 0, 0th index = i0 << 24 + i1 << 16 + i2 << 8 + i3
  (model-inv-table (model :int-argb))
  => {0 {0 {0 [24 8]
            1 [16 8]
            2 [8 8]
            3 [0 8]}}}

  (model-inv-table {:type :color
                    :label :custom
                    :meta [{:type Byte/TYPE, :span 2}
                           {:type Integer/TYPE, :span 1}]
                    :channel {:count 2 :fn identity :inv identity}
                    :data {:alpha {:type Byte/TYPE :channel 0 :index 0}
                           :red   {:type Byte/TYPE :channel 0 :index 1},
                           :green {:type Byte/TYPE :channel 1 :index 0 :access [16 16]},
                           :blue  {:type Byte/TYPE :channel 1 :index 0 :access [0  16]}}})
  => {0 {0 {0 nil},
         1 {1 nil}},
      1 {0 {2 [16 16],
            3 [0 16]}}})

^{:refer std.image.base.model/model :added "3.0"}
(fact "creates a model with overwrites"

  (model)
  => [:3-byte-bgr :3-byte-rgb :3ch-byte-rgb
      :4-byte-abgr :4-byte-argb :4ch-byte-argb
      :byte-binary :byte-gray
      :int-argb :int-bgr :int-rgb
      :standard-argb :standard-gray
      :ushort-555-rgb :ushort-565-rgb :ushort-gray]

  (model :int-rgb {:type :stuff
                   :data :none
                   :channel {:hello :world}})
  => (contains-in {:type :stuff,
                   :data :none,
                   :channel {:count 1,
                             :hello :world}}))

(comment
  (./import))

