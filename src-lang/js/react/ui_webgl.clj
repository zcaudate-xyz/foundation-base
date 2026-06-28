^{:no-test true}
(ns js.react.ui-webgl
  "Reusable browser-side WebGL helpers for rendering fragment shaders and
   reading back pixels. Designed to be used from `js.react` UI code or from
   playground tests."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]]})

(defn.js compile-shader
  "compiles a single WebGL shader and throws on failure"
  {:added "4.1"}
  [gl type source]
  (var s (. gl (createShader type)))
  (. gl (shaderSource s source))
  (. gl (compileShader s))
  (when (not (. gl (getShaderParameter s (. gl COMPILE_STATUS))))
    (throw (new Error (. gl (getShaderInfoLog s)))))
  (return s))

(defn.js create-program
  "compiles vertex and fragment shaders and links a program"
  {:added "4.1"}
  [gl vs-source fs-source]
  (var program (. gl (createProgram)))
  (. gl (attachShader program (-/compile-shader gl (. gl VERTEX_SHADER) vs-source)))
  (. gl (attachShader program (-/compile-shader gl (. gl FRAGMENT_SHADER) fs-source)))
  (. gl (linkProgram program))
  (when (not (. gl (getProgramParameter program (. gl LINK_STATUS))))
    (throw (new Error (. gl (getProgramInfoLog program)))))
  (return program))

(defn.js make-canvas
  "creates a canvas element with the given dimensions"
  {:added "4.1"}
  [width height]
  (var canvas (document.createElement "canvas"))
  (:= (. canvas width) width)
  (:= (. canvas height) height)
  (:= (. canvas style width) "100%")
  (:= (. canvas style height) "100%")
  (return canvas))

(defn.js render-fragment-shader
  "renders a fragment shader to a canvas.

   `opts` may contain:
     :width        canvas width (default 256)
     :height       canvas height (default 256)
     :canvas       an existing canvas to render into
     :precision    float precision prefix (default \"mediump\")
     :stripVersion when not false, strips #version directives (default true)
     :time         value for the u_time uniform (default 0)"
  {:added "4.1"}
  [frag-src opts]
  (var width (or (and opts (. opts ["width"])) 256))
  (var height (or (and opts (. opts ["height"])) 256))
  (var canvas (or (and opts (. opts ["canvas"]))
                  (-/make-canvas width height)))
  (var gl (. canvas (getContext "webgl")))
  (when (== null gl)
    (throw (new Error "WebGL not supported")))
  (var vs "attribute vec2 a_position; void main() { gl_Position = vec4(a_position, 0.0, 1.0); }")
  (var fs-source frag-src)
  (when (not= false (. opts ["stripVersion"]))
    (:= fs-source (. frag-src (replace (new RegExp "^#version[^\\n]*\\n?" "gm") ""))))
  (var fs (+ "precision " (or (and opts (. opts ["precision"])) "mediump") " float;\n"
             fs-source))
  (var program (-/create-program gl vs fs))
  (. gl (useProgram program))
  (var buf (. gl (createBuffer)))
  (. gl (bindBuffer (. gl ARRAY_BUFFER) buf))
  (. gl (bufferData (. gl ARRAY_BUFFER)
                    (new Float32Array [-1 -1 3 -1 -1 3])
                    (. gl STATIC_DRAW)))
  (var loc (. gl (getAttribLocation program "a_position")))
  (. gl (enableVertexAttribArray loc))
  (. gl (vertexAttribPointer loc 2 (. gl FLOAT) false 0 0))
  (var u-res (. gl (getUniformLocation program "u_resolution")))
  (var u-time (. gl (getUniformLocation program "u_time")))
  (. gl (viewport 0 0 width height))
  (. gl (clearColor 0 0 0 1))
  (. gl (clear (. gl COLOR_BUFFER_BIT)))
  (when (not (== null u-res)) (. gl (uniform2f u-res width height)))
  (when (not (== null u-time)) (. gl (uniform1f u-time (or (and opts (. opts ["time"])) 0))))
  (. gl (drawArrays (. gl TRIANGLES) 0 3))
  (return {"canvas" canvas
           "gl" gl
           "program" program
           "u-resolution" u-res
           "u-time" u-time}))

(defn.js read-pixel
  "reads the RGBA pixel at (x,y) from the canvas"
  {:added "4.1"}
  [canvas x y]
  (var gl (. canvas (getContext "webgl")))
  (var pixels (new Uint8Array 4))
  (. gl (readPixels x y 1 1 (. gl RGBA) (. gl UNSIGNED_BYTE) pixels))
  (return [(. pixels [0])
           (. pixels [1])
           (. pixels [2])
           (. pixels [3])]))

(defn.js read-center-pixel
  "reads the RGBA pixel at the center of the canvas"
  {:added "4.1"}
  [canvas]
  (return (-/read-pixel canvas
                        (Math.floor (/ (. canvas width) 2))
                        (Math.floor (/ (. canvas height) 2)))))
