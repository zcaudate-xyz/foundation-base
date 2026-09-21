(ns melbourne.ui-base-palette-test
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
             [js.react.ext-form :as ext-form]
             [xt.event.base-form :as event-form]
             [melbourne.slim-common :as slim-common]
             [melbourne.base-palette :as base-palette]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]]
   :export [MODULE]})

^{:refer melbourne.base-palette/PaletteBase :adopt true :added "0.1"}
(fact "creates the dialog"
  ^:hidden
  
  (defn.js PaletteBaseDemo
    []
    (var [color setColor] (r/local "purple"))
    (var [type setType]   (r/local "dark"))
    (var [foil setFoil]   (r/local "background"))
    (var palette (base-palette/createPalette type color))
    (return
     (n/EnclosedCode 
      {:label "melbourne.base-palette/PaletteBase"} 
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
                                  (or foil "background"))}}
       (j/map ["primary" "error" "neutral" "background"]
              (fn [colorKey]
                (return
                 [:% n/View
                  {:key colorKey
                   :style {:padding 10}}
                  [:% n/Text
                   {:style {:color (base-palette/getColorRaw
                                    palette
                                    (:? (== foil "background")
                                        "neutral"
                                        "background"))
                            :margin 5}}
                   (j/toUpperCase colorKey)]
                  [:% n/Row
                   
                   [:% n/View
                    {:style {:backgroundColor (base-palette/getColorRaw
                                               palette
                                               colorKey
                                               "flatten")
                             :height 30
                             :width 30
                             :margin 5}}]
                   
                   [:% n/View
                    {:style {:backgroundColor (base-palette/getColorRaw
                                               palette
                                               colorKey
                                               "diminish")
                             :height 30
                             :width 30
                             :margin 5}}]
                   [:% n/View
                    {:style {:backgroundColor (base-palette/getColorRaw
                                               palette
                                               colorKey)
                             :height 30
                             :width 30
                             :margin 5}}]
                   [:% n/View
                    {:style {:backgroundColor (base-palette/getColorRaw
                                               palette
                                               colorKey
                                               "augment")
                             :height 30
                             :width 30
                             :margin 5}}]
                   
                   [:% n/View
                    {:style {:backgroundColor (base-palette/getColorRaw
                                               palette
                                               colorKey
                                               "sharpen")
                             :height 30
                             :width 30
                             :margin 5}}]]])))]))))

^{:refer melbourne.base-palette/PaletteTone :adopt true :added "0.1"}
(fact "creates the dialog"
  ^:hidden
  
  (defn.js PaletteToneDemo
    []
    (var [color setColor] (r/local "purple"))
    (var [type setType]   (r/local "dark"))
    (var [foil setFoil]   (r/local "background"))
    (var palette (base-palette/createPalette type color))
    (return
     (n/EnclosedCode 
{:label "melbourne.base-palette/PaletteTone"} 
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
                                  (or foil "background"))}}
       [:% n/View
        {:style {:padding 10}}
        [:% n/Text
         {:style {:color (base-palette/getColorRaw
                          palette
                          (:? (== foil "background")
                              "neutral"
                              "background"))
                  :margin 5}}
         "PRIMARY/BACKGROUND"]
        [:% n/Row
         (j/map [0 1 2 3 4 5 6 7]
                (fn [i]
                  (return
                   [:% n/View
                    {:key i
                     :style {:backgroundColor (base-palette/getColorRaw
                                                palette
                                                "primary"
                                                "mix"
                                                "background"
                                                i)
                             :height 30
                             :width 30
                             :margin 5}}])))]]
       [:% n/View
        {:style {:padding 10}}
        [:% n/Text
         {:style {:color (base-palette/getColorRaw
                          palette
                          (:? (== foil "background")
                              "neutral"
                              "background"))
                  :margin 5}}
         "PRIMARY/NEUTRAL"]
        [:% n/Row
         (j/map [0 1 2 3 4 5 6 7]
                (fn [i]
                  (return
                   [:% n/View
                    {:key i
                     :style {:backgroundColor (base-palette/getColorRaw
                                                palette
                                                "primary"
                                                "mix"
                                                "neutral"
                                                i)
                             :height 30
                             :width 30
                             :margin 5}}])))]]
       [:% n/View
        {:style {:padding 10}}
        [:% n/Text
         {:style {:color (base-palette/getColorRaw
                          palette
                          (:? (== foil "background")
                              "neutral"
                              "background"))
                  :margin 5}}
         "NEUTRAL/BACKGROUND"]
        [:% n/Row
         (j/map [0 1 2 3 4 5 6 7]
                (fn [i]
                  (return
                   [:% n/View
                    {:key i
                     :style {:backgroundColor (base-palette/getColorRaw
                                                palette
                                                "neutral"
                                                "mix"
                                                "background"
                                                i)
                             :height 30
                             :width 30
                             :margin 5}}])))]]

       [:% n/View
        {:style {:padding 10}}
        [:% n/Text
         {:style {:color (base-palette/getColorRaw
                          palette
                          (:? (== foil "background")
                              "neutral"
                              "background"))
                  :margin 5}}
         "PRIMARY/LIGHTEN"]
        [:% n/Row
         (j/map [0 1 2 3 4 5 6 7]
                (fn [i]
                  (return
                   [:% n/View
                    {:key i
                     :style {:backgroundColor (base-palette/getColorRaw
                                                palette
                                                "primary"
                                                "lighten"
                                                nil
                                                i)
                             :height 30
                             :width 30
                             :margin 5}}])))]]
       [:% n/View
        {:style {:padding 10}}
        [:% n/Text
         {:style {:color (base-palette/getColorRaw
                          palette
                          (:? (== foil "background")
                              "neutral"
                              "background"))
                  :margin 5}}
         "PRIMARY/DARKEN"]
        [:% n/Row
         (j/map [0 1 2 3 4 5 6 7]
                (fn [i]
                  (return
                   [:% n/View
                    {:key i
                     :style {:backgroundColor (base-palette/getColorRaw
                                                palette
                                                "primary"
                                                "darken"
                                                nil
                                                i)
                             :height 30
                             :width 30
                             :margin 5}}])))]]
       [:% n/View
        {:style {:padding 10}}
        [:% n/Text
         {:style {:color (base-palette/getColorRaw
                          palette
                          (:? (== foil "background")
                              "neutral"
                              "background"))
                  :margin 5}}
         "PRIMARY/SATURATE"]
        [:% n/Row
         (j/map [1 2 3 4 5 6 7]
                (fn [i]
                  (return
                   [:% n/View
                    {:key i
                     :style {:backgroundColor (base-palette/getColorRaw
                                                palette
                                                "primary"
                                                "saturate"
                                                nil
                                                i)
                             :height 30
                             :width 30
                             :margin 5}}])))]]
       [:% n/View
        {:style {:padding 10}}
        [:% n/Text
         {:style {:color (base-palette/getColorRaw
                          palette
                          (:? (== foil "background")
                              "neutral"
                              "background"))
                  :margin 5}}
         "PRIMARY/DESATURATE"]
        [:% n/Row
         (j/map [1 2 3 4 5 6 7]
                (fn [i]
                  (return
                   [:% n/View
                    {:key i
                     :style {:backgroundColor (base-palette/getColorRaw
                                                palette
                                                "primary"
                                                "desaturate"
                                                nil
                                                i)
                             :height 30
                             :width 30
                             :margin 5}}])))]]])))
  
  (def.js MODULE (!:module)))
