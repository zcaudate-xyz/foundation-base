(ns hara.runtime.basic.impl.process-glsl-test
  (:require [hara.lang :as l]
            [hara.runtime.basic.impl.process-glsl :as glsl]
            [std.lib.context.space :as space]
            [std.lib.env :as env]
            [std.string.prose :as prose])
  (:use code.test))

(l/script :glsl
  hara.runtime.basic.impl.process-glsl-test
  {:runtime :oneshot})

^{:refer hara.runtime.basic.impl.process-glsl/transform-form-run :added "4.0"}
(fact "prepends a #version directive to emitted shader source"
  (l/emit-as
   :glsl '[(:- "#version 460")
           (do (var :uniform :vec3 pos)
               (fn ^{:- [:void]} main []
                 (:= gl_Position (:vec4 pos 1.0))))])
  => (prose/|
      "#version 460"
      ""
      "uniform vec3 pos;"
      "void main(){"
      "  gl_Position = vec4(pos,1.0);"
      "}"))

^{:refer hara.runtime.basic.impl.process-glsl/glsl-oneshot :added "4.0"}
(fact "starts the glsl run runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/glsl]))

(fact:global
 {:skip (not (env/program-exists? "gcc"))})

^{:refer hara.runtime.basic.impl.process-glsl/!.gl :added "4.0"}
(fact "runs a simple fragment shader through the runtime"
  (do (defrun.gl test-shader
        (fn ^{:- [:void]} main []
          (:= gl_FragColor (:vec4 0.2 0.5 0.8 1.0))))
      (string? (!.gl test-shader)))
  => true)
