(ns gdscript.tutorial.example-sphere-test
  (:require [std.lib.env :as env]
            [gdscript.tutorial.example-sphere])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "godot-4"))})

(defn- in-sphere
  "Evaluates form inside the example-sphere namespace."
  [form]
  (binding [*ns* (find-ns 'gdscript.tutorial.example-sphere)]
    (eval form)))

^{:refer gdscript.tutorial.example-sphere/build-sphere-scene :added "4.1"}
(fact "builds a sphere scene with light and camera"
  (in-sphere '(!.gd (gdscript.tutorial.example-sphere/build-sphere-scene)))
  => 3)

^{:refer gdscript.tutorial.example-sphere/save-sphere-scene :added "4.1"}
(fact "saves a sphere scene to disk"
  (let [path (str (System/getProperty "user.home") "/hara_godot_sphere_scene.tscn")]
    (in-sphere `(clojure.core/deref (!.gd (gdscript.tutorial.example-sphere/save-sphere-scene ~path))))
    (boolean (.exists (java.io.File. path))))
  => true)
