(ns python.blender.tutorial.example-export-test
  (:require [std.lib.env :as env]
            [python.blender.tutorial.example-export])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(fact:global {:skip (not (or (env/program-exists? "blender")
                              (env/program-exists? "docker")))})

(defn- unwrap [x]
  (if (instance? Wrapped x)
    (clojure.core/deref x)
    x))

(defn- in-export
  "Evaluates form inside the example-export namespace."
  [form]
  (binding [*ns* (find-ns 'python.blender.tutorial.example-export)]
    (unwrap (eval form))))

^{:refer python.blender.tutorial.example-export/export-scene :added "4.1"}
(fact "exports a scene to multiple formats"
  (in-export
   '(!.py (python.blender.tutorial.example-export/export-scene
           "/tmp/tutorial-output.blend"
           "/tmp/tutorial-output.stl")))
  => ["/tmp/tutorial-output.blend"
      "/tmp/tutorial-output.stl"])
