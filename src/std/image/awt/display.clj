(ns std.image.awt.display
  (:require [std.image.awt.common :as awt]
            [std.image.base.common :as base])
  (:import (java.awt BorderLayout Dimension Graphics) (java.awt.image BufferedImage) (javax.swing JComponent JFrame WindowConstants)))

(defn create-viewer
  "creates a viewer for the awt image
 
   (create-viewer \"hello\")
   => (contains {:frame javax.swing.JFrame})"
  {:added "3.0"}
  ([] (create-viewer (str (rand-int 100))))
  ([^String title]
   (let [container   (atom nil)
         picture (proxy [JComponent] []
                   (paintComponent [^Graphics g]
                     (if @container
                       (.drawImage g @container 0 0 nil))))
         frame   (doto (JFrame. title)
                   (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE)
                   (.setLayout (BorderLayout.))
                   (.add picture BorderLayout/CENTER)
                   (.pack))]
     {:container container
      :picture picture
      :frame frame})))

(defn display
  "displays a BufferedImage in a JFrame
 
   (doto (display (io/read \"test-data/std.image/circle-100.png\")
                  {})
    (-> :frame (.hide)))"
  {:added "3.0"}
  ([image {:keys [viewer
                  channel]
           :as opts}]
   (let [{:keys [^JFrame frame
                 ^JComponent picture
                 container] :as viewer} (or viewer (create-viewer))
         ^BufferedImage image (if channel
                                (-> (base/slice (base/base-map image) channel)
                                    (awt/image))
                                image)
         size (Dimension. (.getWidth image) (.getHeight image))
         g    (.getGraphics frame)]
     (do (reset! container image)
         (.setPreferredSize picture size)
         (.pack frame)
         (.show frame)
         (.update frame g)
         viewer))))
