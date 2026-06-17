(ns python.blender.nodes
  (:require [hara.lang :as l]))

(l/script :python {:runtime :blender})

(defn.py ensure-material-tree
  "Ensures obj-name has a material with use_nodes enabled.
   If mat-name is nil a name is derived from the object. Returns the
   material's node tree."
  {:added "4.1"}
  [obj-name mat-name]
  (:= obj (. bpy.data.objects [obj-name]))
  (:= name (or mat-name (+ obj-name "-Material")))
  (:= mat (bpy.data.materials.new name))
  (:= mat.use_nodes true)
  (if (and obj obj.data)
    (if (== 0 (len obj.data.materials))
      (. obj.data.materials (append mat))
      (:= (. obj.data.materials [0]) mat)))
  (return mat.node_tree))

(defn.py ensure-geometry-tree
  "Creates a new GeometryNodeTree, attaches it to obj-name via a
   Geometry Nodes modifier and returns the tree."
  {:added "4.1"}
  [obj-name group-name]
  (:= obj (. bpy.data.objects [obj-name]))
  (:= name (or group-name (+ obj-name "-GeometryNodes")))
  (:= tree (bpy.data.node_groups.new name "GeometryNodeTree"))
  (:= modifier (. obj modifiers (new name "NODES")))
  (:= modifier.node_group tree)
  (return tree))

(defn.py ensure-compositor-tree
  "Enables scene compositor nodes and returns the scene node tree."
  {:added "4.1"}
  []
  (:= bpy.context.scene.use_nodes true)
  (return bpy.context.scene.node_tree))

(defn.py clear-tree
  "Removes all nodes from tree and returns tree."
  {:added "4.1"}
  [tree]
  (for [node :in (list tree.nodes)]
    (. tree nodes (remove node)))
  (return tree))

(defn.py node
  "Adds a node of bl-type to tree, names it id, applies props and
   returns tree so calls can be threaded with ->."
  {:added "4.1"}
  [tree id bl-type props]
  (:= n (. tree nodes (new bl-type)))
  (:= n.name id)
  (for [key :in props]
    (:= value (. props [key]))
    (cond (== key "location") (:= (. n location) value)
          (== key "name")    (:= (. n name) value)
          (== key "label")   (:= (. n label) value)
          (hasattr n key)    (setattr n key value)
          :else
          (do (try
                (:= (. (. n inputs [key]) default_value) value)
                (catch e
                  (try
                    (:= (. (. n outputs [key]) default_value) value)
                    (catch e (pass)))))))))

(defn.py link
  "Links from-node.from-socket to to-node.to-socket and returns tree."
  {:added "4.1"}
  [tree from-id from-socket to-id to-socket]
  (:= from-node (. tree nodes [from-id]))
  (:= to-node   (. tree nodes [to-id]))
  (. tree links (new (. from-node outputs [from-socket])
                     (. to-node inputs [to-socket])))
  (return tree))

(defn.py build-pipeline
  "Builds a node tree for target and applies pipeline steps.

   target is a map with :type in #{\"material\" \"geometry\" \"compositor\"}
   plus the relevant keys:
     {:type \"material\"  :obj obj-name :mat mat-name?}
     {:type \"geometry\"  :obj obj-name :group group-name?}
     {:type \"compositor\"}

   pipeline is a list of steps:
     [\"clear\"]
     [\"node\" id bl-type props?]
     [\"link\" from-id from-socket to-id to-socket]

   Returns the node tree."
  {:added "4.1"}
  [target pipeline]
  (:= ttype (. target ["type"]))
  (cond (== ttype "material")
        (:= tree (ensure-material-tree (. target ["obj"])
                                       (. target (get "mat"))))

        (== ttype "geometry")
        (:= tree (ensure-geometry-tree (. target ["obj"])
                                       (. target (get "group"))))

        (== ttype "compositor")
        (:= tree (ensure-compositor-tree))

        :else
        (throw (Exception (+ "Unknown pipeline target type: " ttype))))
  (for [step :in pipeline]
    (:= op (. step [0]))
    (cond (== op "node")
          (node tree (. step [1]) (. step [2]) (or (. step [3]) {}))

          (== op "link")
          (link tree (. step [1]) (. step [2]) (. step [3]) (. step [4]))

          (== op "clear")
          (clear-tree tree)))
  (return tree))

(comment
  ;; Example: build a simple noise material from data.
  (clojure.core/deref
   (!.py (do
           (python.blender.nodes/build-pipeline
            {:type "material" :obj "Cube" :mat "NoiseMat"}
            [["clear"]
             ["node" "Noise" "ShaderNodeTexNoise" {:scale 5.0 :location [-300 0]}]
             ["node" "Diffuse" "ShaderNodeBsdfDiffuse" {:location [0 0]}]
             ["node" "Output" "ShaderNodeOutputMaterial" {:location [300 0]}]
             ["link" "Noise" "Fac" "Diffuse" "Color"]
             ["link" "Diffuse" "BSDF" "Output" "Surface"]])
           (return "ok")))))
