(ns melbourne.ui-static-test
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
             [melbourne.ui-static :as ui-static]
             [melbourne.base-palette :as base-palette]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]]
   :export [MODULE]})

^{:refer melbourne.ui-static/Div :added "4.0"}
(fact "creates a static div"
  ^:hidden
  
  (defn.js DivDemo
    []
    (var [color setColor] (r/local "teal"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-static/Div"} 
[:% n/Row
       [:% n/Tabs
        {:data ["purple" "teal" "blue" "green"]
         :value color
         :setValue setColor}]] 
[:% n/Row
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "light"
                  :color color}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "light"
                  :color color
                  :invert true}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "light"
                  :color color}
         :variant {:bg {:key "primary"}}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "light"
                  :color color}
         :variant {:bg {:key "neutral"
                        :tone "augment"}}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "light"
                  :color color}
         :variant {:bg {:key "neutral"
                        :tone "diminish"}}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "light"
                  :color color}
         :variant {:bg {:key "primary"
                        :tone "sharpen"}}}]] 
[:% n/Row
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "dark"
                  :color color}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "dark"
                  :color color
                  :invert true}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "dark"
                  :color color}
         :variant {:bg {:key "primary"}}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "dark"
                  :color color}
         :variant {:bg {:key "neutral"
                        :tone "augment"}}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "dark"
                  :color color}
         :variant {:bg {:key "neutral"
                        :tone "diminish"}}}]
       [:% ui-static/Div
        {:style {:height 100
                 :width 100}
         :design {:type "dark"
                  :color color}
         :variant {:bg {:key "primary"
                        :tone "sharpen"}}}]]))))

^{:refer melbourne.ui-static/Text :added "4.0"}
(fact "creates a static text element"
  ^:hidden
  
  (defn.js TextDemo
    []
    (var [color setColor] (r/local "teal"))
    (var [type setType]   (r/local "light"))
    (var [foil setFoil]   (r/local "background"))
    (var [tone setTone]   (r/local "augment"))
    (var palette (base-palette/createPalette type color))
    (return
     (n/EnclosedCode 
{:key (+ color "." type)
       :label "melbourne.ui-static/Text"} 
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
[:% n/Row
       [:% n/Tabs
        {:data ["background" "primary" "neutral" "error"]
         :value foil
         :setValue setFoil}]] 
[:% n/View
       {:style {:backgroundColor (base-palette/getColorRaw
                                  palette
                                  (or foil "background"))}}
       (j/flatMap
        ["primary" "background" "neutral" "error"]
        (fn [key]
          (return (j/map ["flatten" "diminish" "default" "augment" "sharpen"]
                         (fn [tone]
                           (return
                            [:% ui-static/Text
                             {:key (+ key "." tone)
                              :design {:type type
                                       :color color}
                              
                              :variant {:font "h4"
                                        :fg {:key  key
                                             :tone tone}}}
                             (j/toUpperCase (+ key " " tone))]))))))]))))

^{:refer melbourne.ui-static/Separator :added "4.0"}
(fact "creates a seperator"
  ^:hidden
  
  (defn.js SeparatorDemo
    []
    (var [color setColor] (r/local "teal"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-static/Separator"} 
[:% n/Row
       [:% n/Tabs
        {:data ["purple" "teal" "blue" "green"]
         :value color
         :setValue setColor}]] 
[:% n/Row
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "light"
                  :color color}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "light"
                  :color color
                  :invert true}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "light"
                  :color color}
         :variant {:fg {:key "primary"}}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "light"
                  :color color}
         :variant {:fg {:key "neutral"
                        :tone "augment"}}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "light"
                  :color color}
         :variant {:fg {:key "neutral"
                        :tone "diminish"}}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "light"
                  :color color}
         :variant {:fg {:key "primary"
                        :tone "sharpen"}}}]] 
[:% n/Row
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "dark"
                  :color color}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "dark"
                  :color color
                  :invert true}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "dark"
                  :color color}
         :variant {:fg {:key "primary"}}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "dark"
                  :color color}
         :variant {:fg {:key "neutral"
                        :tone "augment"}}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "dark"
                  :color color}
         :variant {:fg {:key "neutral"
                        :tone "diminish"}}}]
       [:% ui-static/Separator
        {:style {:width 100}
         :design {:type "dark"
                  :color color}
         :variant {:fg {:key "primary"
                        :tone "sharpen"}}}]]))))

^{:refer melbourne.ui-static/ScrollView :added "4.0"}
(fact "creates a scrollview"
  ^:hidden
  
  (defn.js ScrollViewDemo
    []
    (var [color setColor] (r/local "teal"))
    (var [type setType]   (r/local "light"))
    (var [foil setFoil]   (r/local "background"))
    (var [tone setTone]   (r/local "augment"))
    (return
     (n/EnclosedCode 
{:key (+ color "." type)
       :label "melbourne.ui-static/ScrollView"} 
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
[:% n/Row
       [:% n/Tabs
        {:data ["background" "primary" "neutral" "error"]
         :value foil
         :setValue setFoil}]
       [:% n/Tabs
        {:data ["augment" "diminish" "flatten" "sharpen"]
         :value tone
         :setValue setTone}]] 
[:% ui-static/ScrollView
       {:design {:type type
                 :color color}
        :variant {:bg {:key foil
                       :tone tone}}
        :style {:height 300
                :width 300}}
       [:% n/View
        {:style {:width 400
                 :height 800
                 :backgroundColor "#333"}}]]))))

^{:refer melbourne.ui-static/TextTooltip :added "4.0"}
(fact "creates the text tooltip for buttons and toggles"
  ^:hidden
  
  (defn.js TextTooltipDemo
    []
    (var [color setColor] (r/local "teal"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-static/TextTooltip"} 
[:% n/Row
       [:% n/Tabs
        {:data ["purple" "teal" "blue" "green"]
         :value color
         :setValue setColor}]] 
[:% ui-static/TextTooltip
       {:design {:type "light"}
        :content "HELLO WORLD"}]))))

^{:refer melbourne.ui-static/TextDisplay :added "4.0"}
(fact "creates the text display"
  ^:hidden
  
  (defn.js TextDisplayDemo
    []
    (var [color setColor] (r/local "teal"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-static/TextDisplay"} 
[:% n/Row
       [:% n/Tabs
        {:data ["purple" "teal" "blue" "green"]
         :value color
         :setValue setColor}]] 
[:% ui-static/TextDisplay
       {:design {:type "light"}
        :content "HELLO WORLD"}])))
  
  (def.js MODULE (!:module)))
