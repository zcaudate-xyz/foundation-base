(ns js.react-native.ui-notify
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
             [js.react-native :as n]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.model-context :as model-context]
             [js.react-native.helper-transition :as helper-transition]
             [xt.lang.base-lib :as k]]})

(defn.js NotifyInner
  "creates the inner notification element"
  {:added "4.0"}
  [#{[position
      margin
      indicators
      chord
      transition
      children]}]
  (var dims (n/useWindowDimensions))
  (var [layouts setLayouts] (r/local {}))
  (var #{sinkRef
         setSinkRef
         contentRef
         getLayouts}  (n/usePortalLayouts nil setLayouts))
  (var transitionFn
       (helper-transition/absoluteAnimateFn
           (j/assign #{transition
                       position
                       margin}
                     (. layouts content))))
  (var transitionRef (r/useFollowRef transitionFn))
  (var absStyle
       (:? (. layouts content)
           (model-context/innerCoordinate
            (j/assign
             #{position
               margin
               {:parent (. layouts sink)}}
             (. layouts content)))))
  (r/watch [dims children] (getLayouts))
  (return
   [:% n/Portal
    {:onSink setSinkRef}
    [:% n/View
     {:style [{:position "absolute"}
              absStyle]}
     [:% physical-base/Box
      {:indicators indicators
       :chord chord
       :style {:position "absolute"
               :opacity 0}
       :transformations
       {:visible (r/const
                  (fn [progress chord]
                    (return ((r/curr transitionRef) progress chord))))}
       :children
       [[:% n/View
         {:ref contentRef
          :key "content"
          :style {:opacity (:? (. layouts content)
                               1
                               0)}}
         children]]}]]]))

(defn.js Notify
  "creates a Notify"
  {:added "4.0"}
  [#{[(:= position "centered")
      transition
      transitionFn
      margin
      visible
      children]}]
  (var isMounted  (r/useIsMounted))
  (var dimensions (n/useWindowDimensions))
  (var [showing
        vindicator] (a/useShowing visible
                                  {:default {:type "timing"
                                             :duration 300
                                             :easing a/linear}}
                                  isMounted))
  (return
   (:? showing
       [:% -/NotifyInner
        #{position
          margin
          transition
          {:chord {:visible visible}
           :indicators {:visible vindicator}}}
        children])))

