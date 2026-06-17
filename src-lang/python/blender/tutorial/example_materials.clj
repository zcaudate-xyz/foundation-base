(ns python.blender.tutorial.example-materials
  (:require [hara.lang :as l]))

(l/script :python
  {:runtime :blender
   :require [[python.blender.tutorial.example-core]]})

(defn.py render-material-sphere
  "Builds a scene with a red sphere, light and camera, then renders it to
   out-path. Returns the output path."
  {:added "4.1"}
  [out-path]
  (python.blender.tutorial.example-core/clear-scene!)
  (:= sphere (python.blender.tutorial.example-core/add-sphere 1 [0 0 0]))
  (python.blender.tutorial.example-core/add-principled-material sphere [1 0 0 1])
  (python.blender.tutorial.example-core/add-light "SUN" [5 5 5] 5)
  (python.blender.tutorial.example-core/add-camera [3 3 3] [0.7 0 0.9])
  (return (python.blender.tutorial.example-core/render-to out-path)))

(comment
  ;; Render a sphere with a Principled BSDF material.
  (clojure.core/deref
   (!.py (python.blender.tutorial.example-materials/render-material-sphere
          "/tmp/tutorial-materials.png"))))
