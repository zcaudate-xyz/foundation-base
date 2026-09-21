(ns melbourne.ui-button-test
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
   :require [[js.react-native :as n :include [:fn]]
             [js.react :as r]
             [melbourne.ui-button :as ui-button]
             [melbourne.base-palette :as base-palette]]
   :export [MODULE]})

^{:refer melbourne.ui-button/Button :added "4.0"}
(fact "constructs a button"
  ^:hidden
  
  (defn.js ButtonDemo
    []
    (var [color setColor] (r/local "teal"))
    (var [type setType]   (r/local "light"))
    (var [foil setFoil]   (r/local "background"))
    (var palette (base-palette/createPalette type color))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:key (+ color "." type)
        :label "melbourne.ui-button/Button"} 
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
        {:style {:backgroundColor (base-palette/getColorRaw
                                   palette
                                   (or foil "background"))
                 :flex 1
                 :padding 20}}
        [:% n/Row
         {:style {:margin 5
                  :flexWrap "wrap"}}
         [:% ui-button/Button
          {:tooltip {:label "HELLO THERE"
                     :position "top"}
           :design {:type type
                    :color color}
           :style {:margin 5
                   :border "1px solid"}
           :outlined true
           :text  "NORMAL"}]
         [:% ui-button/Button
          {:design {:type type
                    :color color
                    :font "h3"}
           :style {:margin 5}
           :text  "H3 BUTTON"}]
         [:% ui-button/Button
          {:tooltip {:label "HELLO THERE"
                     :position "top"}
           :design {:type type
                    :color color}
           :variant {:fg {:key "neutral"}
                     :bg {:key "background"}
                     :hovered {:bg {:key "background"}}
                     :pressed {:bg {:key "background"}}}
           :style {:margin 5}
           :text  "MINOR"}]
         [:% ui-button/Button
          {:tooltip {:label "HELLO THERE"
                     :position "top"}
           :design {:type type
                    :color color}
           :variant {:fg {:key "neutral"}
                     :bg {:key "background"}
                     :pressed {:bg {:key "background"}}}
           :outlined true
           :style {:margin 5
                   :border "1px solid"}
           :text  "OUTLINED"}]
         [:% ui-button/Button
          {:tooltip {:label "HELLO THERE"
                     :position "top"}
           :design {:type type
                    :color color}
           :variant {:fg {:key "background"}
                     :bg {:key "neutral"}
                     :pressed {:bg {:key "neutral"}}}
           :style {:margin 5}
           :text  "UTIL"}]
         [:% ui-button/Button
          {:design {:type type
                    :color color}
           :variant {:fg {:key "primary"
                          :tone "lighten"
                          :ratio 3}
                     :bg {:key "neutral"
                          :tone "lighten"
                          :ratio 7}
                     :pressed {:bg {:key "neutral"}}}
           :style {:margin 5}
           :text  "INVERT"}]
         [:% ui-button/Button
          {:design {:type type
                    :color color}
           :variant {:fg {:key "neutral"
                          :tone "flatten"}
                     :bg {:key "primary"
                          :tone "mix"
                          :mix "background"
                          :ratio 2}
                     :pressed {:bg {:key "primary"
                                    :tone "lighten"
                                    :ratio 4}}}
           :outlined true
           :style {:margin 5}
           :text  "ACCENT"}]
         [:% ui-button/Button
          {:design {:type type
                    :color color}
           :variant {:fg {:key "background"
                          :tone "sharpen"}
                     :bg {:key "error"}
                     :pressed {:bg {:key "error"
                                    :tone "lighten"
                                    :ratio 4}}}
           :style {:margin 5}
           :text  "ERRORED"}]
         [:% ui-button/Button
          {:design {:type type
                    :color color}
           :variant {:fg {:key "neutral"
                          :tone "sharpen"}
                     :bg {:key "primary"}
                     :pressed {:bg {:key "background"}}}
           :style {:margin 5}
           :disabled true
           :text  "DISABLED"}]]])]))
  
  (def.js MODULE (!:module)))
