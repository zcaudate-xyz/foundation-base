(ns melbourne.ui-toggle-button-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn]]
             [melbourne.ui-toggle-button :as ui-toggle-button]
             [melbourne.base-palette :as base-palette]]
   :export [MODULE]})

^{:refer melbourne.ui-toggle-button/ToggleButton :added "0.1"}
(fact "creates a toggle button"
  ^:hidden
  
  (defn.js ToggleButtonDemo
    []
    (var [s0 setS0] (r/local false))
    (var [s1 setS1] (r/local true))
    (var [s2 setS2] (r/local true))
    (var [color setColor] (r/local "blue"))
    (var [type setType]   (r/local "dark"))
    (var [foil setFoil]   (r/local "primary"))
    (var palette (base-palette/createPalette type color))
    
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-button/ToggleButton"} 
[:% n/Row
       [:% n/Tabs
        {:data ["purple" "teal" "blue" "green"]
         :value color
         :setValue setColor}]
       [:% n/Text "   "]
       [:% n/Tabs
        {:data ["light" "dark"]
         :value type
         :setValue setType}]] 
[:% n/Tabs
       {:data ["background" "primary" "neutral" "error"]
        :value foil
        :setValue setFoil}] 
[:% n/View
       {:key (+ type "."  color)
        :style {:backgroundColor (base-palette/getColorRaw
                                  palette
                                  (or foil "background"))
                :flex 1
                :padding 20}}
       [:% n/Row
        {:style {:margin 5}}
        [:% ui-toggle-button/ToggleButton
         {:selected s0
          :onPress (fn:> (setS0 (not s0)))
          :design #{color type}
          :variant {:active {:bg {:key "neutral"}}
                    :font "h2"}
          :text  "H2 Button"}]]
       [:% n/Row
        {:style {:margin 5}}
        [:% ui-toggle-button/ToggleButton
         {:selected s1
          :onPress (fn:> (setS1 (not s1)))
          :design #{color type}
          :variant {:fg {:key "primary"}
                    :active {:bg {:key "primary"}
                             :fg {:key "background"}}}
          :text  "Secondary Light"}]]
       [:% n/Row
        {:style {:margin 5}}
        [:% ui-toggle-button/ToggleButton
         {:selected s2
          :onPress (fn:> (setS2 (not s2)))
          :design #{color type}
          :variant {:fg {:key "primary"}
                    :pressed {:bg {:raw 1}}
                    :active {:bg {:key "background"}
                             :fg {:key "primary"}}}
          :outlined s2
          :style [{:fontSize 12}
                  {:borderWidth 1 :borderStyle "solid"}]
          :text  "Minor Light"
          #_#_:outlined true}]]
       [:% n/Row
        {:style {:margin 5}}
        [:% ui-toggle-button/ToggleButton
         {:selected s2
          :onPress (fn:> (setS2 (not s2)))
          :design #{color type}
          :variant {:fg {:key "background"}
                    :bg {:key "primary"}
                    :hovered {:bg {:raw 1}}
                    :pressed {:bg {:raw 1}}
                    :active {:bg {:key "primary"}
                             :fg {:key "background"}}}
          :outlined s2
          :style [{:fontSize 12}
                  {:borderWidth 1 :borderStyle "solid"}]
          :text  "Color Light"
          #_#_:outlined true}]]
       [:% n/Row
        {:style {:margin 5}}
        [:% ui-toggle-button/ToggleButton
         {:selected s2
          :onPress (fn:> (setS2 (not s2)))
          :design #{color type}
          :variant {:fg {:key "background"}
                    :bg {:key "error"}
                    :pressed {:bg {:raw 1}}
                    :active {:bg {:key "error"}
                             :fg {:key "background"}}}
          :outlined s2
          :style [{:fontSize 12}
                  {:borderWidth 1 :borderStyle "solid"}]
          :text  "Color Light"
          #_#_:outlined true}]]
       [:% n/TextDisplay
        #{s0 s1 s2}]])))

  (def.js MODULE (!:module)))
