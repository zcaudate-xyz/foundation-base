(ns js.react-native.ui-range-test
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
             [js.react-native.ui-range :as ui-range]
             ]
   :export [MODULE]})

^{:refer js.react-native.ui-range/rangeTheme :added "4.0" :unchecked true}
(fact "creates the range theme")

^{:refer js.react-native.ui-range/useKnob :added "4.0" :unchecked true}
(fact "creates the knob for range")

^{:refer js.react-native.ui-range/Range :added "0.1"}
(fact "creates a slim range"
  ^:hidden
  
  (defn.js RangeHDemo
    []
    (var [lower setLower] (r/local 2))
    (var [upper setUpper] (r/local 8))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-range/Range"} 
[:% n/Row
       {:style {:alignItems "center"
                :justifyContent "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "RangeH"]
       [:% ui-range/Range
        #{upper setUpper
          lower setLower
          highlighted
          disabled
          {:theme {:bgNormal "red"
                   :fgNormal "blue"
                   :bgHovered 0.7
                   :fgHovered 0.7
                   :bgPressed -0.2
                   :fgPressed 0.1}
           
           :length 200
           :max 10
           :min 0
           :step 2
           :knobStyle {:borderRadius 3}
           :axisStyle {:borderRadius 3}
           :addons [(physical-addon/tagAll
                     {:style {:paddingHorizontal 20
                              :height 250
                              :width 300
                              :flex 1}})]}}]] 
[:% n/Row
       [:% n/Button
        {:title "H"
         :onPress (fn:> (setHighlighted (not highlighted)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn:> (setDisabled (not disabled)))}]
       [:% n/Text
        (n/format-entry #{lower upper})]])))

  (defn.js RangeVDemo
    []
    (var [lower setLower] (r/local 10))
    (var [upper setUpper] (r/local 10))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-range/RangeV"} 
[:% n/Row
       {:style {:alignItems "center"
                :justifyContent "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "RangeV"]
       [:% ui-range/Range
        #{upper setUpper
          lower setLower
          highlighted
          disabled
          {:layout "vertical"
           :theme {:bgNormal "red"
                   :fgNormal "blue"
                   :bgHovered 0.7
                   :fgHovered 0.7
                   :bgPressed -0.2
                   :fgPressed 0.1}
           :length 200
           :max 10
           :min 0
           :step 2
           :knobStyle {:borderRadius 3}
           :axisStyle {:borderRadius 3}
           :addons [(physical-addon/tagAll
                     {:style {:paddingHorizontal 20
                              :height 250
                              :width 300
                              :flex 1}})]}}]] 
[:% n/Row
       [:% n/Button
        {:title "H"
         :onPress (fn:> (setHighlighted (not highlighted)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn:> (setDisabled (not disabled)))}]
       [:% n/Text
        (n/format-entry #{lower upper})]])))
  
  (def.js MODULE (!:module))
  
  )
