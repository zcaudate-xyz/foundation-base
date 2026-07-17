(ns hara.runtime.basic.impl.process-glsl-test
  (:require [hara.lang :as l]
            [hara.runtime.basic.impl.process-glsl :as glsl]
            [std.lib.context.space :as space]
            [std.lib.env :as env]
            [std.string.prose :as prose]
            [hara.common.util :as ut])
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

(defn- egl-available? []
  (try
    (let [tmp (java.io.File/createTempFile "egl-check" ".c")]
      (spit tmp "#include <EGL/egl.h>\nint main(){return 0;}")
      (let [pb (ProcessBuilder. (into-array String ["gcc" "-x" "c" (.getAbsolutePath tmp) "-o" "/dev/null"]))
            process (.start pb)]
        (.waitFor process)
        (= 0 (.exitValue process))))
    (catch Throwable _ false)))

^{:refer hara.runtime.basic.impl.process-glsl/transform-form-verify :added "4.0"}
(fact "prepends a #version directive and normalizes the body"
  (glsl/transform-form-verify '(do (var :uniform :vec3 pos)
                                   (fn main [] (:= gl_Position (:vec4 pos 1.0))))
                              {})
  => '[(:- "#version 460")
       (do (var :uniform :vec3 pos)
           (fn main [] (:= gl_Position (:vec4 pos 1.0))))]

  (glsl/transform-form-verify nil {})
  => '[(:- "#version 460") (do)])

^{:refer hara.runtime.basic.impl.process-glsl/rt-glsl:create :added "4.0"}
(fact "creates a glsl run runtime with default program and exec"
  (let [rt (glsl/rt-glsl:create {:lang :glsl :runtime :oneshot})]
    (and (record? rt)
         (= #{:id :lang :runtime :program :exec :process} (set (keys rt)))
         (= :glsl (:lang rt))
         (= :oneshot (:runtime rt))
         (= :gcc-egl-frag (:program rt))
         (= ["gcc"] (:exec rt))))
  => true)

(fact:global
 {:skip (not (and (env/program-exists? "gcc")
                  (egl-available?)))})

^{:refer hara.runtime.basic.impl.process-glsl/!.gl :added "4.0"}
(fact "runs a simple fragment shader through the runtime"
  (do (defrun.gl test-shader
        (fn ^{:- [:void]} main []
          (:= gl_FragColor (:vec4 0.2 0.5 0.8 1.0))))
      (let [out (str (!.gl test-shader))]
        (and (string? out)
             (boolean (re-find #"^pixel: \d+\.\d+ \d+\.\d+ \d+\.\d+ \d+\.\d+$" out)))))
  => true)


^{:refer hara.runtime.basic.impl.process-glsl/glsl-sh-exec :added "4.0"}
(fact "compiles and runs a simple C program"
  (glsl/glsl-sh-exec ["gcc"]
                     "#include <stdio.h>\nint main(){printf(\"hello\");return 0;}"
                     {:extension "c"})
  => "hello")

^{:refer hara.runtime.basic.impl.process-glsl/glsl-sh-exec :added "4.0"
  :id test-glsl-sh-exec-compile-failure}
(fact "returns stderr output on compile failure when stderr is enabled"
  (string? (glsl/glsl-sh-exec ["gcc"]
                              "int main(){ UNKNOWN }"
                              {:extension "c" :stderr true}))
  => true)

^{:refer hara.runtime.basic.impl.process-glsl/raw-eval-glsl :added "4.0"}
(fact "compiles and runs a generated C body through the runtime"
  (let [rt (glsl/rt-glsl:create {:lang :glsl :runtime :oneshot})]
    (glsl/raw-eval-glsl rt
                        "#include <stdio.h>\nint main(){printf(\"raw\");return 0;}"))
  => "raw")

^{:refer hara.runtime.basic.impl.process-glsl/invoke-ptr-glsl :added "4.0"}
(fact "invokes a glsl pointer through the run harness"
  (let [rt (glsl/rt-glsl:create {:lang :glsl :runtime :oneshot})
        ptr (ut/lang-pointer :glsl
                             {:form '(fn ^{:- [:void]} main []
                                        (:= gl_FragColor (:vec4 0.0 1.0 0.0 1.0)))})]
    (glsl/invoke-ptr-glsl rt ptr []))
  => "pixel: 0.000 1.000 0.000 1.000")
