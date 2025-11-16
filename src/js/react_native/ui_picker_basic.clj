(ns js.react-native.ui-picker-basic
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.physical-edit :as physical-edit]
             [js.react-native.helper-roller :as helper-roller]
             [js.react-native.helper-theme :as helper-theme]
             [js.react-native.helper-theme-default :as helper-theme-default]]})

(defn.js pickerTheme
  "creates the picker theme"
  {:added "4.0"}
  [#{[theme
      themePipeline
      (:.. rprops)]}]
  (var __theme (j/assign {} helper-theme-default/ButtonDefaultTheme theme))
  (var __themePipeline (j/assign {}
                                 helper-theme-default/PressDefaultPipeline
                                 themePipeline))
  (var [styleStatic transformFn]
       (helper-theme/prepThemeCombined
        #{[:theme __theme
           :themePipeline __themePipeline
           (:.. rprops)]}))
  (return [styleStatic transformFn]))

(defn.js usePickerPosition
  "helper function to connect picker position"
  {:added "4.0"}
  [index setIndex indexRef]
  (var position     (a/val 0))
  (var prevRef      (r/ref index))
  (r/init []
    (a/addListener position
                   (fn []
                     (var #{_value} position)
                     (var nIndex (+ (r/curr indexRef)
                                    (j/round (/ _value 10))))
                     (when (not= nIndex (r/curr prevRef))
                       (setIndex nIndex)
                       (r/curr:set prevRef nIndex)))))
  (return position))

(defn.js PickerBasicIndexed
  "creates a slim picker"
  {:added "0.1"}
  [#{[theme
      themePipeline
      disabled
      index
      setIndex
      style
      styleText
      chord
      onHoverIn
      onHoverOut
      (:= format k/identity)
      (:= divisions 4)
      (:= items [])
      (:.. rprops)]}]
  (var [styleStatic transformFn] (-/pickerTheme #{[theme
                                                   themePipeline
                                                   (:.. rprops)]}))
  (var indexRef       (r/ref index))
  (var position       (-/usePickerPosition index setIndex indexRef))
  (var #{labels
         labelsLu
         offset
         modelFn}     (helper-roller/useRoller #{index items divisions}))
  (var #{touchable
         panHandlers} (physical-edit/usePanTouchable
                       #{[disabled
                          :chord (j/assign #{index} chord)
                          (:.. rprops)]}
                       "vertical"
                       position
                       false))
  (var  #{setPressing
          pressing
          hovering
          setHovering}    touchable)
  (r/watch [pressing]
    (r/curr:set indexRef index))
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
       :style [{:overflow "hidden"
                :width 120
                :padding 5
                :height 30}
               styleStatic
               (:.. (j/arrayify style))]
       :transformations transformFn
       (:.. (j/assign touchable
                      panHandlers))]}
    [:% physical-base/Text
     {:indicators {:offset offset
                   :value  (. labels [index])}
      :style [{:position "absolute"
               :padding 0
               :fontSize 18
               :fontWeight "400"}
              (n/PlatformSelect
               {:web {:cursor "ns-resize"
                      :userSelect "none"}})
              #_{:color fgActive}
              (:.. (j/arrayify styleText))]
      :transformations
      (fn [#{offset value}]
        (return
         {:text (format (. items [value]) value)}))}]]))

