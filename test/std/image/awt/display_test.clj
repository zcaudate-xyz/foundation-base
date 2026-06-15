(ns std.image.awt.display-test
  (:require [std.image.awt.display :refer :all]
            [std.image.awt.io :as io])
  (:use code.test))

^{:refer std.image.awt.display/create-modeler :added "3.0" :unit #{:gui}}
(fact "creates a viewer for the awt image"
  (let [viewer (try
                 (create-modeler "hello")
                 (catch java.awt.HeadlessException _
                    :headless))]
    (if (= viewer :headless)
      true
      (contains? viewer :frame)))
  => true)

^{:refer std.image.awt.display/display :added "3.0" :unit #{:gui}}
(fact "displays a BufferedImage in a JFrame"
  (let [result (try
                 (let [viewer (display (io/read "test-data/std.image/circle-100.png")
                                        {})]
                    (.setVisible ^javax.swing.JFrame (:frame viewer) false)
                    viewer)
                  (catch java.awt.HeadlessException _
                    :headless))]
    (if (= result :headless)
      true
      (contains? result :frame)))
  => true)
