(ns gdscript.tutorial.example-sphere
  (:require [hara.lang :as l]))

(l/script :gdscript)

(defn.gd build-sphere-scene
  "Builds a sphere scene with a light and camera. Returns the node count."
  {:added "4.1"}
  []
  (gdscript.tutorial.example-3d/clear-scene)
  (gdscript.tutorial.example-3d/add-sphere 1 [0 0 0])
  (gdscript.tutorial.example-3d/add-light "DirectionalLight3D" [5 5 5] 5)
  (gdscript.tutorial.example-3d/add-camera [3 3 3])
  (return (gdscript.tutorial.example-3d/node-count)))

(defn.gd save-sphere-scene
  "Builds a sphere scene and saves it to filepath. Returns the filepath."
  {:added "4.1"}
  [filepath]
  (gdscript.tutorial.example-3d/clear-scene)
  (gdscript.tutorial.example-3d/add-sphere 1 [0 0 0])
  (gdscript.tutorial.example-3d/add-light "DirectionalLight3D" [5 5 5] 5)
  (gdscript.tutorial.example-3d/add-camera [3 3 3])
  (return (gdscript.tutorial.example-3d/save-scene-to filepath)))

(comment
  (!.gd (gdscript.tutorial.example-sphere/build-sphere-scene))
  (clojure.core/deref
   (!.gd (gdscript.tutorial.example-sphere/save-sphere-scene
          "/tmp/tutorial-sphere.tscn"))))
