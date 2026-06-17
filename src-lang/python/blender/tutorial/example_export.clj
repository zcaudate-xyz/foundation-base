(ns python.blender.tutorial.example-export
  (:require [hara.lang :as l]))

(l/script :python
  {:runtime :blender
   :require [[python.blender.tutorial.example-core]]})

(defn.py export-scene
  "Builds a simple cube scene and outputs it as a .blend file and an STL
   mesh file. Returns a list of the output paths."
  {:added "4.1"}
  [blend-path stl-path]
  (python.blender.tutorial.example-core/clear-scene!)
  (python.blender.tutorial.example-core/add-cube 1.5 [0 0 0])
  (python.blender.tutorial.example-core/add-light "SUN" [4 4 4] 4)
  (python.blender.tutorial.example-core/add-camera [3 3 3] [0.7 0 0.9])
  (return [(python.blender.tutorial.example-core/save-blend-to blend-path)
           (python.blender.tutorial.example-core/export-stl-to stl-path)]))

(comment
  ;; Write the scene to .blend and .stl.
  (clojure.core/deref
   (!.py (python.blender.tutorial.example-export/export-scene
          "/tmp/tutorial-output.blend"
          "/tmp/tutorial-output.stl")))

  ;; With a full Blender install (numpy available) you can also use:
  ;;   bpy.ops.export_scene.gltf(filepath=...)
  ;;   bpy.ops.export_scene.fbx(filepath=...)
  )
