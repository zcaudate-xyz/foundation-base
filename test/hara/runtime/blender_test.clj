(ns hara.runtime.blender-test
  (:require [hara.lang :as l]
            [hara.runtime.blender.impl :as impl]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :python
  {:runtime :blender})

(fact:global {:skip (not (env/program-exists? "blender"))})

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

^{:refer hara.lang/script- :added "4.1"}
(fact "uses blender runtime through hara.lang"
  (try
    [(!.py (+ 1 2 3))
     (string? @(!.py (bpy.data.meshes.new "Cube")))]
    (finally
      (l/rt:stop :python)))
  => [6 true])
