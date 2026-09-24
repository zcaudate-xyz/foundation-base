(ns pune.ui-menu-vert
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.ui-tooltip :as ui-tooltip]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-toggle-button :as ui-toggle-button]
             [melbourne.ui-static :as ui-static]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js styleMenuButton
  [mini]
  (return
   {:padding 3
    :margin 5
    :marginVertical (:? (not mini) 8)}))

(defn.js MainMenuSeperator
  "creates the main menu seperator"
  {:added "0.1"}
  [#{design
     mini}]
  (return
   [:% ui-static/Separator
    {:design design
     :variant {:fg {:key  "neutral"
                    :tone "sharpen"}}
     :style (:? mini
                {:height 32 :marginHorizontal 15 :marginVertical 12 :width 1}
                {:height 1 :marginHorizontal 12 :marginVertical 10 :width 32})}]))

(defn.js MainMenuButton
  "creates the main menu button"
  {:added "0.1"}
  [#{[design
      mini
      item
      (:.. rprops)]}]
  (var #{[label icon key
          (:.. ritems)]} item)
  (return
   [:% ui-button/Button
    #{[:design design
       :variant {:bg {:key "background"
                      :tone (:? mini "sharpen" "diminish")}
                 :fg {:key "neutral"}
                 :hovered {:bg {:key "background"
                                :mix "neutral"
                                :ratio 2}}
                 :pressed {:bg {:key "neutral"}
                           :fg {:key "background"}}
                 :tooltip {:fg {:key "background"}
                           :bg {:key "neutral"}}} 
       :style (-/styleMenuButton mini)
       :text [:% n/Icon
              {:key "icon"
               :name icon
               :size 24}]
       :tooltip {:text  label
                 :position "top"}
       (:.. (Object.assign rprops ritems))]}]))

(defn.js MainMenuToggle
  "creates the main menu toggle"
  {:added "0.1"}
  [#{[design
      mini
      variant
      item
      (:.. rprops)]}]
  (var #{[label icon key
          (:.. ritems)]} item)
  (return
   [:% ui-toggle-button/ToggleButton
    #{[:design design
       :variant (Object.assign
                 {:bg {:key "background"
                       :tone (:? mini "sharpen" "diminish")}
                  :active  {:bg {:key "neutral"}}
                  :hovered {:bg {:raw 1}
                            :fg {:raw 1}}
                  :pressed {:bg {:raw 1}
                            :fg {:raw 1}}}
                 variant)
       :style (-/styleMenuButton mini)
       :text [:% n/Icon
              {:key "icon"
               :name icon
               :size 24}]
       :tooltip {:text  label
                 :position "top"}
       (:.. (Object.assign rprops ritems))]}]))

(defn.js MainMenuRoute
  "creates the main menu routes"
  {:added "0.1"}
  ([#{[design
       mini
       theme
       item
       routeKey
       setRouteKey
       setVisible]}]
   (var #{key} item)
   (return
    [:% -/MainMenuToggle
     #{theme mini
       {:design design
        :variant {:active  {:bg {:key "primary"}}}
        :item (Object.assign
               {:selected (== routeKey key)
                :onPress (fn []
                           (setRouteKey key)
                           (when setVisible
                             (setVisible false)))}
               item)}}])))

(defn.js MainMenuMiniContext
  "creates the main menu routes"
  {:added "0.1"}
  ([#{[design
       variant
       visible
       setVisible
       children]}]
   (var hostRef (r/ref))
   (var palette (base-palette/designPalette design))
   (return
    [:<>
     [:% -/MainMenuToggle
      #{design variant 
        {:refLink hostRef
         :mini true
         :selected visible
         :onPress (fn:> (setVisible (not visible)))
         :item {:key  "more"
                :icon "dots-three-horizontal"}}}]
     [:% ui-tooltip/Tooltip
      #{[:hostRef hostRef
         :visible visible
         :setVisible setVisible
         :position "top"
         :alignment "end"
         :arrow {:placement "none"
                 :backdrop true
                 :baseHeight 5
                 :baseLength 20
                 :backdropStyle {:backgroundColor (base-palette/getColor
                                                   palette
                                                   {:key "background"})
                                 :opacity 0.3}}]}
      [:% n/View
       {:style {:width 60}}
       children]]])))

(defn.js MainMenu
  "creates the main menu"
  {:added "0.1"}
  [#{[mini
      routeKey
      setRouteKey
      (:= design {})
      (:= items [])]}]
  (:= items (j/filter items lib/identity))
  (var [visible setVisible] (r/local false))
  (var itemFn
       (fn [mini]
         (return (fn [item i]
                   (var #{[(:= component -/MainMenuRoute)]} item)
                   (return
                    (r/% component
                         #{design
                           routeKey
                           setRouteKey
                           item
                           setVisible
                           mini
                           {:key i}}))))))
  (cond mini
        (return
         [:% ui-static/Div
          {:design design
           :variant {:bg {:key "background"
                          :tone "sharpen"}}
           :style [{:padding 2
                    :flex 1
                    :overflow "hidden"
                    :flexDirection "row"
                    :justifyContent "space-between"}]}
          (-> items
              (j/filter (data/key-fn "mini"))
              (j/map    (itemFn true)))
          [:% -/MainMenuMiniContext
           #{design visible setVisible}
           [:% ui-static/Div
            {:design design
             :variant {:bg {:key "background"
                            :tone "sharpen"}}
             :style [{:padding 2}]}
            (j/map (j/filter items
                             (fn:> [e]
                               (and (not (. e mini))
                                    (. e key))))
                   (itemFn true))]]])
        

        :else
        (return
         [:% ui-static/Div
          {:design design
           :variant {:bg {:key "background"
                          :tone "diminish"}}
           :style [{:padding 2
                    :flex 1
                    :overflow "hidden"}]}
          (j/map items (itemFn mini))])))

(def.js MODULE (!:module))

