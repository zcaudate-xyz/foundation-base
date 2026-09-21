(ns pune.ui-page-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-page
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [melbourne.base-palette :as base-palette]
             [pune.ui-page :as ui-page]]
   :export [MODULE]})

^{:refer pune.ui-page/PageLayoutHeader :added "0.1"}
(fact "constructs the page header"
  ^:hidden
  
  (defn.js PageLayoutHeaderDemo
    []
    (var [routeKey setRouteKey] (r/local "world"))
    (var routeRoot ["HELLO"])
    (var headerId (r/id))
    (var palette (base-palette/designPalette {:type "light"}))
    (return
     (n/EnclosedCode 
{:label "pune.ui-page/PageLayoutHeader"
       :style {:height 100}} 
[:% n/Row
       [:% n/View
        {:style {:width 300}}
        [:% ui-page/PageLayoutHeader
         #{palette
           headerId
           routeKey
           routeRoot}]]
       [:% n/View
        {:style {:width 300}}
        [:% ui-page/PageLayoutHeader
         #{{:design {:type "dark"}
            :palette (base-palette/designPalette {:type "dark"})}
           routeKey
           routeRoot}]]] 
[:% n/Portal
       {:target headerId}
       [:% n/Text "TOOLBAR"]]))))

^{:refer pune.ui-page/PageLayoutMenu :added "0.1"}
(fact "constructs the page menu"
  ^:hidden
  
  (defn.js PageLayoutMenuDemo
    []
    (var [routeKey setRouteKey] (r/local "world"))
    (var appendId (r/id))
    (var palette (base-palette/designPalette {:type "light"}))
    (return
     (n/EnclosedCode 
{:label "pune.ui-page/PageLayoutMenu"
       :style {:height 200}} 
[:% n/Row
       [:% n/View
        {:style {:width 300}}
        [:% ui-page/PageLayoutMenu
         #{
           palette
           routeKey
           setRouteKey
           {:design {:type "light"}
            :sections ["hello" "world"]}
           appendId}]]
       [:% n/View
        {:style {:width 300}}
        [:% ui-page/PageLayoutMenu
         #{routeKey
           setRouteKey
           {:design {:type "dark"}
            :palette (base-palette/designPalette {:type "dark"})
            :sections ["hello" "world"]}}]]]))))

^{:refer pune.ui-page/PageLayout :added "0.1"}
(fact "constructs the page layout"
  ^:hidden
  
  (defn.js PageLayoutDemo
    []
    (var [routeKey setRouteKey] (r/local "world"))
    (var routeRoot ["HELLO"])
    (var Id (r/id))
    
    (return
     (n/EnclosedCode 
{:label "pune.ui-page/PageLayout"
       :style {:height 200}} 
[:% n/Row
       [:% n/View
        {:style {:width 300}}
        [:% ui-page/PageLayout
         #{routeKey setRouteKey
           {:design {:type "light"}
            :sections ["hello" "world"]
            :sectionRoutes {:hello (fn:> [:% n/Text "HELLO"])
                            :world (fn:> [:% n/Text "WORLD"])}
            :routeRoot ["HOME"]}}]]
       [:% n/View
        {:style {:width 300}}
        [:% ui-page/PageLayout
         #{routeKey setRouteKey
           {:design {:type "dark"}
            :sections ["hello" "world"]
            :sectionRoutes {:hello (fn:> [:% n/Text "HELLO"])
                            :world (fn:> [:% n/Text "WORLD"])}
            :routeRoot ["HOME"]}}]]]))))


^{:refer pune.ui-page/PageLayoutSingle :added "0.1"}
(fact "creates a smaller scale layout for single view"

  
  (def.js MODULE (!:module)))

