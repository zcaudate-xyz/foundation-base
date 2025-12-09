(ns js.react-native.ui-slider-test
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
             [js.react-native.ui-slider :as ui-slider]
             ]
   })

^{:refer js.react-native.ui-slider/sliderTheme :added "4.0" :unchecked true}
(fact "creates the slider theme")

^{:refer js.react-native.ui-slider/Slider :added "0.1"}
(fact "creates a slim slider"
  ^:hidden
  
  (defn.js SliderHDemo
    []
    (var [first setFirst] (r/local 10))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-slider/Slider"} 
[:% n/Row
       {:style {:alignItems "center"
                :justifyContent "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "SliderH"]
       [:% ui-slider/Slider
        {:highlighted highlighted
         :disabled disabled
         :theme {:bgNormal "red"
                 :fgNormal "blue"
                 :bgHovered 0.7
                 :fgHovered 0.7
                 :bgPressed -0.2
                 :fgPressed 0.1}
         
         :value first
         :setValue setFirst
         :length 200
         :max 10
         :min 0
         :step 2
         :knobStyle {:borderRadius 3}
         :axisStyle {:borderRadius 3}
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
         :onPress (fn:> (setDisabled (not disabled)))}]
       [:% n/Text
        (n/format-entry #{first})]])))

  (defn.js SliderVDemo
    []
    (var [first setFirst] (r/local 10))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-slider/SliderV"} 
[:% n/Row
       {:style {:alignItems "center"
                :justifyContent "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "SliderV"]
       [:% ui-slider/Slider
        {:highlighted highlighted
         :disabled disabled
         :layout "vertical"
         :theme {:bgNormal "red"
                 :fgNormal "blue"
                 :bgHovered 0.7
                 :fgHovered 0.7
                 :bgPressed -0.2
                 :fgPressed 0.1}
         
         :value first
         :setValue setFirst
         :length 200
         :max 10
         :min 0
         :step 2
         :knobStyle {:borderRadius 3}
         :axisStyle {:borderRadius 3}
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
         :onPress (fn:> (setDisabled (not disabled)))}]
       [:% n/Text
        (n/format-entry #{first})]])))
  
  
  
  )
