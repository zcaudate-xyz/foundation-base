(ns gdscript.tutorial.example-export
  (:require [hara.lang :as l]))

(l/script :gdscript)

(defn.gd export-scene
  "Builds a simple cube scene and saves it as a Godot scene file.
   Returns the output path."
  {:added "4.1"}
  [tscn-path]
  (gdscript.tutorial.example-3d/clear-scene)
  (gdscript.tutorial.example-3d/add-cube 1.5 [0 0 0])
  (gdscript.tutorial.example-3d/add-light "DirectionalLight3D" [4 4 4] 4)
  (gdscript.tutorial.example-3d/add-camera [3 3 3])
  (return (gdscript.tutorial.example-3d/save-scene-to tscn-path)))

(comment
  (clojure.core/deref
   (!.gd (gdscript.tutorial.example-export/export-scene
          "/tmp/tutorial-export.tscn"))))
