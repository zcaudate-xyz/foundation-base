(ns melbourne.ui-stepper
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.model-roller :as model-roller]
             [melbourne.ui-group :as ui-group]]
   :export [MODULE]})

(defn.js StepperTabs
  "creates the stepper tabs"
  {:added "0.1"}
  [#{[design
      variant
      total
      style
      styleContainer
      index
      setIndex
      (:.. rprops)]}]
  (return
   [:% ui-group/TabsIndexed
    #{[design
       variant
       setIndex
       index
       :style [{:height 10
                :width 10
                :borderRadius 10
                :padding 2
                :marginHorizontal 6}
               (:.. (data/arrayify style))]
       :items (data/arr-repeat "" total)
       (:.. rprops)]}]))

(defn.js stepperOffset
  "offset function for the stepper"
  {:added "0.1"}
  [modelFn total offset index]
  (var v (- offset index))
  (var #{translate
         scale
         visible} (modelFn v))
  (return
   {:style {:opacity (:? visible
                         (math/mix 0 1 scale)
                         0)
            :zIndex (:? visible (* 100 scale) -100)}}))

(defn.js Stepper
  "creates a stepper"
  {:added "0.1"}
  [#{[index
      setIndex
      style
      pageStyle
      (:= offsetFn -/stepperOffset)
      (:= pages [])
      (:.. rprops)]}]
  (var total (Math.max (xt/x:len pages) 1))
  (var iindicator   (a/useCircularIndicator
                     index
                     {:default {:type "timing"
                                :duration 500
                                :easing a/linear}}
                     
                     nil
                     nil
                     total
                     nil))
  (var modelFn (r/const (model-roller/roller-model total 10)))
  (var pageFn
       (fn [page i]
         (return
          [:% physical-base/Box
           {:key (+ "page" i)
            :style [{:position "absolute"}
                    (:.. (data/arrayify pageStyle))]
            :indicators {:offset iindicator}
            :children [[:% n/View
                        {:key "parent"}
                        page]]
            :transformations
            {:offset (fn:> [offset] (offsetFn modelFn total offset i))}}])))
  (return
   [:% n/View
    {:style [{:overflow "hidden"}
             (:.. (data/arrayify style))]}
    (j/map pages pageFn)]))

(def.js MODULE (!:module))


