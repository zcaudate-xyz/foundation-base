(ns js.react-native.ui-swiper-test
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
             [js.react-native.ui-swiper :as ui-swiper]
             ]
   :export [MODULE]})

^{:refer js.react-native.ui-swiper/swiperTransform :added "4.0" :unchecked true}
(fact "transforms swiper position to screen")

^{:refer js.react-native.ui-swiper/swiperTheme :added "4.0" :unchecked true}
(fact "creates the swiper theme")

^{:refer js.react-native.ui-swiper/useSwiperPosition :added "4.0" :unchecked true}
(fact "helper function to connect swiper position")

^{:refer js.react-native.ui-swiper/Swiper :added "0.1"}
(fact "creates a slim swiper"
  ^:hidden
  
  (defn.js SwiperDemo
    []
    (var [first setFirst] (r/local 5))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-swiper/Swiper"} 
[:% n/Row
       #_{:style {:alignItems "center"
                :justifyContent "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "Swiper"]
       [:% ui-swiper/Swiper
        {:highlighted highlighted
         :disabled disabled
         :theme {:bgNormal "red"
                 :fgNormal "blue"
                 :bgHovered 0.7
                 :fgHovered 0.7
                 :bgPressed -0.2
                 :fgPressed 0.1}
         :posEnabled true
         :posView [:% n/View
                   {:key "pos"
                    :style {:backgroundColor "green"
                            :height 100
                            :width 200}}]
         :negEnabled true
         :negView [:% n/View
                   {:key "neg"
                    :style {:backgroundColor "blue"
                            :height 100
                            :width 200}}]
         :styleContainer {:height 300
                          :overflow "hidden"}
         :style {:width 300
                 :height 100
                 :cursor "grab"}
         ;;:styleContainer {:width 200}
         :addons [(physical-addon/tagAll
                   {:style {:paddingHorizontal 20
                            :height 300
                            :width 200
                            :flex 1}})]}]] 
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
