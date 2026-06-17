(ns glsl.effects.checkerboard
  (:require [hara.lang :as l]))

(l/script :glsl
  glsl.effects.checkerboard
  {:runtime :oneshot})

(defrun.gl checkerboard
  (do
    (var :uniform :vec2 u_resolution)
    (fn ^{:- [:void]} main []
      (do
        (var :float cx (floor (/ (* 8.0 gl_FragCoord.x) u_resolution.x)))
        (var :float cy (floor (/ (* 8.0 gl_FragCoord.y) u_resolution.y)))
        (var :float c (fract (* 0.5 (+ cx cy))))
        (var :float v (step 0.5 c))
        (:= gl_FragColor (:vec4 v v v 1.0))))))
