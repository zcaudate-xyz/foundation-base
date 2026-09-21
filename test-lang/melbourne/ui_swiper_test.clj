(ns melbourne.ui-swiper-test
  (:use code.test)
  (:require [lang.core :as  l]
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
             [melbourne.ui-swiper :as ui-swiper]]
   :export [MODULE]})

^{:refer melbourne.ui-swiper/Swiper :added "0.1"}
(fact "creates a slim swiper"
  ^:hidden
  
  (defn.js SwiperDemo
    []
    (var [first setFirst] (r/local 5))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-swiper/Swiper"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-swiper/Swiper
         {:highlighted highlighted
          :disabled disabled
          :posEnabled true
          :posView [:% n/View
                    {:key "pos"
                     :style {:backgroundColor "red"
                             :height 100
                             :width 200}}]
          :negEnabled true
          :negView [:% n/View
                    {:key "neg"
                     :style {:backgroundColor "black"
                             :height 100
                             :width 200}}]
          :styleContainer {:height 300
                           :overflow "hidden"}
          :style {:width 300
                  :height 100
                  :cursor "grab"}}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}
        [:% ui-swiper/Swiper
         {:design {:type "dark"
                  }
          :highlighted highlighted
          :disabled disabled
          :posView [:% n/View
                    {:key "pos"
                     :style {:backgroundColor "red"
                             :height 100
                             :width 200}}]
          :negView [:% n/View
                    {:key "neg"
                     :style {:backgroundColor "black"
                             :height 100
                             :width 200}}]
          :styleContainer {:height 300
                           :overflow "hidden"}
          :style {:width 300
                  :height 100
                  :cursor "grab"}}]]] 
[:% n/Row
       [:% n/Button
        {:title "+1"
         :onPress (fn:> (setFirst (+ first 1)))}]
       [:% n/Button
        {:title "-1"
         :onPress (fn:> (setFirst (- first 1)))}]
       [:% n/Button
        {:title "H"
         :onPress (fn:> (setHighlighted (not highlighted)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn:> (setDisabled (not disabled)))}]
       [:% n/Text
        (n/format-entry #{first disabled highlighted})]])))
  
  (def.js MODULE (!:module))
  )
