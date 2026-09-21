(ns pune.ui-sidebar-test
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
             [pune.ui-sidebar :as ui-sidebar]
             [melbourne.ui-section :as ui-section]]
   :export [MODULE]})

^{:refer pune.ui-sidebar/SidebarMenu :added "0.1"}
(fact "creates a sidebar menu"
  ^:hidden
  
  (defn.js SidebarMenuDemo
    []
    (var [sectionKey setSectionKey] (r/local))
    (return
     (n/EnclosedCode 
{:label "pune.ui-sidebar/SidebarMenu"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% ui-sidebar/SidebarMenu
        #{sectionKey setSectionKey
          {:design {:type "dark"}
           :sections ["A" "B" "C"]}}]]))))

^{:refer pune.ui-sidebar/Sidebar :added "0.1"}
(fact "creates a sidebar"
  ^:hidden

  (defn.js SidebarDemo
    []
    (var [sectionKey setSectionKey] (r/local))
    (return
     (n/EnclosedCode 
{:label "pune.ui-sidebar/Sidebar"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% ui-sidebar/Sidebar
        #{sectionKey setSectionKey
          {:design {:type "dark"}
           :sections ["A" "B" "C"]}}]])))
  
  (def.js MODULE (!:module)))
