^{:seedgen/skip true}
(ns glsl.playground-test
  "Browser playground test for GLSL effects.

   Uses the :runtime :playground js runtime to serve a browser page, then
   creates three GLSL effect tabs and renders each effect to its own canvas."
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.js-playground :as js-playground]
            [hara.runtime.chromedriver :as chromedriver]
            [std.json :as json]
            [std.lib.component :as component]
            [glsl.playground-shader-sources :as sources]))

(l/script- :js
  {:runtime :playground
   :config {:port 0}
   :test-mode true
   :require [[xt.lang.spec-base :as xt]
             [hara.runtime.js-playground.client :as client]]
   :emit {:lang/jsx false}})

(def +gradient-src+ (json/write (sources/gradient-src)))
(def +checkerboard-src+ (json/write (sources/checkerboard-src)))
(def +ripple-src+ (json/write (sources/ripple-src)))

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

(defn.js create-effect-canvas
  "creates a canvas, compiles the supplied fragment shader, and draws once"
  {:added "4.1"}
  [frag-src]
  (var canvas (document.createElement "canvas"))
  (:= (. canvas width) 256)
  (:= (. canvas height) 256)
  (:= (. canvas style width) "100%")
  (:= (. canvas style height) "100%")
  (var gl (. canvas (getContext "webgl")))
  (when (== null gl)
    (throw (new Error "WebGL not supported")))
  (var vs "attribute vec2 a_position; void main() { gl_Position = vec4(a_position, 0.0, 1.0); }")
  (var fs (+ "precision mediump float;\n"
             (. frag-src (replace (new RegExp "^#version[^\\n]*\\n?" "gm") ""))))
  (var program (. gl (createProgram)))
  (. gl (attachShader program (-/compile-shader gl (. gl VERTEX_SHADER) vs)))
  (. gl (attachShader program (-/compile-shader gl (. gl FRAGMENT_SHADER) fs)))
  (. gl (linkProgram program))
  (when (not (. gl (getProgramParameter program (. gl LINK_STATUS))))
    (throw (new Error (. gl (getProgramInfoLog program)))))
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
  (. gl (viewport 0 0 256 256))
  (. gl (clearColor 0 0 0 1))
  (. gl (clear (. gl COLOR_BUFFER_BIT)))
  (when (!= null u-res) (. gl (uniform2f u-res 256 256)))
  (when (!= null u-time) (. gl (uniform1f u-time 0)))
  (. gl (drawArrays (. gl TRIANGLES) 0 3))
  (return {"canvas" canvas
           "gl" gl
           "program" program
           "u-resolution" u-res
           "u-time" u-time}))

(defn.js show-effect
  "creates a new playground tab and renders the effect into it"
  {:added "4.1"}
  [tab-id frag-src]
  (window.PLAYGROUND.createTab tab-id tab-id)
  (var result (-/create-effect-canvas frag-src))
  (var canvas (. result ["canvas"]))
  (when (== undefined (typeof (!:G __glsl_canvases__)))
    (:= (!:G __glsl_canvases__) {}))
  (:= (. (!:G __glsl_canvases__) [tab-id]) canvas)
  (window.PLAYGROUND.setTabContent tab-id canvas)
  (return tab-id))

(defn.js read-center-pixel
  "reads the RGBA pixel at the center of the rendered effect canvas"
  {:added "4.1"}
  [tab-id]
  (var canvas (. (!:G __glsl_canvases__) [tab-id]))
  (var gl (. canvas (getContext "webgl")))
  (var pixels (new Uint8Array 4))
  (. gl (readPixels 128 128 1 1 (. gl RGBA) (. gl UNSIGNED_BYTE) pixels))
  (return [(. pixels [0])
           (. pixels [1])
           (. pixels [2])
           (. pixels [3])]))

(defn- wait-for-channel
  "waits up to 5s for the playground websocket channel to be connected"
  [rt]
  (let [channel (:channel rt)]
    (loop [i 0]
      (when (and (< i 50) (not @channel))
        (Thread/sleep 100)
        (recur (inc i))))))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (def +url+ (js-playground/play-url (l/rt :js)))
          (def +browser+ (chromedriver/browser {}))
          (chromedriver/goto +url+ 5000 +browser+)
          (wait-for-channel (l/rt :js))]
  :teardown [(l/rt:stop :js)
             (component/stop +browser+)]})

^{:refer glsl.playground-test/CANARY :adopt true :added "4.1"}
(fact "basic eval reaches the playground-served browser"

  (!.js (+ 1 2 3))
  => 6)

^{:refer glsl.playground-test/three-effects-in-tabs :added "4.1"}
(fact "three glsl effects are rendered in separate playground tabs"

    (!.js
      (do (-/show-effect "gradient" (@! +gradient-src+))
          (-/show-effect "checkerboard" (@! +checkerboard-src+))
          (-/show-effect "ripple" (@! +ripple-src+))
          (window.PLAYGROUND.switchTab "gradient")
          {"active" (window.PLAYGROUND.getActiveTab)}))
    => (contains {"active" "gradient"})

    (!.js (-/read-center-pixel "gradient"))
    => (fn [pixels]
         (and (vector? pixels)
              (> (nth pixels 0) 100)
              (< (nth pixels 1) 200)
              (= (nth pixels 3) 255)))

    (!.js (-/read-center-pixel "checkerboard"))
    => (fn [pixels]
         (and (vector? pixels)
              (let [avg (/ (+ (nth pixels 0)
                              (nth pixels 1)
                              (nth pixels 2))
                           3)]
                (or (< avg 20) (> avg 235)))
              (= (nth pixels 3) 255)))

    (!.js (-/read-center-pixel "ripple"))
    => (fn [pixels]
         (and (vector? pixels)
              (> (nth pixels 2) 150)
              (= (nth pixels 3) 255))))
