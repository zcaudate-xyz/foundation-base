(ns godot.examples.gaussian-splat
  "Gaussian splat rendering example using the xtalk Godot/GDScript DSL.

   Evaluating `build-gaussian-splat-demo` from a REPL with the :default runtime
   active will generate a `splat.gdshader` file and a `gaussian_splat.tscn`
   scene file at the paths you provide."
  (:require [hara.lang :as l]))

(l/script :gdscript)

(defn.gd save-splat-shader
  "Writes the Gaussian splat billboard shader to filepath. Returns filepath."
  {:added "4.1"}
  [filepath]
  (var code
    (str
      "shader_type spatial;\n"
      "render_mode blend_mix, unshaded, cull_disabled, depth_draw_always;\n"
      "\n"
      "varying vec2 v_uv;\n"
      "varying vec4 v_color;\n"
      "\n"
      "void vertex() {\n"
      "    v_uv = UV * 2.0 - 1.0;\n"
      "    v_color = COLOR;\n"
      "\n"
      "    vec2 scale = INSTANCE_CUSTOM.xy;\n"
      "    float rot = INSTANCE_CUSTOM.z;\n"
      "\n"
      "    float c = cos(rot);\n"
      "    float s = sin(rot);\n"
      "    mat2 rot_mat = mat2(vec2(c, -s), vec2(s, c));\n"
      "\n"
      "    vec2 local_pos = rot_mat * (v_uv * scale);\n"
      "\n"
      "    vec3 camera_right = normalize(INV_VIEW_MATRIX[0].xyz);\n"
      "    vec3 camera_up = normalize(INV_VIEW_MATRIX[1].xyz);\n"
      "\n"
      "    VERTEX += camera_right * local_pos.x + camera_up * local_pos.y;\n"
      "}\n"
      "\n"
      "void fragment() {\n"
      "    float d = dot(v_uv, v_uv);\n"
      "    float alpha = exp(-d * 2.0) * v_color.a;\n"
      "    ALBEDO = v_color.rgb;\n"
      "    ALPHA = alpha;\n"
      "}\n"))
  (var file (. FileAccess (open filepath (. FileAccess WRITE))))
  (. file (store_string code))
  (. file (close))
  (return filepath))

(defn.gd build-gaussian-splat-demo
  "Builds a Gaussian splat demo scene and saves it to scene-path.
   The shader is written to shader-path. Returns scene-path."
  {:added "4.1"}
  [scene-path shader-path]
  (save-splat-shader shader-path)

  ;; Clear any existing children on the eval node.
  (for [child :in (. self (get_children))]
    (. self (remove_child child))
    (. child (queue_free)))

  (var splat-count 2000)
  (var spawn-radius 5.0)

  ;; Each splat instance is a 1x1 quad expanded by the vertex shader.
  (var quad (. QuadMesh (new)))
  (:= (. quad size) (Vector2 1.0 1.0))
  
  ;; MultiMesh stores per-instance transform, color, and custom data.
  (var mm (. MultiMesh (new)))
  (:= (. mm transform_format) (. MultiMesh TRANSFORM_3D))
  (:= (. mm color_format) (. MultiMesh COLOR_FLOAT))
  (:= (. mm custom_data_format) (. MultiMesh CUSTOM_DATA_FLOAT))
  (:= (. mm mesh) quad)
  (:= (. mm instance_count) splat-count)

  (for [i :in (range splat-count)]
    (var pos (Vector3
               (randf_range (- spawn-radius) spawn-radius)
               (randf_range (- spawn-radius) spawn-radius)
               (randf_range (- spawn-radius) spawn-radius)))
    (var color (Color
                 (randf)
                 (randf)
                 (randf)
                 (randf_range 0.2 0.7)))
    (var scale (randf_range 0.05 0.4))
    (var rot (randf_range 0.0 6.28318530718))

    (var t (Transform3D))
    (:= (. t origin) pos)

    (. mm (set_instance_transform i t))
    (. mm (set_instance_color i color))
    ;; xy = scale, z = rotation, w = unused
    (. mm (set_instance_custom_data i (Color scale scale rot 0.0))))

  ;; Shader material using the generated shader file.
  (var material (. ShaderMaterial (new)))
  (:= (. material shader) (. ResourceLoader (load shader-path)))

  ;; Renderer node.
  (var renderer (. MultiMeshInstance3D (new)))
  (:= (. renderer name) "SplatRenderer")
  (:= (. renderer multimesh) mm)
  (:= (. renderer material_override) material)
  (. self (add_child renderer))

  ;; Light.
  (var light (. DirectionalLight3D (new)))
  (:= (. light name) "DirectionalLight3D")
  (:= (. light position) (Vector3 5.0 5.0 5.0))
  (:= (. light light_energy) 2.0)
  (. self (add_child light))

  ;; Camera.
  (var cam (. Camera3D (new)))
  (:= (. cam name) "Camera3D")
  (var cam-pos (Vector3 0.0 5.0 10.0))
  (:= (. cam position) cam-pos)
  (. cam (look_at_from_position cam-pos (Vector3 0.0 0.0 0.0)))
  (. self (add_child cam))
  
  ;; Save the assembled scene to disk.
  (var packer (. PackedScene (new)))
  (. packer (pack self))
  (. ResourceSaver (save packer scene-path))
  (return scene-path))

(comment
  ;; Eval these forms in a REPL with the :godot runtime active.
  (!.gd (godot.examples.gaussian-splat/save-splat-shader "/tmp/splat.gdshader"))
  (!.gd (godot.examples.gaussian-splat/build-gaussian-splat-demo
         "/tmp/gaussian_splat.tscn"
         "/tmp/splat.gdshader")))
