(ns js.react-native.ui-modal
  (:require [std.lang :as l]
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
             [js.react-native :as n]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.model-context :as model-context]
             [js.react-native.helper-transition :as helper-transition]
             [xt.lang.base-lib :as k]]})

(defn.js ModalBackdrop
  "constructs the modal backdrop"
  {:added "4.0"}
  [#{permanent
     onClose
     opacityBackdrop
     styleBackdrop
     indicators
     chord
     children}]
  (return
   [:% n/View
    {:style {:flex 1
             :zIndex -10}}
    [:% n/TouchableWithoutFeedback
     {:style {:flex 1}
      :onPress (fn [e]
                 (when (not permanent)
                   (onClose)))}
     [:% n/View
      {:style {:flex 1}}
      [:% physical-base/Box
       {:indicators indicators
        :chord chord
        :style [{:flex 1}
                (:.. (j/arrayify styleBackdrop))]
        :transformations
        {:visible (fn [visible]
                    (return {:style {:opacity (* opacityBackdrop
                                                 visible)}}))}}]
      children]]]))

(defn.js useMeasureContent
  "measures the modal content"
  {:added "4.0"}
  [children visible dimensions setLayout]
  (var contentRef (r/ref))
  (var getLayout
       (fn []
         (when (r/curr contentRef)
           (var contentElem
                (or (k/get-in contentRef
                              ["current"
                               "children"
                          0])
                    (k/get-in contentRef
                              ["current"
                               "_children"
                               0])))
           (n/measure contentElem setLayout))))
  (r/watch [children visible dimensions]
    (getLayout))
  (return #{contentRef
            getLayout}))

(defn.js ModalElementAbsolute
  "originates the modal from the screen"
  {:added "4.0"}
  [#{visible
     position
     transition
     effect
     margin
     dimensions
     indicators
     chord
     children}]
  (var [layout setLayout] (r/local {:height 0
                                    :width 0}))
  (var #{height width} layout)
  (var layoutRef (r/useFollowRef layout))
  (var absStyle
       (model-context/innerCoordinate
        #{position
          margin
          width
          height
          {:parent dimensions}}))
  (var #{contentRef
         getLayout} (-/useMeasureContent children visible dimensions setLayout))
  (return
   [:% physical-base/Box
    #{[indicators
       chord
       :style (j/assign {:position "absolute"
                         :opacity 0}
                        absStyle)
       :onLayout getLayout
       :transformations {:visible
                         (fn [visible]
                           (return
                            (helper-transition/absoluteAnimateProgress
                             #{[transition
                                position
                                margin
                                effect
                                :height (. layoutRef current height)
                                :width  (. layoutRef current width)]}
                             visible)))}]}
    [:% n/View
     {:ref contentRef}
     children]]))

(defn.js ModalElementHost
  "allows the modal to originate from an element"
  {:added "4.0"}
  [#{position
     transition
     effect
     margin
     dimensions
     indicators
     chord
     children
     hostRef
     visible}]
  (var [layout setLayout] (r/local {:height 0
                                    :width 0
                                    :px 0
                                    :py 0}))
  (var [hostLayout setHostLayout] (r/local {:height 0
                                            :width 0
                                            :px 0
                                            :py 0}))
  (k/LOG! layout hostLayout)
  (var layoutRef (r/useFollowRef layout))
  (var hostLayoutRef (r/useFollowRef hostLayout))
  (var contentRef (-/useMeasureContent children visible dimensions setLayout))
  
  (var absStyle
       (model-context/innerCoordinate
        (j/assign #{position
                    margin
                    {:parent dimensions}}
                  layout)))
  (var absStyleRef (r/useFollowRef absStyle))
  (r/watch [layout]
    (when (r/curr hostRef)
      (n/measureRef hostRef setHostLayout)))
  (return
   [:% physical-base/Box
    #{[indicators
       chord
       :style (j/assign {:position "absolute"}
                        absStyle)
       :transformations {:visible
                         (fn [progress #{visible}]
                           (return
                            (helper-transition/relativeAnimateProgress
                             #{[transition
                                position
                                margin
                                effect
                                :xOffset (- (. hostLayoutRef current px)
                                            (. absStyleRef current left))
                                :yOffset (- (. hostLayoutRef current py)
                                            (. absStyleRef current top))
                                :height (. layoutRef current height)
                                :width  (. layoutRef current width)]}
                             progress)))}]}
    [:% n/View
     {:ref contentRef}
     children]]))

(defn.js Modal
  "creates a Modal"
  {:added "4.0"}
  [#{[visible
      (:= position "centered")
      (:= margin 20)
      transition
      hostRef
      effect
      onClose
      permanent
      styleBackdrop
      (:= opacityBackdrop 0.85)
      children]}]
  (var isMounted (r/useIsMounted))
  (var dimensions (n/useWindowDimensions))
  (var [showing
        vindicator] (a/useShowing visible
                                  {:default {:type "timing"
                                             :duration 300
                                             :easing a/linear}}
                                  isMounted))
  (when (not showing)
    (return [:% n/View]))
  (var indicators {:visible vindicator})
  (var chord #{visible})
  (return
   [:% n/Modal
    {:visible showing
     :transparent true
     :onRequestClose onClose}
    (:? hostRef
        [:% -/ModalElementHost
         #{effect
           visible
           margin
           children
           transition
           chord
           indicators
           hostRef
           dimensions
           position}]
        [:% -/ModalElementAbsolute
         #{effect
           visible
           margin
           children
           transition
           chord
           indicators
           dimensions
           position}])
    [:% -/ModalBackdrop
     #{permanent
       onClose
       opacityBackdrop
       styleBackdrop
       indicators
       chord}]]))

