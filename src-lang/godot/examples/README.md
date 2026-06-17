# Godot Gaussian Splat Example

This example renders a cloud of colored Gaussian splats in Godot 4 using the
xtalk `hara.lang` GDScript DSL.

## File

- `gaussian_splat.clj` — Hara DSL source that compiles to GDScript

## What it does

`gaussian_splat.clj` defines two functions:

- `save-splat-shader` — writes a `splat.gdshader` file that turns each quad
  instance into a camera-facing Gaussian billboard.
- `build-gaussian-splat-demo` — generates 2,000 random splats as a `MultiMesh`,
  wires them to the shader, adds a camera and light, and saves the resulting
  scene as a `.tscn` file.

## How to run

From a Clojure REPL with the `:godot` runtime active:

```clojure
(!.gd (godot.examples.gaussian-splat/save-splat-shader "/tmp/splat.gdshader"))
(!.gd (godot.examples.gaussian-splat/build-gaussian-splat-demo
       "/tmp/gaussian_splat.tscn"
       "/tmp/splat.gdshader"))
```

Then open `/tmp/gaussian_splat.tscn` in Godot 4 and run the scene.

## Limitations

- No per-frame depth sorting, so overlapping transparent splats may draw
  out of order.
- The splat is a simple circular billboard, not a true oriented 3D Gaussian
  covariance projection.
- No `.ply` loader; splats are procedurally generated.

These limitations are intentional for a minimal DSL demonstration. A production
Gaussian Splat renderer would use a GDExtension with compute-shader sorting.
