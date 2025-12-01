(ns js.react-native.ui-toggle-button-test
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
   :require [[js.core :as j]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.physical-addon :as physical-addon]
             [js.react-native.ui-toggle-button :as ui-toggle-button]
             ]
   :export [MODULE]})

^{:refer js.react-native.ui-toggle-button/toggleButtonTheme :added "4.0" :unchecked true}
(fact "creates the toggle button theme")

^{:refer js.react-native.ui-toggle-button/ToggleButton
  :added "0.1"}
(fact "gets a toggleButton button"
  ^:hidden
  
  (defn.js ToggleButtonSimpleDemo
    []
    (var [selected setSelected] (r/local true))
    (var [highlighted setHighlighted]   (r/local false))
    (var [disabled setDisabled]   (r/local false))
    (return
     (n/EnclosedCode 
{:label " js.react-native.ui-toggle-button/ToggleButtonSimple"} 
[:% n/Row
       {:style {:alignItems "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "SIMPLE"]
       [:% ui-toggle-button/ToggleButton
        {:selected selected
         :highlighted highlighted
         :disabled disabled
         :theme {:bgNormal   "blue"
                 :fgNormal   "#fefefe"
                 :bgActive   "red"
                 :bgPressed  -0.4
                 :bgHovered  0.8}
         :text  "PUSH"
         :style {:borderRadius 3}
         :styleContainer {:flexDirection "row-reverse"}
         :size "lg"
         :onPress (fn []
                    (setSelected (not selected)))
         :inner  [{:component n/View
                   :style {:height 2
                           :top -6
                           :backgroundColor "purple"}
                   :transformations
                   (fn [#{active
                          pressing}]
                     (return {:style {:width (+ (* 100 (j/abs (- pressing active)))
                                                "%")}}))}
                  {:component n/View
                   :style {:height 2
                           :top -4
                           :backgroundColor "orange"}
                   :transformations
                   {:pressing
                    (fn [v]
                      (return {:style {:width (+ (* 100 v) "%")}}))}}
                  {:component n/View
                   :style {:height 2
                           :top -2
                           :backgroundColor "green"}
                   :transformations
                   (fn [#{hovering
                          pressing}]
                     (return {:style {:width (+ (* 100 (- hovering pressing))
                                                "%")}}))}]
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
       {:text (n/format-obj #{selected})
        :style {:marginTop 10}}])))
  

  (def.js MODULE (!:module))
  
  )

(comment

  (defn.js ToggleButtonDemo
    []
    (var [selected setSelected] (r/local true))
    (var [selected2 setSelected2] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-toggle-button/ToggleButton"} 
[:% n/Row
       {:style {:alignItems "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "TOGGLEBUTTON"]
       [:% ui-toggle-button/ToggleButton
        {:selected selected
         :theme {:fgNormal    "green"
                 :fgSelected  "red"
                 #_#_:fgNormal    "#fefefe"
                 :bgPressed   "#888"
                 :bgHovered   (:? selected "red" "green")}
         :text "HELLO"
         :style {:fontWeight "800"}
         :size "lg"
         :onPress (fn []
                    (setSelected (not selected)))}]
       [:% n/Padding {:style {:width 10}}]] 
[:% n/Padding {:style {:height 10}}] 
[:% n/Row
       {:style {:alignItems "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "TOGGLEBUTTON"]
       [:% ui-toggle-button/ToggleButton
        {:selected selected
         :theme {:bgNormal    "darkgreen"
                 :fgSelected  "darkred"
                 :bgSelected  "white"
                 :fgNormal   "#fefefe"
                 :bgPressed  "#333"
                 :bgHovered   (fn:> [x] (* 0.6 x))
                 :fgHovered   (fn:> [x] (* 0.6 x))}
         :text "HELLO"
         :styleText {:fontSize 25
                     :fontWeight "800"}
         :onPress (fn []
                    (setSelected (not selected)))}]
       [:% n/Padding {:style {:width 10}}]] 
[:% n/Caption
       {:text (n/format-obj #{selected})
        :style {:marginTop 10}}]))))
