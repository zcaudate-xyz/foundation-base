(ns js.react-native.ui-check-box-test
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
             [js.react-native.physical-addon :as physical-addon]
             [js.react-native.ui-check-box :as ui-check-box]
             [js.react-native.physical-addon :as physical-addon]
             ]
   })

^{:refer js.react-native.ui-check-box/CheckBoxSimple
  :adopt true
  :added "0.1"}
(fact "creates a slim checkbox"
  ^:hidden
  
  (defn.js CheckBoxSimpleDemo
    []
    (var [first setFirst]   (r/local true))
    (var [highlighted setHighlighted]   (r/local true))
    (var [disabled setDisabled]   (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-check-box/CheckBoxSimple"} 
[:% n/Row
       {:style {:alignItems "center"
                #_#_:margin 3}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "CHECKBOX"]
       [:% ui-check-box/CheckBox
        {:highlighted highlighted
         :disabled disabled
         
         :theme {:fgActive   "limegreen"
                 :fgHovered  0.9
                 :bgActive   "green"
                 :bgPressed  "palegreen"
                 :bgNormal "black"}
         :selected first
         :setSelected setFirst
         :outlined true
         :addons [(physical-addon/tagAll
                   {:style {:paddingHorizontal 20
                            :height 80
                            :flex 1}})]}]] 
[:% n/Row
       [:% n/Button
        {:title "H"
         :onPress (fn:> (setHighlighted (not highlighted)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn:> (setDisabled (not disabled)))}]]))))

^{:refer js.react-native.ui-check-box/checkBoxTheme :added "4.0" :unchecked true}
(fact "creates the checkbox theme")

^{:refer js.react-native.ui-check-box/CheckBox :added "0.1"}
(fact "creates a slim checkbox"
  ^:hidden
  
  (defn.js CheckBoxDemo
    []
    (var [first setFirst]   (r/local true))
    (var [second setSecond] (r/local true))
    (var [highlighted setHighlighted] (r/local true))
    (var [errored setErrored] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-check-box/CheckBox"} 
[:% n/Row
       {:style {:alignItems "center"
                #_#_:margin 3}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "CHECKBOX"]
       [:% ui-check-box/CheckBox
        {:theme {:fgSelected "limegreen"}
         :selected first
         :setSelected setFirst
         :outlined true
         }]
       [:% n/Padding {:style {:width 10}}]
       [:% ui-check-box/CheckBox
        {:theme {:fgSelected "white"
                 :fgNormal "white"
                 :bgNormal  "darkblue"
                 :bgHovered "blue"
                 :bgPressed "purple"}
         :selected second
         :setSelected setSecond
         :outlined true}]
       
       [:% n/Padding {:style {:width 10}}]
       [:% ui-check-box/CheckBox
        {:disabled true
         :selected false
         :outlined true}]
       [:% n/Padding {:style {:width 10}}]
       
       [:% ui-check-box/CheckBox
        {:highlighted highlighted
         :setSelected setHighlighted
         :selected highlighted
         :outlined true}]
       [:% n/Padding {:style {:width 10}}]
       [:% ui-check-box/CheckBox
        {:highlighted errored
         :setSelected setErrored
         :selected errored
         :theme {:fgHighlighted "white"
                 :bgHighlighted "red"}
         :outlined true}]])))

  
  
  )
