(ns gdscript.tutorial.example-procedural-test
  (:require [std.lib.env :as env]
            [gdscript.tutorial.example-procedural])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "godot-4"))})

(defn- in-procedural
  "Evaluates form inside the example-procedural namespace."
  [form]
  (binding [*ns* (find-ns 'gdscript.tutorial.example-procedural)]
    (eval form)))

^{:refer gdscript.tutorial.example-procedural/build-procedural-scene :added "4.1"}
(fact "builds a procedural mesh scene"
  (in-procedural '(!.gd (gdscript.tutorial.example-procedural/build-procedural-scene)))
  => 1)

^{:refer gdscript.tutorial.example-procedural/save-procedural-scene :added "4.1"}
(fact "saves a procedural mesh scene to disk"
  (let [path (str (System/getProperty "user.home") "/hara_godot_procedural_scene.tscn")]
    (in-procedural `(clojure.core/deref (!.gd (gdscript.tutorial.example-procedural/save-procedural-scene ~path))))
    (boolean (.exists (java.io.File. path))))
  => true)
