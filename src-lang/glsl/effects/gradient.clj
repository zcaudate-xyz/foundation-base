(ns glsl.effects.gradient
  (:require [hara.lang :as l]))

(l/script :glsl
  glsl.effects.gradient
  {:runtime :oneshot})

(defrun.gl gradient
  (do
    (var :uniform :vec2 u_resolution)
    (fn ^{:- [:void]} main []
      (do
        (var :float t (/ gl_FragCoord.x u_resolution.x))
        (:= gl_FragColor (:vec4 t (- 1.0 t) 0.2 1.0))))))
