(ns melbourne.ui-static
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react-native :as n :include [:fn]]
             [js.react-native.ui-tooltip :as ui-tooltip]
             [js.react-native.ui-scrollview :as ui-scrollview]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-font :as base-font]]
   :export [MODULE]})

(defn.js Div
  "creates a static div"
  {:added "4.0"}
  [#{[refLink
      design
      variant
      style
      (:.. rprops)]}]
  (var palette  (base-palette/designPalette design))
  (var __variant (Object.assign
                  {:fg {:key "neutral"}
                   :bg {:key "background"}}
                  variant))
  (return
   [:% n/View 
    #{[:ref refLink
       :style [{:backgroundColor
                (:? (. __variant bg)
                    (base-palette/getColor
                     palette
                     (. __variant bg)))
                :borderColor
                (:? (. __variant fg)
                    (base-palette/getColor
                     palette
                     (. __variant fg)))}
               (:.. (data/arrayify style))]
       (:.. rprops)]}]))

(defn.js Text
  "creates a static text element"
  {:added "4.0"}
  [#{[refLink
      design
      variant
      style
      (:.. rprops)]}]
  (var palette  (base-palette/designPalette design))
  (var __variant (Object.assign
                  {:fg {:key "neutral"}}
                  variant))
  (var __style (base-font/getFontStyle
                  (. __variant font)))
  (return
   [:% n/Text
    #{[:ref refLink
       :style [{:backgroundColor
                (:? (. __variant bg)
                    (base-palette/getColor palette (. __variant bg)))
                :color (base-palette/getColor
                        palette
                        (. __variant fg))}
               __style
               (:.. (data/arrayify style))]
       (:.. rprops)]}]))

(defn.js Separator
  "creates a seperator"
  {:added "4.0"}
  [#{[refLink
      design
      variant
      style
      (:.. rprops)]}]
  (var palette  (base-palette/designPalette design))
  (var __variant (Object.assign
                  {:fg {:key "neutral"}}
                  variant))
  (return
   [:% n/View
    #{[:ref refLink
       :style [{:backgroundColor
                (base-palette/getColor
                 palette
                 (. __variant fg))
                :height 1}
               (:.. (data/arrayify style))]
       #_(:.. rprops)]}]))

(defn.js ScrollView
  "creates a scrollview"
  {:added "4.0"}
  [#{[design
      variant
      styleBackground
      styleIndicator
      (:.. rprops)]}]
  (var palette  (base-palette/designPalette design))
  (var __variant (Object.assign
                  {:bg {:key "background"
                        :tone "augment"}
                   :fg {:key "primary"
                        :mix "background"
                        :ratio 6}}
                  variant))
  (return
   [:% ui-scrollview/ScrollView
    #{[:styleBackground
       [{:backgroundColor (base-palette/getColor
                           palette
                           (. __variant bg))}
        (:.. (data/arrayify styleBackground))]
       :styleIndicator
       [{:backgroundColor (base-palette/getColor
                           palette
                           (. __variant fg))}
        (:.. (data/arrayify styleIndicator))]
       (:.. rprops)]}]))

(defn.js TextTooltip
  "creates the text tooltip for buttons and toggles"
  {:added "4.0"}
  [#{[design
      variant
      text
      textProps
      arrow
      style
      (:.. rprops)]}]
  (var palette (base-palette/designPalette design))
  (var __variant (Object.assign {:fg {:key "background"}
                            :bg {:key "neutral"}}
                           variant))
  (var __style  (base-font/getFontStyle
                 (or (. __variant font)
                     "h6")))
  (return
   [:% ui-tooltip/Tooltip
    #{[:arrow (Object.assign {:color (base-palette/getColor
                                 palette
                                 (. __variant bg))}
                        arrow)
       (:.. rprops)]}
    [:% -/Text
     #{[:design design
        :variant __variant
        :style [__style
                {:position "absolute"
                 :borderRadius 5
                 :padding 10}
                (:.. (data/arrayify style))]
        (:.. textProps)]}
     text]]))

(defn.js TextDisplay
  "creates the text display"
  {:added "4.0"}
  [#{[style
      styleText
      content
      outlined
      design
      children
      (:.. rprops)]}]
  (return
   [:% -/ScrollView
    #{[design
       :variant (data/get-in design ["variant" "scrollview"])
       (:.. rprops)]}
    [:% -/Text
     #{[design
        :variant (data/get-in design ["variant" "text"])
        :style [(n/PlatformSelect {:ios {:fontFamily "Courier"}
                                   :default {:fontFamily "monospace"}})
                {:fontSize 10}
                (:.. (data/arrayify styleText))]]}
     (or content children)]]))

(def.js MODULE (!:module))
