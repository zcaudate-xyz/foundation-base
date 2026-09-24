(ns melbourne.ui-color-input
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-input :as ui-input]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js ColorInput
  "constructs a themed input"
  {:added "0.1"}
  [#{[design
      variant
      theme
      styleContainer
      style
      value
      setValue
      (:.. rprops)]}]
  (var [currentText setCurrentText] (r/local value))
  (r/watch [value] (setCurrentText value))
  (return
   [:% n/Row
    {:style [{:alignItems "center"}
             styleContainer]}
    
    [:% ui-input/Input
     #{design
       {:value currentText
        :onSubmitEditing (fn []
                           (setValue currentText))
        :onChangeText setCurrentText
        :onBlur (fn:> (setCurrentText value))}}]
    (:? (data/not-empty? value)
        [:% n/View
         {:style {:backgroundColor value
                  :height 30
                  :width 30}}])]))

(def.js MODULE (!:module))
