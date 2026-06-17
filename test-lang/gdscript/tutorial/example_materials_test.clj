(ns gdscript.tutorial.example-materials-test
  (:require [std.lib.env :as env]
            [gdscript.tutorial.example-materials])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "godot-4"))})

(defn- in-materials
  "Evaluates form inside the example-materials namespace."
  [form]
  (binding [*ns* (find-ns 'gdscript.tutorial.example-materials)]
    (eval form)))

^{:refer gdscript.tutorial.example-materials/build-material-sphere :added "4.1"}
(fact "builds a red sphere with a material"
  (in-materials '(!.gd (gdscript.tutorial.example-materials/build-material-sphere)))
  => 3)

^{:refer gdscript.tutorial.example-materials/save-material-sphere :added "4.1"}
(fact "saves a material sphere scene to disk"
  (let [path (str (System/getProperty "user.home") "/hara_godot_materials_scene.tscn")]
    (in-materials `(clojure.core/deref (!.gd (gdscript.tutorial.example-materials/save-material-sphere ~path))))
    (boolean (.exists (java.io.File. path))))
  => true)
