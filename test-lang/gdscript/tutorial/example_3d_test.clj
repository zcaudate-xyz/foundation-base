(ns gdscript.tutorial.example-3d-test
  (:require [std.lib.env :as env]
            [gdscript.tutorial.example-3d])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "godot-4"))})

(defn- in-3d
  "Evaluates form inside the example-3d namespace so that the !.gd macro
   and the Godot runtime set up by that namespace are used."
  [form]
  (binding [*ns* (find-ns 'gdscript.tutorial.example-3d)]
    (eval form)))

^{:refer gdscript.tutorial.example-3d/clear-scene :added "4.1"}
(fact "clears the eval node children"
  (in-3d '(!.gd (gdscript.tutorial.example-3d/clear-scene)))
  => 0)

^{:refer gdscript.tutorial.example-3d/add-cube :added "4.1"}
(fact "adds a cube mesh"
  (in-3d '(clojure.core/deref (!.gd (gdscript.tutorial.example-3d/add-cube 2 [0 0 0]))))
  => "Cube")

^{:refer gdscript.tutorial.example-3d/add-sphere :added "4.1"}
(fact "adds a sphere mesh"
  (in-3d '(clojure.core/deref (!.gd (gdscript.tutorial.example-3d/add-sphere 1 [0 0 0]))))
  => "Sphere")

^{:refer gdscript.tutorial.example-3d/add-light :added "4.1"}
(fact "adds a directional light"
  (in-3d '(clojure.core/deref (!.gd (gdscript.tutorial.example-3d/add-light "DirectionalLight3D" [5 5 5] 3))))
  => "DirectionalLight3D")

^{:refer gdscript.tutorial.example-3d/add-camera :added "4.1"}
(fact "adds a camera"
  (in-3d '(clojure.core/deref (!.gd (gdscript.tutorial.example-3d/add-camera [3 3 3]))))
  => "Camera")

^{:refer gdscript.tutorial.example-3d/node-count :added "4.1"}
(fact "counts nodes in a scene"
  (in-3d '(!.gd (do (gdscript.tutorial.example-3d/clear-scene)
                    (gdscript.tutorial.example-3d/add-cube 1 [0 0 0])
                    (gdscript.tutorial.example-3d/add-sphere 1 [1 0 0])
                    (gdscript.tutorial.example-3d/node-count))))
  => 2)

^{:refer gdscript.tutorial.example-3d/build-procedural-mesh :added "4.1"}
(fact "builds a procedural mesh"
  (in-3d '(!.gd (gdscript.tutorial.example-3d/build-procedural-mesh)))
  => 1)

^{:refer gdscript.tutorial.example-3d/save-scene-to :added "4.1"}
(fact "saves the current scene to disk"
  (let [path (str (System/getProperty "user.home") "/hara_godot_scene_3d_test.tscn")]
    (in-3d `(do (gdscript.tutorial.example-3d/clear-scene)
                (gdscript.tutorial.example-3d/add-cube 1 [0 0 0])
                (gdscript.tutorial.example-3d/save-scene-to ~path)))
    (boolean (.exists (java.io.File. path))))
  => true)
