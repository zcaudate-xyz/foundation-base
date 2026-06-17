(ns glsl.effects.plasma
  (:require [hara.lang :as l]))

(l/script :glsl
  glsl.effects.plasma
  {:runtime :oneshot})

(defrun.gl plasma
  (do
    (var :uniform :vec2 u_resolution)
    (var :uniform :float u_time)
    (fn ^{:- [:void]} main []
      (do
        (var :float x (/ gl_FragCoord.x u_resolution.x))
        (var :float y (/ gl_FragCoord.y u_resolution.y))
        (var :float v
          (+ (sin (* x 10.0))
             (cos (* y 10.0))
             (sin (+ x y u_time))))
        (:= gl_FragColor
            (:vec4 (+ 0.5 (* 0.5 (sin v)))
                   (+ 0.5 (* 0.5 (cos v)))
                   (+ 0.5 (* 0.5 (sin (+ v 1.0))))
                   1.0))))))
