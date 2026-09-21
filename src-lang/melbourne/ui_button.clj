(ns melbourne.ui-button
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native.ui-button :as ui-button]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [melbourne.addon-tooltip :as addon-tooltip]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-theme :as base-theme]
             [melbourne.base-font :as base-font]]
   :export [MODULE]})

(defn.js Button
  "constructs a button"
  {:added "4.0"}
  [#{[design
      variant 
      theme
      style
      transformations
      addons
      tooltip
      (:= refLink (r/ref))
      (:.. rprops)]}]
  (var [chord setChord] (r/local {}))
  (var __variant (Object.assign
                  {:fg {:key "background"}
                   :bg {:key "primary"}}
                  variant))
  (var __style   (base-font/getFontStyle (or (. __variant font)
                                             "h6")))
  (var __theme   (Object.assign (base-theme/themeUiButton
                            (base-palette/designPalette design)
                            __variant)
                           theme))
  (return
   [:% ui-button/Button
    #{[:refLink refLink
       :onChord setChord
       :theme __theme
       :style [{:padding 8
                :borderRadius 3}
               __style
               (:.. (data/arrayify style))]
       :addons [(:? tooltip
                    (addon-tooltip/addonTooltip
                     refLink
                     (. chord hovering)
                     #{design
                       tooltip
                       {:variant (data/get-in design ["variant" "tooltip"])}}))
                (:.. (data/arrayify addons))]
       :transformations
       (Object.assign
        {:bg (fn:> [#{pressing}]
                   {:style {:transform [{:scale (+ 1 (* 0.08 pressing))}]}})}
        transformations)
       (:.. rprops)]}]))

(def.js MODULE (!:module))
