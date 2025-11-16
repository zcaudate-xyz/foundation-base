(ns js.lib.r3-postprocessing
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:bundle  {:default [["@react-three/postprocessing" :as [* ReactThreePp]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactThreePp"
                                   :tag "js"}]
  [ASCII
   Autofocus
   Bloom
   BrightnessContrast
   ChromaticAberration
   ColorAverage
   ColorDepth
   Depth
   DepthOfField
   DotScreen
   EffectComposer
   EffectComposerContext
   FXAA
   Glitch
   GodRays
   Grid
   HueSaturation
   LUT
   N8AO
   Noise
   Outline
   Pixelation
   SMAA
   SSAO
   SSR
   Scanline
   Select
   Selection
   SelectiveBloom
   Sepia
   ShockWave
   Texture
   TiltShift
   TiltShift2
   TiltShiftEffect
   ToneMapping
   Vignette
   resolveRef
   selectionContext
   useVector2
   wrapEffect])
