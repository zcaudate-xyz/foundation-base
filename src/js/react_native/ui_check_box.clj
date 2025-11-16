(ns js.react-native.ui-check-box
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
             [js.react-native :as n]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.helper-theme-default :as helper-theme-default]
             [js.react-native.helper-theme :as helper-theme]]})

(defn.js checkBoxTheme
  "creates the checkbox theme"
  {:added "4.0"}
  [#{[theme
      themePipeline
      (:.. rprops)]}]
  (var __theme (j/assign {} helper-theme-default/CheckBoxDefaultTheme theme))
  (var __themePipeline (j/assign {} helper-theme-default/BinaryDefaultPipeline themePipeline))
  (var [styleStatic transformFn]
       (helper-theme/prepThemeCombined
        #{[:theme __theme
           :themePipeline __themePipeline
           (:.. rprops)]}))
  (return [styleStatic transformFn]))

(defn.js CheckBox
  "creates a slim checkbox"
  {:added "0.1"}
  [#{[selected
      setSelected
      style
      icon
      iconProps
      styleContainer
      theme
      themePipeline
      (:= inner [])
      (:.. rprops)]}]
  (var [styleStatic transformFn] (-/checkBoxTheme #{[theme
                                                     themePipeline
                                                     (:.. rprops)]}))
  (return
   [:% physical-base/TouchableBinary
    #{[:active selected
       :onPress (fn []
                  (when setSelected
                    (setSelected (not selected))))
       :inner [(j/assign
                {:component n/Icon
                 :allowRef true
                 :name  (or icon
                            "check")
                 :style [helper-theme-default/CheckBoxDefaultStyle
                         (:..  styleStatic)
                         (n/PlatformSelect
                          {:web {:outlineWidth 0}})
                         (:.. (j/arrayify style))]
                 :transformations transformFn}
                iconProps)
               (:.. inner)]
       (:.. rprops)]}]))

