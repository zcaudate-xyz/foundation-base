(ns js.react-native.ui-spinner
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.physical-edit :as physical-edit]
             [js.react-native.helper-roller :as helper-roller]
             [js.react-native.helper-theme :as helper-theme]
             [js.react-native.helper-theme-default :as helper-theme-default]]})
  
(def.js ITEMS
  ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"])

(def.js styleDigit
  {:height 25
   :width 10
   :overflow "hidden"
   :backgroundColor "blue"})

(def.js styleDigitText
  {:height 25
   :position "absolute"
   :width 10
   :fontSize 16
   :fontWeight "400"
   :backgroundColor "red"
   :color "#333"})

(defn.js spinnerTheme
  "creates the spinner theme"
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

(defn.js SpinnerStatic
  "creates the spinner padding"
  {:added "0.1"}
  [#{[text
      styleText
      style
      editable]}]
  (return
   [:% n/View
    {:style [-/styleDigit
             (:.. (j/arrayify style))]}
    [:% n/Text
     {:style [-/styleDigitText
              (n/PlatformSelect
               {:web {:userSelect "none"
                      :cursor (:? editable
                                  "ns-resize"
                                  "default")}})
              (:.. (j/arrayify styleText))]}
     text]]))

(defn.js SpinnerDigit
  "creates the spinner digit"
  {:added "4.0"}
  [#{[index
      style
      styleText
      (:= brand {})
      (:= items -/ITEMS)
      (:= divisions 5)
      editable]}]
  (var #{labels
         labelsLu
         offset
         modelFn} (helper-roller/useRoller #{index items divisions}))
  (return
   [:% n/View
    {:style [-/styleDigit
             (:.. (j/arrayify style))]}
    (j/map (k/arr-range divisions)
           (fn:> [index i]
             [:% physical-base/Text
              {:key i
               :indicators {:offset offset
                            :value (. labels [index])}
               :style [-/styleDigitText
                       (n/PlatformSelect
                        {:web {:userSelect "none"
                               :cursor (:? editable
                                           "ns-resize"
                                           "default")}})
                       (:.. (j/arrayify styleText))]
               :transformations
               (fn [#{offset value}]
                 (var v (- offset index))
                 (var #{translate
                        scale
                        visible} (modelFn v))
                 (return
                  {:text  (. items [value])
                   :style {:opacity (:? visible
                                        (k/mix -2 1 scale)
                                        0)
                           :zIndex (* 10 scale)
                           :transform [{:translateY (* -2 translate)}]}}))}]))]))

(defn.js SpinnerValues
  "creates the spinner values"
  {:added "4.0"}
  [#{[max
      min
      onChange
      value
      editable
      setValue
      styleDigit
      styleDigitText
      styleDecimal
      styleDecimalText
      (:= decimal 0)]}]
  (var arrDigits [])
  (var arrTotal  (j/ceil (j/log10 (+ max 0.0001))))
  (k/for:index [i [0 (j/max arrTotal
                            (+ 1 decimal)) 1]]
    (when (and (== i decimal)
               (< 0 i))
      (x:arr-push-first arrDigits {:type "decimal"}))
    (x:arr-push-first arrDigits {:type "digit"
                                 :order i}))
  
  (var digitFn
       (fn [#{type order} i]
         (var limit (j/pow 10 order))
         (var hideDigit
              (:? (== 0 decimal)
                  (< value limit)
                  false))
         (cond (== type "digit")
               (return
                [:% n/View
                 {:key (+ "digit" i)
                  :style (:? hideDigit {:opacity 0})}
                 [:% -/SpinnerDigit
                  {:index (j/floor (/ value (j/round (k/pow 10 order))))
                   :style styleDigit
                   :styleText styleDigitText
                   :editable editable}]])

               (== type "decimal")
               (return
                [:% -/SpinnerStatic
                 {:key (+ "decimal" i)
                  :text "."
                  :style [{:width 5}
                          styleDecimal]
                  :styleText styleDecimalText
                  :editable editable}]))))
  (return
   [:<>
    (j/map arrDigits digitFn)]))

(defn.js useSpinnerPosition
  "helper function to connect spinner position"
  {:added "4.0"}
  [value setValue valueRef min max stride]
  (var position     (a/val 0))
  (var prevRef      (r/ref value))
  (r/init []
    (a/addListener position
                   (fn []
                     (var #{_value _offset} position)
                     (var nValue (k/clamp
                                  min max
                                  (- (r/curr valueRef)
                                     (j/round (/ _value (or stride 8))))))
                     
                     (when (not= nValue (r/curr prevRef))
                       (setValue nValue)
                       (r/curr:set prevRef nValue)))))
  (return position))

(defn.js Spinner
  "creates the spinner value"
  {:added "0.1"}
  [#{[theme
      themePipeline
      disabled
      min
      max
      panDirection
      panStride
      value
      setValue
      style
      styleText
      chord
      onHoverIn
      onHoverOut
      (:.. rprops)]}]
  (var [__value __setValue] (r/local value))
  (var __valueRef     (r/ref __value))
  (var [styleStatic
        transformFn]  (-/spinnerTheme #{[theme
                                         themePipeline
                                         (:.. rprops)]}))
  (var position     (-/useSpinnerPosition __value __setValue __valueRef
                                          min
                                          max
                                          panStride))
  (var #{touchable
         panHandlers} (physical-edit/usePanTouchable
                       #{[disabled
                          :chord (j/assign {:value __value} chord)
                          (:.. rprops)]}
                       (or panDirection
                           "vertical")
                       position
                       false))
  (var  #{setPressing
          pressing
          hovering
          setHovering}    touchable)
  (r/watch [pressing]
    (r/curr:set __valueRef __value))
  (r/watch [pressing __value]
    (when (not pressing)
      (setValue __value)))
  (r/watch [value]
    (when (not= value __value)
      (__setValue value)))
  (var iconElem
       [:% n/View
        {:key "icon"
         :style {:zIndex -10
                 :transform
                 [{:rotateZ (:? (== panDirection "horizontal")
                                "45deg"
                                "-45deg")}]}}
        [:% n/Icon
         {:name "resize-full-screen"
          :style {:color (k/get-in styleStatic [0 "color"]) 
                  :paddingLeft 5}
          :size 15}]])
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
                :flexDirection "row"
                :alignItems "center"
                :padding 5}
               styleStatic
               (n/PlatformSelect
                {:web {:userSelect "none"
                       :cursor "default"}})
               (:.. (j/arrayify style))]
       :transformations transformFn
       (:.. (j/assign touchable
                      panHandlers))
       :children [[:% -/SpinnerValues
                   #{[:key "values"
                      :editable true
                      :value __value
                      :setValue __setValue
                      min
                      max
                      (:.. rprops)]}]
                  iconElem
                  [:% n/View
                   {:key "background"
                    :style {:position "absolute"
                            :height "100%"
                            :width "100%"}}]]]}]))

(comment

  panDirection
                  panStride)
