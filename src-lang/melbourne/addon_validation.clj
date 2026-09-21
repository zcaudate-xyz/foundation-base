(ns melbourne.addon-validation
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :playground/web-basic
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react-native.helper-color :as c]
             [js.react :as r]
             [js.react-native :as n]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-theme :as base-theme]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js addonValidation
  "creates the form validation"
  {:added "0.1"}
  [#{[(:= design {})
      palette
      result
      styleText
      style]}]
  (var #{status message} result)
  (var [label setLabel]  (r/local (:? (== status "errored") message "")))
  (r/watch [status message]
    (when (== status "errored")
      (setLabel message)))

  (:= palette (base-palette/getPalette design palette))
  (var [fgColor bgColor] (base-theme/themeBase
                          palette
                          (Object.assign
                           {:fg {:key "neutral"}
                            :bg {:key "primary"}}
                           (data/get-in design
                                     ["theme" "validation"]))))
  (var #{mainError
         mainPrimary
         mainNeutral
         mainBackground} palette)
  (return
   {:component n/View
    :inner [{:component n/Text
             :children [label]
             :style [{:color "white"
                      :textAlign "right"
                      :fontSize 12
                      :padding 2
                      :paddingHorizontal 5
                      :borderRadius 3
                      :backgroundColor mainBackground}
                     (:.. (data/arrayify styleText))]
             :transformations
             {:focusing
              (fn [focusing]
                (return {:style {:color (:? (< 0.5 focusing)
                                            mainPrimary
                                            mainBackground)
                                 :backgroundColor
                                 (c/mix [mainError
                                         mainBackground]
                                        focusing)}}))}}]
    :style [{:flexDirection "row-reverse"
             :overflow "hidden"
             :marginHorizontal 2
             :height 0}
            (:.. (data/arrayify style))]
    :transformations
    {:highlighted (fn [highlighted]
                    (return {:style {:height (:? (< 0.3 highlighted)
                                                 (* 18 highlighted)
                                                 0)}}))}}))

(def.js MODULE (!:module))
