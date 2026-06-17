(ns gdscript.tutorial.example-3d
  (:require [hara.lang :as l]))

(l/script :gdscript)

(defn.gd clear-scene
  "Removes all children from the eval node and returns the remaining child count."
  {:added "4.1"}
  []
  (for [child :in (. self (get_children))]
    (. self (remove_child child))
    (. child (queue_free)))
  (return (. (. self (get_children)) (size))))

(defn.gd add-cube
  "Adds a cube mesh of the given size at location and returns the node name."
  {:added "4.1"}
  [size location]
  (var mesh (. MeshInstance3D (new)))
  (:= (. mesh mesh) (. BoxMesh (new)))
  (:= (. mesh position) (Vector3 (. location [0]) (. location [1]) (. location [2])))
  (:= (. mesh scale) (Vector3 size size size))
  (:= (. mesh name) "Cube")
  (. self (add_child mesh))
  (return (str (. mesh name))))

(defn.gd add-sphere
  "Adds a UV sphere mesh of the given radius at location and returns the node name."
  {:added "4.1"}
  [radius location]
  (var mesh (. MeshInstance3D (new)))
  (var sphere (. SphereMesh (new)))
  (:= (. sphere radius) radius)
  (:= (. sphere height) (* 2 radius))
  (:= (. mesh mesh) sphere)
  (:= (. mesh position) (Vector3 (. location [0]) (. location [1]) (. location [2])))
  (:= (. mesh name) "Sphere")
  (. self (add_child mesh))
  (return (str (. mesh name))))

(defn.gd add-light
  "Adds a 3D light of the given type at location with the specified energy and
   returns the node name. Type should be a string such as 'DirectionalLight3D,
   'OmniLight3D or 'SpotLight3D."
  {:added "4.1"}
  [type location energy]
  (var light (. ClassDB (instantiate type)))
  (:= (. light position) (Vector3 (. location [0]) (. location [1]) (. location [2])))
  (:= (. light light_energy) energy)
  (:= (. light name) type)
  (. self (add_child light))
  (return type))

(defn.gd add-camera
  "Adds a Camera3D at location looking at the origin and returns the node name."
  {:added "4.1"}
  [location]
  (var cam (. Camera3D (new)))
  (var pos (Vector3 (. location [0]) (. location [1]) (. location [2])))
  (:= (. cam position) pos)
  (. cam (look_at_from_position pos (Vector3 0 0 0)))
  (:= (. cam name) "Camera")
  (. self (add_child cam))
  (return (str (. cam name))))

(defn.gd node-count
  "Returns the number of children attached to the eval node."
  {:added "4.1"}
  []
  (return (. (. self (get_children)) (size))))

(defn.gd save-scene-to
  "Packs the current eval node into a PackedScene and saves it to filepath.
   Returns the filepath."
  {:added "4.1"}
  [filepath]
  (var packer (. PackedScene (new)))
  (. packer (pack self))
  (. ResourceSaver (save packer filepath))
  (return filepath))

(defn.gd add-material
  "Creates a StandardMaterial3D with the given [r g b a] color and assigns it
   to the mesh named node-name. Returns the material name."
  {:added "4.1"}
  [node-name color]
  (var mat (. StandardMaterial3D (new)))
  (:= (. mat albedo_color) (Color (. color [0]) (. color [1]) (. color [2]) (. color [3])))
  (var node (. self (get_node node-name)))
  (:= (. (. node mesh) material) mat)
  (return (str (. mat resource_name))))

(defn.gd build-procedural-mesh
  "Builds a simple custom mesh (two triangles) with SurfaceTool and returns the
   number of surfaces."
  {:added "4.1"}
  []
  (var st (. SurfaceTool (new)))
  (. st (begin (. Mesh PRIMITIVE_TRIANGLES)))
  (. st (add_vertex (Vector3 0 0 0)))
  (. st (add_vertex (Vector3 1 0 0)))
  (. st (add_vertex (Vector3 0 1 0)))
  (. st (add_vertex (Vector3 0 1 0)))
  (. st (add_vertex (Vector3 1 0 0)))
  (. st (add_vertex (Vector3 1 1 0)))
  (. st (generate_normals))
  (var mesh (. st (commit)))
  (var inst (. MeshInstance3D (new)))
  (:= (. inst mesh) mesh)
  (:= (. inst name) "Procedural")
  (. self (add_child inst))
  (return (. mesh (get_surface_count))))
