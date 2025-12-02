(ns js.react-native.physical-carosel-test
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
             [js.react-native.model-roller :as model-roller]
             [xt.lang.base-lib :as k]]
   :export [MODULE]})

^{:refer js.react-native.physical-carosel/DigitCaroselManual
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a set of edit points"
  ^:hidden

  (def.js DIVISIONS 2)
  
  (def.js DIGITS
    ["0" "1"])

  (defn.js DigitCaroselManualDemo
    []
    (var values  (r/const (k/arr-map (k/arr-range -/DIVISIONS)
                                     (fn:> [i] (new a/Value i)))))
    (var lu      (r/const (k/arr-juxt values
                                      (fn:> [v] (+ "index" v._value))
                                      k/identity)))
    (var [offset0 setOffset0] (r/local 0))
    (var ioffset0   (a/useIndexIndicator offset0
                                         {:default {:duration 800}}
                                         (fn [progress #{status}]
                                           (when (== status "stopped")
                                             (model-roller/roller-set-values
                                              values
                                              -/DIVISIONS
                                              offset0
                                              (k/len -/DIGITS))))))
    (var modelFn (r/const (model-roller/roller-model -/DIVISIONS 10)))
    
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-carosel/DigitCaroselManual"} 
[:% n/Row
       [:% n/View
        {:style {:flex 1
                 :height 200
                 :backgroundColor "white"
                 :overflow "hidden"}}
        (j/map (k/arr-range -/DIVISIONS)
               (fn:> [index i]
                 [:% physical-base/Box
                  {:key i
                   :indicators {:offset ioffset0
                                :value (. values [index])}
                   :style [{:position "absolute"
                            :padding 2
                            :fontWeight "800"
                            :height 200
                            :width "100%"
                            :textAlign "center"
                            :backgroundColor "white"}]
                   :children [[:% n/View
                               {:key "A"
                                :style {:flex 1
                                        :justifyContent "center"
                                        :alignItems "center"}}
                               [:% n/Text
                                {:key "B"
                                 :style {:position "absolute"
                                         :top 0
                                         :left 0}}
                                (+ "HELLO" i)]
                               [:% n/Text
                                {:key "C"
                                 :style {:position "absolute"
                                         :top 0
                                         :right 0}}
                                (+ "HELLO" i)]
                               [:% n/Text
                                {:key "D"
                                 :style {:fontSize 100
                                         :backgroundColor "red"
                                         :shadowOpacity 0.8
                                         :shadowRadius 10
                                         :width 400}}
                                (+ "HELLO" i)]]]
                   :transformations
                   (fn [#{offset value}]
                     (var v (- offset index))
                     (var #{translate
                            scale
                            visible} (modelFn v))
                     (return
                      {:value (. -/DIGITS [value])
                       :style {:opacity (:? visible
                                            1
                                            0)
                               :zIndex (* 10 scale)
                               :transform
                               [#_{:scale scale}
                                {:translateX (* 8
                                                (k/sign translate)
                                                translate translate)}
                                #_{:translateY (* 0.5 (j/abs translate))}]}}))}]))]] 
[:% n/Row
       
       [:% n/Button
        {:title "-1"
         :onPress (fn:> (setOffset0 (- offset0 1)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "+1"
         :onPress (fn:> (setOffset0 (+ offset0 1)))}]
       [:% n/Text " "]
       [:% n/Fill]])))

  (def.js MODULE (!:module))
  
  )
