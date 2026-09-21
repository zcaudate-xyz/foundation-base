(ns pune.ui-menu-top
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [js.react-native.ui-tooltip :as ui-tooltip]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-group :as ui-group]
             [melbourne.ui-toggle-button :as ui-toggle-button]
             [melbourne.ui-static :as ui-static]]
   :export [MODULE]})

(defn.js TopMenuTitle
  [#{design
     title}]
  (return
   [:% ui-static/Div
    #{design
      {:variant {:bg {:key "primary"
                      :mix "background"
                      :ratio 6}}
       :style {:width 40
               :flexDirection "column-reverse"}}}
    [:% n/View
     {:style {:transform [{:rotate "-90deg"}
                          {:translateX 20}]}}
     [:% ui-static/Text
      #{design
        {:style {:width 10}
         :variant {:font "h4"
                   :bg {:key "primary"
                        :mix "background"
                        :ratio 6}
                   :fg {:key "neutral"}}}}
      title]]]))

(defn.js TopMenuList
  "creates the top menu"
  {:added "0.1"}
  [#{[design
      mini
      miniTitle
      variant
      routeKey
      setRouteKey
      data
      header
      children
      styleContainer
      floating
      (:.. rprops)]}]
  (var __design design)
  (var __variant (Object.assign
                  {:font "h4"
                   :bg {:key "background"
                        :tone "diminish"}
                   :fg {:key "neutral"
                        :tone "diminish"}
                   :pressed {:bg  {:raw 1}
                             :fg {:raw 1}}
                   :hovered {:bg {:raw 1}
                             :fg {:raw 1}}
                   :active {:bg {:key "primary"}}}
                  variant))
  
  (return
   [:% n/Row
    {:style [{:width 160 #_(:? mini 160 140)
              :minHeight 80}
             (:.. (data/arrayify styleContainer))]}
    [:% ui-static/Div
     {:design __design
      :variant __variant
      :style {:paddingHorizontal 10
              :paddingVertical 5
              :flex 1}}
     header
     [:% ui-group/List
      #{[:design __design
         :variant __variant
         :value routeKey
         :setValue setRouteKey
         :allowNotFound true
         :style {:width 120
                 :fontWeight "900"
                 :fontSize 12.5
                 :borderRadius 1}
         :format (fn:> [s] (string/capitalize (string/tag-string s)))
         :transformations {:bg nil}
         :data data
         (:.. rprops)]}]
     children]]))

(defn.js TopMenuFloating
  "constructs a background given design"
  {:added "0.1"}
  [#{design
     mini
     visible
     setVisible
     children
     tooltip}]
  (var palette (base-palette/designPalette design))
  
  (var buttonRef (r/ref))
  (return
   [:<>
    [:% ui-toggle-button/ToggleButton
     #{[:key mini
        :design design
        :variant (:? mini
                     {:bg {:key "neutral"
                           :mix "primary"
                           :ratio 2}
                      :fg {:key "background"}
                      :pressing {:bg 1}
                      :active {:bg {:key "neutral"}}}
                     {:bg {:key "neutral"
                           :mix "primary"
                           :ratio 2}
                      :fg {:key "background"}
                      :pressing {:bg 1}
                      :active {:bg {:key "neutral"}}})
        :style {:paddingVertical 6
                :borderRadius 2}
        :refLink buttonRef
        :selected visible
        :onPress (fn:> (setVisible (not visible)))
        :transformations
        {:bg (fn:> [#{active}]
               {:style {:transform [{:scale (- 1 active)}]}})}
        :text [:% n/Icon
               {:key  "menu"
                :name "menu"
                :size 18}]]}]
    
    (r/% ui-tooltip/Tooltip
         (Object.assign
          {:hostRef buttonRef
           :visible visible
           :setVisible setVisible
           :position "right_edge"
           :alignment "start"
           :children children
           :arrow {:animate true
                   :placement "none"
                   :backdrop true
                   :backdropStyle {:backgroundColor (base-palette/getColor
                                                     palette
                                                     {:key "neutral"}) 
                                   :opacity 0.4}}}
          tooltip))]))

(defn.js TopMenu
  [#{[design
      mini
      miniTitle
      tooltip
      routeKey
      setRouteKey
      data
      format
      narrowed
      header
      children
      visible
      setVisible
      (:.. rprops)]}]
  (var listProps
       (Object.assign
        #{design
          routeKey
          setRouteKey
          data
          format
          header
          children}
        {:floating true
         :styleContainer {:padding 5
                          :borderRadius 3
                          :paddingLeft 10
                          :overflow "hidden"}
         :onChange (fn [s]
                     (setVisible false))}))
  (var listElem (r/% -/TopMenuList listProps))
  (return
   (r/% -/TopMenuFloating
    #{design
      mini
      tooltip
      visible
      setVisible
      {:children
       (r/% -/TopMenuList listProps)}})))

(def.js MODULE (!:module))


