(ns js.react-native.ui-input-test
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
             [js.react-native.ui-input :as ui-input]
             ]
   })

^{:refer js.react-native.ui-input/InputSimple
  :adopt true
  :added "0.1"}
(fact "creates a slim input"
  ^:hidden
  
  (defn.js InputSimpleDemo
    []
    (var [first setFirst]   (r/local "Hello"))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-input/InputSimple"} 
[:% n/Row
       {:style {:alignItems "center"
                :justifyContent "center"
                :height 40}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "First Name"]
       [:% ui-input/Input
        {:outlined true
         :highlighted highlighted
         :disabled disabled
         :theme {:bgNormal "#eee"
                 :bgHovered "#ddd"}
         :value first
         :onChangeText setFirst
         :styleContainer {:flex 1}
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

^{:refer js.react-native.ui-input/inputTheme :added "4.0" :unchecked true}
(fact "creates the input theme")

^{:refer js.react-native.ui-input/Input :added "0.1"}
(fact "creates a slim input"
  ^:hidden
  
  (defn.js InputDemo
    []
    (var [first setFirst] (r/local "Hello"))
    (var [last  setLast]  (r/local "World"))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-core/TextInput"} 
[:% n/Row
       {:style {:alignItems "center"
                :justifyContent "center"
                :height 40}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "First Name"]
       [:% ui-input/Input
        {:theme {:bgNormal "#eee"
                 :bgHovered "#ddd"}
         :value first
         :onChangeText setFirst
         :style {:flex 1}
         :styleContainer {:flex 1}}]] 
[:% n/Row
       {:style {:alignItems "center"
                :justifyContent "center"
                :height 40}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "Last Name"]
       [:% ui-input/Input
        {:theme {:bgNormal "#eee"
                 :bgHovered "#ddd"}
         :value last
         :onChangeText setLast
         :style {:flex 1}
         :styleContainer {:flex 1}}]] 
[:% n/Caption
       {:text (n/format-obj #{first last})
        :style {:marginTop 10}}])))

  
  
  )
