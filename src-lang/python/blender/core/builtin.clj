(ns python.blender.core.builtin
  "Blender Python (bpy) builtins and namespaces.

   This file is a hand-curated outline of the most commonly used `bpy` module
   members, similar in purpose to `postgres.core.builtin` and
   `matlab.core.builtin`.  It is not an exhaustive introspection of the bpy API;
   new entries can be added here as needed."
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [eval]))

(l/script :python {:runtime :blender}
  python.blender.core
  {})

;; ---------------------------------------------------------------------------
;; Top-level module
;; ---------------------------------------------------------------------------

(def.py bpy bpy)

;; ---------------------------------------------------------------------------
;; Core submodules
;; ---------------------------------------------------------------------------

(def.py context bpy.context)
(def.py data bpy.data)
(def.py ops bpy.ops)
(def.py props bpy.props)
(def.py types bpy.types)
(def.py utils bpy.utils)
(def.py app bpy.app)
(def.py msgbus bpy.msgbus)

;; ---------------------------------------------------------------------------
;; bpy.data collections
;; ---------------------------------------------------------------------------

(def.py objects bpy.data.objects)
(def.py meshes bpy.data.meshes)
(def.py materials bpy.data.materials)
(def.py collections bpy.data.collections)
(def.py cameras bpy.data.cameras)
(def.py lights bpy.data.lights)
(def.py images bpy.data.images)
(def.py node-groups bpy.data.node_groups)
(def.py worlds bpy.data.worlds)
(def.py scenes bpy.data.scenes)
(def.py actions bpy.data.actions)
(def.py armatures bpy.data.armatures)
(def.py curves bpy.data.curves)
(def.py grease-pencils bpy.data.grease_pencils)
(def.py lattices bpy.data.lattices)
(def.py metaballs bpy.data.metaballs)
(def.py movies bpy.data.movies)
(def.py palettes bpy.data.palettes)
(def.py particles bpy.data.particles)
(def.py sounds bpy.data.sounds)
(def.py speakers bpy.data.speakers)
(def.py texts bpy.data.texts)
(def.py textures bpy.data.textures)
(def.py window-managers bpy.data.window_managers)

;; ---------------------------------------------------------------------------
;; bpy.context accessors
;; ---------------------------------------------------------------------------

(def.py context:scene bpy.context.scene)
(def.py context:view-layer bpy.context.view_layer)
(def.py context:object bpy.context.object)
(def.py context:active-object bpy.context.active_object)
(def.py context:selected-objects bpy.context.selected_objects)
(def.py context:selected-editable-objects bpy.context.selected_editable_objects)
(def.py context:visible-objects bpy.context.visible_objects)
(def.py context:mode bpy.context.mode)
(def.py context:area bpy.context.area)
(def.py context:region bpy.context.region)
(def.py context:space-data bpy.context.space_data)

;; ---------------------------------------------------------------------------
;; Common collection methods
;; ---------------------------------------------------------------------------

(def$.py ^{:arglists '([name & [default]])}
  objects:get
  bpy.data.objects.get)

(def$.py ^{:arglists '([name & [default]])}
  meshes:get
  bpy.data.meshes.get)

(def$.py ^{:arglists '([name & [default]])}
  materials:get
  bpy.data.materials.get)

(def$.py ^{:arglists '([name & [default]])}
  collections:get
  bpy.data.collections.get)

(def$.py ^{:arglists '([name & [default]])}
  cameras:get
  bpy.data.cameras.get)

(def$.py ^{:arglists '([name & [default]])}
  lights:get
  bpy.data.lights.get)

(def$.py ^{:arglists '([name])}
  objects:new
  bpy.data.objects.new)

(def$.py ^{:arglists '([name data])}
  meshes:new
  bpy.data.meshes.new)

(def$.py ^{:arglists '([name])}
  materials:new
  bpy.data.materials.new)

(def$.py ^{:arglists '([name])}
  collections:new
  bpy.data.collections.new)

(def$.py ^{:arglists '([name data])}
  cameras:new
  bpy.data.cameras.new)

(def$.py ^{:arglists '([name data])}
  lights:new
  bpy.data.lights.new)

;; ---------------------------------------------------------------------------
;; Common mesh/object operators
;; ---------------------------------------------------------------------------

(def$.py ^{:arglists '([& kwargs])}
  ops:cube-add
  bpy.ops.mesh.primitive_cube_add)

(def$.py ^{:arglists '([& kwargs])}
  ops:sphere-add
  bpy.ops.mesh.primitive_uv_sphere_add)

(def$.py ^{:arglists '([& kwargs])}
  ops:ico-sphere-add
  bpy.ops.mesh.primitive_ico_sphere_add)

(def$.py ^{:arglists '([& kwargs])}
  ops:cylinder-add
  bpy.ops.mesh.primitive_cylinder_add)

(def$.py ^{:arglists '([& kwargs])}
  ops:cone-add
  bpy.ops.mesh.primitive_cone_add)

(def$.py ^{:arglists '([& kwargs])}
  ops:torus-add
  bpy.ops.mesh.primitive_torus_add)

(def$.py ^{:arglists '([& kwargs])}
  ops:plane-add
  bpy.ops.mesh.primitive_plane_add)

(def$.py ^{:arglists '([& kwargs])}
  ops:light-add
  bpy.ops.object.light_add)

(def$.py ^{:arglists '([& kwargs])}
  ops:camera-add
  bpy.ops.object.camera_add)

(def$.py ^{:arglists '([& kwargs])}
  ops:delete
  bpy.ops.object.delete)

(def$.py ^{:arglists '([& kwargs])}
  ops:select-all
  bpy.ops.object.select_all)

;; ---------------------------------------------------------------------------
;; File / render operators
;; ---------------------------------------------------------------------------

(def$.py ^{:arglists '([& kwargs])}
  ops:save-as-mainfile
  bpy.ops.wm.save_as_mainfile)

(def$.py ^{:arglists '([& kwargs])}
  ops:render
  bpy.ops.render.render)

;; ---------------------------------------------------------------------------
;; Utility functions
;; ---------------------------------------------------------------------------

(def$.py ^{:arglists '([obj name])}
  utils:previews:clear
  bpy.utils.previews.clear)

(def$.py ^{:arglists '([id])}
  utils:unregister-class
  bpy.utils.unregister_class)

(def$.py ^{:arglists '([id])}
  utils:register-class
  bpy.utils.register_class)

(comment
  ;; Eval in a REPL with the :blender runtime active.
  (!.py (python.blender.core/context:scene))
  (!.py (python.blender.core/objects:new "Empty"))
  (!.py (python.blender.core/materials:new "Material"))
  (!.py (python.blender.core/objects:get "Cube")))
