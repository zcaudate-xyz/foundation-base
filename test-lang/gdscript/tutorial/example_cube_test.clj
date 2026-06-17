(ns gdscript.tutorial.example-cube-test
  (:require [std.lib.env :as env]
            [gdscript.tutorial.example-cube])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "godot-4"))})

(defn- in-cube
  "Evaluates form inside the example-cube namespace."
  [form]
  (binding [*ns* (find-ns 'gdscript.tutorial.example-cube)]
    (eval form)))

^{:refer gdscript.tutorial.example-cube/build-cube-scene :added "4.1"}
(fact "builds a cube scene with light and camera"
  (in-cube '(!.gd (gdscript.tutorial.example-cube/build-cube-scene)))
  => 3)

^{:refer gdscript.tutorial.example-cube/save-cube-scene :added "4.1"}
(fact "saves a cube scene to disk"
  (let [path (str (System/getProperty "user.home") "/hara_godot_cube_scene.tscn")]
    (in-cube `(clojure.core/deref (!.gd (gdscript.tutorial.example-cube/save-cube-scene ~path))))
    (boolean (.exists (java.io.File. path))))
  => true)
