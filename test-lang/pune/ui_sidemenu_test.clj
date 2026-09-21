(ns pune.ui-sidemenu-test
  (:use code.test)
  (:require [lang.core :as l]
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
             [pune.ui-sidemenu :as ui-sidemenu]]
   :export [MODULE]})

^{:refer pune.ui-sidemenu/SideMenuTitle :added "0.1"}
(fact "creates a sidemenu title"
  ^:hidden
  
  (defn.js SideMenuTitleDemo
    []
    (var [routeKey setRouteKey] (r/local))
    (return
     (n/EnclosedCode 
{:label "pune.ui-sidemenu/SideMenuTitle"} 
[:% n/Row
       [:% n/View
        {:style {:padding 10
                 :height 100
                 :flexDirection "column-reverse"
                 :paddingRight 40
                 :backgroundColor "#eee"}}
        [:% ui-sidemenu/SideMenuTitle
         {:design {:type   "light"}
          :style {:height 100}
          :title "HELLO"}]]
       [:% n/View
        {:style {:padding 10
                 :height 100
                 :flexDirection "column-reverse"
                 :paddingRight 40
                 :backgroundColor "#333"}}
        [:% ui-sidemenu/SideMenuTitle
         {:design {:type   "dark"}
          :style {:height 100}
          :title "HELLO"}]]]))))

^{:refer pune.ui-sidemenu/SideMenuList :added "0.1"}
(fact "creates the side menu"
  ^:hidden
  
  (defn.js SideMenuListDemo
      []
      (var [routeKey setRouteKey] (r/local))
      (return
       (n/EnclosedCode 
{:label "pune.ui-sidemenu/SideMenuList"} 
[:% n/Row
         [:% n/View
          {:style {:padding 10
                   :paddingRight 40
                   :backgroundColor "#eee"}}
          [:% ui-sidemenu/SideMenuList
           {:design {:type   "light"}
            :data ["Security"
                   "Profile"
                   "Notifications"
                   "Organisation"]
            :routeKey routeKey
            :setRouteKey setRouteKey}]]
         [:% n/View
          {:style {:padding 10
                   :paddingRight 40
                   :backgroundColor "#333"}}
          [:% ui-sidemenu/SideMenuList
           {:design {:type   "dark"}
            :data ["Security"
                   "Profile"
                   "Notifications"
                   "Organisation"]
            :routeKey routeKey
            :setRouteKey setRouteKey}]]]))))

^{:refer pune.ui-sidemenu/SideMenuFloating :added "0.1"}
(fact "creates the side menu button"

  (defn.js SideMenuFloatingDemo
      []
      (var [routeKey setRouteKey] (r/local))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "pune.ui-sidemenu/SideMenuFloating"} 
[:% n/Row
        [:% n/View
         {:style {:flex 1
                  :flexDirection "row-reverse"
                  :padding 10
                  :paddingRight 40
                  :backgroundColor "#eee"}}
         [:% ui-sidemenu/SideMenuFloating
          {:design {:type   "light"}
           :data ["Security"
                  "Profile"
                  "Notifications"
                  "Organisation"]
           :routeKey routeKey
           :setRouteKey setRouteKey}]]
        [:% n/View
         {:style {:flex 1
                  :flexDirection "row-reverse"
                  :padding 10
                  :paddingRight 40
                  :backgroundColor "#333"}}
         [:% ui-sidemenu/SideMenuFloating
          {:design {:type   "dark"}
           :data ["Security"
                  "Profile"
                  "Notifications"
                  "Organisation"]
           :routeKey routeKey
           :setRouteKey setRouteKey}]]] 
[:% n/TextDisplay
        #{routeKey}])])))

^{:refer pune.ui-sidemenu/SideMenu :added "0.1"}
(fact "combination for both menu and list views"

  
  (def.js MODULE (!:module))
    
  )
