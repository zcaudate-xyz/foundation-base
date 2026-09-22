(ns melbourne.ui-input
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]
             [js.react-native.ui-input :as ui-input]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-theme :as base-theme]
             [melbourne.base-font :as base-font]]
   :export [MODULE]})

(defn.js Input
  "constructs a themed input"
  {:added "0.1"}
  [#{[design
      variant
      theme
      styleContainer
      style
      (:.. rprops)]}]
  (var palette (base-palette/designPalette design))
  (var __variant
       (Object.assign
        {:fg   {:key "neutral"}
         :bg   {:key "background"
                :mix "primary"
                :ratio 1}
         :pressed {:bg {:key "primary"}}
         :highlighted {:fg {:key "neutral"}
                       :bg   {:key "background"
                              :mix "primary"
                              :ratio 1}}
         :active  {:fg {:key "background"}
                   :bg {:key "primary"
                        :mix "neutral"
                        :ratio 4}}}
        variant))
  (var __theme  (Object.assign (base-theme/themeUiInput
                           palette
                           __variant)
                          theme))
  (return
   [:% ui-input/Input
    #{[:theme __theme
       :selectionColor (. palette mainColor)
       :style [base-font/fontFamily
               (:.. (data/arrayify style))]
       :styleContainer [{:flex 1
                         :minHeight 42
                         :paddingHorizontal 12
                         :borderStyle "solid"
                         :borderWidth 1
                         :borderRadius 10
                         :borderColor (base-palette/getColor
                                       palette
                                       {:key "neutral"
                                        :mix "background"
                                        :ratio 5})}
                        (:.. (data/arrayify styleContainer))]
       (:.. rprops)]}]))

(def.js MODULE (!:module))
