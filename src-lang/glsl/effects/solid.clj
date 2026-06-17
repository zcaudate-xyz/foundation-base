(ns glsl.effects.solid
  (:require [hara.lang :as l]))

(l/script :glsl
  glsl.effects.solid
  {:runtime :oneshot})

(defrun.gl solid
  (fn ^{:- [:void]} main []
    (:= gl_FragColor (:vec4 0.2 0.6 0.9 1.0))))
