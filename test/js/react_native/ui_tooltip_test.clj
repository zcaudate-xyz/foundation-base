(ns js.react-native.ui-tooltip-test
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
             [js.react-native.ui-tooltip :as ui-tooltip]
             [js.react-native.ui-button :as ui-button]]
   })

^{:refer js.react-native.ui-tooltip/tooltipPortalOffset :added "4.0" :unchecked true}
(fact "calculates the portal offset")

^{:refer js.react-native.ui-tooltip/tooltipContextOffset :added "4.0" :unchecked true}
(fact "calculates the context offset")

^{:refer js.react-native.ui-tooltip/TooltipRawArrow :added "4.0" :unchecked true}
(fact "creates the tooltip arrow")

^{:refer js.react-native.ui-tooltip/TooltipInnerContentArrow :added "4.0" :unchecked true}
(fact "places arrow relative to content")

^{:refer js.react-native.ui-tooltip/TooltipInnerHostArrow :added "4.0" :unchecked true}
(fact "places arrow relative to host")

^{:refer js.react-native.ui-tooltip/TooltipInner :added "4.0" :unchecked true}
(fact "creates the tooltip inner (with portal)")

^{:refer js.react-native.ui-tooltip/Tooltip :added "4.0" :unchecked true}
(fact "creates a tooltip"
  ^:hidden
  
  (defn.js TooltipPane
    []
    (var [visible setVisible] (r/local true))
    (var [placement setPlacement] (r/local "content"))
    (var [alignment setAlignment] (r/local "center"))
    (var [position setPosition]  (r/local "top"))
    (var [text setText] (r/local "HELLO zWORL"))
    (var buttonRef (r/ref))
    (return
     [:% n/View
      {:style {:flex 1}}
      [:% n/Row
       [:% n/Tabs
        {:value alignment
         :setValue setAlignment
         :data ["start" "center" "end"]}]
       [:% n/Text " "]
       [:% n/Tabs
        {:value position
         :setValue setPosition
         :data ["top"
                "bottom"
                "left"
                "left_edge"
                "right"
                "right_edge"
                "centered"]}]
       [:% n/Text " "]
       [:% n/Tabs
        {:value placement
         :setValue setPlacement
         :data ["host" "content" "none"]}]]
      [:% n/Row
       [:% n/TextInput
        {:value text
         :onChangeText setText}]]
      [:% n/View
       {:style {:backgroundColor "#eee"
                :flex 1
                :justifyContent "center"
                :alignItems "center"}}
       [:% ui-tooltip/Tooltip
             #{alignment
               position
               visible
               {:hostRef buttonRef
                :arrow  {:size   10
                         :margin 3
                         :placement placement}}}
        [:% n/Text
              {:style {:position "absolute"
                       :color "white"
                       :backgroundColor "red"
                       :borderRadius 5
                       :padding 10
                       :fontSize 12}}
              text]]
       [:% ui-button/Button
        {:refLink buttonRef
         :text "ACTION"
         :theme {:bgNormal   "green"
                 :fgNormal   "#fefefe"
                 :bgPressed  "limegreen"
                 :bgHovered  "limegreen"}
         :style {
                 :height 100
                 :width 100}
         :onPress (fn:> (setVisible (not visible)))}]]]))  
  
  (defn.js TooltipDemo
    []
    (var [first setFirst] (r/local 10))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-tooltip/Tooltip"
       :style {:height 400}} 
[:% n/Row
       {:style {:flex 1}}
       [:% n/PortalProvider
        [:% n/PortalSink
         {:style {:flex 1}}
         [:% -/TooltipPane]]]])))
  
  

  )
