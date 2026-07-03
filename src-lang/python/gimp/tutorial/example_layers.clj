^{:no-test true}
(ns python.gimp.tutorial.example-layers
  (:require [hara.lang :as l]))

(l/script :python
  {:require [[python.gimp.tutorial.example-core :as core]]})

(defn.py create-striped-png
  "Creates a width x height image with horizontal stripes of the given
   color names and saves it to out-path. Returns out-path."
  {:added "4.1"}
  [out-path width height colors]
  (:= id (core/create-image width height))
  (:= Gimp (core/ensure-gimp))
  (:= step (int (/ height (len colors))))
  (var y 0)
  (for [color-name :in colors]
    (:= layer (core/add-layer id (+ "stripe-" color-name) width step))
    (core/fill-layer-color layer color-name)
    (:= item (. Gimp Item (get_by_id layer)))
    (. item (set_offsets 0 y))
    (:= y (+ y step)))
  (core/save-to id out-path)
  (core/delete-image id)
  (return out-path))

(comment
  (clojure.core/deref
   (!.py (python.gimp.tutorial.example-layers/create-striped-png
          "/tmp/tutorial-striped.png" 120 60 ["red" "white" "blue"]))))
