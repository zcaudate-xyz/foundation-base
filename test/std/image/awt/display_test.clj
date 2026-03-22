(ns std.image.awt.display-test
  (:require [std.image.awt.display :refer :all]
            [std.image.awt.io :as io])
  (:use code.test))

^{:refer std.image.awt.display/create-viewer :added "3.0" :unit #{:gui}}
(comment "creates a viewer for the awt image"

  (create-viewer "hello")
  => (contains {:frame javax.swing.JFrame}))

^{:refer std.image.awt.display/display :added "3.0" :unit #{:gui}}
(comment "displays a BufferedImage in a JFrame"

  (doto (display (io/read "test-data/std.image/circle-100.png")
                 {})
    (-> :frame (.hide))))
