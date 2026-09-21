(ns pune.ui-sparkline-test
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [pune.ui-sparkline :as ui-sparkline]]
   :export [MODULE]})

^{:refer pune.ui-sparkline/getPath :added "0.1"}
(fact "creates a path")

^{:refer pune.ui-sparkline/Sparkline :added "0.1"}
(fact "creates the sub menu toggle"
  ^:hidden
  
  (defn.js SparklineDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.ui-sparkline/Sparkline"} 
[:% n/Row
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#eee"}}
        [:% ui-sparkline/Sparkline
         #{[:design {:type "light"}
            :height 30
            :width  150
            :values [45 42 42 45 62 59 59 58 57 59 53 53 41 41 59]]}]]
       [:% n/View
        {:style {:padding 5
                 :paddingRight 100
                 :backgroundColor "#333"}}
        [:% ui-sparkline/Sparkline
         #{[:design {:type "dark"}
            :height 30
            :width  100
            :values [45 42 42 45 62 59 59 58 57 59 53 53 41 41 59]]}]]] 
(r/% n/TextDisplay
           {:data [45 42 42 45 62 59 59 58 57 59 53 53 41 41 59]}))))

  (def.js MODULE (!:module)))

