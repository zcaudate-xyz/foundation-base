(ns js.react-native.ui-toggle-switch-test
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
             [js.react-native.ui-toggle-switch :as ui-toggle-switch]
             ]
   })

^{:refer js.react-native.ui-toggle-switch/ToggleSwitchSimple
  :adopt true
  :added "4.0" :unchecked true}
(fact  "creates a slim switch box"
  ^:hidden

  (defn.js ToggleSwitchSimpleDemo
    []
    (var [first setFirst]   (r/local true))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-toggle-switch/ToggleSwitchSimple"} 
[:% n/Row
       {:style {:alignItems "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "SWITCH"]
       [:% ui-toggle-switch/ToggleSwitch
        {:selected first
         :setSelected setFirst
         :highlighted highlighted
         :disabled disabled
         :theme {:bgActive        "gray"
                 :fgActive        "green"
                 :bgNormal        "gray"}
         :style {:flex 1}
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
         :onPress (fn:> (setDisabled (not disabled)))}]] 
[:% n/Caption
       {:text (n/format-obj #{first})
        :style {:marginTop 10}}]))))

^{:refer js.react-native.ui-toggle-switch/ToggleSwitchSquare
  :adopt true
  :added "4.0" :unchecked true}
(fact  "creates a slim switch box"
  ^:hidden

  (defn.js ToggleSwitchSquareDemo
    []
    (var [first setFirst]   (r/local true))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-toggle-switch/ToggleSwitchSquare"} 
[:% n/Row
       {:style {:alignItems "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "SWITCH"]
       [:% ui-toggle-switch/ToggleSwitch
        {:selected first
         :setSelected setFirst
         :highlighted highlighted
         :disabled disabled
         :theme {:bgActive        "gray"
                 :fgActive        "darkgreen"
                 :fgPressed       "black"
                 :fgNormal        "red"
                 :bgNormal        "gray"}
         :style {:flex 1}
         :knobStyle {:borderRadius 0}
         :axisStyle {:borderRadius 0}
         :size 40
         :transformations
         {:knob 
          {:fg (fn:> [#{active}]
                 {:style {:transform [{:translateX (* active 40)}
                                      {:rotateZ (+ (* active 90) "deg")}]}})}}
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
         :onPress (fn:> (setDisabled (not disabled)))}]] 
[:% n/Caption
       {:text (n/format-obj #{first})
        :style {:marginTop 10}}]))))

^{:refer js.react-native.ui-toggle-switch/toggleSwitchTheme :added "4.0" :unchecked true}
(fact "creates the toggle switch theme")

^{:refer js.react-native.ui-toggle-switch/ToggleSwitch :added "4.0" :unchecked true}
(fact  "creates a toggle switch box"
  ^:hidden

  (defn.js SwitchBoxDemo
    []
    (var [first setFirst]   (r/local true))
    (var [second setSecond] (r/local true))
    (var [highlighted setHighlighted] (r/local true))
    (var [errored setErrored] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-toggle-switch/ToggleSwitch"} 
[:% n/Row
       {:style {:alignItems "center"
                #_#_:margin 3}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "SWITCH"]
       [:% ui-toggle-switch/ToggleSwitch
        {:selected first
         :setSelected setFirst
         :style {:flex 1}
         :styleContainer {:flex 1}}]
       [:% n/Padding {:style {:width 20}}]
       [:% ui-toggle-switch/ToggleSwitch
        {:selected second
         :setSelected setSecond
         :theme {#_#_:bgNormal "black"
                 :fgSelected "black"}}]
       [:% n/Padding {:style {:width 20}}]
       [:% ui-toggle-switch/ToggleSwitch
        {:highlighted highlighted
         :setSelected setHighlighted
         :selected highlighted}]

       [:% n/Padding {:style {:width 20}}]
       [:% ui-toggle-switch/ToggleSwitch
        {:disabled true
         :highlighted highlighted
         :setSelected setHighlighted
         :selected highlighted}]] 
[:% n/Padding {:style {:height 20}}] 
[:% n/Row
       {:style {:alignItems "center"
                #_#_:margin 3}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "SWITCH"]
       [:% ui-toggle-switch/ToggleSwitch
        {:selected first
         :setSelected setFirst
         :theme {:fgSelected "darkgreen"
                 :fgNormal "darkred"}
         :knobProps {:children [[:% n/Text
                                 {:style {:fontWeight "800"
                                          :color "white"}}
                                 (:? first "ON" "OFF")]]}
         :knobStyle {:width 40
                     :height 40
                     :justifyContent "center"
                     :alignItems "center"}
         :lineStyle {:height 40
                     :width 64
                     :marginVertical 0}}]
       [:% n/Padding {:style {:width 20}}]])))

  

  )
