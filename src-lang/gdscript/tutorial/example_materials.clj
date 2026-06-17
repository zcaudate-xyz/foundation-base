(ns gdscript.tutorial.example-materials
  (:require [hara.lang :as l]))

(l/script :gdscript)

(defn.gd build-material-sphere
  "Builds a red sphere with a StandardMaterial3D, light and camera.
   Returns the material name."
  {:added "4.1"}
  []
  (gdscript.tutorial.example-3d/clear-scene)
  (gdscript.tutorial.example-3d/add-sphere 1 [0 0 0])
  (gdscript.tutorial.example-3d/add-material "Sphere" [1 0 0 1])
  (gdscript.tutorial.example-3d/add-light "DirectionalLight3D" [5 5 5] 5)
  (gdscript.tutorial.example-3d/add-camera [3 3 3])
  (return (gdscript.tutorial.example-3d/node-count)))

(defn.gd save-material-sphere
  "Builds a red sphere scene and saves it to filepath. Returns the filepath."
  {:added "4.1"}
  [filepath]
  (gdscript.tutorial.example-3d/clear-scene)
  (gdscript.tutorial.example-3d/add-sphere 1 [0 0 0])
  (gdscript.tutorial.example-3d/add-material "Sphere" [1 0 0 1])
  (gdscript.tutorial.example-3d/add-light "DirectionalLight3D" [5 5 5] 5)
  (gdscript.tutorial.example-3d/add-camera [3 3 3])
  (return (gdscript.tutorial.example-3d/save-scene-to filepath)))

(comment
  (!.gd (gdscript.tutorial.example-materials/build-material-sphere))
  (clojure.core/deref
   (!.gd (gdscript.tutorial.example-materials/save-material-sphere
          "/tmp/tutorial-materials.tscn"))))
