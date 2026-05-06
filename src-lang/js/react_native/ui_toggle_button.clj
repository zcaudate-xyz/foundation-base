(ns js.react-native.ui-toggle-button
  (:require [hara.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react-native :as n]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.helper-theme-default :as helper-theme-default]
             [js.react-native.helper-theme :as helper-theme]]})

(defn.js toggleButtonTheme
  "creates the toggle button theme"
  {:added "4.0"}
  [#{[theme
      themePipeline
      (:.. rprops)]}]
  (var __theme (xt/x:obj-assign {} helper-theme-default/ButtonDefaultTheme theme))
  (var __themePipeline (xt/x:obj-assign {}
                                 helper-theme-default/BinaryDefaultPipeline
                                 themePipeline))
  (var [styleStatic transformFn]
       (helper-theme/prepThemeCombined
        #{[:theme __theme
           :themePipeline __themePipeline
           (:.. rprops)]}))
  (return [styleStatic transformFn]))

(defn.js ToggleButton
  "gets a toggleButton button"
  {:added "0.1"}
  [#{[selected
      text
      textProps
      style
      styleContainer
      theme
      themePipeline
      (:= inner  [])
      (:.. rprops)]}]
  (var [styleStatic transformFn] (-/toggleButtonTheme #{[theme
                                                         themePipeline
                                                         (:.. rprops)]}))
  (return
   [:% physical-base/TouchableBinary
    #{[:active selected
       :style styleContainer
       :inner [(xt/x:obj-assign
                {:component n/Text
                 :children (xtd/arrayify text)
                 :style [helper-theme-default/ButtonDefaultStyle
                         (:.. (xtd/arrayify style))
                         styleStatic]
                 :transformations transformFn}
                textProps)
               (:.. inner)]
       (:.. rprops)]}]))

