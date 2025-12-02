(ns js.react-native.model-roller-impl-test
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
             [js.react-native.physical-edit :as physical-edit]
             [xt.lang.base-lib :as k]]
   :export [MODULE]})

^{:refer js.react-native.model-roller/DigitRollerManual
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a set of edit points"
  ^:hidden

  (def.js DIVISIONS 7)
  
  (def.js DIGITS
    ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"])

  (defn.js DigitRollerManualDemo
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
    (var modelFn (r/const (model-roller/roller-model -/DIVISIONS 20)))
    (r/init []
      (model-roller/roller-set-values
       values
       -/DIVISIONS
       offset0
       (k/len -/DIGITS)))
    (return
     (n/EnclosedCode 
{:label "js.react-native.model-roller/DigitRollerManual"} 
[:% n/Row
       [:% n/View
        {:style {:height 80
                 :width 80
                 :backgroundColor "black"}}
        (j/map (k/arr-range -/DIVISIONS)
               (fn:> [index i]
                 [:% physical-base/Text
                  {:key i
                   :indicators {:offset ioffset0
                                :value (. values [index])}
                   :style [{:position "absolute"
                            :width 20
                            :padding 2
                            :fontWeight "800"
                            :textAlign "center"
                            :color "white"
                            :top 30
                            :left 20}]
                   :transformations
                   (fn [#{offset value}]
                     (var v (- offset index))
                     (var #{translate
                            scale
                            visible} (modelFn v))
                     (return
                      {:text  (. -/DIGITS [value])
                       :style {:opacity (:? visible
                                            scale
                                            0)
                               :transform
                               [{:scale (* 2 scale)}
                                {:translateY translate}
                                {:translateX (* 0.5 (j/abs translate))}]}}))}]))]
       [:% n/View
        [:% n/Button
         {:title "-1"
          :onPress (fn:> (setOffset0 (- offset0 1)))}]
        [:% n/Button
         {:title "+1"
          :onPress (fn:> (setOffset0 (+ offset0 1)))}]]
       [:% n/Text offset0]
       [:% n/Fill]]))))

^{:refer js.react-native.model-roller/DigitRollerPan
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a set of edit points"
  ^:hidden

  (def.js DIVISIONS_PAN 10)
  
  (def.js DIGITS
    ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"])

  
  (defn.js doubleTransform
    [offset i modelFn]
    (var v (- offset i))
    (var #{translate
           scale
           visible} (modelFn v))
    (return
     {:style
      {:opacity (:? visible
                    (k/mix -2 1 scale)
                    0)
       :zIndex (* 10 scale)
       :transform
       [{:translateY translate}
        #_{:translateX (* 0.5 (j/abs translate))}
        #_{:scaleX (k/mix 0.2 1 scale)}
        #_{:scaleY (k/mix 0.2 1 scale)}]}}))

  (defn.js make-values
    [values divisions]
    (var total (k/len values))
    (var n (/ (k/lcm total divisions)
              total))
    (return (k/arr-mapcat (k/arr-repeat values n)
                          k/identity)))
  
  (defn.js get-element
    [offset index divisions values]
    (var noffset  (j/round offset))
    (var center   (k/mod-pos noffset divisions))
    (var shifted     (- index center))
    (var shifted-mod (k/mod-pos shifted divisions))
    (var normalised  (:? (< shifted-mod (/ divisions 2))
                         shifted-mod
                         (- shifted-mod divisions)))
    (var total (k/len values))
    (return (k/get-key values (k/mod-pos (+ noffset normalised)
                                         total))))
  
  (defn.js createComponent
    [index indicator modelFn transformFn style divisions values]
    (return
     {:component n/TextInput
      :editable false
      :style [{:position "absolute"
               :width 15
               :padding 2
               :fontWeight "800"
               :textAlign "center"
               :color "white"}
              style]
      :transformations
      {indicator
       (fn:> [offset]
         (j/assign (transformFn offset index modelFn)
                   {:text (-/get-element offset index divisions values)}))}}))
  
  (defn.js DigitRollerPanDemo
    []
    (var [position0 setPosition0] (r/local 50))
    (var offset0   (a/useIndexIndicator position0
                                        {:default {:duration 200}}))
    (var [position1 setPosition1] (r/local 0))
    (var offset1   (a/useIndexIndicator position1
                                        {:default {:duration 200}}))
    (var modelFn (r/const (model-roller/roller-model -/DIVISIONS 20)))
    (var DIGITS_ARRAY (r/const (-/make-values -/DIGITS -/DIVISIONS)))
    (var HOURS_ARRAY  (r/const (-/make-values -/DIGITS -/DIVISIONS)))
    (var position     (a/val 100))
    (var responder    (r/const (physical-edit/createPan
                                {:pan {:dy position}
                                 :absolute true})))
    (r/init []
      (a/addListener position
                     (fn [#{value}]
                       (var normed (j/floor (/ (- position._value) 2)))
                       (when (not= position0 normed)
                         (setPosition0 normed)))))
    (r/watch [position0]
      (setPosition1 (j/floor (/ position0 10))))
    (return
     (n/EnclosedCode 
{:label "js.react-native.model-roller/DigitRollerPan"} 
[:% n/Row
       [:% physical-base/Box
        {:indicators {:offset0 offset0
                      :offset1 offset1} 
         :style {:height 80
                 :width 80
                 :backgroundColor "black"}
         :addons [(:.. (j/map (k/arr-range -/DIVISIONS)
                              (fn:> [index]
                                (-/createComponent index
                                                   "offset1"
                                                   modelFn
                                                   -/doubleTransform
                                                   {:top 30
                                                    :left 18}
                                                   -/DIVISIONS
                                                   HOURS_ARRAY))))
                  (:.. (j/map (k/arr-range -/DIVISIONS)
                              (fn:> [index]
                                (-/createComponent index
                                                   "offset0"
                                                   modelFn
                                                   -/doubleTransform
                                                   {:top 30
                                                    :left 30}
                                                   -/DIVISIONS
                                                   DIGITS_ARRAY))))]}]
       [:% physical-base/Box
        {:indicators #{position} 
         :style [{:justifyContent "end"
                  :alignItems "center"
                  :height 200
                  :backgroundColor "blue"
                  :width 30}]
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
                                       [{:translateY v}]}}))}}
                  
                  responder.panHandlers)]}]
       [:% n/Fill]
       [:% physical-base/Tag
        {:indicator position}]
       ])))
  
  (def.js MODULE (!:module))
  
  )

(comment
  (defn.js CreatePanVelocityDemo
    []
    (let [axis       "horizontal"
          
          rotation   (a/val 0)
          speed      (r/ref 0)
          size 40
          ]
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
          {:indicator position}]]))))
  )
