(ns js.react-native.ui-tooltip
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
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.model-geom :as model-geom]
             [js.react-native.model-context :as model-context]
             [xt.lang.base-lib :as k]]})

(defn.js tooltipPortalOffset
  "calculates the portal offset"
  {:added "4.0"}
  [layouts]
  (when layouts
    (var #{sink
           host} layouts)
    (return
     {:left (- (. host px)
               (. sink px))
      :top  (- (. host py)
               (. sink py))})))

(defn.js tooltipContextOffset
  "calculates the context offset"
  {:added "4.0"}
  [layouts
   #{position
     alignment
     margin
     marginCross}]
  (when layouts
    (var #{host content} layouts)
    (return
     (model-context/contextCoordinate
      #{host
        content
        position
        alignment
        margin
        marginCross}))))

(defn.js TooltipRawArrow
  "creates the tooltip arrow"
  {:added "4.0"}
  [#{color
       point
       baseLength
       baseHeight}]
    (return [:% n/View
             {:style (model-geom/triangleBaseStyle
                      color
                      point
                      baseLength
                      baseHeight)}]))

(defn.js TooltipInnerContentArrow
  "places arrow relative to content"
  {:added "4.0"}
  [#{position
     alignment
     arrow
     layouts
     arrowLayout}]
  (return
   [:% n/View
    {:key "arrow"
     :style [{:position "absolute"
              :zIndex -100}
             (-/tooltipContextOffset
              (:? layouts
                  {:host (. layouts content) 
                   :content arrowLayout})
              {:position (. arrow point)
               :margin -1
               :marginCross 1
               :alignment alignment})]}
    [:% -/TooltipRawArrow
     #{(:.. arrow)}]]))

(defn.js TooltipInnerHostArrow
  "places arrow relative to host"
  {:added "4.0"}
  [#{position
     alignment
     arrow
     layouts
     arrowLayout}]
  (return
   [:% n/View
    {:key "arrow"
     :style [{:position "absolute"
              :zIndex -100}
             (-/tooltipContextOffset
              (:? layouts
                  {:host (. layouts host) 
                   :content arrowLayout})
              {:position position
               :margin 1
               :marginCross -1
               :alignment alignment})]}
    [:% -/TooltipRawArrow
     #{(:..  arrow)}]]))

(defn.js TooltipInner
  "creates the tooltip inner (with portal)"
  {:added "4.0"}
  [#{[hostRef
      chord
      indicators
      position
      alignment
      style
      arrow
      setVisible
      children]}]
  (var [layouts setLayouts] (r/local))
  (var dims (n/useWindowDimensions))
  (var #{sinkRef
         setSinkRef
         contentRef
         getLayouts}  (n/usePortalLayouts hostRef setLayouts))
  (r/watch [dims children] (getLayouts))
  
  (var arrowLayout (:? (or (== "top" position)
                           (== "bottom" position))
                       {:height (. arrow baseHeight)
                        :width (. arrow baseLength)}
                       {:height (. arrow baseLength)
                        :width (. arrow baseHeight)}))
  (var #{placement
         backdrop
         backdropStyle} arrow)
  (return
   [:% n/Portal
    {:onSink setSinkRef}
    [:<>
     (:? (and backdrop
              (. chord visible))
         [:% n/TouchableWithoutFeedback
          {:onPress (fn []
                      (setVisible false))}
          [:% n/View
           {:style {:position "absolute"
                    :top 0 :bottom 0 :left 0 :right 0}}
           [:% physical-base/Box
            {:chord chord
             :indicators indicators
             :style {:flex 1
                     :opacity (:? (. arrow animate) 0 1)}
             :transformations
             (:? (. arrow animate)
                 {:visible 
                  (fn [v]
                    (return {:style {:opacity v}}))})}
            [:% n/View
             {:style [{:flex 1}
                      (:.. (j/arrayify backdropStyle))]}]]]])
     [:% physical-base/Box
      {:style [{:position "absolute"}
               (:? (. arrow animate)
                   {:opacity 0
                    :transform [{:scale 0}]}) 
               (-/tooltipPortalOffset layouts)]
       :chord chord
       :onLayout getLayouts
       :indicators indicators
       :transformations
       (:? (. arrow animate)
           {:visible 
            (fn [v]
              (return {:style {:opacity v
                               :transform [{:scale v}]}}))})}
      (:? (== placement "host")
          [:% -/TooltipInnerHostArrow
           #{[position
              :alignment "center"
              arrow
              arrowLayout
              layouts]}])
      [:% n/View
       {:ref contentRef
        :key "content"
        :style [{:position "absolute"
                 :opacity (:? layouts 1 0)}
                style
                (-/tooltipContextOffset
                 layouts #{position
                           alignment
                           {:margin (. arrow baseHeight)
                            :marginCross 0}})]}
       children
       (:? (== placement "content")
           [:% -/TooltipInnerContentArrow
            #{alignment arrow arrowLayout layouts position}])]]]]))

(defn.js Tooltip
  "creates a tooltip"
  {:added "4.0"}
  [#{[hostRef
      (:= position "right")
      (:= alignment "center")
      visible
      setVisible
      (:= delay 0)
      style
      arrow
      children]}]  
  (var isMounted (r/useIsMounted))
  (var [delayed
        setDelayed] (r/useFollowDelayed
                     visible delay isMounted))
  (var arrowPosition (model-geom/oppositePosition position))
  (:= arrow (j/assign {:color "black"
                       :placement "content"
                       :baseLength 20
                       :point arrowPosition
                       :baseHeight 5
                       :animate  false}
                      arrow))
  (when (k/nil? (. arrow point))
    (k/set-key arrow "placement" "none"))

  (var [showing
        vindicator] (a/useShowing (and visible delayed)
                                  {:default {:type "timing"
                                             :duration 300
                                             :easing a/linear}}
                                  isMounted))
  (var flag (:? (. arrow animate)
                showing
                (and visible delayed)))
  (return
   (:? flag
       [:% -/TooltipInner
        #{alignment
          position
          arrow
          hostRef
          setVisible
          {:chord {:visible (and visible delayed)}
           :indicators {:visible vindicator}}}
        children])))

