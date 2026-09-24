(ns pune.ui-submenu
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
             [melbourne.ui-toggle-button :as ui-toggle-button]
             [melbourne.ui-static :as ui-static]
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

(defn.js SubMenuToggle
  "creates the main menu toggle"
  {:added "0.1"}
  [#{[design
      mini
      variant
      item
      (:.. rprops)]}]
  (var #{[label icon key
          (:.. ritems)]} item)
  (var [value setValue] (r/local "STATS"))
  (return
   [:% ui-text/TabsMinor
    {:design {:type "dark"}
     :styleContainer {:flexDirection "column"}
     :data ["STATS" "XLM" "USD"]
     :value value
     :setValue setValue}]
   #_[:% ui-toggle-button/ToggleButton
    #{[:design design
       :variant (Object.assign
                 {:bg {:key "background"
                       #_#_:tone (:? mini "sharpen" "diminish")}
                  :active  {:bg {:key "background"}}
                  :hovered {:bg {:raw 1}
                            :fg {:raw 1}}
                  :pressed {:bg {:raw 1}
                            :fg {:raw 1}}}
                 variant)
       :style {:paddingVertical 3
               :paddingHorizontal 8
               :margin 5
               :marginVertical 4
               }
       :text [:% n/Icon
              {:key "icon"
               :name icon
               :size 15}]
       :tooltip {:text  label}
       (:.. (Object.assign rprops ritems))]}]))

(defn.js SubMenuRoute
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
    [:% -/SubMenuToggle
     #{theme mini
       {:design design
        :variant {:active  {:bg {:key "background"}
                            :fg {:key "neutral"}}}
        :item (Object.assign
               {:selected (== routeKey key)
                :onPress (fn []
                           (setRouteKey key)
                           (when setVisible
                             (setVisible false)))}
               item)}}])))

(defn.js SubMenu
  "creates the main menu"
  {:added "0.1"}
  [#{[mini
      routeKey
      setRouteKey
      (:= design {})
      (:= items [])]}]
  (var [visible setVisible] (r/local false))
  (var itemFn
       (fn [mini]
         (return (fn [item i]
                   (var #{[(:= component -/SubMenuRoute)]} item)
                   (return
                    (r/% component
                         #{design
                           routeKey
                           setRouteKey
                           item
                           setVisible
                           mini
                           {:key i}}))))))
  (return
   [:% ui-static/Div
    {:design design
     :variant {:bg {:key "background"}}
     :style [{:margin 4
              :overflow "hidden"}]}
    (j/map items (itemFn mini))]))

(def.js MODULE (!:module))

