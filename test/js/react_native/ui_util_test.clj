(ns js.react-native.ui-util-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.ui-util :as ui-util]
             ]
   })

^{:refer js.react-native.ui-util/Page :added "4.0" :unchecked true}
(fact "creates a Page"
  ^:hidden
  
  (defn.js PageDemo
    []
    (return
     (n/EnclosedCode 
      {:label "js.react-native.ui-util/Page"
       :style {:height 200}} 
      [:% ui-util/Page
       {:headerComponent (fn:> [:% n/View {:style {:height 30
                                                   :backgroundColor "red"}}])
        :footerComponent (fn:> [:% n/View {:style {:height 30
                                                   :backgroundColor "orange"}}])
        :styleMenu {:height 60}
        :titleComponent  (fn:> [:% n/View {:style {:flex 1
                                                   :backgroundColor "yellow"}}])
        
        :leftComponent   (fn:> [:% n/View {:style {:flex 1
                                                   :backgroundColor "green"}}])
        
        :rightComponent  (fn:> [:% n/View {:style {:flex 1
                                                   :backgroundColor "blue"}}])}
       [:% n/View
        {:style {:flex 1
                 :backgroundColor "black"}}]]))))

^{:refer js.react-native.ui-util/Fade :added "4.0" :unchecked true}
(fact "creates a Fade"
  ^:hidden
  
  (defn.js FadeDemo
    []
    (var [visible setVisible] (r/local true))
    (var [size setSize] (r/local 100))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-util/Fade"} 
[:% n/Row
       [:% n/Button
        {:title "V"
         :onPress (fn:> (setVisible (not visible)))}]] 
[:% ui-util/Fade
       {:visible visible}
       [:% n/View
        {:style {:height size
                 :width 100
                 :backgroundColor "red"}}]]))))

^{:refer js.react-native.ui-util/FadeIn :added "4.0" :unchecked true}
(fact "creates a fade in helper")

^{:refer js.react-native.ui-util/useFoldContent :added "4.0" :unchecked true}
(fact "creates the fold inner helper")

^{:refer js.react-native.ui-util/FoldInner :added "4.0" :unchecked true}
(fact "creates the fold inner container"
  ^:hidden
  
  (defn.js FoldInnerDemo
    []
    (var [visible setVisible] (r/local true))
    (var [size setSize] (r/local 100))
    (var vindicator (a/useBinaryIndicator visible))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-util/FoldInner"} 
[:% n/Row
       [:% n/Button
        {:title "V"
         :onPress (fn:> (setVisible (not visible)))}]
       [:% n/Tabs
        {:value size
         :setValue setSize
         :data [100 200]}]] 
[:% n/Row
       {:style {:height 100}}
       [:% ui-util/FoldInner
        {:aspect "width"
         :visible visible
         :chord {:visible visible}
         :indicators {:visible vindicator}}
        [:% n/View
         {:style {:height 100
                  :width size
                  :backgroundColor "red"}}]]]))))

^{:refer js.react-native.ui-util/FoldImpl :added "4.0" :unchecked true}
(fact "creates the transitioning fold")

^{:refer js.react-native.ui-util/Fold :added "4.0" :unchecked true}
(fact "creates the fold"
  ^:hidden
  
  (defn.js FoldDemo
    []
    (var [visible setVisible] (r/local true))
    (var [size setSize] (r/local 100))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-util/Fold"} 
[:% n/Row
       [:% n/Button
        {:title "V"
         :onPress (fn:> (setVisible (not visible)))}]
       [:% n/Tabs
        {:value size
         :setValue setSize
         :data [100 200]}]] 
[:% n/Row
       {:style {:height 100}}
       [:% ui-util/Fold
        {:visible visible}
        [:% n/View
         {:style {:height size
                  :width 100
                  :backgroundColor "green"}}]]
       [:% ui-util/Fold
        {:visible visible
         :aspect "width"}
        [:% n/View
         {:style {:height 100
                  :width size
                  :backgroundColor "blue"}}]]])))
  
  

  )

(comment

  
  
  
  )
