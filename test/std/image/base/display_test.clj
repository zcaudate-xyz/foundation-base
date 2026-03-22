(ns std.image.base.display-test
  (:require [std.image.base.display :refer :all]
            [std.image.base.model :as model]
            [std.image.base.util :as util])
  (:use code.test))

^{:refer std.image.base.display/render-string :added "3.0"}
(fact "render string based on rows containing values"

  (render-string [[10 20 30 40]
                  [10 20 30 40]])
  ;;"ÂNHœŠm\nWNÀXŠm"
  => string?)

^{:refer std.image.base.display/byte-gray->rows :added "3.0"}
(fact "creates rows from byte-gray array"

  (-> (byte-array [10 20 30 40])
      (byte-gray->rows [2 2])
      vec)
  => [[10 20] [30 40]])

^{:refer std.image.base.display/render-byte-gray :added "3.0"}
(fact "creates an ascii string for the byte-gray array"

  (render-byte-gray {:data (byte-array [10 20 30 40])
                     :size [2 2]})
  ;;"Ã#\nŠp"
  => string?)

^{:refer std.image.base.display/int-argb->rows :added "3.0"}
(fact "creates rows from int-argb array"

  (-> (map util/bytes->int [[255 10 20 30] [255 40 50 60]])
      (int-array)
      (int-argb->rows [1 2]))
  => [[20] [50]])

^{:refer std.image.base.display/render-int-argb :added "3.0"}
(fact "creates an ascii string for the int-argb array"

  (render-int-argb {:data (->> [[255 10 20 30] [255 40 50 60]]
                               (map util/bytes->int)
                               (int-array))
                    :size [2 1]})
  ;; "Äp"
  => string?)

^{:refer std.image.base.display/render :added "3.0"}
(fact "renders an image for output")

^{:refer std.image.base.display/display :added "3.0"}
(fact "outputs an ascii string based on image input"

  (with-out-str
    (display {:data (byte-array [10 20 30 40 50])
              :size [5 1]
              :model (model/model :byte-gray)}))
  ;;"Ã#HXÕß¶$\n"
  )

^{:refer std.image.base.display/animate :added "3.0"}
(fact "allows for animation of images to occur")

(comment
  (./import)
  (./scaffold))
