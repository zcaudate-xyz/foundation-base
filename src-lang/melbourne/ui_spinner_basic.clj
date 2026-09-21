(ns melbourne.ui-spinner-basic
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react-native :as n :include [:fn]]
             [js.react-native.ui-spinner-basic :as ui-spinner-basic]
             [melbourne.ui-helper :as ui-helper]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-theme :as base-theme]
             [melbourne.base-font :as base-font]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js SpinnerBasicControls
  "creates spinner controls"
  {:added "0.1"}
  [#{[setValue
      value
      (:= step 1)
      max
      min
      (:.. rprops)]}]
  (return
   [:% ui-helper/HelperControl
    #{[:leftDisabled (<= value min)
       :rightDisabled (>= value max)
       :onLeft  (fn:> (setValue (Math.max min (Math.min max (-  value step)))))
       :onRight (fn:> (setValue (Math.max min (Math.min max (+  value step)))))
       (:.. rprops)]}]))

(defn.js SpinnerBasic
  "Creates a spinner"
  {:added "0.1"}
  [#{[design
      variant
      theme
      max
      min
      decimal
      step
      value
      setValue
      style
      styleDigit
      styleDigitText
      styleDecimal
      styleDecimalText
      (:.. rprops)]}]
  (var __variant
       (Object.assign
        {:fg   {:key "primary"
                :tone "flatten"}
         :bg   {:key "background"
                :tone "darken"
                :ratio 1}
         :pressed {:fg {:key "primary"}
                   :bg {:key "primary"
                        :tone "sharpen"}}
         :highlighted {:fg {:key "neutral"}
                       :bg {:key "background"
                            :tone "darken"
                            :ratio 1}}
         :active  {:fg {:key "background"}
                   :bg {:key "primary"}}}
        variant))
  (var __style (base-font/getFontStyle (or (. __variant font)
                                           "h6")))
  (var __theme  (Object.assign (base-theme/themeUiInput
                           (base-palette/designPalette design)
                           __variant)
                          theme))
  (var #{fgNormal} __theme)
  (return
   [:% ui-spinner-basic/SpinnerBasic
    #{[:theme __theme
       :style [{:padding 0}
               __style
               (:.. (data/arrayify style))]
       max
       min
       step
       decimal
       value
       setValue
       :styleDigit     [{:backgroundColor nil}
                        (:.. (data/arrayify styleDigit))]
       :styleDigitText [{:color fgNormal
                         :backgroundColor nil}
                        (:.. (data/arrayify styleDigitText))]
       :styleDecimal   [{:backgroundColor nil}
                        (:.. (data/arrayify styleDecimal))]
       :styleDecimalText [{:color fgNormal
                           :backgroundColor nil}
                          (:.. (data/arrayify styleDecimalText))]
       (:.. rprops)]}]))

(def.js MODULE (!:module))

