(ns std.image.awt.io
  (:require [clojure.java.io :as io])
  (:import (java.awt.image BufferedImage) (javax.imageio ImageIO) (javax.imageio.spi IIORegistry ImageReaderWriterSpi ImageInputStreamSpi ImageReaderSpi ImageTranscoderSpi ImageWriterSpi))
  (:refer-clojure :exclude [read]))

(def lookup
  {:reader ImageReaderSpi
   :writer ImageWriterSpi
   :transcoder ImageTranscoderSpi})

(defn providers
  "list providers for ImageIO
 
   (providers :reader)
 
   (providers :writer)"
  {:added "3.0"}
  ([] (providers :reader))
  ([type]
   (-> (IIORegistry/getDefaultInstance)
       (.getServiceProviders (lookup type) true)
       (iterator-seq))))

(defn supported-formats
  "list supported formats for ImageIO
 
   (supported-formats :reader)
   => (contains [\"BMP\" \"GIF\" \"JPEG\" \"JPG\" \"PNG\" \"WBMP\"] :in-any-order :gaps-ok)"
  {:added "3.0"}
  ([] (supported-formats :reader))
  ([type]
   (->> (providers type)
        (mapcat #(.getFormatNames ^ImageReaderWriterSpi %))
        (map #(.toUpperCase ^String %))
        set sort vec)))

(defn awt->int-argb
  "converts an indexed or custom image into and int-argb image
 
   (-> (ImageIO/read (java.io.File. \"test-data/std.image/circle-10.gif\"))
       awt->int-argb)
   ;;#image.awt[10 10]{:model :int-argb}
   => java.awt.image.BufferedImage"
  {:added "3.0"}
  ([^BufferedImage image]
   (let [w (.getWidth image)
         h (.getHeight image)
         nimage (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)]
     (doall (for [i (range h)
                  j (range w)]
              (.setRGB nimage j i (.getRGB image j i))))
     nimage)))

(defn read
  "loads a BufferedImage from input
 
   (read \"test-data/std.image/circle-100.png\")
   ;; #image.awt[100 100]{:model :4-byte-abgr}
   => java.awt.image.BufferedImage"
  {:added "3.0"}
  ([input]
   (read input nil))
  ([input model]
   (let [image (ImageIO/read (io/input-stream input))]
     (if (and (#{BufferedImage/TYPE_CUSTOM
                 BufferedImage/TYPE_BYTE_INDEXED}
               (.getType image))
              model)
       (awt->int-argb image)
       image))))

(defn write
  "saves a BufferedImage to file or stream
 
   (-> (read \"test-data/std.image/circle-100.png\")
       (write \"test-data/std.image/circle-100a.png\" {:format \"PNG\"}))
   => true"
  {:added "3.0"}
  ([^BufferedImage img output opts]
   (ImageIO/write img ^String (:format opts) (io/output-stream output))))

