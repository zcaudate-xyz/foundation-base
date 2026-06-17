(ns glsl.effects.ripple
  (:require [hara.lang :as l]))

(l/script :glsl
  glsl.effects.ripple
  {:runtime :oneshot})

(defrun.gl ripple
  (do
    (var :uniform :vec2 u_resolution)
    (var :uniform :float u_time)
    (fn ^{:- [:void]} main []
      (do
        (var :float cx (- gl_FragCoord.x (* 0.5 u_resolution.x)))
        (var :float cy (- gl_FragCoord.y (* 0.5 u_resolution.y)))
        (var :float d (sqrt (+ (* cx cx) (* cy cy))))
        (var :float v (sin (+ (* d 0.2) u_time)))
        (:= gl_FragColor
            (:vec4 (+ 0.5 (* 0.5 v))
                   (+ 0.5 (* 0.5 v))
                   1.0
                   1.0))))))
