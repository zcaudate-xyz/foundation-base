(ns js.react-native.ui-button
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

(defn.js buttonTheme
  "creates the botton theme"
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

(defn.js Button
  "creates a simple button"
  {:added "0.1"}
  [#{[text
      textProps
      style
      styleContainer
      theme
      themePipeline
      (:= inner  [])
      (:.. rprops)]}]
  (var [styleStatic transformFn] (-/buttonTheme #{[theme
                                                   themePipeline
                                                   (:.. rprops)]}))
  (return
   [:% physical-base/TouchableBasePressing
    #{[:inner [(j/assign
                {:component n/Text
                 :key "text"
                 :numberOfLines 1
                 :children (j/arrayify text)
                 :style [helper-theme-default/ButtonDefaultStyle
                         (:.. styleStatic)
                         (:.. (j/arrayify style))]
                 :transformations transformFn}
                textProps)
               (:.. inner)]
       :style styleContainer
       (:.. rprops)]}]))

