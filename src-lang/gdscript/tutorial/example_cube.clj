(ns gdscript.tutorial.example-cube
  (:require [hara.lang :as l]))

(l/script :gdscript)

(defn.gd build-cube-scene
  "Builds a simple cube scene with a light and camera. Returns the node count."
  {:added "4.1"}
  []
  (gdscript.tutorial.example-3d/clear-scene)
  (gdscript.tutorial.example-3d/add-cube 2 [0 0 0])
  (gdscript.tutorial.example-3d/add-light "DirectionalLight3D" [5 5 5] 5)
  (gdscript.tutorial.example-3d/add-camera [3 3 3])
  (return (gdscript.tutorial.example-3d/node-count)))

(defn.gd save-cube-scene
  "Builds a cube scene and saves it to filepath. Returns the filepath."
  {:added "4.1"}
  [filepath]
  (gdscript.tutorial.example-3d/clear-scene)
  (gdscript.tutorial.example-3d/add-cube 2 [0 0 0])
  (gdscript.tutorial.example-3d/add-light "DirectionalLight3D" [5 5 5] 5)
  (gdscript.tutorial.example-3d/add-camera [3 3 3])
  (return (gdscript.tutorial.example-3d/save-scene-to filepath)))

(comment
  (!.gd (gdscript.tutorial.example-cube/build-cube-scene))
  (clojure.core/deref
   (!.gd (gdscript.tutorial.example-cube/save-cube-scene
          "/tmp/tutorial-cube.tscn"))))
