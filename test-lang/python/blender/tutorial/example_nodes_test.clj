(ns python.blender.tutorial.example-nodes-test
  (:require [std.lib.env :as env]
            [python.blender.tutorial.example-nodes])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(fact:global {:skip (not (or (env/program-exists? "blender")
                              (env/program-exists? "docker")))})

(defn- unwrap [x]
  (if (instance? Wrapped x)
    (clojure.core/deref x)
    x))

(defn- in-nodes
  "Evaluates form inside the example-nodes namespace so that the !.py macro
   and the Blender runtime set up by that namespace are used."
  [form]
  (binding [*ns* (find-ns 'python.blender.tutorial.example-nodes)]
    (unwrap (eval form))))

^{:refer python.blender.nodes/ensure-material-tree :added "4.1"}
(fact "returns a material node tree"
  (in-nodes
   '(!.py (do
            (python.blender.tutorial.example-core/clear-scene!)
            (python.blender.tutorial.example-core/add-cube 1 [0 0 0])
            (return (python.blender.tutorial.example-nodes/material-node-count
                     "Cube")))))
  => integer?)

^{:refer python.blender.nodes/clear-tree :added "4.1"}
(fact "removes all nodes from a tree"
  (in-nodes
   '(!.py (do
            (python.blender.tutorial.example-core/clear-scene!)
            (python.blender.tutorial.example-core/add-cube 1 [0 0 0])
            (return (python.blender.tutorial.example-nodes/clear-tree-count
                     "Cube")))))
  => 0)

^{:refer python.blender.nodes/node :added "4.1"}
(fact "adds a node to a tree"
  (in-nodes
   '(!.py (do
            (python.blender.tutorial.example-core/clear-scene!)
            (python.blender.tutorial.example-core/add-cube 1 [0 0 0])
            (return (python.blender.tutorial.example-nodes/add-node-count
                     "Cube")))))
  => 1)

^{:refer python.blender.nodes/link :added "4.1"}
(fact "links two nodes"
  (in-nodes
   '(!.py (do
            (python.blender.tutorial.example-core/clear-scene!)
            (python.blender.tutorial.example-core/add-cube 1 [0 0 0])
            (return (python.blender.tutorial.example-nodes/link-count
                     "Cube")))))
  => 1)

^{:refer python.blender.nodes/build-pipeline :added "4.1"}
(fact "builds a material pipeline from data"
  (in-nodes
   '(!.py (do
            (python.blender.tutorial.example-core/clear-scene!)
            (python.blender.tutorial.example-core/add-cube 1 [0 0 0])
            (return (python.blender.tutorial.example-nodes/build-pipeline-count
                     "Cube")))))
  => 3)

^{:refer python.blender.nodes/ensure-geometry-tree :added "4.1"}
(fact "returns a geometry node tree"
  (in-nodes
   '(!.py (do
            (python.blender.tutorial.example-core/clear-scene!)
            (python.blender.tutorial.example-core/add-cube 1 [0 0 0])
            (return (python.blender.tutorial.example-nodes/geometry-tree-name
                     "Cube")))))
  => string?)

^{:refer python.blender.nodes/ensure-compositor-tree :added "4.1"}
(fact "returns the compositor node tree"
  (in-nodes
   '(!.py (do
            (python.blender.tutorial.example-core/clear-scene!)
            (return (python.blender.tutorial.example-nodes/compositor-tree-name)))))
  => string?)

^{:refer python.blender.tutorial.example-nodes/render-noise-sphere :added "4.1"}
(fact "renders a sphere with a noise material"
  (in-nodes
   '(!.py (python.blender.tutorial.example-nodes/render-noise-sphere
           "NoiseSphere"
           "/tmp/tutorial-nodes.png")))
  => "/tmp/tutorial-nodes.png")
