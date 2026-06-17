(ns test-site.glsl-effects
  (:use code.test))

[[:chapter {:title "GLSL Effects"}]]

[[:section {:title "Introduction"}]]

"The `:shader` directive embeds a GLSL effect from `src-lang/glsl/effects/`
and renders it as a syntax-highlighted source block with a live WebGL preview."

[[:section {:title "Solid Color"}]]

"A flat color effect."

[[:shader {:refer "glsl.effects.solid/solid"}]]

[[:section {:title "Gradient"}]]

"A horizontal gradient based on fragment x coordinate."

[[:shader {:refer "glsl.effects.gradient/gradient"}]]

[[:section {:title "Checkerboard"}]]

"An 8×8 checker pattern."

[[:shader {:refer "glsl.effects.checkerboard/checkerboard"}]]

[[:section {:title "Noise"}]]

"Pseudo-random value noise from screen coordinates."

[[:shader {:refer "glsl.effects.noise/noise"}]]

[[:section {:title "Plasma"}]]

"Animated sine/cosine plasma."

[[:shader {:refer "glsl.effects.plasma/plasma"}]]

[[:section {:title "Ripple"}]]

"A distance ripple from the center of the canvas."

[[:shader {:refer "glsl.effects.ripple/ripple"}]]
