(ns js.blessed.frame-sidemenu-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.lib.valtio :as v]
              [js.blessed.frame-sidemenu :as frame-sidemenu]
              [js.blessed.ui-core :as ui-core]
              [js.lib.chalk :as chk]]
   :export  [MODULE]})

^{:refer js.blessed.frame-sidemenu/SideButton :added "4.0" :unchecked true}
(fact "creates a primary frame-sidebutton button"
  ^:hidden
  
  (defn.js SideButtonDemo
    []
    (var [index setIndex] (r/local 1))
    (return
     [:% ui-core/Enclosed
      {:label "frame-sidemenu/SideButton"}
      [:box {:top 2}
       [:% frame-sidemenu/SideButton
        {:top 0
         :label "Hello"
         :index 1
         :setIndex setIndex
         :selected (== index 1)}]
       [:% frame-sidemenu/SideButton 
        {:top 2
         :label "World"
         :index 2
         :setIndex setIndex
         :selected (== index 2)}]]])))

^{:refer js.blessed.frame-sidemenu/SideMenu :added "4.0" :unchecked true}
(fact "creates a primary frame-sidemenu button"
  ^:hidden
  
  (defn.js SideMenuDemo
    []
    (var [index setIndex] (r/local 1))
    (return
     [:% ui-core/Enclosed
      {:label "frame-sidemenu/SideMenu"
       :height 25}
      [:box {:top 1}
       [:% frame-sidemenu/SideMenu
        #{index setIndex
          
          {:menuContent (fn:> [:box {:bg "blue"
                                     :content "MENU CONTENT"}])
           :menuFooter  (fn:> [:box {:bg "red"
                                     :content "MENU FOOTER"}])
           :label "MENU TITLE"
           :items [{:label "Hello"}
                   {:label "World"}
                   {:label "Again"}
                   {:label "Foo"}
                   {:label "Bar"}]}}]]]))

  (def.js MODULE (!:module)))
