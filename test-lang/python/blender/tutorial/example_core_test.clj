(ns python.blender.tutorial.example-core-test
  (:require [std.lib.env :as env]
            [python.blender.tutorial.example-core])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(fact:global {:skip (not (or (env/program-exists? "blender")
                              (env/program-exists? "docker")))})

(defn- unwrap [x]
  (if (instance? Wrapped x)
    (clojure.core/deref x)
    x))

(defn- in-core
  "Evaluates form inside the example-core namespace so that the !.py macro
   and the Blender runtime set up by that namespace are used."
  [form]
  (binding [*ns* (find-ns 'python.blender.tutorial.example-core)]
    (unwrap (eval form))))

^{:refer python.blender.tutorial.example-core/clear-scene! :added "4.1"}
(fact "clears the default scene"
  (in-core '(!.py (python.blender.tutorial.example-core/clear-scene!)))
  => 0)

^{:refer python.blender.tutorial.example-core/add-cube :added "4.1"}
(fact "adds a cube"
  (in-core '(!.py (python.blender.tutorial.example-core/add-cube 2 [0 0 0])))
  => string?)

^{:refer python.blender.tutorial.example-core/add-sphere :added "4.1"}
(fact "adds a sphere"
  (in-core '(!.py (python.blender.tutorial.example-core/add-sphere 1 [0 0 0])))
  => string?)

^{:refer python.blender.tutorial.example-core/add-light :added "4.1"}
(fact "adds a light"
  (in-core '(!.py (python.blender.tutorial.example-core/add-light "SUN" [5 5 5] 5)))
  => string?)

^{:refer python.blender.tutorial.example-core/add-camera :added "4.1"}
(fact "adds a camera"
  (in-core '(!.py (python.blender.tutorial.example-core/add-camera [3 3 3] [0.7 0 0.9])))
  => string?)

^{:refer python.blender.tutorial.example-core/add-principled-material :added "4.1"}
(fact "adds a material"
  (in-core
   '(!.py
     (python.blender.tutorial.example-core/add-principled-material
      (python.blender.tutorial.example-core/add-sphere 1 [0 0 0])
      [1 0 0 1])))
  => string?)

^{:refer python.blender.tutorial.example-core/render-to :added "4.1"}
(fact "renders to a png file"
  (in-core
   '(!.py (do
            (python.blender.tutorial.example-core/clear-scene!)
            (python.blender.tutorial.example-core/add-cube 2 [0 0 0])
            (python.blender.tutorial.example-core/add-light "SUN" [5 5 5] 5)
            (python.blender.tutorial.example-core/add-camera [3 3 3] [0.7 0 0.9])
            (python.blender.tutorial.example-core/render-to "/tmp/tutorial-core-render.png"))))
  => "/tmp/tutorial-core-render.png")

^{:refer python.blender.tutorial.example-core/save-blend-to :added "4.1"}
(fact "saves a .blend file"
  (in-core '(!.py (python.blender.tutorial.example-core/save-blend-to "/tmp/tutorial-core.blend")))
  => "/tmp/tutorial-core.blend")

^{:refer python.blender.tutorial.example-core/export-stl-to :added "4.1"}
(fact "exports an stl file"
  (in-core
   '(!.py (do
            (python.blender.tutorial.example-core/clear-scene!)
            (python.blender.tutorial.example-core/add-cube 1 [0 0 0])
            (python.blender.tutorial.example-core/export-stl-to "/tmp/tutorial-core.stl"))))
  => "/tmp/tutorial-core.stl")
