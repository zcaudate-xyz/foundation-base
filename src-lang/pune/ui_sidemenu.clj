(ns pune.ui-sidemenu
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as xtd]
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

(defn.js SideMenuTitle
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
        {:style {:width 100}
         :variant {:font "h4"
                   :bg {:key "primary"
                        :mix "background"
                        :ratio 6}
                   :fg {:key "neutral"}}}}
      title]]]))

(defn.js SideMenuList
  "creates the side menu"
  {:added "0.1"}
  [#{[design
      mini
      miniTitle
      variant
      routeKey
      setRouteKey
      data
      children
      styleContainer
      floating
      (:.. rprops)]}]
  (var __design design #_(:? floating
                             (base-palette/invertDesign design)
                             ))
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
    {:style [{:width 140 #_(:? mini 160 140)
              :minHeight 80}
             (:.. (xtd/arrayify styleContainer))]}
    [:% ui-static/Div
     {:design __design
      :variant __variant
      :style {:paddingHorizontal 10
              :paddingVertical 5
              :flex 1}}
     [:% ui-group/List
      #{[:design __design
         :variant __variant
         :value routeKey
         :setValue setRouteKey
         :style {:width 120
                 :fontWeight "600"
                 :fontSize 12.5
                 :borderRadius 1}
         :format (fn:> [s] (string/capitalize (string/tag-string s)))
         :transformations {:bg nil}
         :data data
         (:.. rprops)]}]
     children]]))

(defn.js SideMenuFloating
  "constructs a background given design"
  {:added "0.1"}
  [#{design
     mini
     visible
     setVisible
     children}]
  (var palette (base-palette/designPalette design))
  
  (var buttonRef (r/ref))
  (return
   [:<>
    [:% ui-toggle-button/ToggleButton
     #{[:key mini
        :design design
        :variant (:? mini
                     {:bg {:key "neutral"}
                      :fg {:key "background"}
                      :pressing {:bg 1}
                      :active {:bg {:key "neutral"}}}
                     {:bg {:key "neutral"}
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
    [:% ui-tooltip/Tooltip
     {:hostRef buttonRef
      :visible visible
      :setVisible setVisible
      :position "right_edge"
      :alignment "start"
      :arrow {:animate true
              :placement "none"
              :backdrop true
              :backdropStyle {:backgroundColor (base-palette/getColor
                                                palette
                                                {:key "neutral"}) 
                              :opacity 0.4}}}
     children]]))

(defn.js SideMenu
  [#{[design
      mini
      miniTitle
      routeKey
      setRouteKey
      data
      narrowed
      children
      (:.. rprops)]}]
  (var [visible setVisible] (r/local false))
  (var listProps
       (Object.assign
        #{design
          routeKey
          setRouteKey
          data}
        (:? narrowed
            {:floating true
             :styleContainer {:padding 5
                              :borderRadius 3
                              :paddingLeft 10}
             :onChange (fn:> (setVisible false))})))
  (var listElem (r/% -/SideMenuList listProps))
  (return
   [:% n/View
    {:style [{:zIndex 100
              :top 5
              :position "absolute"}
             (n/PlatformSelect
              {:ios     {:left 5}
               :default {:right 5}})]}
    (:? narrowed
        (r/%
         -/SideMenuFloating
         #{design
           mini
           visible
           setVisible
           {:children (r/% -/SideMenuList listProps)}})
        (r/% -/SideMenuList listProps))]))

(def.js MODULE (!:module))

(comment
  (defn.js SideMenuFloating
  "constructs a background given design"
  {:added "0.1"}
  [#{[design
      mini
      miniTitle
      routeKey
      setRouteKey
      data
      listProps
      (:.. rprops)]}]
  (var palette (base-palette/designPalette design))
  (var [visible setVisible] (r/local false))
  (var buttonRef (r/ref))
  (return
   [:<>
    [:% ui-toggle-button/ToggleButton
     #{[:key mini
        :design design
        :variant (:? mini
                     {:bg {:key "primary"
                           :tone "flatten"}
                      :fg {:key "background"}
                      :pressing {:bg 1}
                      :active {:bg {:key "neutral"}}}
                     {:bg {:key "neutral"}
                      :fg {:key "background"}
                      :pressing {:bg 1}
                      :active {:bg {:key "neutral"}}})
        :refLink buttonRef
        :selected visible
        :onPress (fn:> (setVisible (not visible)))
        :transformations
        {:bg (fn:> [#{active}]
               {:style {:transform [{:scale (- 1 active)}]}})}
        :text [:% n/Icon
               {:key  "menu"
                :name "bars"
                :size 24}]
        (:.. rprops)]}]
    [:% ui-tooltip/Tooltip
     {:hostRef buttonRef
      :visible visible
      :setVisible setVisible
      :position "right_edge"
      :alignment "start"
      :arrow {:animate true
              :placement "none"
              :backdrop true
              :backdropStyle {:backgroundColor (base-palette/getColor
                                                palette
                                                {:key "neutral"}) 
                              :opacity 0.4}}}
     [:% -/SideMenuList
      #{[design
         mini
         miniTitle
         palette
         routeKey
         setRouteKey
         data
         :floating true
         :styleContainer {:padding 5
                          :borderRadius 3
                          :paddingLeft 10}
         :onChange (fn:> (setVisible false))
         (:.. listProps)]}]]]))
  )
