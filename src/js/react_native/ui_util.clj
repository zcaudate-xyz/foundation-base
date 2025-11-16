(ns js.react-native.ui-util
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
             [xt.lang.base-lib :as k]]})

(defn.js Page
  "creates a Page"
  {:added "4.0"}
  [#{[style
      styleContainer
      styleBody
      styleMenu
      styleTitle
      styleLeft
      styleRight
      headerComponent
      headerProps
      footerComponent
      footerProps
      titleComponent
      titleProps
      leftComponent
      leftProps
      rightComponent
      rightProps
      children]}]
  (return
   [:% n/View
    {:style [{:flex 1}
             (:.. (j/arrayify styleContainer))]}
    (:? headerComponent
        (r/createElement headerComponent headerProps))
    [:% n/View
     {:style [{:flex 1}
              (:.. (j/arrayify style))]}
     
     [:% n/Row
      {:style styleMenu}
      [:% n/View
       {:style [{:flex 1}
                (:.. (j/arrayify styleLeft))]}
       (:? leftComponent
           (r/createElement leftComponent leftProps))]
      [:% n/View
       {:style [{:flex 4}
                (:.. (j/arrayify styleTitle))]}
       (:? titleComponent
           (r/createElement titleComponent titleProps))]
      [:% n/View
       {:style [{:flex 1}
                (:.. (j/arrayify styleRight))]}
       (:? rightComponent
           (r/createElement rightComponent rightProps))]]
     [:% n/View
      {:style [{:flex 1}
               (:.. (j/arrayify styleBody))]}
      children]]
    (:? footerComponent
        (r/createElement footerComponent footerProps))]))

;;
;; FADER
;;

(defn.js Fade
  "creates a Fade"
  {:added "4.0"}
  [#{[visible
      children
      (:= indicatorParams
          {:default {:type "timing"
                     :duration 250
                     :easing a/linear}})
      (:.. rprops)]}]
  (var isMounted (r/useIsMounted))
  (var [showing vindicator] (a/useShowing visible indicatorParams isMounted))
  (return
   [:% physical-base/Box
    #{[:chord #{visible}
       :indicators {:visible vindicator}
       :transformations
       {:visible 
        (fn [visible]
          (return {:style {:opacity visible}}))}
       :children (:? (or showing visible)
                       children)
       (:.. rprops)]}]))

(defn.js FadeIn
  "creates a fade in helper"
  {:added "4.0"}
  [props]
  (var [visible setVisible] (r/local false))
  (r/init []
    (setVisible true))
  (return
   (r/% -/Fade (j/assignNew props #{visible}))))

;;
;; Fold
;;

(defn.js useFoldContent
  "creates the fold inner helper"
  {:added "4.0"}
  [#{visible
     children
     indicators}]
  (var contentRef (r/ref))
  (var layoutRef  (r/ref {:height 0
                          :width 0}))
  (var vindicator (k/get-key indicators "visible"))
  (r/watch [visible children]
    (var contentElem (or (k/get-in contentRef
                                   ["current"
                                    "children"
                                    0])
                         (k/get-in contentRef
                                   ["current"
                                    "_children"
                                    0])))
    (. (n/measure contentElem)
       (then (fn [layout]
               (r/curr:set layoutRef layout)))
       (then (fn []
               (j/delayed [100]
                 (var curr (. vindicator _value))
                 (when (== curr 1)
                   (a/setValue vindicator 1)))))))
  (return #{contentRef
            layoutRef}))

(defn.js FoldInner
  "creates the fold inner container"
  {:added "4.0"}
  [#{[style
      visible
      chord
      indicators
      fade
      children
      (:= aspect "height")]}]
  (var #{contentRef
         layoutRef} (-/useFoldContent #{visible
                                        indicators
                                        children}))
  (return
   [:% physical-base/Box
    {:chord chord
     :indicators indicators
     :style [{aspect 0
              :overflow "hidden"}
             (:.. (j/arrayify style))]
     :transformations
     {:visible 
      (fn [visible]
        (return {:style {:opacity (:? fade visible 1)
                         aspect (* visible
                                   (. (r/curr layoutRef)
                                      [aspect]))}}))}
     :children [[:% n/View
                 {:key "content"
                  :ref contentRef}
                 children]]}]))

(defn.js FoldImpl
  "creates the transitioning fold"
  {:added "4.0"}
  [#{[visible
      chord
      onComplete
      indicators
      (:= indicatorParams
          {:default {:type "timing"
                     :duration 200
                     :easing a/linear}})
      (:.. rprops)]}]
  (var isMounted (r/useIsMounted))
  (var [showing vindicator] (a/useShowing visible indicatorParams isMounted onComplete))
  (return (:? showing
              [:% -/FoldInner
               #{[visible
                  :chord
                  (j/assign {:visible visible} chord)
                  :indicators
                  (j/assign
                   {:visible vindicator}
                   indicators)
                  (:.. rprops)]}])))

(defn.js Fold
  "creates the fold"
  {:added "4.0"}
  [props]
  (var #{noTransition
         visible
         children} props)
  (when noTransition
    (if visible
      (return children)
      (return [:% n/View])))
  
  (return (r/% -/FoldImpl props)))

