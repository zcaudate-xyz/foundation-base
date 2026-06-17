(ns hara.runtime.basic.impl.process-glsl-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :glsl
  hara.runtime.basic.impl.process-glsl-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-glsl/transform-form-verify :added "4.0"}
(fact "starts the glsl verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/glsl]))

(fact:global
 {:skip (not (env/program-exists? "glslangValidator"))})

^{:refer hara.runtime.basic.impl.process-glsl/!.gl :added "4.0"}
(fact "validates a simple vertex shader through the runtime"
  (do (defrun.gl test-shader
        (var :uniform :vec3 pos)
        (fn ^{:- [:void]} main []
          (:= gl_Position (:vec4 pos 1.0))))
      (string? (!.gl test-shader)))
  => true)
