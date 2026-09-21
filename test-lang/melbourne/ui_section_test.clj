(ns melbourne.ui-section-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [melbourne.ui-section :as ui-section]
             [melbourne.ui-text :as ui-text]]
   :export [MODULE]})

^{:refer melbourne.ui-section/SectionBase :added "0.1"}
(fact "Constructs a Section"
  ^:hidden
  
  (defn.js SectionBaseDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-section/SectionBase"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-section/SectionBase
         {:title "Market"
          :design {:type "light"}}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-section/SectionBase
         {:title "Market"
          :design {:type "dark"}}]]]))))

^{:refer melbourne.ui-section/SectionSeparator :added "4.0"}
(fact "Constructs the section seperator")

^{:refer melbourne.ui-section/Section :added "0.1"}
(fact "Constructs a Section"
  ^:hidden
  
  (defn.js SectionDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-section/Section"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-section/Section
         {:title "Market"
          :design {:type "light"}}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-section/Section
         {:title "Market"
          :design {:type "dark"}}]]]))))

^{:refer melbourne.ui-section/SectionFold :added "0.1"}
(fact "Constructs a Section"
  ^:hidden
  
  (defn.js SectionFoldDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-section/SectionFold"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-section/SectionFold
         {:title "Market"
          :design {:type "light"}}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-section/SectionFold
         {:title "Market"
          :design {:type "dark"}}]]]))))

^{:refer melbourne.ui-section/CardBoundary :added "0.1"}
(fact "creates a card boundary"
  ^:hidden
  
  (defn.js CardBoundaryDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-section/CardBoundary"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-section/CardBoundary
         [:% n/View
          {:style {:backgroundColor "blue"}}]]]]))))

^{:refer melbourne.ui-section/EmptyButton :added "0.1"}
(fact "creates an empty button"
  ^:hidden
  
  (defn.js EmptyButtonDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-section/EmptyButton"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-section/EmptyButton
         {:design {:type "light"}}]]])))
  
  (def.js MODULE (!:module)))
