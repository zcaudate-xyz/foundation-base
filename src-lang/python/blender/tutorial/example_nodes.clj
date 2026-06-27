(ns python.blender.tutorial.example-nodes
  (:require [hara.lang :as l]))

(l/script :python
  {:runtime :blender
   :require [[python.blender.nodes :as n]
             [python.blender.tutorial.example-core]
             [python.blender.tutorial.example-nodes :as self]]})

(defn.py make-noise-material
  "Builds a procedural noise -> diffuse material on obj-name using the
   pipeline helpers and returns the material node tree."
  {:added "4.1"}
  [obj-name]
  (-> (n/ensure-material-tree obj-name nil)
      (n/clear-tree)
      (n/node "Noise" "ShaderNodeTexNoise" {:scale 5.0
                                            :location [-300 0]})
      (n/node "Diffuse" "ShaderNodeBsdfDiffuse" {:location [0 0]})
      (n/node "Output" "ShaderNodeOutputMaterial" {:location [300 0]})
      (n/link "Noise" "Fac" "Diffuse" "Color")
      (n/link "Diffuse" "BSDF" "Output" "Surface")))

(defn.py render-noise-sphere
  "Clears the scene, adds a sphere with a noise material, light and
   camera, then renders the image to out-path. Returns out-path."
  {:added "4.1"}
  [obj-name out-path]
  (python.blender.tutorial.example-core/clear-scene!)
  (python.blender.tutorial.example-core/add-sphere 1 [0 0 0])
  (:= (. bpy.context.view_layer.objects.active name) obj-name)
  (self/make-noise-material obj-name)
  (python.blender.tutorial.example-core/add-light "SUN" [5 5 5] 5)
  (python.blender.tutorial.example-core/add-camera [3 3 3] [0.7 0 0.9])
  (return (python.blender.tutorial.example-core/render-to out-path)))

(defn.py build-geometry-pipeline
  "Creates a geometry nodes modifier on obj-name and builds a pipeline
   that subdivides the mesh and sets material. Returns the tree."
  {:added "4.1"}
  [obj-name]
  (-> (n/ensure-geometry-tree obj-name nil)
      (n/node "Input" "NodeGroupInput" {:location [-400 0]})
      (n/node "Subdivide" "GeometryNodeSubdivisionSurface" {:location [-100 0]})
      (n/node "Output" "NodeGroupOutput" {:location [200 0]})
      (n/link "Input" "Geometry" "Subdivide" "Mesh")
      (n/link "Subdivide" "Mesh" "Output" "Geometry")))

(comment
  ;; Render a sphere with a procedural noise material.
  (clojure.core/deref
   (!.py (python.blender.tutorial.example-nodes/render-noise-sphere
          "NoiseSphere"
          "/tmp/tutorial-nodes.png")))

  ;; Build a geometry-nodes pipeline on the active object.
  (clojure.core/deref
   (!.py (python.blender.tutorial.example-nodes/build-geometry-pipeline
          "Cube"))))

(defn.py material-node-count
  "Test helper: returns the number of nodes in a fresh material tree."
  {:added "4.1"}
  [obj-name]
  (:= tree (n/ensure-material-tree obj-name nil))
  (return (len tree.nodes)))

(defn.py clear-tree-count
  "Test helper: clears a material tree and returns the node count."
  {:added "4.1"}
  [obj-name]
  (:= tree (n/ensure-material-tree obj-name nil))
  (n/clear-tree tree)
  (return (len tree.nodes)))

(defn.py add-node-count
  "Test helper: clears a material tree, adds one node and returns the count."
  {:added "4.1"}
  [obj-name]
  (:= tree (n/ensure-material-tree obj-name nil))
  (n/clear-tree tree)
  (n/node tree "Noise" "ShaderNodeTexNoise" {})
  (return (len tree.nodes)))

(defn.py link-count
  "Test helper: clears a material tree, adds two nodes, links them and
   returns the link count."
  {:added "4.1"}
  [obj-name]
  (:= tree (n/ensure-material-tree obj-name nil))
  (n/clear-tree tree)
  (n/node tree "Noise" "ShaderNodeTexNoise" {})
  (n/node tree "Output" "ShaderNodeOutputMaterial" {})
  (n/link tree "Noise" "Fac" "Output" "Surface")
  (return (len tree.links)))

(defn.py build-pipeline-count
  "Test helper: builds the example material pipeline and returns the
   number of nodes."
  {:added "4.1"}
  [obj-name]
  (:= tree (n/build-pipeline {:type "material" :obj obj-name :mat "PipelineMat"}
                             [["clear"]
                              ["node" "Noise" "ShaderNodeTexNoise" {:scale 5.0}]
                              ["node" "Diffuse" "ShaderNodeBsdfDiffuse" {}]
                              ["node" "Output" "ShaderNodeOutputMaterial" {}]
                              ["link" "Noise" "Fac" "Diffuse" "Color"]
                              ["link" "Diffuse" "BSDF" "Output" "Surface"]]))
  (return (len tree.nodes)))

(defn.py geometry-tree-name
  "Test helper: creates a geometry tree and returns its name."
  {:added "4.1"}
  [obj-name]
  (:= tree (n/ensure-geometry-tree obj-name nil))
  (return tree.name))

(defn.py compositor-tree-name
  "Test helper: enables compositor nodes and returns the tree name."
  {:added "4.1"}
  []
  (:= tree (n/ensure-compositor-tree))
  (return tree.name))
