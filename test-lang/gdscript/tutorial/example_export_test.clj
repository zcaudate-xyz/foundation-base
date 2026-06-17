(ns gdscript.tutorial.example-export-test
  (:require [std.lib.env :as env]
            [gdscript.tutorial.example-export])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "godot-4"))})

(defn- in-export
  "Evaluates form inside the example-export namespace."
  [form]
  (binding [*ns* (find-ns 'gdscript.tutorial.example-export)]
    (eval form)))

^{:refer gdscript.tutorial.example-export/export-scene :added "4.1"}
(fact "exports a cube scene to a Godot scene file"
  (let [path (str (System/getProperty "user.home") "/hara_godot_export_scene.tscn")]
    (in-export `(clojure.core/deref (!.gd (gdscript.tutorial.example-export/export-scene ~path))))
    (boolean (.exists (java.io.File. path))))
  => true)
