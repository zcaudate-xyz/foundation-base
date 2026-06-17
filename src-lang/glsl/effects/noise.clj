(ns glsl.effects.noise
  (:require [hara.lang :as l]))

(l/script :glsl
  glsl.effects.noise
  {:runtime :oneshot})

(defrun.gl noise
  (fn ^{:- [:void]} main []
    (do
      (var :float v
        (fract (* (sin (+ (* gl_FragCoord.x 12.9898)
                          (* gl_FragCoord.y 78.233)))
                  43758.5453)))
      (:= gl_FragColor (:vec4 v v v 1.0)))))
