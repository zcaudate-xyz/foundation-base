(ns pune.ui-console
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-text :as ui-text]]
   :export [MODULE]})

(def.js ConsoleTabStyle
  {:padding 2
   :paddingHorizontal 15
   :fontSize 12
   :borderWidth 1
   :borderStyle "solid"})

(defn.js Console
  "creates the console"
  {:added "0.1"}
  [#{[design
      variant
      style
      screens
      current
      setCurrent
      onClose
      (:.. rprops)]}]
  (var data (xt/x:arr-sort (data/obj-keys screens) lib/identity (fn [x y] (return (x:str-lt x y)))))
  (var target (or (xt/x:get-key screens current)
                  (xt/x:get-key screens (data/first data))))
  (return
   [:% ui-static/Div
    {:design design
     :style [{:flex 1}
             (:.. (data/arrayify style))]}
    [:% ui-static/Div
     {:design design
      :style {:flexDirection "row"}
      :variant {:bg {:key "background"
                     :tone "sharpen"}}}
     [:% ui-text/ButtonAccent
      {:design design
       :style {:padding 2}
       :text "X"
       :onPress onClose}]
     [:% ui-text/TabsMinor
      #{[data
         :design design
         :style -/ConsoleTabStyle
         :value current
         :transformations {:bg nil}
         :setValue setCurrent]}]]
    (n/displayTarget target)]))

(def.js MODULE (!:module))
