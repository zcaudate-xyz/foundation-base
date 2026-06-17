(ns python.blender.tutorial.example-materials-test
  (:require [std.lib.env :as env]
            [python.blender.tutorial.example-materials])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "blender"))})

(defn- unwrap [x]
  (if (instance? Wrapped x)
    (clojure.core/deref x)
    x))

(defn- in-materials
  "Evaluates form inside the example-materials namespace."
  [form]
  (binding [*ns* (find-ns 'python.blender.tutorial.example-materials)]
    (unwrap (eval form))))

^{:refer python.blender.tutorial.example-materials/render-material-sphere :added "4.1"}
(fact "renders a sphere with a material"
  (in-materials
   '(!.py (python.blender.tutorial.example-materials/render-material-sphere
           "/tmp/tutorial-materials.png")))
  => "/tmp/tutorial-materials.png")
