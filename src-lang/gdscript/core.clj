(ns gdscript.core
  (:require [gdscript.core.builtin :as builtin]
            [hara.lang :as l]
            [std.lib.foundation :as f]))

(f/intern-all gdscript.core.builtin)

(l/script :gdscript
  {})

(comment
  ;; gdscript.core provides an outline of Godot builtins:
  ;;   +utility-functions+  - global functions like sin, abs, clamp, print
  ;;   +builtin-classes+    - value types like Vector3, Color, Array
  ;;   +singletons+         - engine singletons like ClassDB, OS, RenderingServer
  ;;   +classes-*+          - engine classes grouped by inheritance
  )
