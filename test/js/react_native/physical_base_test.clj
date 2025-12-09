(ns js.react-native.physical-base-test
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
             [js.react-native.helper-color :as c]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as ui]
             ]
   })

^{:refer js.react-native.physical-base/Tag :added "4.0" :unchecked true}
(fact "listens to a single indicator to set ref"
  ^:hidden
  
  (defn.js TagDemo
    []
    (let [[active setActive]  (r/local 1)
          ind    (a/useBinaryIndicator
                  active
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})]
      (return
       (n/EnclosedCode 
{:label "js.react-native.physical-base/Tag"} 
[:% n/Row
         [:% n/Button
          {:title "PUSH"
           :onPress (fn [] (setActive (not active)))}]
         [:% n/Fill]
         [:% ui/Tag
          {:indicator ind}]])))))

^{:refer js.react-native.physical-base/transformInner :added "4.0" :unchecked true}
(fact "allow inner components access to chords and indicators")

^{:refer js.react-native.physical-base/transformProps :added "4.0" :unchecked true}
(fact "higher order react function or transforming props")

^{:refer js.react-native.physical-base/transformInnerFn :added "4.0" :unchecked true}
(fact "transforms inner props")

^{:refer js.react-native.physical-base/Box :added "4.0" :unchecked true}
(fact "demo of indicators"
  ^:hidden
  
  (defn.js BoxDemo
    []
    (let [[val0 setVal0]  (r/local true)
          ind0    (a/useBinaryIndicator
                  val0
                  {:default {:type "timing"
                             :duration 500
                             :easing a/linear}})

          [val1 setVal1]  (r/local false)
          ind1    (a/useBinaryIndicator
                  val1
                  {:default {:type "timing"
                             :duration 500
                             :easing a/linear}})

          [val2 setVal2]  (r/local true)
          ind2    (a/useBinaryIndicator
                  val2
                  {:default {:type "timing"
                             :duration 500
                             :easing a/linear}})]
      (return
       (n/EnclosedCode 
{:label "js.react-native.physical-base/Box"} 
[:% n/Row 
         [:% n/Row
          {:style {:flex 1}}
          [:% n/Button
           {:title "Ind0"
            :onPress (fn [] (setVal0 (not val0)))}]
          [:% n/Padding {:style {:width 10}}]
          [:% ui/Tag
           {:indicator ind0}]]
         [:% n/Row
          {:style {:flex 1}}
          [:% n/Button
           {:title "Ind1"
            :onPress (fn [] (setVal1 (not val1)))}]
          [:% n/Padding {:style {:width 10}}]
          [:% ui/Tag
           {:indicator ind1}]]
         [:% n/Row
          {:style {:flex 1}}
          [:% n/Button
           {:title "Ind2"
            :onPress (fn [] (setVal2 (not val2)))}]
          [:% n/Padding {:style {:width 10}}]
          [:% ui/Tag
           {:indicator ind2}]]] 
[:% ui/Box
         {:style {:height 200
                  :backgroundColor "hsl(12,100.00%,47.51%)"}
          :indicators #{ind0 ind1 ind2}
          :transformations
          {:ind0 (fn [v]
                   (return {:style {:backgroundColor
                                    (c/mix ["red" "green"]
                                           v)}}))}
          :inner [{:component n/View
                   :style {:height 20
                           :width 20}
                   :transformations
                   {:ind0 (fn [v]
                            (return {:style {:backgroundColor
                                             (c/mix ["green" "red"]
                                                    v)}}))}}
                  {:component n/View
                   :style {:margin 20
                           :height 20
                           :width 20}
                   :transformations
                   {:ind1 (fn [v]
                            (return {:style {:transform [{:scale (+ 1 v)}]
                                             :backgroundColor
                                             (c/mix ["blue" "yellow"]
                                                    v)}}))}}
                  {:component n/View
                   :style {:margin 40
                           :height 20
                           :width 20}
                   :transformations
                   {:ind2 (fn [v]
                            (return {:style {:transform [{:scale (+ 1 v)}]
                                             :backgroundColor (c/mix ["orange" "purple"]
                                                                     v)}}))}}]}])))))

^{:refer js.react-native.physical-base/Text :added "4.0" :unchecked true}
(fact "creates a text element"
  ^:hidden
  
  (defn.js TextDemo
    []
    (let [[val0 setVal0]  (r/local true)
          ind0    (a/useBinaryIndicator
                  val0
                  {:default {:type "timing"
                             :duration 500
                             :easing a/linear}})]
      (return
       (n/EnclosedCode 
{:label "js.react-native.physical-base/Text"} 
[:% n/Row
         {:style {:height 50}}
         [:% ui/Text
              {:indicators {:ind0 ind0}
               :value "HELLO"
               :transformations
               {:ind0 (fn:> [v]
                        {:style {:fontSize (+ 20 (* 20 v))}})}}]] 
[:% n/Row
          {:style {:flex 1}}
          [:% n/Button
           {:title "Ind0"
            :onPress (fn [] (setVal0 (not val0)))}]
          [:% n/Padding {:style {:width 10}}]
          [:% ui/Tag
           {:indicator ind0}]])))))

^{:refer js.react-native.physical-base/useChordDisabled :added "4.0" :unchecked true}
(fact "constructs chord and indicator for disable flag")

^{:refer js.react-native.physical-base/useChordHighlighted :added "4.0" :unchecked true}
(fact "constructs chord and indicator for highlighted flag")

^{:refer js.react-native.physical-base/useChordHoverable :added "4.0" :unchecked true}
(fact "constructs chord and indicator for hovering motion")

^{:refer js.react-native.physical-base/useChordPressable :added "4.0" :unchecked true}
(fact "constructs chord and indicator for pressing motion")

^{:refer js.react-native.physical-base/useChordEmptyable :added "4.0" :unchecked true}
(fact "constructs chord and indicator for empty value on inputs")

^{:refer js.react-native.physical-base/useChordFocusable :added "4.0" :unchecked true}
(fact "constructs chord and indicator for focusing motion")

^{:refer js.react-native.physical-base/useChordDraggable :added "4.0" :unchecked true}
(fact "constructs chord and indicator for drag enable and drag capture motion")

^{:refer js.react-native.physical-base/mergeChords :added "4.0" :unchecked true}
(comment "merges all chords, indicators and methods"
  ^:hidden
  
  (ui/mergeChords [{:chord {:a "A"}
                    :indicators {:a "A"}
                    :getA "getA"
                    :setA "setA"}
                   {:chord {:b "B"}
                    :indicators {:b "B"}
                    :getB "getB"
                    :setB "setB"}
                   {:chord {:c "C"}
                    :indicators {:c "C"}
                    :getC "getC"
                    :setC "setC"}])
  => {"getB" "getB",
      "setB" "setB",
      "getC" "getC",
      "getA" "getA",
      "setC" "setC",
      "setA" "setA",
      "indicators" {"a" "A", "b" "B", "c" "C"},
      "chord" {"a" "A", "b" "B", "c" "C"}})

^{:refer js.react-native.physical-base/useIndicatorCapture :added "4.0" :unchecked true}
(fact "allow capture of indicators from a top level element")

^{:refer js.react-native.physical-base/useHoverable :added "4.0" :unchecked true}
(fact "helper function for hoverable targets")

^{:refer js.react-native.physical-base/HoverableTarget :added "4.0" :unchecked true}
(fact "an element that responds when mouse is hovering")

^{:refer js.react-native.physical-base/useTouchable :added "4.0" :unchecked true}
(fact "helper function for all touchable type components")

^{:refer js.react-native.physical-base/TouchableBasePressing :added "4.0" :unchecked true}
(fact "base touchable with pressing indicator"
  ^:hidden
  
  (defn.js TouchableBasePressingDemo
    []
    (let [[active setActive]   (r/local true)]
      (return
       (n/EnclosedCode 
{:label "js.react-native.physical-base/TouchableBasePressing"} 
[:% ui/TouchableBasePressing
         {:style {:justifyContent "center"
                  :alignItems "center"
                  :width 100}
          :inner [{:component n/Text
                   :children ["HELLO"]
                   :style {:backgroundColor "blue"
                           :color "white"
                           :padding 10}
                   :transformations
                   (fn [#{pressing
                                hovering}]
                     (return
                      {:style {:opacity   (- 1 (* pressing 0.4))
                               :borderRadius (j/floor (* pressing 10))
                               :transform [{:translateX (* pressing 140)}
                                           {:scale (+ 1 (* (j/max pressing hovering) 0.4))}]}}))}]
          :addons [{:component n/TextInput
                    :size 11
                    :editable false
                    :multiline true
                    :style      {:backgroundColor "#eee"
                                 :marginTop 10
                                 :height 110}
                    :transformations
                    (fn [data chord]
                      (return {:text (n/format-obj #{data chord})}))}]}])))))

^{:refer js.react-native.physical-base/TouchableBinary :added "4.0" :unchecked true}
(fact "base touchable with single state"
  ^:hidden
  
  (defn.js TouchableBinaryDemo
    []
    (var [active setActive]    (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-base/TouchableBinary"} 
[:% ui/TouchableBinary
       {:style {:justifyContent "center"
                :alignItems "center"
                :width 100}
        :active active
        :onPress (fn []
                   (setActive (not active)))
        :inner [{:component n/Text
                 :style {:color "white"
                         :width 80
                         :padding 10}
                 :children ["PRESS"]
                 :transformations
                 (fn [#{pressing
                        hovering
                        active}]
                   (var transform [{:scale (+ 1
                                              (* (j/max pressing hovering)
                                                 0.1)
                                              (* pressing
                                                 0.3))}])
                   (var backgroundColor (+ "rgb(" (* 100 (- 1 active))
                                           "," (* 100 active), ", 0)"))
                   (if (< active 0.5)
                     (return {:style {:opacity (- 1 (* 0.3 active))
                                      :backgroundColor backgroundColor
                                      :transform transform}})
                     (return {:style {:opacity (+ 0.7 (* 0.3 active))
                                      :backgroundColor backgroundColor
                                      :transform transform}})))}]
        :addons [{:component n/TextInput
                  :size 11
                  :editable false
                  :multiline true
                  :style      {:backgroundColor "#eee"
                               :marginTop 10
                               :height 130}
                  :transformations
                  (fn [data chord]
                    (return {:text (n/format-obj #{data chord})}))}]}]))))

^{:refer js.react-native.physical-base/useInputable :added "4.0" :unchecked true}
(fact "helper function for inputable components")

^{:refer js.react-native.physical-base/TouchableInput :added "4.0" :unchecked true}
(fact "base touchable with single state"
  ^:hidden
  
  (defn.js TouchableInputDemo
    []
    (var [active setActive]    (r/local true))
    (var [value setValue] (r/local ""))
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-base/TouchableInput"} 
[:% ui/TouchableInput
       {:style {:justifyContent "center"
                :alignItems "center"
                :backgroundColor "black"
                :color "white"
                :height 60
                :width 200}
        :value value
        :onChangeText setValue
        :styleContainer {:width 200}
        :onPress (fn []
                   (setActive (not active)))
        :indicatorParams {:focusing {:type "timing"
                                     :default {:duration 300}}}
        :addons [{:component n/TextInput
                  :size 11
                  :editable false
                  :multiline true
                  :style      {:backgroundColor "#eee"
                               :marginTop 10
                               :height 120}
                  :transformations
                  (fn [data chord]
                    (return {:text (n/format-obj #{data chord})}))}]}])))

  (def.js MODULE
    (do (:# (!:uuid))
        (!:module)))
  
  )
