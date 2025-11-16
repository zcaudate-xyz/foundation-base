(ns js.react-native.ui-toggle-switch
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react-native :as n :include [:fn]]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.helper-theme-default :as helper-theme-default]
             [js.react-native.helper-theme :as helper-theme]
             ]})

(defn.js toggleSwitchTheme
  "creates the toggle switch theme"
  {:added "4.0"}
  [#{[theme
      themePipeline
      (:= transformations {})
      (:.. rprops)]}
   size]
  (var __theme (j/assign {} helper-theme-default/ToggleSwitchDefaultTheme theme))
  (var __themePipeline (j/assign {} helper-theme-default/BinaryDefaultPipeline themePipeline))
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
           :transformations (j/assign
                             {:fg (fn:> [#{active}]
                                    {:style {:transform [{:translateX (- (* active size) 1)}]}})}
                             knob)
           (:.. rprops)]}
        "fg"
        ["backgroundColor"]))
  (return #{bgStyleStatic bgTransformFn
            fgStyleStatic fgTransformFn}))

(defn.js ToggleSwitch
  "creates a toggle switch box"
  {:added "4.0"}
  [#{[style
      selected
      setSelected
      outlined
      knobProps
      knobStyle
      axisProps
      axisStyle
      theme
      themePipeline
      (:= transformations {})
      (:= inner [])
      (:= size 24)
      (:.. rprops)]}]
  (var #{bgStyleStatic bgTransformFn
         fgStyleStatic fgTransformFn} (-/toggleSwitchTheme
                                       #{[theme
                                          themePipeline
                                          transformations
                                          (:.. rprops)]}
                                       size))
  (return
   [:% physical-base/TouchableBinary
    #{[:active selected
       :onPress (fn []
                  (when setSelected
                    (setSelected (not selected))))
       :inner [(j/assign
                {:component n/View
                 :key "axis"
                 :style [{:marginVertical (/ size 4) 
                          :borderRadius (/ size 2) 
                          :height (/ size 2) 
                          :width (* size 2)}
                         bgStyleStatic
                         (:.. (j/arrayify axisStyle))]
                 :transformations bgTransformFn}
                axisProps)
               (j/assign
                {:component n/View
                 :key "knob"
                 :style [{:borderRadius (/ size 2)
                          :height size
                          :position "absolute"
                          :width size}
                         fgStyleStatic
                         (:.. (j/arrayify knobStyle))]
                 :transformations fgTransformFn}
                knobProps)
               (:.. inner)]
       (:.. rprops)]}]))

