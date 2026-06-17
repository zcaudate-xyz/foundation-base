(ns python.blender.core
  "Core Blender Python (bpy) bindings for the xtalk Python DSL.

   Provides the main `bpy` module, its submodules, and common data-collection
   helpers under the `python.blender.core` namespace.  Requiring this namespace
   lets Blender scripts refer to `objects`, `materials`, `context:scene`, etc.
   without repeatedly typing the full `bpy.data...` path."
  (:require [python.blender.core.builtin :as builtin]
            [hara.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [eval]))

(f/intern-all python.blender.core.builtin)

(l/script :python {:runtime :blender}
  python.blender.core
  {})

(comment
  ;; After requiring this namespace in a REPL with the :blender runtime active,
  ;; the following forms work:
  ;;
  ;;   (objects.new "Cube")
  ;;   (materials.new "Material")
  ;;   (objects:get "Cube")
  ;;   (context:scene)
  )
