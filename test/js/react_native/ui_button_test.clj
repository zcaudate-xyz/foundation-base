(ns js.react-native.ui-button-test
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
             [js.react-native.ui-button :as ui-button]]
   :export [MODULE]})

^{:refer js.react-native.ui-button-test/ButtonOpacity
  :adopt true
  :added "0.1"}
(fact "creates a opacity button"
  ^:hidden
  
  (defn.js ButtonOpacityDemo
    []
    (var [active setActive] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-button/ButtonOpacity"} 
[:% n/Row
       {:style {:alignItems "center"
                :backgroundColor "yellow"
                :height 100}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "OPACITY"]
       [:% ui-button/Button
        {:theme {:bgNormal   "green"
                 :fgNormal   "#fefefe"
                 :bgPressed  "limegreen"
                 :bgHovered  "limegreen"}
         :indicatorParams {:pressing {:default {:duration 1000}}}
         :text  "PUSH"
         :style {:borderRadius 3}
         :size "lg"
         :onPress (fn []
                    (setActive (not active)))
         :addons [(physical-addon/tagAll
                   {:style {:paddingHorizontal 20
                            :height 80
                            :flex 1}})]
         :transformations
         {:bg (fn:> [#{pressing}]
                {:style {:opacity (- 1 (* 0.8 pressing))}})}}]] 
[:% n/Caption
       {:text (n/format-obj #{active})
        :style {:marginTop 10}}]))))

^{:refer js.react-native.ui-button-test/ButtonSize
  :adopt true
  :added "0.1"}
(fact  "creates a size button"
  ^:hidden
  
  (defn.js ButtonSizeDemo
    []
    (var [active setActive] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-button/ButtonSize"} 
[:% n/Row
       {:style {:alignItems "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "SIZE"]
       [:% ui-button/Button
        {:theme {:bgNormal   "green"
                 :fgNormal   "#fefefe"
                 :bgPressed  "limegreen"
                 :bgHovered  "limegreen"}
         :indicatorParams {:pressing {:default {:duration 1000}}}
         :text  "PUSH"
         :style {:borderRadius 3}
         :styleContainer {:height 100
                          :paddingVertical 30}
         :size "lg"
         :onPress (fn []
                    (setActive (not active)))
         :addons [(physical-addon/tagAll
                   {:style {:paddingHorizontal 20
                            :height 80
                            :flex 1}})]
         :transformations
         {:bg (fn:> [#{pressing}]
                {:style {:transform [{:scaleY (+ 1 pressing)}
                                     {:scaleX (+ 1 pressing)}]}})}}]] 
[:% n/Caption
       {:text (n/format-obj #{active})
        :style {:marginTop 10}}]))))

^{:refer js.react-native.ui-button/ButtonFraction
  :adopt true
  :added "0.1"}
(fact  "creates a button"
  ^:hidden
  
  (defn.js ButtonFractionDemo
    []
    (var [active setActive] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-button/ButtonFraction"} 
[:% n/Row
       {:style {:alignItems "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "FRACTION"]
       [:% ui-button/Button
        {:theme {:bgNormal   "blue"
                 :fgNormal   "red"
                 :fgHovered   0.1
                 :bgPressed   -0.5
                 :bgHovered  -0.8}
         :text "PUSH"
         :style {:fontSize 30
                 :fontWeight "800"
                 :borderRadius 10
                 :margin 20}
         :onPress (fn []
                    (setActive (not active)))
         :addons [(physical-addon/tagAll
                   {:style {:paddingHorizontal 20
                            :height 80
                            :flex 1}})]}]
       [:% n/Padding {:style {:width 10}}]] 
[:% n/Caption
       {:text (n/format-obj #{active})
        :style {:marginTop 10}}]))))

^{:refer js.react-native.ui-button/buttonTheme :added "4.0" :unchecked true}
(fact "creates the botton theme")

^{:refer js.react-native.ui-button/Button :added "0.1"}
(fact  "creates a simple button"
  ^:hidden
  
  (defn.js ButtonSimpleDemo
    []
    (var [active setActive] (r/local true))
    (var [highlighted setHighlighted]   (r/local false))
    (var [disabled setDisabled]   (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-button/ButtonSimple"} 
[:% n/Row
       {:style {:alignItems "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "SIMPLE"]
       [:% ui-button/Button
        {:highlighted highlighted
         :disabled disabled
         :theme {:bgNormal   "darkred"
                 :fgNormal   "#fefefe"
                 :bgPressed  "orange"
                 :bgHovered  "firebrick"}
         :text  "PUSH"
         :style {:borderRadius 3}
         :size "lg"
         :onPress (fn []
                    (setActive (not active)))
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
       {:text (n/format-obj #{active})
        :style {:marginTop 10}}])))

  
  (def.js MODULE (!:module))
  
  )
