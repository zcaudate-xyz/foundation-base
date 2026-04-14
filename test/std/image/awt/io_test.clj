(ns std.image.awt.io-test
  (:require [std.image.awt.io :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [read])
  (:import javax.imageio.ImageIO))

^{:refer std.image.awt.io/providers :added "3.0"}
(fact "list providers for ImageIO"

  (count (providers :reader))
  => pos?

  (count (providers :writer))
  => pos?)

^{:refer std.image.awt.io/supported-formats :added "3.0"}
(fact "list supported formats for ImageIO"

  (supported-formats :reader)
  => (contains ["BMP" "GIF" "JPEG" "JPG" "PNG" "WBMP"] :in-any-order :gaps-ok))

^{:refer std.image.awt.io/awt->int-argb :added "3.0"}
(fact "converts an indexed or custom image into and int-argb image"

  (-> (ImageIO/read (java.io.File. "test-data/std.image/circle-10.gif"))
      awt->int-argb)
  ;;#image.awt[10 10]{:model :int-argb}
  => java.awt.image.BufferedImage)

^{:refer std.image.awt.io/read :added "3.0"}
(fact "loads a BufferedImage from input"

  (read "test-data/std.image/circle-100.png")
  ;; #image.awt[100 100]{:model :4-byte-abgr}
  => java.awt.image.BufferedImage)

^{:refer std.image.awt.io/write :added "3.0"}
(fact "saves a BufferedImage to file or stream"

  (-> (read "test-data/std.image/circle-100.png")
      (write "test-data/std.image/circle-100a.png" {:format "PNG"}))
  => true)

(comment
  (./import)
  (./scaffold))
