(ns python.blender.tutorial.example-cube-test
  (:require [std.lib.env :as env]
            [python.blender.tutorial.example-cube])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(fact:global {:skip (not (or (env/program-exists? "blender")
                              (env/program-exists? "docker")))})

(defn- unwrap [x]
  (if (instance? Wrapped x)
    (clojure.core/deref x)
    x))

(defn- in-cube
  "Evaluates form inside the example-cube namespace."
  [form]
  (binding [*ns* (find-ns 'python.blender.tutorial.example-cube)]
    (unwrap (eval form))))

^{:refer python.blender.tutorial.example-cube/render-cube :added "4.1"}
(fact "renders a cube scene"
  (in-cube '(!.py (python.blender.tutorial.example-cube/render-cube "/tmp/tutorial-cube.png")))
  => "/tmp/tutorial-cube.png")
