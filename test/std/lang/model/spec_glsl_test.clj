(ns std.lang.model.spec-glsl-test
  (:require [std.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

^{:refer std.lang.model.spec-glsl/CANARY :adopt true :added "4.0"}
(fact "top-level definition for shaders"

  (l/emit-as
   :glsl '[(defrun shader-vs
             (var :attribute :vec3 pos)
             (var :uniform :mat4 u_ModelView)
             (var :uniform :mat4 u_Persp)
             (fn ^{:- [:void]}
               main []
               (:= gl_Position (* u_persp u_ModelView (:vec4 pos 1.0)))))])
  => (prose/|
      "attribute vec3 pos;"
      "uniform mat4 u_ModelView;"
      "uniform mat4 u_Persp;"
      "void main(){"
      "  gl_Position = (u_persp * u_ModelView * vec4(pos,1.0));"
      "}"))

(fact "supports sampler declarations and constructors"
  (l/emit-as
   :glsl '[(var :uniform :sampler2D u_Texture)
           (:= color (:vec4 1.0 0.5 0.0 1.0))])
  => (prose/|
      "uniform sampler2D u_Texture"
      ""
      "color = vec4(1.0,0.5,0.0,1.0)"))
