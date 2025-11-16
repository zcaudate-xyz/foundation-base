(ns js.react-native.ui-input
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
             [js.react-native.helper-theme :as helper-theme]
             [xt.lang.base-lib :as k]]})

(defn.js inputTheme
  "creates the input theme"
  {:added "4.0"}
  [#{[theme
      themePipeline
      (:.. rprops)]}]
  (var __theme (j/assign {} helper-theme-default/InputDefaultTheme theme))
  (var __themePipeline (j/assign {} helper-theme-default/InputDefaultPipeline themePipeline))
  (var [fgStyleStatic fgTransformFn]
       (helper-theme/prepThemeSingle
        #{[:theme __theme
           :themePipeline __themePipeline
           (:.. rprops)]}
        "fg"
        ["color"]))
  (var [bgStyleStatic bgTransformFn]
       (helper-theme/prepThemeCombined
        #{[:theme __theme
           :themePipeline __themePipeline
           (:.. rprops)]}))
  (return #{fgStyleStatic fgTransformFn
            bgStyleStatic bgTransformFn}))

(defn.js Input
  "creates a slim input"
  {:added "0.1"}
  ([#{[theme
       themePipeline
       style
       onChangeText
       styleContainer
       containerProps
       (:.. rprops)]}]
   (var #{fgStyleStatic fgTransformFn
          bgStyleStatic bgTransformFn} (-/inputTheme #{[theme
                                                        themePipeline
                                                        (:.. rprops)]}))
   (return
    [:% physical-base/TouchableInput
     #{[:styleContainer [helper-theme-default/InputDefaultStyle
                         (:.. bgStyleStatic)
                         (:.. (j/arrayify styleContainer))]
        :containerProps
        (j/assign
         {:transformations bgTransformFn}
         containerProps)
        :transformations fgTransformFn
        :onChangeText (fn [v]
                        (return (j/map (k/arrayify onChangeText)
                                      (fn:> [f] (f v)))))
        :size  "sm"
        :style [{:flex 1}
                (:.. fgStyleStatic)
                (n/PlatformSelect
                 {:web {:outlineWidth 0
                        :outline "none"}})
                (:.. (j/arrayify style))]
        (:..  rprops)]}])))
  
