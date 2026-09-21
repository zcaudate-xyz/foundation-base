(ns melbourne.ui-section
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
             [js.react-native.ui-util :as ui-util]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-text :as ui-text]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

;;
;; Section
;;

(def.js styleSection
  {:borderRadius 3
   :marginHorizontal 5
   :marginVertical 5
   :paddingVertical 5
   :paddingHorizontal 15
   :minWidth 260
   :maxWidth 500
   :justifyItems "center"})

(defn.js SectionBase
  "Constructs a Section"
  {:added "0.1"}
  [#{[design
      variant
      mini
      style
      styleTitle
      title
      action
      children]}]
  (var Text (:? mini
                ui-text/H4
                ui-text/H3))
  (return
   [:% ui-static/Div
    {:design design
     :variant (Object.assign
               {}
               #_{:bg {:key "background"
                     :tone "augment"}}
               variant)
     :style [-/styleSection
             (:.. (data/arrayify style))
             (:? mini {:padding 4})]}
    [:% n/Row
     {:style {:marginBottom 5
              :alignItems "center"
              :alignContent "center"}}
     [:% Text
      #{design
        {:style [{:fontFamily "impact"}
                 (:.. (data/arrayify styleTitle))]
         :variant {:fg {:key "neutral"
                        }}
         :children title}}]
     [:% n/Padding {:style {:flex 1}}]
     action]
    children]))

(defn.js SectionSeparator
  "Constructs the section seperator"
  {:added "4.0"}
  [#{design
     variant}]
  (return
   [:% ui-static/Separator
    {:design design
     :variant (Object.assign
               {:fg {:key "background"
                     :mix "neutral"
                     :ratio 1}}
               variant)
     :style [{:marginVertical 3}]}]))

(defn.js Section
  "Constructs a Section"
  {:added "0.1"}
  [#{[design
      variant
      mini
      title
      action
      children]}]
  (return [:% -/SectionBase
           #{design variant mini title action}
           children
           [:% n/View {:style {:height 10}}]
           [:% -/SectionSeparator
            #{design}]]))

(defn.js SectionFold
  "Constructs a Section"
  {:added "0.1"}
  [#{[design
      variant
      mini
      title
      action
      children
      (:= visible false)]}]
  (return
   [:% -/SectionBase
    #{design variant mini title action}
    [:% ui-util/Fold
     {:visible visible}
     [:% n/View
      {:style (:? mini
                  {:paddingVertical 5})}
      children]]
    [:% n/View {:style {:height 10}}]
    [:% -/SectionSeparator
       #{design}]]))

(defn.js CardBoundary
  "creates a card boundary"
  {:added "0.1"}
  [#{style
     children}]
  (return
   [:% n/View
    {:style [{:marginHorizontal 10
              :marginBottom 0
              :maxWidth 500}
             style]}
    children]))

(defn.js EmptyButton
  "creates an empty button"
  {:added "0.1"}
  [#{design
     textHeader
     textButton
     onPress}]
  (return
   [:<>
    (:? (data/not-empty? textButton)
        [:% ui-text/ButtonMinor
         #{onPress
           design
           {:text textButton
            :style {:textAlign "center"
                    :padding 10
                    :paddingHorizontal 50}}}])]))

(def.js MODULE (!:module))
