(ns js.react-native.ui-router
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
             [js.react-native.model-context :as model-context]
             [xt.lang.base-lib :as k]]})

(defn.js useTransition
  "creates all props involved with transition"
  {:added "4.0"}
  [#{[indicators
      chord
      (:= indicatorParams {})
      onComplete]}]
  (var isMountedWrap (r/useIsMountedWrap))
  (var [changing setChanging] (r/local (fn:> false)))
  (var [even setEven]  (r/local (fn:> false)))
  (var evenRef       (r/useFollowRef even))
  (var changingRef   (r/useFollowRef changing))
  (var cindicator (a/useBinaryIndicator changing
                                        (or (. indicatorParams ["changing"])
                                            {:default {:type "timing"
                                                       :duration 200
                                                       :easing a/linear}})))
  (r/init []
    (a/addListener cindicator
                   (isMountedWrap
                    (fn []
                      (when (and (== cindicator._value 1)
                                 (r/curr changingRef))
                        (setChanging false)
                        (onComplete)
                        (setEven (not (r/curr evenRef))))))))
  (return #{changing
            setChanging
            even
            setEven
            {:indicators (j/assign {:changing cindicator}
                                   indicators)
             :chord (j/assign #{changing even} chord)}}))

(defn.js RouterImpl
  "creates the transitioning router"
  {:added "4.0"}
  [#{[routeKey
      (:= routeComponentFn (fn:>))
      (:= routePropsFn (fn:> {}))
      transition
      (:= transitionMap {})
      transitionDefault
      style
      debug
      (:= onComplete (fn:>))]}]
  (var isMountedWrap (r/useIsMountedWrap))
  (var [dims setDims] (r/local {:height 0
                                :width  0}))
  (var #{height width} dims)
  (var [prevKey setPrevKey] (r/local routeKey))
  (:= transition (or transition
                     (k/get-in transitionMap
                               [prevKey
                                routeKey])
                     transitionDefault))
  (var #{changing
         setChanging
         even setEven
         settling
         setSettling
         indicators
         chord} (-/useTransition {:chord #{transition
                                           height
                                           width}
                                  :onComplete onComplete}))
  (var oddKey  (:? (and changing even) routeKey prevKey))
  (var OddComponent (or (routeComponentFn oddKey)
                        n/View))
  (var oddProps      (routePropsFn oddKey))

  (var evenKey (:? (and changing (not even)) routeKey prevKey))
  (var EvenComponent (or (routeComponentFn evenKey)
                         n/View))
  (var evenProps      (routePropsFn evenKey))
  (r/watch [routeKey]
    (when (not= routeKey prevKey)
      (setChanging true)))
  (r/watch [changing]
    (when (not changing)
      (setPrevKey routeKey)))
  (var inFn
       (model-context/animateIn chord))
  (var outFn
       (model-context/animateOut
        (j/assign {} chord {:transition (k/get-in model-context/ANIMATE
                                                  [transition "counter"])})))
  (var evenFn
       (fn [progress chord]
         (var #{even
                changing} chord)
         (return (:? changing
                     {:style
                      (:? even
                          {;;:opacity 1 #_(k/mix 1 0 (j/min 1 (* 2 progress)))
                           :transform (j/arrayify (outFn progress))}
                          {:transform (j/arrayify (inFn progress))})}))))
  (var oddFn
       (fn [progress chord]
         (var #{even
                changing} chord)
         (var out (:? changing
                      {:style
                       (:? (not even)
                          {;;:opacity 1 #_(k/mix 1 0 (j/min 1 (* 2 progress)))
                           :transform (j/arrayify (outFn progress))}
                          {:transform (j/arrayify (inFn progress))})}))
         (return out)))
  (var evenRef (r/useFollowRef evenFn))
  (var oddRef  (r/useFollowRef oddFn))
  (var containerRef (r/ref))
  (r/run []
    (n/measureRef containerRef
                  (fn [e]
                    (when (k/not-empty? (k/obj-diff e dims))
                      ((isMountedWrap setDims) e)))))
  (return
   [:<>
    [:% n/View
     {:style style
      :ref containerRef}
     [:% physical-base/Box
      {:indicators indicators
       :style {:overflow "hidden"
               :height height
               :width  width}
       :chord chord
       :inner [{:component n/View
                :key "even"
                :style [{:backfaceVisibility "hidden"
                         :position "absolute"
                         :overflow "hidden"
                         :height  height
                         :width   width
                         :opacity 1
                         :zIndex (:? even
                                     100
                                     -100)}]
                :children [(:? (or even
                                   changing)
                               [:% n/GlobalProvider
                                {:key "even"
                                 :value {:isTransition changing}}
                                [:% EvenComponent #{[(:.. evenProps)]}]])]
                :transformations
                {:changing (fn [progress chord]
                             (return {})
                             #_(return ((r/curr evenRef) progress chord)))}}
               {:component n/View
                :key "odd"
                :style [{:backfaceVisibility "hidden"
                         :position "absolute"
                         :overflow "hidden"
                         :height height
                         :width  width
                         :opacity 1
                         :zIndex (:? (not even)
                                     100
                                     -100)}]
                :transformations
                {:changing (fn [progress chord]
                             #_((r/curr oddRef) progress chord)
                             (return {})
                             #_((r/curr oddRef) progress chord))}
                :children [(:? (or (not even)
                                   changing)
                               [:% n/GlobalProvider
                                {:key "odd"
                                 :value {:isTransition changing}}
                                [:% OddComponent #{[:key "odd" (:.. oddProps)]}]])]}]}]]
    (:? debug
        [:% n/TextDisplay
         {:content (n/format-entry #{routeKey
                                     prevKey
                                     even
                                     transition
                                     oddKey
                                     changing
                                     evenKey})}])]))

(defn.js Router
  "creates a Router"
  {:added "4.0"}
  [props]
  (var #{[routeKey
          (:= routeComponentFn (fn:>))
          (:= routePropsFn (fn:> {}))
          noTransition]} props)
  (when noTransition
    (var Component (or (routeComponentFn routeKey)
                       n/View))
    (var componentProps     (routePropsFn routeKey))
    (return (r/% Component componentProps)))

  (return (r/% -/RouterImpl props)))


