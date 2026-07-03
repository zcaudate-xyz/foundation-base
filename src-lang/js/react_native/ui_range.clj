(ns js.react-native.ui-range
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.common-data :as xtd]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.physical-edit :as physical-edit]
             [js.react-native.helper-theme-default :as helper-theme-default]
             [js.react-native.helper-theme :as helper-theme]
             [xt.lang.spec-base :as xt]]})

(defn.js rangeTheme
  "creates the range theme"
  {:added "4.0"}
  [#{[theme
      themePipeline
      (:.. rprops)]}
   forwardFn
   side
   layout
   length]
  (var __theme (Object.assign {} helper-theme-default/ButtonDefaultTheme theme))
  (var __themePipeline (Object.assign {}
                                 helper-theme-default/PressDefaultPipeline
                                 themePipeline))
  (var [fgStyleStatic fgTransformFn]
       (helper-theme/prepThemeSingle
        #{[:theme __theme
           :themePipeline __themePipeline
           :transformations
           {:fg (fn [m]
                  (var #{[(:= position 0)
                          (:= lower 0)
                          (:= upper 0)]} m)
                  (var limit (:? (== side "lower")
                                 (Math.max 0 (Math.min position upper))
                                 (Math.min length (Math.max position lower))))
                  
                  (return {:style {:transform
                                   [(:? (== layout "horizontal")
                                        {:translateX limit}
                                        {:translateY limit})]}}))}
           (:.. rprops)]}
        "fg"
        ["backgroundColor"]))
  (return #{fgStyleStatic fgTransformFn}))

(defn.js useKnob
  "creates the knob for range"
  {:added "4.0"}
  [#{[theme
      themePipeline
      disabled
      highlighted
      outlined
      indicators
      chord
      onChord
      indicatorParams
      
      onIndicators
      position
      forwardFn
      value
      layout
      (:= length 0)
      side]}]
  (var #{touchable
         panHandlers} (physical-edit/usePanTouchable
                       #{[disabled
                          highlighted
                          outlined
                          :chord (xt/x:obj-assign #{value} chord)
                          indicators
                          onChord
                          indicatorParams
                          
                          onIndicators]}
                       layout
                       position
                       true))
  (var  #{pressing}   touchable)
  (var #{fgStyleStatic fgTransformFn} (-/rangeTheme
                                       #{theme
                                         themePipeline}
                                       forwardFn
                                       side
                                       layout
                                       length))
  (r/watch [value]
    (when (not pressing)
      (a/setValue position (forwardFn value))))
  (return (xt/x:obj-assign touchable
                    #{panHandlers
                      fgStyleStatic
                      fgTransformFn})))

(defn.js Range
  "creates a slim range"
  {:added "0.1"}
  [#{[disabled
      highlighted
      outlined
      chord
      onChord
      indicators
      (:= indicatorParams {})
      
      onIndicators
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
      length
      lower
      setLower
      upper
      setUpper
      (:= size 15)
      (:= layout "horizontal")
      (:.. rprops)]}]
  (var #{positionUpper
         positionLower
         forwardFn
         reverseFn}    (a/useRange #{max
                                     min
                                     step
                                     length
                                     lower
                                     setLower
                                     upper
                                     setUpper}))
  (var touchableLower (-/useKnob #{[theme
                                    themePipeline
                                    transformations
                                    disabled
                                    highlighted
                                    outlined
                                    indicators
                                    chord
                                    onChord
                                    indicatorParams
                                    
                                    onIndicators
                                    forwardFn
                                    length
                                    :side "lower"
                                    :value lower
                                    :position positionLower
                                    layout]}))
  (var touchableUpper (-/useKnob #{[theme
                                    themePipeline
                                    transformations
                                    disabled
                                    highlighted
                                    outlined
                                    indicators
                                    chord
                                    onChord
                                    indicatorParams
                                    
                                    onIndicators
                                    forwardFn
                                    length
                                    :side "upper"
                                    :value upper
                                    :position positionUpper
                                    layout]}))
  (return
   [:% physical-base/Box
    #{[:chord      {:lower touchableLower.chord
                    :upper touchableUpper.chord}
       :inner [(xt/x:obj-assign
                {:component n/View
                 :key "axis"
                 :style [(:? (== layout "horizontal") {:height size :width (+ length (* 2 size))} {:height (+ length (* 2 size)) :width size})
                         {:borderRadius 3}
                         (:.. (xtd/arrayify axisStyle))]}
                axisProps)
               (Object.assign
                {:component n/View
                 :key "knobLower"
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
                         touchableLower.fgStyleStatic
                         (:.. (xtd/arrayify knobStyle))]
                 :transformations touchableLower.fgTransformFn}
                touchableLower.panHandlers
                (do:> (var #{[indicators
                              chord
                              (:.. rprops)]} touchableLower)
                      (return (xt/x:obj-assign rprops
                                        {:indicators
                                         (xt/x:obj-assign indicators
                                                   {:upper touchableUpper.indicators.position})})))
                knobProps)
               (Object.assign
                {:component n/View
                 :key "knobUpper"
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
                         touchableUpper.fgStyleStatic
                         (:.. (xtd/arrayify knobStyle))]
                 :transformations touchableUpper.fgTransformFn}
                touchableUpper.panHandlers
                (do:> (var #{[indicators
                              chord
                              (:.. rprops)]} touchableUpper)
                      (return (xt/x:obj-assign rprops
                                        {:indicators
                                         (xt/x:obj-assign indicators
                                                   {:lower touchableLower.indicators.position})})))
                knobProps)
               (:.. (xtd/arrayify inner))]
       (:.. rprops)]}]))

