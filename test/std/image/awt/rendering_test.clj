(ns std.image.awt.rendering-test
  (:require [std.image.awt.rendering :refer :all])
  (:use code.test)
  (:import java.awt.RenderingHints))

^{:refer std.image.awt.rendering/hint-options :added "3.0"}
(fact "show the options for hints"

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
                :fractionalmetrics [:default :off :on]}))

^{:refer std.image.awt.rendering/hints :added "3.0"}
(fact "creates a map to be used in `setRenderingHints` on `Graphics2d` objects"

  (hints {:antialiasing :on})
  => {RenderingHints/KEY_ANTIALIASING
      RenderingHints/VALUE_ANTIALIAS_ON})

(comment
  (./import))