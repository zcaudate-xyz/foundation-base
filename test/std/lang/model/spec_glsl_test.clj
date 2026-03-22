(ns std.lang.model.spec-glsl-test
  (:require [std.lang :as l]
            [std.string.prose])
  (:use code.test))

^{:refer std.lang.model.spec-glsl/CANARY :adopt true :added "4.0"}
(fact "top-level definition for shaders"
  ^:hidden
  
  (l/emit-as
   :glsl '[(defrun shader-vs
             (var :attribute :vec3 pos)
             (var :uniform :mat4 u_ModelView)
             (var :uniform :mat4 u_Persp)
             (fn ^{:- [:void]}
               main []
               (:= gl_Position (* u_persp u_ModelView (:vec4 pos 1.0)))))])
  => (std.string.prose/|
      "attribute vec3 pos;"
      "uniform mat4 u_ModelView;"
      "uniform mat4 u_Persp;"
      "void main(){"
      "  gl_Position = (u_persp * u_ModelView * vec4(pos,1.0));"
      "}"))
