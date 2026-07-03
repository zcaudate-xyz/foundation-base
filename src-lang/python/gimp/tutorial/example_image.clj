^{:no-test true}
(ns python.gimp.tutorial.example-image
  (:require [hara.lang :as l]))

(l/script :python
  {:require [[python.gimp.tutorial.example-core :as core]]})

(defn.py create-white-png
  "Creates a width x height white image and saves it to out-path as PNG.
   Returns out-path."
  {:added "4.1"}
  [out-path width height]
  (:= id (core/create-image width height))
  (:= layer (core/add-layer id "Background" width height))
  (:= Gimp (core/ensure-gimp))
  (core/fill-layer layer (. Gimp FillType WHITE))
  (core/save-to id out-path)
  (core/delete-image id)
  (return out-path))

(comment
  (clojure.core/deref
   (!.py (python.gimp.tutorial.example-image/create-white-png
          "/tmp/tutorial-white.png" 64 64))))
