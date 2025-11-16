(ns js.react-native.ui-slider
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core :as j]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.physical-edit :as physical-edit]
             [js.react-native.helper-theme-default :as helper-theme-default]
             [js.react-native.helper-theme :as helper-theme]
             [xt.lang.base-lib :as k]]})

(defn.js sliderTheme
  "creates the slider theme"
  {:added "4.0"}
  [#{[theme
      themePipeline
      transformations
      (:.. rprops)]}
   layout
   length]
  (var __theme (j/assign {} helper-theme-default/ButtonDefaultTheme theme))
  (var __themePipeline (j/assign {} helper-theme-default/PressDefaultPipeline themePipeline))
  (var #{knob axis} transformations)
  (var [bgStyleStatic bgTransformFn]
       (helper-theme/prepThemeCombined
        #{[:theme __theme
           :themePipeline __themePipeline
           :transformations (or axis {})
           (:.. rprops)]}))
  (var [fgStyleStatic fgTransformFn]
       (helper-theme/prepThemeSingle
        #{[:theme __theme
           :themePipeline __themePipeline
           :transformations
           (j/assign
            {:fg (fn [#{[(:= position 0)]}]
                   (return
                    {:style {:transform
                             [(:? (== layout "horizontal")
                                  {:translateX (j/max 0 (j/min length position))}
                                  {:translateY (j/max 0 (j/min length position))})]}}))}
            knob)
           (:.. rprops)]}
        "fg"
        ["backgroundColor"]))
  (return #{bgStyleStatic bgTransformFn
            fgStyleStatic fgTransformFn}))

(defn.js Slider
  "creates a slim slider"
  {:added "0.1"}
  [#{[disabled
      highlighted
      outlined
      chord
      onChord
      indicators
      onIndicators
      (:= indicatorParams {})
      
      knobProps
      knobStyle
      axisProps
      axisStyle
      onHoverIn
      onHoverOut

      theme
      themePipeline
      (:= transformations {})
      inner
      
      max
      min
      step
      (:= length 0)
      (:= value 0)
      setValue
      (:= size 15)
      (:= layout "horizontal")
      (:.. rprops)]}]
  (var #{forwardFn
         reverseFn
         position}    (a/usePosition #{max
                                       min
                                       step
                                       length
                                       value
                                       setValue
                                       {:flip (== layout "vertical")}}))
  (var #{touchable
         panHandlers} (physical-edit/usePanTouchable
                       #{[disabled
                          highlighted
                          outlined
                          :chord (j/assign #{value} chord)
                          indicators
                          onChord
                          indicatorParams
                          
                          onIndicators]}
                       layout
                       position
                       true))
  (var  #{setPressing
          pressing
          hovering
          setHovering}    touchable)
  (var #{bgStyleStatic bgTransformFn
         fgStyleStatic fgTransformFn} (-/sliderTheme
                                       #{theme
                                         themePipeline
                                         transformations}
                                       layout
                                       length))
  (r/watch [value]
    (when (not pressing)
      (a/setValue position (forwardFn value))))
  (return
   [:% physical-base/Box
    #{[:indicators touchable.indicators
       :chord      touchable.chord
       :onMouseEnter  (fn [e] (setHovering true)
                        (if onHoverIn (onHoverIn e)))
       :onMouseLeave  (fn [e] (setHovering false)
                        (if onHoverOut (onHoverOut e)))
       :onMouseUp     (fn []
                        (setPressing false))
       :onMouseDown   (fn [e]
                        (. e preventDefault))
       :inner [(j/assign
                {:component n/View
                 :key "axis"
                 :style [(:? (== layout "horizontal")
                             {:height size
                              :width (+ length (* 2 size))}
                             {:height (+ length (* 2 size)) :width size})
                         {:borderRadius 3}
                         bgStyleStatic
                         (:.. (j/arrayify axisStyle))]
                 :transformations bgTransformFn}
                axisProps)
               (j/assign
                {:component n/View
                 :key "knob"
                 :style [(:? (== layout "horizontal")
                             {:top (- (* 0.5 size))
                              :height (* 2 size)
                              :width (* 2 size)}
                             {:left (- (* 0.5 size))
                              :height (* 2 size)
                              :width (* 2 size)})
                         {:position "absolute"
                          :borderRadius 3}
                         (n/PlatformSelect
                          {:web (:? (== layout "horizontal")
                                    {:cursor "ew-resize"}
                                    {:cursor "ns-resize"})})
                         fgStyleStatic
                         (:.. (j/arrayify knobStyle))]
                 :transformations fgTransformFn}
                panHandlers
                knobProps)
               (:.. (j/arrayify inner))]
       (:.. rprops)]}]))

