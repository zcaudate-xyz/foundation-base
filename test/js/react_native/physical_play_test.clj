(ns js.react-native.physical-play-test
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
   })

^{:refer js.react-native.physical-play/DigitRollerStatic
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a set of edit points"
  ^:hidden

  (def.js DATES
    ["1989" "1990" "1991" "1992" "1993" "1994" "1995" "1996" "1997" "1998"])

  (defn.js DigitRollerStaticDemo
    []
    (var modelFn (model-roller/roller-model 10 30))
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-play/DigitRollerStatic"} 
[:% n/Row
       [:% n/View
        {:style {:height 80
                 :width 80}}
        (j/map -/DATES
               (fn [date i]
                 (var #{translate
                        scale
                        visible} (modelFn (- i 5)))
                 (return
                  (:? visible
                      [[:% n/Text {:key date
                                   :style {:position "absolute"
                                           :padding 1
                                           :top 30
                                           :left 30
                                           :opacity (k/mix 0.5 1 scale
                                                           (fn:> [v] (* v v v v v v v)))
                                           :transform
                                           [{:translateY translate}
                                            {:translateX (* 0.15 (j/abs translate))}
                                            {:scaleY scale}]}}
                        date]]))))]
       [:% n/Fill]]))))

^{:refer js.react-native.physical-play/DigitRoller
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a set of edit points"
  ^:hidden

  (def.js DIVISIONS 6)
  
  (def.js DIGITS
    ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"])
  
  (def.js HOURS
    ["0" "1" "2" "3" "4" "5" "0" "1" "2" "3" "4" "5"])
  
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
  
  (defn.js singleTransform
    [offset i modelFn]
    (var v (- offset i))
    (var #{translate
           scale
           visible} (modelFn v))
    (return
     {:style
      {:opacity (:? visible
                    (k/mix -1.5 1 scale)
                    0)
       :zIndex (* 10 scale)
       :transform
       [{:translateY (- translate)}
        {:translateX (* 0.5 (j/abs translate))}
        {:scaleX (k/mix 0.2 1 scale)}
        {:scaleY (k/mix 0.2 1 scale)}]}}))
  
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
               :color "white"
               ;;:backgroundColor "black"
               }
              style]
      :transformations
      {indicator
       (fn:> [offset]
         (j/assign (transformFn offset index modelFn)
                   {:text (-/get-element offset index divisions values)}))}}))
  
  
  (defn.js DigitRollerSingleDemo
    []
    (var [position0 setPosition0] (r/local 9))
    (var offset0   (a/useIndexIndicator position0
                                        {:default {:duration 500}}))

    (var modelFn (r/const (model-roller/roller-model -/DIVISIONS 20)))
    (var DIGITS_ARRAY (r/const (-/make-values -/DIGITS -/DIVISIONS)))
  
    (var HOURS_ARRAY (r/const (-/make-values -/HOURS -/DIVISIONS)))
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-play/DigitRollerSingle"} 
[:% n/Row
       [:% physical-base/Box
        {:indicators {:offset0 offset0} 
         :style {:height 80
                 :width 80
                 :backgroundColor "black"}
         :addons [(:.. (j/map (k/arr-range -/DIVISIONS)
                              (fn:> [index]
                                (-/createComponent index
                                                   "offset0"
                                                   modelFn
                                                   -/singleTransform
                                                   {:top 30
                                                    :left 30}
                                                   -/DIVISIONS
                                                   DIGITS_ARRAY))))]}]
       [:% n/View
        [:% n/Button
         {:title "+1"
          :onPress (fn:> (setPosition0 (+ position0 1)))}]
        [:% n/Button
         {:title "-1"
          :onPress (fn:> (setPosition0 (- position0 1)))}]
        ]
       [:% n/Fill]])))

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
       [{:translateY (- translate)}
        #_{:translateX (* 0.5 (j/abs translate))}
        #_{:scaleX (k/mix 0.2 1 scale)}
        #_{:scaleY (k/mix 0.2 1 scale)}]}}))
  
  (defn.js DigitRollerDoubleDemo
    []
    
    (var [position0 setPosition0] (r/local 99))
    (var offset0   (a/useIndexIndicator position0
                                        {:default {:duration 200}}))
    (var [position1 setPosition1] (r/local 0))
    (var offset1   (a/useIndexIndicator position1
                                        {:default {:duration 200}}))
    (var modelFn (r/const (model-roller/roller-model -/DIVISIONS 20)))
    (var DIGITS_ARRAY (r/const (-/make-values -/DIGITS -/DIVISIONS)))
  
    (var HOURS_ARRAY (r/const (-/make-values -/HOURS -/DIVISIONS)))
    (r/watch [position0]
      (setPosition1 (j/floor (/ position0 10))))
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-play/DigitRollerDouble"} 
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
       [:% n/View
        [:% n/Button
         {:title "+1"
          :onPress (fn:> (setPosition0 (+ position0 1)))}]
        [:% n/Button
         {:title "-1"
          :onPress (fn:> (setPosition0 (- position0 1)))}]
        ]
       [:% n/Fill]]))))

^{:refer js.react-native.physical-play/DigitClock
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a set of edit points"
  ^:hidden

  (def.js CLOCK_DIVISIONS 10)
  
  (defn.js useInterval
    [f ms]
    (var interval (r/ref nil))
    (var [active setActive] (r/local true))
    (r/watch [active]
      (when (and active
                 (not (r/curr interval)))
        (r/curr:set interval (j/setInterval f ms)))
      (when (and (not active)
                 (r/curr interval))
        (j/clearInterval (r/curr interval))
        (r/curr:set interval nil)))
    (return [active setActive]))
  
  (defn.js clockTransform
    [offset i modelFn]
    (var v (- offset i))
    (var #{translate
           scale
           visible} (modelFn v))
    (return
     {:style
      {:opacity (:? visible
                    (k/mix -10 1 scale)
                    0)
       :zIndex (* 100 scale)
       :transform
       [{:translateY (* -1.4 translate)}
        {:scale (k/mix -4 2 scale)}
        #_#_{:scaleX (k/mix -3 2 scale)}
        {:scaleY (k/mix -3 2 scale)}]}}))
  
  (defn.js DigitClockDemo
    []
    (var [seconds0 setSeconds0] (r/local (j/floor (/ (k/now-ms) 1000))))
    (var iseconds0  (a/useIndexIndicator seconds0
                                        {:default {:duration 100}}))
    
    (var [seconds1 setSeconds1] (r/local  (j/floor (/ seconds0 10))))
    (var iseconds1   (a/useIndexIndicator seconds1
                                          {:default {:duration 100}}))
    (var [minutes0 setMinutes0] (r/local (j/floor (/ (k/now-ms) 1000))))
    (var iminutes0  (a/useIndexIndicator minutes0
                                        {:default {:duration 100}}))
    
    (var [minutes1 setMinutes1] (r/local  (j/floor (/ minutes0 10))))
    (var iminutes1   (a/useIndexIndicator minutes1
                                          {:default {:duration 100}}))
    (var [active setActive] (-/useInterval (fn []
                                               (var now (j/floor (/ (k/now-ms) 1000)))
                                               (when (not= seconds0 now)
                                                 (setSeconds0 now)))
                                             200))
    
    (var hexFn  (r/const (model-roller/roller-model 6 20)))
    (var modelFn (r/const (model-roller/roller-model -/CLOCK_DIVISIONS 20)))
    
    (var DIGITS_ARRAY (r/const (-/make-values -/DIGITS -/CLOCK_DIVISIONS)))
    (r/watch [seconds0]
      (setSeconds1 (j/floor (/ seconds0 10))))
    (r/watch [seconds1]
      (setMinutes0 (j/floor (/ seconds0 60))))
    (r/watch [minutes0]
      (setMinutes1 (j/floor (/ seconds0 600))))
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-play/DigitClock"} 
[:% n/Row
       [:% n/View
        [:% physical-base/Box
         {:indicators {:minutes1 iminutes1} 
          :style {:height 40
                  :width 20
                  :backgroundColor "black"}
          :addons [(:.. (j/map (k/arr-range 6)
                               (fn:> [index]
                                 (-/createComponent index
                                                    "minutes1"
                                                    hexFn
                                                    -/clockTransform
                                                    {:top 10
                                                     :left 2}
                                                    6
                                                    -/HOURS
                                                    ))))]}]]
       [:% n/View
        [:% physical-base/Box
         {:indicators {:minutes0 iminutes0} 
          :style {:height 40
                  :width 20
                  :backgroundColor "black"}
          :addons [(:.. (j/map (k/arr-range -/CLOCK_DIVISIONS)
                               (fn:> [index]
                                 (-/createComponent index
                                                    "minutes0"
                                                    modelFn
                                                    -/clockTransform
                                                    {:top 10
                                                     :left 2}
                                                    -/CLOCK_DIVISIONS
                                                    DIGITS_ARRAY))))]}]]
       [:% n/View
        {:style {:height 40
                 :width 5
                 }}]
       [:% n/View
        [:% physical-base/Box
         {:indicators {:seconds1 iseconds1} 
          :style {:height 40
                  :width 20
                  :backgroundColor "black"}
          :addons [(:.. (j/map (k/arr-range 6)
                               (fn:> [index]
                                 (-/createComponent index
                                                    "seconds1"
                                                    hexFn
                                                    -/clockTransform
                                                    {:top 10
                                                     :left 2}
                                                    6
                                                    -/HOURS
                                                    ))))]}]]
       [:% n/View
        [:% physical-base/Box
         {:indicators {:seconds0 iseconds0} 
          :style {:height 40
                  :width 20
                  :backgroundColor "black"}
          :addons [(:.. (j/map (k/arr-range -/CLOCK_DIVISIONS)
                               (fn:> [index]
                                 (-/createComponent index
                                                    "seconds0"
                                                    modelFn
                                                    -/clockTransform
                                                    {:top 10
                                                     :left 2}
                                                    -/CLOCK_DIVISIONS
                                                    DIGITS_ARRAY))))]}]]] 
[:% n/View
       [:% n/Button
        {:title "CLOCK"
         :onPress (fn:> (setActive (not active)))}]] 
[:% n/Text seconds0] 
[:% n/Fill])))

  
  
  )

