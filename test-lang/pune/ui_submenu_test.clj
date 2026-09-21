(ns pune.ui-submenu-test
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
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [pune.ui-submenu :as ui-submenu]]
   :export [MODULE]})

^{:refer pune.ui-submenu/SubMenuToggle :added "0.1"}
(fact "creates the sub menu toggle"
  ^:hidden
  
  (defn.js SubMenuToggleDemo
    []
    (var [selected0 setSelected0] (r/local true))
    (var [selected1 setSelected1] (r/local false))
    (return
     (n/EnclosedCode 
{:label "pune.ui-submenu/SubMenuToggle"} 
[:% n/Row
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#eee"}}
        [:% ui-submenu/SubMenuToggle
         #{[:design {:type "light"}
            :item {:key "info"
                   :icon "appstore-o"
                   :label "INFO"}
            :selected selected0 
            :onPress (fn:> (setSelected0 (not selected0)))]}]]
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#333"}}
        [:% ui-submenu/SubMenuToggle
         #{[:design {:type "dark"}
            :item {:key "info"
                   :icon "appstore-o"
                   :label "INFO"}
            :selected selected1
            :onPress (fn:> (setSelected1 (not selected1)))]}]]]))))

^{:refer pune.ui-submenu/SubMenuRoute :added "0.1"}
(fact "creates the sub menu routes"
  ^:hidden
  
  (defn.js SubMenuRouteDemo
    []
    (var [routeKey0 setRouteKey0] (r/local "home"))
    (var [routeKey1 setRouteKey1] (r/local "account"))
    (return
     (n/EnclosedCode 
{:label "pune.ui-submenu/SubMenuRoute"} 
[:% n/Row
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#eee"}}
        [:% ui-submenu/SubMenuRoute
         #{[:design {:type "light"}
            :item {:key "info"
                   :icon "appstore-o"
                   :label "INFO"}
            :routeKey routeKey0 
            :setRouteKey setRouteKey0]}]
        [:% ui-submenu/SubMenuRoute
         #{[:design {:type "light"}
            :item {:key  "stats"
                   :icon "dashboard"
                   :label "STC"}
            :routeKey routeKey0 
            :setRouteKey setRouteKey0]}]]
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#333"}}
        [:% ui-submenu/SubMenuRoute
         #{[:design {:type "dark"}
            :item {:key "info"
                   :icon "appstore-o"
                   :label "INFO"}
            :routeKey routeKey1 
            :setRouteKey setRouteKey1]}]
        [:% ui-submenu/SubMenuRoute
         #{[:design {:type "dark"}
            :item {:key  "stats"
                   :icon "dashboard"
                   :label "STC"}
            :routeKey routeKey1 
            :setRouteKey setRouteKey1]}]]]))))

^{:refer pune.ui-submenu/SubMenu :added "0.1"}
(fact "creates the sub menu "
  ^:hidden
  
  (defn.js SubMenuDemo
    []
    (var [routeKey0 setRouteKey0] (r/local "home"))
    (var [routeKey1 setRouteKey1] (r/local "user"))
    (return
     (n/EnclosedCode 
{:label "pune.ui-submenu/SubMenu"} 
[:% n/PortalSink
       [:% n/Row
        [:% n/View
         {:style {:paddingRight 90
                  :backgroundColor "#eee"}}
         [:% ui-submenu/SubMenu
          #{[:design {:type "light"}
             :items [{:key "info"
                      :icon "appstore-o"
                      :label "INFO"}
                     {:key  "stats"
                      :icon "dashboard"
                      :label "STC"}]
             :routeKey routeKey0 
             :setRouteKey setRouteKey0]}]]
        [:% n/View
         {:style {:paddingRight 90
                  :backgroundColor "#333"}}
         [:% ui-submenu/SubMenu
          #{[:design {:type "dark"}
             :items [{:key "info"
                      :icon "appstore-o"
                      :label "INFO"}
                     {:key  "stats"
                      :icon "dashboard"
                      :label "STC"}]
             :routeKey routeKey1
             :setRouteKey setRouteKey1]}]]]])))
  
  (def.js MODULE (!:module)))
