(ns js.react-native.physical-edit
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
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [xt.lang.base-lib :as k]]})

(defn.js createPan
  "creates a PanResponder"
  {:added "4.0"}
  [#{[pan
      absolute
      setPressing
      onPressIn
      onPressOut
      (:= disabledRef {:current false})
      (:.. rprops)]}]
  (return (. n/PanResponder
             (create
              (j/assign {:onStartShouldSetPanResponderCapture
                         (fn []
                           (when (not (r/curr disabledRef))
                             (when setPressing (setPressing true)))
                           (when onPressIn (onPressIn)))
                         :onMoveShouldSetPanResponder
                         (fn []
                           (when (not (r/curr disabledRef))
                             (return true)))
                         :onPanResponderGrant
                         (fn []
                           (when (not (r/curr disabledRef))
                             (k/for:object [[dk ind] pan]
                               (a/setOffset ind ind._value))))
                         :onPanResponderMove
                         (fn [e state]
                           (when (not (r/curr disabledRef))
                             (cond absolute
                                   (k/for:object [[dk ind] pan]
                                     (var dv (k/get-key state dk))
                                     (a/setValue ind (+ ind._offset dv)))
                                   
                                   :else
                                   (k/for:object [[dk ind] pan]
                                     (var dv (k/get-key state dk))
                                     (a/setValue ind dv)))))
                         :onPanResponderRelease
                         (fn []
                           (when setPressing (setPressing false))
                           (when onPressOut (onPressOut))
                           (when (not absolute)
                             (k/for:object [[dk ind] pan]
                               (a/flattenOffset ind))))}
                        rprops)))))

(defn.js usePanTouchable
  "creates a pan touchable responder for slider, picker and spinner"
  {:added "4.0"}
  [#{[disabled
      highlighted
      outlined
      indicators
      chord
      (:.. rprops)]}
   layout
   position
   absolute]
  (var disabledRef  (r/useFollowRef disabled))
  (var touchable    (physical-base/useTouchable
                     #{[disabled highlighted
                        :indicators (j/assign #{position} indicators)
                        :chord (j/assign #{outlined} chord)
                        (:.. rprops)]}))
  (var  #{pressing
          setPressing
          hovering
          setHovering}  touchable)
  
  (var pan              (r/const (:? (== layout "horizontal")
                                     {:dx position}
                                     {:dy position})))
  (var #{panHandlers}   (r/const (-/createPan
                                  #{[disabledRef
                                     pressing
                                     setPressing
                                     pan
                                     absolute
                                     (:.. rprops)]})))
  (return #{touchable panHandlers}))




