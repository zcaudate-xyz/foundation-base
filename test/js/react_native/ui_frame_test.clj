(ns js.react-native.ui-frame-test
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
             [js.react-native.ui-frame :as ui-frame]
             ]
   :export [MODULE]})

^{:refer js.react-native.ui-frame/translationOffset :added "4.0" :unchecked true}
(fact "creates the offset translation")

^{:refer js.react-native.ui-frame/FramePane :added "4.0" :unchecked true}
(fact "creates a Frame Pane"
  ^:hidden
  
  (defn.js FramePaneDemo
    []
    (var [visible setVisible] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-frame/FramePane"} 
[:% n/Row
       {:style {:paddingBottom 10}}
       [:% n/Button
        {:title "T"
         :onPress (fn:> (setVisible (not visible)))}]
       [:% ui-frame/FramePane
        {:location "top"
         :visible visible
         :fade false}
        [:% n/Text {:style {:backgroundColor "red"
                            :width 100}}
         "HELLO"]]] 
[:% n/Caption
       {:text (n/format-obj #{visible})
        :style {:marginTop 10}}]))))

^{:refer js.react-native.ui-frame/Frame :added "4.0" :unchecked true}
(fact "creates a Frame"
  ^:hidden
  
  (defn.js FrameDemo
    []
    (var [topVisible setTopVisible] (r/local true))
    (var [bottomVisible setBottomVisible] (r/local true))
    (var [rightVisible setRightVisible] (r/local true))
    (var [leftVisible setLeftVisible] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-frame/Frame"
       :style {:height 350}} 
[:% n/Row
       {:style {:paddingBottom 10}}
       [:% n/Button
        {:title "T"
         :onPress (fn:> (setTopVisible (not topVisible)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "B"
         :onPress (fn:> (setBottomVisible (not bottomVisible)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "L"
         :onPress (fn:> (setLeftVisible (not leftVisible)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "R"
         :onPress (fn:> (setRightVisible (not rightVisible)))}]] 
[:% ui-frame/Frame
       #{[topVisible
          bottomVisible
          leftVisible
          rightVisible
          :brand {:type "dark"}
          :topSize 50
          :topComponent n/View
          :topProps {:style {:flex 1
                             :backgroundColor "yellow"}
                     :children [[:% n/Text {:key "text"}
                                 "TOP"]]}
          :bottomSize 30
          :bottomComponent (fn []
                             (return [:% n/View {:style {:flex 1
                                                         :backgroundColor "magenta"}}
                                      [:% n/Text "BOTTOM"]]))
          :leftSize 100
          :leftComponent (fn []
                           (return [:% n/View {:style {:flex 1
                                                       :backgroundColor "cyan"}}
                                    [:% n/Text "LEFT"]]))
          :rightSize 200
          :rightComponent (fn:> []
                                (return [:% n/View {:style {:flex 1
                                                            :backgroundColor "lightgreen"}}
                                         [:% n/Text "RIGHT"]]))
          :rightIndicatorParams 
          {:default {:type "timing"
                     :duration 1000
                     :easing a/linear}}]}
       [:% n/View
        {:style {:padding 10}}
        [:% n/Text "BODY"]]] 
[:% n/TextDisplay
       {:style {:flex nil
                :height 80}
        :content (n/format-entry #{topVisible
                                   bottomVisible
                                   leftVisible
                                   rightVisible})}])))
  
  (def.js MODULE (!:module))
  
  )
