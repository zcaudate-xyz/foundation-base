(ns gdscript.tutorial.example-procedural
  (:require [hara.lang :as l]))

(l/script :gdscript {:runtime :godot
                     :config {:bench :scratch}
                     :require [[gdscript.tutorial.example-3d]]})

(defn.gd build-procedural-scene
  "Builds a procedural quad mesh and returns the number of surfaces."
  {:added "4.1"}
  []
  (gdscript.tutorial.example-3d/clear-scene)
  (return (gdscript.tutorial.example-3d/build-procedural-mesh)))

(defn.gd save-procedural-scene
  "Builds a procedural mesh scene and saves it to filepath. Returns the filepath."
  {:added "4.1"}
  [filepath]
  (gdscript.tutorial.example-3d/clear-scene)
  (gdscript.tutorial.example-3d/build-procedural-mesh)
  (return (gdscript.tutorial.example-3d/save-scene-to filepath)))

(comment
  (!.gd (gdscript.tutorial.example-procedural/build-procedural-scene))
  (clojure.core/deref
   (!.gd (gdscript.tutorial.example-procedural/save-procedural-scene
          "/tmp/tutorial-procedural.tscn"))))
