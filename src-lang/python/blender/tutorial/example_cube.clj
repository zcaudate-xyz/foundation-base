(ns python.blender.tutorial.example-cube
  (:require [hara.lang :as l]))

(l/script :python
  {:runtime :blender
   :require [[python.blender.tutorial.example-core]]})

(defn.py render-cube
  "Builds a simple cube scene with a light and camera, then renders it to
   out-path. Returns the output path."
  {:added "4.1"}
  [out-path]
  (python.blender.tutorial.example-core/clear-scene!)
  (python.blender.tutorial.example-core/add-cube 2 [0 0 0])
  (python.blender.tutorial.example-core/add-light "SUN" [5 5 5] 5)
  (python.blender.tutorial.example-core/add-camera [3 3 3] [0.7 0 0.9])
  (return (python.blender.tutorial.example-core/render-to out-path)))

(comment
  ;; Render a cube to disk.
  (clojure.core/deref
   (!.py (python.blender.tutorial.example-cube/render-cube "/tmp/tutorial-cube.png"))))
