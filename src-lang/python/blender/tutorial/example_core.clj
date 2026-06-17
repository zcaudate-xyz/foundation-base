(ns python.blender.tutorial.example-core
  (:require [hara.lang :as l]))

(l/script :python {:runtime :blender})

(defn.py clear-scene!
  "Removes all objects from the current Blender scene and returns the
   remaining object count."
  {:added "4.1"}
  []
  (for [obj :in (list bpy.data.objects)]
    (. bpy.data.objects (remove obj :do_unlink True)))
  (return (len bpy.data.objects)))

(defn.py add-cube
  "Adds a cube of the given size at location and returns the object name."
  {:added "4.1"}
  [size location]
  (bpy.ops.mesh.primitive_cube_add :size size :location location)
  (return bpy.context.view_layer.objects.active.name))

(defn.py add-sphere
  "Adds a UV sphere of the given radius at location and returns the object name."
  {:added "4.1"}
  [radius location]
  (bpy.ops.mesh.primitive_uv_sphere_add :radius radius :location location)
  (return bpy.context.view_layer.objects.active.name))

(defn.py add-light
  "Adds a light of the given type at location with the specified energy
   and returns the light object name."
  {:added "4.1"}
  [type location energy]
  (bpy.ops.object.light_add :type type :location location)
  (:= light bpy.context.view_layer.objects.active)
  (:= (. light data energy) energy)
  (return light.name))

(defn.py add-camera
  "Adds a camera at location with rotation_euler and sets it as the active
   scene camera. Returns the camera object name."
  {:added "4.1"}
  [location rotation]
  (bpy.ops.object.camera_add :location location)
  (:= cam bpy.context.view_layer.objects.active)
  (:= (. cam rotation_euler) rotation)
  (:= (. bpy.context.scene camera) cam)
  (return cam.name))

(defn.py add-principled-material
  "Creates a Principled BSDF material with the given base color and appends
   it to the object named obj-name. Returns the material name."
  {:added "4.1"}
  [obj-name color]
  (:= obj (. bpy.data.objects [obj-name]))
  (:= mat (bpy.data.materials.new (+ obj-name "-Material")))
  (:= (. mat use_nodes) true)
  (:= tree (. mat node_tree))
  (:= nodes (. tree nodes))
  (:= bsdf (. nodes ["Principled BSDF"]))
  (:= inputs (. bsdf inputs))
  (:= base (. inputs ["Base Color"]))
  (:= (. base default_value) color)
  (. obj data materials (append mat))
  (return mat.name))

(defn.py render-to
  "Renders the current scene to filepath using the Workbench engine and
   returns the filepath."
  {:added "4.1"}
  [filepath]
  (:= (. bpy.context.scene.render engine) "BLENDER_WORKBENCH")
  (:= (. bpy.context.scene.render resolution_x) 320)
  (:= (. bpy.context.scene.render resolution_y) 240)
  (:= (. bpy.context.scene.render filepath) filepath)
  (bpy.ops.render.render :write_still true)
  (return filepath))

(defn.py save-blend-to
  "Saves the current .blend file to filepath and returns the filepath."
  {:added "4.1"}
  [filepath]
  (bpy.ops.wm.save_as_mainfile :filepath filepath :check_existing false)
  (return filepath))

(defn.py export-stl-to
  "Exports the active object's mesh to filepath as an STL file and returns
   the filepath."
  {:added "4.1"}
  [filepath]
  (bpy.ops.export_mesh.stl :filepath filepath)
  (return filepath))

(comment
  ;; Call the Python functions from Clojure with !.py.
  ;; The result is a Wrapped pointer; use clojure.core/deref to get the value.
  (clojure.core/deref (!.py (python.blender.tutorial.example-core/clear-scene!)))
  (clojure.core/deref (!.py (python.blender.tutorial.example-core/add-cube 2 [0 0 0])))
  (clojure.core/deref (!.py (python.blender.tutorial.example-core/add-light "SUN" [5 5 5] 5)))
  (clojure.core/deref (!.py (python.blender.tutorial.example-core/add-camera [3 3 3] [0.7 0 0.9])))
  (clojure.core/deref (!.py (python.blender.tutorial.example-core/render-to "/tmp/tutorial-core.png"))))
