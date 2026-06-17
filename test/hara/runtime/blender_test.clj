(ns hara.runtime.blender-test
  (:require [hara.lang :as l]
            [hara.runtime.blender.impl :as impl]
            [hara.runtime.basic.type-common :as common])
  (:use code.test))

(def +blender-available+
  (delay (common/program-exists? "blender")))

(l/script- :python
  {:runtime :blender})

^{:refer hara.runtime.blender.impl/blender :added "4.1"}
(fact "starts and stops a blender runtime"
  (when @+blender-available+
    (let [rt (impl/blender {})]
      [(boolean rt)
       (boolean (impl/raw-eval-blender rt "OUT = 1 + 2 + 3"))
       (do (std.lib.component/stop rt)
           true)]))
  => (when @+blender-available+
       [true true true]))

^{:refer hara.runtime.blender.impl/raw-eval-blender :added "4.1"}
(fact "evaluates python in blender"
  (when @+blender-available+
    (let [rt (impl/blender {})]
      (try
        [(impl/raw-eval-blender rt "OUT = 1 + 2 + 3")
         (string? (impl/raw-eval-blender rt "OUT = str(bpy.data.meshes.new('Cube'))"))]
        (finally
          (std.lib.component/stop rt)))))
  => (when @+blender-available+
       [6 true]))

^{:refer hara.lang/script- :added "4.1"}
(fact "uses blender runtime through hara.lang"
  (when @+blender-available+
    (try
      [(!.py (+ 1 2 3))
       (string? @(!.py (bpy.data.meshes.new "Cube")))]
      (finally
        (l/rt:stop :python))))
  => (when @+blender-available+
       [6 true]))
