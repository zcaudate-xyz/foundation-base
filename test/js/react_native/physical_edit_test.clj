(ns js.react-native.physical-edit-test
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
   :require [[js.core :as j]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.physical-edit :as physical-edit]
             ]
   :export [MODULE]})

^{:refer js.react-native.physical-edit/createPan :added "4.0" :unchecked true}
(fact "creates a PanResponder"
  ^:hidden
  
  (defn.js CreatePanDemo
    []
    (let [axis       "horizontal"
          position   (a/val 0)
          size 40
          responder  (physical-edit/createPan {:pan {:dx position}
                                               :onPressOut
                                               (fn []
                                                 (. (a/spring position
                                                              {:toValue 0
                                                               :useNativeDriver false})
                                                    (start)))})]
      (return
       (n/EnclosedCode 
{:label "js.react-native.physical-edit/createPan"} 
[:% n/Row
         [:% physical-base/Box
          #{[:indicators #{position}
             :style [{:justifyContent "center"
                      :alignItems "center"
                      :height 50
                      :flex 1}]
             :inner [(j/assign
                      {:component n/View
                       :style [(:? (== axis "horizontal")
                                   {:left (- (/ size 2))}
                                   {:top (- (/ size 2))})
                               {:cursor "grab"
                                :height size
                                :width size
                                :borderRadius (/ size 2)
                                :backgroundColor "red"}]
                       :transformations
                       {:position
                        (fn [v]
                          (return {:style {:transform [{(:? (== axis "horizontal")
                                                            "translateX"
                                                            "translateY")
                                                        v}]}}))}}
                      
                      responder.panHandlers)]]}]
         [:% physical-base/Tag
          {:indicator position}]])))))

^{:refer js.react-native.physical-edit/createPanVelocity
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a PanResponder"
  ^:hidden
  
  (defn.js CreatePanVelocityDemo
    []
    (let [axis       "horizontal"
          position   (a/val 0)
          rotation   (a/val 0)
          speed      (r/ref 0)
          size 40
          responder  (physical-edit/createPan {:pan {:dx position}
                                               :onPressOut
                                               (fn []
                                                 (. (a/spring position
                                                              {:toValue 0
                                                               :useNativeDriver false})
                                                    (start)))})]
      (r/init []
        (var interval (j/setInterval
                       (fn []
                         (when (< 0.1 (j/abs (r/curr speed)))
                           (a/setValue
                            rotation
                            (+ rotation._value
                               (* 0.1 (r/curr speed))))))
                       20))
        (a/addListener position
                       (fn [#{value}]
                         (r/curr:set speed value)))
        (return (fn []
                  (j/clearInterval interval))))
      (return
       (n/EnclosedCode 
{:label "js.react-native.physical-edit/createPanVelocity"} 
[:% n/Row
         [:% physical-base/Box
          {:indicators #{rotation}
           :style {:height 40
                   :width 40
                   :borderBottom "5px solid black"
                   :backgroundColor "blue"}
           :transformations
           {:rotation (fn:> [v]
                        {:style {:transform [{:rotateZ (+ v "deg")}]}})}}]
         [:% physical-base/Box
          #{[:indicators #{position}
             :style [{:justifyContent "center"
                      :alignItems "center"
                      :height 50
                      :flex 1}]
             :inner [(j/assign
                      {:component n/View
                       :style [(:? (== axis "horizontal")
                                   {:left (- (/ size 2))}
                                   {:top (- (/ size 2))})
                               {:cursor "grab"
                                :height size
                                :width size
                                :borderRadius (/ size 2)
                                :backgroundColor "red"}]
                       :transformations
                       {:position
                        (fn [v]
                          (return {:style {:transform [{(:? (== axis "horizontal")
                                                            "translateX"
                                                            "translateY")
                                                        v}]}}))}}
                      
                      responder.panHandlers)]]}]
         [:% physical-base/Tag
          {:indicator position}]])))))


^{:refer js.react-native.physical-edit/Progress
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a Progress element"
  ^:hidden
  
  (defn.js ProgressDemo
    []
    (var position     (a/val 100))
    (var percentage   (a/derive (fn:> [p]
                                  (j/max 0 (j/min 100 (j/floor (/  p 2)))))
                                [position]))
    (var responder    (r/const (physical-edit/createPan
                                {:pan {:dx position}
                                 :absolute true})))
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-edit/ProgressDemo"} 
[:% n/Row
       [:% n/View
        [:% physical-base/Box
         {:indicators #{position
                        percentage} 
          :style [{:height 20
                   :backgroundColor "blue"
                   :width 220}]
          :addons []
          :inner [(j/assign
                   {:component n/View
                    :style [{:cursor "grab"
                             :height 20
                             :width 20
                             :backgroundColor "red"}]
                    :transformations
                    {:position
                     (fn [v]
                       (return {:style {:transform
                                        [{:translateX (j/max 0 (j/min 200 v))}]}}))}}
                   
                   responder.panHandlers)
                  {:component n/View
                   :style {:alignItems "end"}
                   :inner
                   [{:component n/View
                     :style [{:height 20
                              :width 20
                              :backgroundColor "green"}]
                     :transformations
                     {:percentage
                      (fn [v]
                        (return {:style {:width (* 2.2 v)}}))}}]}]}]]
       [:% n/Fill]
       [:% physical-base/Tag
        {:indicator percentage}]]))))

^{:refer js.react-native.physical-edit/usePanTouchable :added "4.0" :unchecked true}
(fact "creates a pan touchable responder for slider, picker and spinner"
  ^:hidden
  
  (def.js MODULE (!:module))
  
  )

