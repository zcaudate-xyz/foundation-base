(ns melbourne.ui-toggle-button
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native.ui-toggle-button :as ui-toggle-button]
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

(defn.js ToggleButton
  "creates a toggle button"
  {:added "0.1"}
  [#{[(:= refLink (r/ref))
      design
      variant
      style
      theme
      transformations
      addons
      tooltip
      (:.. rprops)]}]
  (var palette  (base-palette/designPalette design))
  (var __variant (Object.assign
                  {:fg {:key "neutral"}
                   :bg {:key "background"}
                   :active  {:fg {:key "background"}
                             :bg {:key "primary"}}}
                  variant))
  (var __style   (base-font/getFontStyle (or (. __variant font)
                                             "h6")))
  (var __theme   (Object.assign (base-theme/themeUiState
                            (base-palette/designPalette design)
                            __variant)
                           theme))
  (var [chord setChord] (r/local {}))
  (return
   [:% ui-toggle-button/ToggleButton
    #{[:refLink refLink
       :onChord setChord
       :theme __theme
       :style [{:paddingVertical 10
                :paddingHorizontal 16
                :minHeight 40
                :borderRadius 10
                :alignItems "center"
                :justifyContent "center"}
               __style
               (:.. (data/arrayify style))]
       :addons [(:? tooltip
                    (addon-tooltip/addonTooltip
                     refLink
                     (. chord hovering)
                     #{design
                       {:variant (data/get-in design ["variant" "tooltip"])}
                       tooltip}))
                (:.. (data/arrayify addons))]
       :transformations
       (Object.assign
        {:bg (fn:> [#{pressing}]
                   {:style {:transform [{:scale (+ 1 (* 0.08 pressing))}]}})}
        transformations)
       (:.. rprops)]}]))

(def.js MODULE (!:module))
