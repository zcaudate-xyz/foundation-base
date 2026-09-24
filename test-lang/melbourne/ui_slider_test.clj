(ns melbourne.ui-slider-test
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
             [js.react-native :as n]
             [melbourne.ui-slider :as ui-slider]]
   :export [MODULE]})

^{:refer melbourne.ui-slider/Slider :added "0.1"}
(fact "creates a horizontal slider"
  ^:hidden
  
  (defn.js SliderHDemo
    []
    (var [value setValue] (r/local 5))
    (return
     (n/EnclosedCode 
{:label " melbourne.ui-slider/SliderH"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-slider/Slider
         #{[:design {:type "light"}
            :length 200
            value setValue]}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}
        [:% ui-slider/Slider
         #{[:design {:type "dark"}
            :length 200
            value setValue]}]]] 
[:% n/Text
       (n/format-entry #{value})])))

  (defn.js SliderVDemo
    []
    (var [value setValue] (r/local 55))
    (return
     (n/EnclosedCode 
{:label " melbourne.ui-slider/SliderV"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-slider/Slider
         #{[:design {:type "light"}
            :length 200
            :layout "vertical"
            value setValue
            :max 99
            :min 1
            :step 1]}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}
        [:% ui-slider/Slider
         #{[:design {:type "dark"}
            :length 200
            :layout "vertical"
            value setValue
            :max 99
            :min 1
            :step 1]}]]] 
[:% n/Text
       (n/format-entry #{value})])))

  (def.js MODULE (!:module))
  )
