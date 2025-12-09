(ns js.react-native.ui-radio-box-test
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
             [js.react-native.ui-radio-box :as ui-radio-box]
             ]
   })

^{:refer js.react-native.ui-radio-box/RadioBoxSimple
  :adopt true
  :added "0.1"}
(fact  "creates a slim radiobox"
  ^:hidden
  
  (defn.js RadioBoxSimpleDemo
    []
    (var [first setFirst]   (r/local true))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-radio-box/RadioBoxSimple"} 
[:% n/Row
       {:style {:alignItems "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "RADIO"]
       [:% ui-radio-box/RadioBox
        {:outlined true
         :highlighted highlighted
         :disabled disabled
         :theme {:fgActive "limegreen"
                 :fgNormal "#666"
                 :bgActive "green"
                 :bgNormal "#444"}
         :selected first
         :setSelected setFirst
         :style {:flex 1}
         :size 30
         :styleContainer {:flex 1}
         :outsideStyle {:borderWidth 4}
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

^{:refer js.react-native.ui-radio-box/radioBoxTheme :added "4.0" :unchecked true}
(fact "creates a radio box theme")

^{:refer js.react-native.ui-radio-box/RadioBox :added "0.1"}
(fact  "creates a radio box"
  ^:hidden
  
  (defn.js RadioBoxDemo
    []
    (var [first setFirst]   (r/local true))
    (var [second setSecond] (r/local true))
    (var [highlighted setHighlighted] (r/local true))
    (var [errored setErrored] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-radio-box/RadioBox"} 
[:% n/Row
       {:style {:alignItems "center"
                #_#_:margin 3}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "RADIO"]
       [:% ui-radio-box/RadioBox
        {:theme {:fgActive "limegreen"}
         :selected first
         :setSelected setFirst
         :style {:flex 1}
         :styleContainer {:flex 1}}]
       
       [:% n/Padding {:style {:width 10}}]
       [:% ui-radio-box/RadioBox
        {:theme {:fgActive "black"
                 :fgNormal   "#333"
                 :bgNormal   "white"
                 :bgHovered  "#555"
                 :bgPressed  "black"}
         :outerStyle {:borderWidth 5
                      :borderStyle "solid"}
         :outlined true
         :selected second
         :setSelected setSecond}]
       
       
       [:% n/Padding {:style {:width 10}}]
       [:% ui-radio-box/RadioBox
        {:disabled true
         :selected true}]
       [:% n/Padding {:style {:width 10}}]
       
       [:% ui-radio-box/RadioBox
        {:highlighted highlighted
         :setSelected setHighlighted
         :selected highlighted}]
       [:% n/Padding {:style {:width 10}}]
       [:% ui-radio-box/RadioBox
        {:highlighted errored
         :setSelected setErrored
         :selected errored
         :theme {:fgHighlighted "white"
                 :bgHighlighted "red"}}]] 
[:% n/Padding {:style {:height 10}}] 
[:% n/Row
       {:style {:alignItems "center"
                #_#_:margin 3}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "RADIO"]
       [:% ui-radio-box/RadioBox
        {:theme {:fgActive "darkred"}
         :selected first
         :outlined true
         :setSelected setFirst
         :size 32
         :sizeInner 18}]
       [:% n/Padding {:style {:width 10}}]
       [:% ui-radio-box/RadioBox
        {:theme {:fgActive "limegreen"
                 :fgNormal   "#888"
                 :bgNormal   "#aaa"
                 :bgPressed  "limegreen"
                 :bgHovered 0.5
                 :fgHovered 0}
         :selected second
         :outlined true
         :setSelected setSecond
         :size 32
         :outerStyle {:borderWidth 4
                      :borderStyle "solid"
                      :borderRadius 3}
         :innerStyle {:borderRadius 0}
         :sizeInner 12}]])))

  
  
  )
