(ns hara.runtime.blender-test
  (:require [hara.runtime.blender.impl :as impl]
            [std.lib.env :as env])
  (:use code.test))

(fact:global {:skip (not (or (env/program-exists? "blender")
                              (env/program-exists? "docker")))})

^{:refer hara.runtime.blender.impl/blender :added "4.1"}
(fact "starts and stops a blender runtime"
  (let [rt (impl/blender {})]
    [(boolean rt)
     (boolean (impl/raw-eval-blender rt "OUT = 1 + 2 + 3"))
     (do (std.lib.component/stop rt)
         true)])
  => [true true true])

^{:refer hara.runtime.blender.impl/raw-eval-blender :added "4.1"}
(fact "evaluates python in blender"
  (let [rt (impl/blender {})]
    (try
      [(impl/raw-eval-blender rt "OUT = 1 + 2 + 3")
       (string? (impl/raw-eval-blender rt "OUT = str(bpy.data.meshes.new('Cube'))"))]
      (finally
        (std.lib.component/stop rt))))
  => [6 true])
