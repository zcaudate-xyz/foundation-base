(ns pune.ui-menu-vert-test
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
             [pune.ui-menu-vert :as ui-menu-vert]]
   :export [MODULE]})

^{:refer pune.ui-menu-vert/styleMenuButton :added "0.1"}
(fact "creates the style for a menu button")

^{:refer pune.ui-menu-vert/MainMenuSeperator :added "0.1"}
(fact  "creates the main menu seperator"
  ^:hidden
  
  (defn.js MainMenuSeperatorDemo
    []
    (var [show setShow] (r/local true))
    (return
     (n/EnclosedCode 
{:label "pune.ui-menu-vert/MainMenuSeperator"} 
[:% n/Row
       [:% n/View
        {:style {:padding 20
                 :backgroundColor "#eee"}}
        [:% ui-menu-vert/MainMenuSeperator
         #{[:design {:type "light"
                    :mode "secondary"}]}]]
       [:% n/View
        {:style {:padding 20
                 :backgroundColor "#333"}}
        [:% ui-menu-vert/MainMenuSeperator
         #{[:design {:type "dark"
                    :mode "minor"}]}]]]))))

^{:refer pune.ui-menu-vert/MainMenuButton :added "0.1"}
(fact  "creates the main menu button"
  ^:hidden
  
  (defn.js MainMenuButtonDemo
    []
    (var [selected0 setSelected0] (r/local "home"))
    (var [selected1 setSelected1] (r/local "account"))
    (return
     (n/EnclosedCode 
{:label "pune.ui-menu-vert/MainMenuButton"} 
[:% n/Row
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#eee"}}
        [:% ui-menu-vert/MainMenuButton
         #{[:design {:type "light"}
            :item {:key "home"
                   :icon "home"
                   :label "HOME"}
            :onPress (fn:> (alert "PRESSED"))]}]]
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#333"}}
        [:% ui-menu-vert/MainMenuButton
         #{[:design {:type "dark"}
            :item {:key "home"
                   :icon "home"
                   :label "HOME"}
            :onPress (fn:> (alert "PRESSED"))]}]]]))))

^{:refer pune.ui-menu-vert/MainMenuToggle :added "0.1"}
(fact "creates the main menu toggle"
  ^:hidden
  
  (defn.js MainMenuToggleDemo
    []
    (var [selected0 setSelected0] (r/local true))
    (var [selected1 setSelected1] (r/local false))
    (return
     (n/EnclosedCode 
{:label "pune.ui-menu-vert/MainMenuToggle"} 
[:% n/Row
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#eee"}}
        [:% ui-menu-vert/MainMenuToggle
         #{[:design {:type "light"}
            :item {:key "home"
                   :icon "home"
                   :label "HOME"}
            :selected selected0 
            :onPress (fn:> (setSelected0 (not selected0)))]}]]
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#333"}}
        [:% ui-menu-vert/MainMenuToggle
         #{[:design {:type "dark"}
            :item {:key "home"
                   :icon "home"
                   :label "HOME"}
            :selected selected1
            :onPress (fn:> (setSelected1 (not selected1)))]}]]]))))

^{:refer pune.ui-menu-vert/MainMenuRoute :added "0.1"}
(fact "creates the main menu routes"
  ^:hidden
  
  (defn.js MainMenuRouteDemo
    []
    (var [routeKey0 setRouteKey0] (r/local "home"))
    (var [routeKey1 setRouteKey1] (r/local "account"))
    (return
     (n/EnclosedCode 
{:label "pune.ui-menu-vert/MainMenuRoute"} 
[:% n/Row
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#eee"}}
        [:% ui-menu-vert/MainMenuRoute
         #{[:design {:type "light"}
            :item {:key "home"
                   :icon "home"
                   :label "HOME"}
            :routeKey routeKey0 
            :setRouteKey setRouteKey0]}]
        [:% ui-menu-vert/MainMenuRoute
         #{[:design {:type "light"}
            :item {:key "account"
                   :icon "user"
                   :label "ACCOUNT"}
            :routeKey routeKey0 
            :setRouteKey setRouteKey0]}]]
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#333"}}
        [:% ui-menu-vert/MainMenuRoute
         #{[:design {:type "dark"}
            :item {:key "home"
                   :icon "home"
                   :label "HOME"}
            :routeKey routeKey1 
            :setRouteKey setRouteKey1]}]
        [:% ui-menu-vert/MainMenuRoute
         #{[:design {:type "dark"}
            :item {:key "account"
                   :icon "user"
                   :label "ACCOUNT"}
            :routeKey routeKey1 
            :setRouteKey setRouteKey1]}]]]))))

^{:refer pune.ui-menu-vert/MainMenuMiniContext :added "0.1"}
(fact "mini context for main menu"
  ^:hidden
  
  (defn.js MainMenuMiniContextDemo
    []
    (var [visible setVisible] (r/local false))
    (return
     (n/EnclosedCode 
{:label "pune.ui-menu-vert/MainMenuMiniContext"} 
[:% n/Row
       {:style {:paddingRight 90
                :backgroundColor "#eee"}}
       [:% ui-menu-vert/MainMenuMiniContext
        #{{:design {:type "dark"}}
          visible setVisible}
        [:% n/View
         [:% n/Button
          {:title "Link A"}]
         [:% n/Button
          {:title "Link B"}]
         [:% n/Button
          {:title "Link C"}]]]]))))

^{:refer pune.ui-menu-vert/MainMenu :added "0.1"}
(fact "creates the main menu "
  ^:hidden

  (defn.js MainMenuDemo
    []
    (var [routeKey0 setRouteKey0] (r/local "home"))
    (var [routeKey1 setRouteKey1] (r/local "user"))
    (return
     (n/EnclosedCode 
{:label "pune.ui-menu-vert/MainMenu"} 
[:% n/PortalSink
       [:% n/Row
        [:% n/View
         {:style {:paddingRight 90
                  :backgroundColor "#eee"}}
         [:% ui-menu-vert/MainMenu
          #{[:design {:type "light"}
             :items [{:key "home"
                      :icon "home"
                      :label "HOME"}
                     {:component ui-menu-vert/MainMenuSeperator}
                     {:key "account"
                      :icon "user"
                      :label "ACCOUNT"}
                     {:key "market"
                      :icon  "line-graph"
                      :label "MARKET"}]
             :routeKey routeKey0 
             :setRouteKey setRouteKey0]}]]
        [:% n/View
         {:style {:paddingRight 90
                  :backgroundColor "#333"}}
         [:% ui-menu-vert/MainMenu
          #{[:design {:type "dark"}
             :items [{:key "home"
                      :icon "home"
                      :label "HOME"}
                     {:component ui-menu-vert/MainMenuSeperator}
                     {:key "account"
                      :icon "user"
                      :label "ACCOUNT"}
                     {:key "market"
                      :icon  "line-graph"
                      :label "MARKET"}]
             :routeKey routeKey1
             :setRouteKey setRouteKey1]}]]]])))
  
  (def.js MODULE (!:module)))

(comment
  
  (def.js ICONS
    {rc/F01_HOME     "home"
     rc/F00_GUEST    "logout"
     rc/F02_ACCOUNT  "user"
     rc/F04_MARKET   "line-graph"
     rc/F06_GAME    "isv"
     rc/F08_DEBUG    "codesquareo"
     rc/F09_SCRATCH  "link"
     rc/F12_HARNESS  "dashboard"
     rc/F11_WIDGET   "windowso"}))
