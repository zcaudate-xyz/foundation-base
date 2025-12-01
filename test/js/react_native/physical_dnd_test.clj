(ns js.react-native.physical-dnd-test
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

^{:refer js.react-native.physical-dnd/DragAndDropDemo
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a PanResponder"
  ^:hidden
  
  (defn.js DragAndDropDemo
    []
    (let [axis       "horizontal"
          position   (a/val 0)
          size 40
          [dragging setDragging] (r/local false)
          idragging  (a/useBinaryIndicator dragging {})
          responder  (physical-edit/createPan {:pan {:dx position}
                                               :onPressIn
                                               (fn []
                                                 (setDragging true))
                                               :onPressOut
                                               (fn []
                                                 (setDragging false)
                                                 (. (a/spring position
                                                              {:toValue 0
                                                               :useNativeDriver false})
                                                    (start)))})]
      (return
       (n/EnclosedCode 
{:label "js.react-native.physical-dnd/DragAndDropDemo"} 
[:% n/Row
         [:% physical-base/HoverableTarget
          {:indicators {:dragging idragging}
           :style [{:justifyContent "center"
                    :alignItems "center"
                    :height 50
                    :backgroundColor "blue"
                    :flex 1}]
           :transformations (fn [m]
                              (return {:style
                                       {:backgroundColor
                                        (:? (and (> m.dragging 0.5)
                                                 (> m.hovering 0.5))
                                            ["yellow" "blue"])}}))}]
         [:% physical-base/Box
          #{[:indicators {:dragging idragging
                          :position position}
             :style [{:justifyContent "center"
                      :alignItems "center"
                      :height 50
                      :flex 1}]
             :inner [{:component n/View
                      :inner [(j/assign
                               {:component n/View
                                :style [(:? (== axis "horizontal")
                                            {:left (- (/ size 2))}
                                            {:top (- (/ size 2))})
                                        {:position "absolute"
                                         :cursor "grab"
                                         :height size
                                         :width size
                                         :borderRadius (/ size 2)
                                         :backgroundColor "red"
                                         :zIndex 100}]
                                :transformations
                                (fn [m]
                                  (return {:style {:backgroundColor
                                                   (:? (> m.dragging 0.5)
                                                       ["black" "red"])}}))}
                               
                               responder.panHandlers)
                              (j/assign
                               {:component n/View
                                :style [(:? (== axis "horizontal")
                                            {:left (- (/ size 2))}
                                            {:top -20})
                                        {:position "absolute"
                                         :cursor "grab"
                                         :height size
                                         :width size
                                         :borderRadius (/ size 2)
                                         :backgroundColor "grey"}]
                                :transformations
                                {:position
                                 (fn [v]
                                   (return {:style {:transform [{(:? (== axis "horizontal")
                                                                     "translateX"
                                                                     "translateY")
                                                                 v}]}}))}}
                               
                               responder.panHandlers)]}]]}]
         [:% physical-base/Tag
          {:indicator position}]]))))
  
  (def.js MODULE (!:module))
  
  )
