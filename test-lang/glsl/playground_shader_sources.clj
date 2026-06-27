(ns glsl.playground-shader-sources
  "Helper namespace for retrieving emitted GLSL source from the effect modules.

   It sets up a :glsl :oneshot runtime so that workspace/ptr-display-str can
   emit the shader source strings."
  (:require [hara.lang :as l]
            [hara.lang.workspace :as workspace]
            [glsl.effects.gradient]
            [glsl.effects.checkerboard]
            [glsl.effects.ripple]))

(l/script :glsl glsl.playground-shader-sources {:runtime :oneshot})

(defn gradient-src
  "returns the emitted GLSL source for the gradient effect"
  {:added "4.1"}
  []
  (workspace/ptr-display-str (var-get #'glsl.effects.gradient/gradient)))

(defn checkerboard-src
  "returns the emitted GLSL source for the checkerboard effect"
  {:added "4.1"}
  []
  (workspace/ptr-display-str (var-get #'glsl.effects.checkerboard/checkerboard)))

(defn ripple-src
  "returns the emitted GLSL source for the ripple effect"
  {:added "4.1"}
  []
  (workspace/ptr-display-str (var-get #'glsl.effects.ripple/ripple)))
