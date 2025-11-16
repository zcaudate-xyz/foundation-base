(ns js.react-native.ui-frame
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]]})

(defn.js translationOffset
  "creates the offset translation"
  {:added "4.0"}
  [location visible size]
  (cond (== location "left")
        (return {:translateX (* (- visible 1) size)})

        (== location "right")
        (return {:translateX (* (- 1 visible) size)})

        (== location "top")
        (return {:translateY (* (- visible 1) size)})

        (== location "bottom")
        (return {:translateY (* (-  1 visible) size)})))

(defn.js FramePane
  "creates a Frame Pane"
  {:added "4.0"}
  [#{[style
      (:= aspect "width")
      (:= size 60)
      location
      visible
      fade
      children
      (:= indicatorParams
          {:default {:type "timing"
                     :duration 200
                     :easing a/linear}})]}]
  (var isMounted (r/useIsMounted))
  (var [showing vindicator] (a/useShowing visible indicatorParams isMounted))
  (return
   [:% physical-base/Box
    {:chord #{visible}
     :indicators {:visible vindicator}
     :style [{aspect 0}
             (:.. (j/arrayify style))]
     :transformations
     {:visible (fn [visible]
                 (return {:style {:overflow (:? (< visible 0.95)
                                                "hidden"
                                                "default")
                                  aspect (* visible size)}}))}
     :inner [{:component n/View
              :key "pane"
              :style {:flex 1}
              :children (:? (or showing visible)
                            children)
              :transformations
              {:visible
               (fn [visible]
                 (return {:style {:opacity (:? fade visible 1)
                                  :transform
                                  [(-/translationOffset location visible size)]}}))}}]}]))

(defn.js Frame
  "creates a Frame"
  {:added "4.0"}
  [#{[topComponent
      topProps
      topVisible
      topFade
      topSize
      topStyle
      topIndicatorParams
      bottomComponent
      bottomProps
      bottomVisible
      bottomFade
      bottomSize
      bottomStyle
      bottomIndicatorParams
      leftComponent
      leftProps
      leftVisible
      leftFade
      leftSize
      leftStyle
      leftIndicatorParams
      rightComponent
      rightProps
      rightVisible
      rightFade
      rightSize
      rightStyle
      rightIndicatorParams
      children]}]
  (return
   [:% n/View
    {:style {:flex 1}}
    (:? topComponent
        [:% -/FramePane
         {:aspect "height",
          :fade topFade,
          :indicatorParams topIndicatorParams,
          :key topSize,
          :location "top",
          :size topSize,
          :style topStyle,
          :visible topVisible}
         (r/createElement topComponent topProps)])
    [:% n/View
     {:style {:flexDirection "row-reverse"
              :flex 1}}
     (:? rightComponent
         [:% -/FramePane
          {:aspect "width",
           :fade rightFade,
           :indicatorParams rightIndicatorParams,
           :key rightSize,
           :location "right",
           :size rightSize,
           :style rightStyle,
           :visible rightVisible}
          (r/createElement rightComponent rightProps)])
     [:% n/View
      {:style {:flex 1}}
      [:% n/View
       {:style {:position "absolute"
                :top 0
                :bottom 0
                :left 0
                :right 0
                :overflow "auto"}}
       children]]
     (:? leftComponent
         [:% -/FramePane
          {:aspect "width",
           :fade leftFade,
           :indicatorParams leftIndicatorParams,
           :key leftSize,
           :location "left",
           :size leftSize,
           :style leftStyle,
           :visible leftVisible}
          (r/createElement leftComponent leftProps)])]
    (:? bottomComponent
        [:% -/FramePane
         {:aspect "height",
          :fade bottomFade,
          :indicatorParams bottomIndicatorParams,
          :key bottomSize,
          :location "bottom",
          :size bottomSize,
          :style bottomStyle,
          :visible bottomVisible}
         (r/createElement bottomComponent bottomProps)])]))

(comment
  )
