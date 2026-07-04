(ns python.blender.nodes-test
  (:require [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :python
  {:runtime :blender
   :require [[python.blender.nodes :as n]
             [python.blender.tutorial.example-core :as core]]})

(fact:global
 {:skip     (not (or (env/program-exists? "blender")
                     (env/program-exists? "docker")))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.blender.nodes/ensure-material-tree :added "4.1"}
(fact "returns a material node tree"

  (!.py (do (core/clear-scene!)
            (core/add-cube 1 [0 0 0])
            (var tree (n/ensure-material-tree "Cube" nil))
            (return (len tree.nodes))))
  => #(> % 0))

^{:refer python.blender.nodes/ensure-geometry-tree :added "4.1"}
(fact "returns a geometry node tree"

  (!.py (do (core/clear-scene!)
            (core/add-cube 1 [0 0 0])
            (var tree (n/ensure-geometry-tree "Cube" nil))
            (return (> (len tree.name) 0))))
  => true)

^{:refer python.blender.nodes/ensure-compositor-tree :added "4.1"}
(fact "returns the compositor node tree"

  (!.py (do (core/clear-scene!)
            (var tree (n/ensure-compositor-tree))
            (return (> (len tree.name) 0))))
  => true)

^{:refer python.blender.nodes/clear-tree :added "4.1"}
(fact "removes all nodes from a tree"

  (!.py (do (core/clear-scene!)
            (core/add-cube 1 [0 0 0])
            (var tree (n/ensure-material-tree "Cube" nil))
            (n/clear-tree tree)
            (return (len tree.nodes))))
  => 0)

^{:refer python.blender.nodes/node :added "4.1"}
(fact "adds a node to a tree"

  (!.py (do (core/clear-scene!)
            (core/add-cube 1 [0 0 0])
            (var tree (n/ensure-material-tree "Cube" nil))
            (n/clear-tree tree)
            (n/node tree "Noise" "ShaderNodeTexNoise" {})
            (return (len tree.nodes))))
  => 1)

^{:refer python.blender.nodes/link :added "4.1"}
(fact "links two nodes"

  (!.py (do (core/clear-scene!)
            (core/add-cube 1 [0 0 0])
            (var tree (n/ensure-material-tree "Cube" nil))
            (n/clear-tree tree)
            (n/node tree "Noise" "ShaderNodeTexNoise" {})
            (n/node tree "Output" "ShaderNodeOutputMaterial" {})
            (n/link tree "Noise" "Fac" "Output" "Surface")
            (return (len tree.links))))
  => 1)

^{:refer python.blender.nodes/build-pipeline :added "4.1"}
(fact "builds a material pipeline from data"

  (!.py (do (core/clear-scene!)
            (core/add-cube 1 [0 0 0])
            (var tree (n/build-pipeline {:type "material" :obj "Cube" :mat "PipelineMat"}
                                        [["clear"]
                                         ["node" "Noise" "ShaderNodeTexNoise" {:scale 5.0}]
                                         ["node" "Diffuse" "ShaderNodeBsdfDiffuse" {}]
                                         ["node" "Output" "ShaderNodeOutputMaterial" {}]
                                         ["link" "Noise" "Fac" "Diffuse" "Color"]
                                         ["link" "Diffuse" "BSDF" "Output" "Surface"]]))
            (return (len tree.nodes))))
  => 3)
