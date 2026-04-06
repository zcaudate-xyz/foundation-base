(ns std.image-test
  (:require [std.fs :as fs]
            [std.image :as image :refer :all]
            [std.image.base.model :as model]
            [std.image.base.util :as util])
  (:use code.test)
  (:refer-clojure :exclude [read]))

^{:refer std.image/default-type :added "3.0"}
(fact "displays and sets the default type"

  (default-type std.image.base.Image)

  (default-type java.awt.image.BufferedImage)

  (default-type)
  => java.awt.image.BufferedImage)

^{:refer std.image/default-model :added "3.0"}
(fact "displays and set the default model"

  (:label (default-model))
  => :int-argb)

^{:refer std.image/default-view :added "3.0"}
(fact "displays and set the default view"

  (default-view)
  => :awt)

^{:refer std.image/image? :added "3.0"}
(fact "checks whether object is an image"

  (image? (image/read "test-data/std.image/circle-30.png"))
  => true)

^{:refer std.image/image-channels :added "3.0"}
(fact "returns the channels of an image"

  (-> (image/read "test-data/std.image/circle-30.png")
      (image-channels)
      (count))
  => 4)

^{:refer std.image/image-size :added "3.0"}
(fact "returns the size of the image"

  (-> (image/read "test-data/std.image/circle-30.png")
      (image-size))
  => {:height 30, :width 30})

^{:refer std.image/image-model :added "3.0"}
(fact "returns the model associated with the image"

  (-> (image/read "test-data/std.image/circle-30.png")
      (image-model))
  => (model/model :4-byte-abgr))

^{:refer std.image/image-data :added "3.0"}
(fact "returns the raw data associated with the image"

  (->> (image/read "test-data/std.image/circle-30.png")
       (image-data)
       (take 10))
  => [-1 -1 -1 -1 -1 -1 -1 -1 -1 -1])

^{:refer std.image/size? :added "3.0"}
(fact "checks whether object is a size"

  (size? [10 10]) => true

  (size? {:height 10 :width 10}) => true)

^{:refer std.image/height :added "3.0"}
(fact "returns the height of an image"

  (-> (image/read "test-data/std.image/circle-30.png")
      (image/height))
  => 30)

^{:refer std.image/width :added "3.0"}
(fact "returns the width of an image"

  (-> (image/read "test-data/std.image/circle-30.png")
      (image/width))
  => 30)

^{:refer std.image/subimage :added "3.0"}
(fact "returns a subimage given image and bounds"
  (-> (image/read "test-data/std.image/circle-30.png")
      (subimage 10 10 10 10)
      (image-size))
  ;; #image.awt[10 10]{:model :4-byte-abgr}
  => {:height 10, :width 10})

^{:refer std.image/blank :added "3.0"}
(fact "returns a blank image"

  (image/blank [10 10])
  ;;#image.awt[10 10]{:model :int-argb}
  => java.awt.image.BufferedImage)

^{:refer std.image/read :added "3.0"}
(fact "reads an image from file"

  (image/read "test-data/std.image/circle-30.png"
              (model/model :byte-gray)
              :base)
  ;;#image.base[30 30]{:model :byte-gray}
  => std.image.base.Image)

^{:refer std.image/to-byte-gray :added "3.0"}
(fact "converts an image :byte-gray representation"

  (-> (image/read "test-data/std.image/circle-30.png")
      (to-byte-gray)
      (alength))
  => 900)

^{:refer std.image/to-int-argb :added "3.0"}
(fact "converts an image :int-argb representation"

  (-> (image/read "test-data/std.image/circle-30.png")
      (to-int-argb)
      (alength))
  => 900)

^{:refer std.image/write :added "3.0"}
(fact "writes an image to file"
  ^:hidden

  (do (fs/delete "test-scratch/circle-30.jpg")
      (-> (image/read "test-data/std.image/circle-30.png")
          (image/coerce (model/model :3-byte-bgr) :base)
          (image/write "test-scratch/circle-30.jpg" {:format "JPG"}))
      (fs/exists? "test-scratch/circle-30.jpg"))
  => true

  (fs/delete "test-scratch/circle-30.jpg"))

^{:refer std.image/image :added "3.0"}
(fact "creates an image given parameters"

  (image {:size [2 2]
          :model :byte-gray
          :data (byte-array [1 2 3 4])})
  ;;#image.awt[2 2]{:model :byte-gray}
  => *default-type*)

^{:refer std.image/coerce :added "3.0"}
(fact "coerces one image to another"

  (-> (image/read "test-data/std.image/circle-30.png")
      (image/coerce :base))
  ;; #image.base[30 30]{:model :4-byte-abgr}
  => std.image.base.Image)

^{:refer std.image/display-class :added "3.0"}
(fact "shows which types can be displayed"

  (display-class :base)
  => #{std.image.base.Image}

  (display-class :awt)
  => #{java.awt.image.BufferedImage})

^{:refer std.image/display :added "3.0"}
(fact "displays an image"

  (with-out-str
    (-> (image/read "test-data/std.image/circle-10.png")
        (display {:channel :red} :base)))
  => string?)

(comment
  (./scaffold)
  (./import))

(comment
  ;;#image.awt[100 100]{:model :4-byte-abgr}
  ;;#image.base[100 100]{:model :byte-gray}
  (-> (image/read "test-data/std.image/circle-100.png")
      (coerce (model/model :byte-gray) :base)
      (display {:channel :red} :awt))

  (-> (image/read "test-data/std.image/gump.jpeg"))
  ;;#image.awt[100 100]{:model :3-byte-bgr}
  (coerce (model/model :byte-gray) :base)
  ;;#image.base[100 100]{:model :byte-gray}
  (display {} :awt)

  (-> (image/read "test-data/std.image/gump.jpeg")
      (coerce (model/model :byte-gray) :base)
      (display {} :base))

  (-> (image/read "test-data/std.image/gump.jpeg")
      (display {:channel :green} :awt))
  (-> (image/read "test-data/std.image/gump.jpeg")
      (display {:channel :green} :awt))
  (-> (image/read "test-data/std.image/gump.jpeg")
      (display {:channel :blue} :awt))

  (-> (image/read "test-data/std.image/gump.jpeg")
      (display {:channel :red} :base))

  (-> (image/read "test-data/std.image/gump.jpeg")
      (display {} :base))

  (-> (image/read "test-data/std.image/gump.jpeg")
      (display {:channel :red} :awt)))
