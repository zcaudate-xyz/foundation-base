(ns glsl.playground-test
  "Example/test showing how to use the js playground for GLSL effects.

   This does not require a real browser. It verifies that:
   - the playground can be configured with a 'glsl' tab,
   - a GLSL demo script can be emitted into the served root, and
   - the emitted script wires itself into the playground via PLAYGROUND.setTabContent."
  (:require [hara.runtime.js-playground :as playground]
            [std.lib.component :as component])
  (:use code.test))

^{:refer hara.runtime.js-playground/page-html :added "4.0"}
(fact "playground page can be configured with a glsl tab"

  (let [page (playground/page-html {:title "GLSL Playground"
                                    :tabs [{:id "stage" :label "Stage"}
                                           {:id "glsl" :label "GLSL"}]})]
    page => #"window\.PLAYGROUND_CONFIG"
    page => #"\"title\":\"GLSL Playground\""
    page => #"\"id\":\"glsl\""
    page => #"\"label\":\"GLSL\""))

^{:refer hara.runtime.js-playground/play-script :added "4.0"}
(fact "emits a GLSL demo script that renders to the glsl tab"

  (let [rt (component/start (playground/rt-js-playground:create {:lang :js
                                                                 :port 0
                                                                 :title "GLSL Playground"
                                                                 :tabs [{:id "stage" :label "Stage"}
                                                                        {:id "glsl" :label "GLSL"}]}))]
    (try
      (let [script (playground/play-script
                    rt
                    '[(defn.js setup-glsl []
                        (var canvas (document.createElement "canvas"))
                        (:= (. canvas width) 256)
                        (:= (. canvas height) 256)
                        (:= (. canvas style width) "100%")
                        (:= (. canvas style height) "100%")
                        (var gl (. canvas (getContext "webgl")))
                        (var vs "attribute vec2 position; void main() { gl_Position = vec4(position, 0.0, 1.0); }")
                        (var fs "precision mediump float; void main() { gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); }")
                        (var compile-shader
                             (fn [type source]
                               (var s (. gl (createShader type)))
                               (. gl (shaderSource s source))
                               (. gl (compileShader s))
                               (return s)))
                        (var program (. gl (createProgram)))
                        (. gl (attachShader program (compile-shader (. gl VERTEX_SHADER) vs)))
                        (. gl (attachShader program (compile-shader (. gl FRAGMENT_SHADER) fs)))
                        (. gl (linkProgram program))
                        (. gl (useProgram program))
                        (var buffer (. gl (createBuffer)))
                        (. gl (bindBuffer (. gl ARRAY_BUFFER) buffer))
                        (. gl (bufferData (. gl ARRAY_BUFFER)
                                          (new Float32Array [-1 -1 1 -1 -1 1 1 1])
                                          (. gl STATIC_DRAW)))
                        (var position (. gl (getAttribLocation program "position")))
                        (. gl (enableVertexAttribArray position))
                        (. gl (vertexAttribPointer position 2 (. gl FLOAT) false 0 0))
                        (. gl (drawArrays (. gl TRIANGLE_STRIP) 0 4))
                        (. PLAYGROUND (setTabContent "glsl" canvas)))]
                    true)]
        script => #"setTabContent"
        script => #"webgl"
        script => #"TRIANGLE_STRIP"
        script => #"gl_Position"
        script => #"gl_FragColor")
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.js-playground/play-page :added "4.0"}
(fact "a standalone glsl demo page can be served from the runtime root"

  (let [rt (component/start (playground/rt-js-playground:create {:lang :js
                                                                 :port 0
                                                                 :title "GLSL Playground"
                                                                 :tabs [{:id "stage" :label "Stage"}
                                                                        {:id "glsl" :label "GLSL"}]}))]
    (try
      (let [filename (playground/play-page
                      rt
                      {:name "glsl-demo"
                       :title "GLSL Demo"
                       :tabs [{:id "stage" :label "Stage"}
                              {:id "glsl" :label "GLSL"}]
                       :body [:div
                              [:h1 "GLSL Demo"]
                              [:p "Open the browser console and run:"]
                              [:pre "window.PLAYGROUND.switchTab('glsl');\nwindow.setupGlsl();"]]})
            path (playground/play-file rt filename)]
        path => #"glsl-demo-.*\.html"
        (slurp path) => #"GLSL Demo")
      (finally
        (component/stop rt)))))
