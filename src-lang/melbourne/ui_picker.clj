(ns melbourne.ui-picker
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]
             [js.react :as r :include [:fn]]
             [js.react-native.ui-picker :as ui-picker]
             [melbourne.ui-helper :as ui-helper]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-theme :as base-theme]
             [melbourne.base-font :as base-font]]
   :export [MODULE]})

(defn.js PickerControls
  "Controls for selecting the index"
  {:added "0.1"}
  [#{design
     variant
     setIndex
     style
     children
     index}]
  (return
   [:% ui-helper/HelperControl
    #{[design
       variant
       style
       children
       :onLeft  (fn:> (setIndex (- index 1)))
       :onRight (fn:> (setIndex (+ index 1)))]}]))

(defn.js PickerValues
  "Controls for picker values"
  {:added "0.1"}
  [#{[design
      variant
      theme
      index
      items
      style
      styleText
      (:.. rprops)]}]
  (var __variant (Object.assign
                  {:fg   {:key "primary"
                          :tone "flatten"}
                   :bg   {:key "background"
                          :tone "darken"
                          :ratio 1}}
                  variant))
  (var __style (base-font/getFontStyle (or (. __variant font)
                                           "h6")))
  (var __theme  (Object.assign (base-theme/themeNormal
                           (base-palette/designPalette design)
                           __variant)
                          theme))
  (var #{fgNormal
         bgNormal} __theme)
  (return
   [:% ui-picker/PickerValues
    #{[:theme __theme
       :style [{:padding 0
                :height 24
                :backgroundColor bgNormal}
               __style
               (:.. (data/arrayify style))]
       index
       items
       :styleText [{:color fgNormal
                    :padding 3
                    :paddingHorizontal 10
                    :fontSize 17}
                   (:.. (data/arrayify styleText))]
       (:.. rprops)]}]))

(defn.js PickerIndexed
  "Creates the picker"
  {:added "0.1"}
  [#{[design
      variant
      theme
      index
      setIndex
      items
      style
      styleText
      (:.. rprops)]}]
  (var __variant (Object.assign
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
  (var #{fgNormal
         bgNormal} __theme)
  (return
   [:% ui-picker/PickerIndexed
    #{[:theme __theme
       :style [{:padding 0
                ;;:width 180
                :height 24
                :backgroundColor bgNormal}
               __style
               (:.. (data/arrayify style))]
       index
       setIndex
       items
       :styleText [{:color fgNormal
                    :padding 3
                    :paddingHorizontal 10
                    :fontSize 17}
                   (:.. (data/arrayify styleText))]
       (:.. rprops)]}]))

(defn.js Picker
  "creates a picker"
  {:added "0.1"}
  ([#{[data
       valueEmpty
       valueFn
       value
       setValue
       (:.. rprops)]}]
   (var indexRef (r/ref 0))
   (var indexFn (fn:> (r/curr indexRef)))
   (var #{setIndex
          items
          index} (r/convertModular #{data
                                     valueFn
                                     value
                                     setValue
                                     indexFn}))
   (r/watch [index]
     (r/curr:set indexRef index))
   (return [:% -/PickerIndexed
            #{[setIndex
               items
               index
               (:.. rprops)]}])))

(def.js MODULE (!:module))

