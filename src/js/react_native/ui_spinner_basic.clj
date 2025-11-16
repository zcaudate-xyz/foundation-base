(ns js.react-native.ui-spinner-basic
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react :as r]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.physical-edit :as physical-edit]
             [js.react-native.helper-theme :as helper-theme]
             [js.react-native.helper-theme-default :as helper-theme-default]]})

(def.js styleDigit
  {;;:height 25
   :overflow "hidden"
   :marginLeft 5
   #_#_:backgroundColor "blue"})

(def.js styleDigitText
  {;;:height 25
   #_#_:position "absolute"
   :fontSize 16
   :marginLeft 5
   :fontWeight "400"
   :backgroundColor "red"
   :color "#333"})

(defn.js spinnerTheme
  "creates a spinner theme"
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

(defn.js useSpinnerPosition
  "gets the spinner position"
  {:added "4.0"}
  [value setValue valueRef min max stride step]
  (var position     (a/val 0))
  (var prevRef      (r/ref value))
  (r/init []
    (a/addListener position
                   (fn []
                     (var #{_value _offset} position)
                     (var nValue (k/clamp
                                  min max
                                  (- (r/curr valueRef)
                                     (* (j/round (/ _value (or stride 8)))
                                        (or step 1)))))
                     
                     (when (not= nValue (r/curr prevRef))
                       (setValue nValue)
                       (r/curr:set prevRef nValue)))))
  (return position))

(defn.js SpinnerStatic
  "creates a static spinner"
  {:added "4.0"}
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

(defn.js SpinnerBasicValues
  "creates basic values for spinner"
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
  (return
   [:% -/SpinnerStatic
    {:text (j/toFixed (/ value
                         (j/pow 10 decimal))
                      (j/max 0 decimal))
     :styleText styleDigitText
     :editable editable}]))

(defn.js SpinnerBasic
  "creates a basic spinner"
  {:added "4.0"}
  [#{[theme
      themePipeline
      disabled
      min
      max
      step
      decimal
      (:= panDirection "vertical")
      (:= panStride 15)
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
                                          panStride
                                          step))
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
  #_(var iconElem
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
                #_#_:alignItems "center"
                #_#_:padding 5}
               styleStatic
               (n/PlatformSelect
               {:web {:userSelect "none"
                      :cursor "default"}})
               (:.. (j/arrayify style))]
       :transformations transformFn
       (:.. (j/assign touchable
                      panHandlers))
       :children [[:% -/SpinnerBasicValues
                   #{[:key "values"
                      :editable true
                      :value __value
                      :setValue __setValue
                      min
                      max
                      decimal
                      (:.. rprops)]}]
                  #_#_[:% n/View
                   {:style {:flex 1}}]
                  iconElem
                  ]]}]))

