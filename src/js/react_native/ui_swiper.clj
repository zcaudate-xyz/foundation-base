(ns js.react-native.ui-swiper
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core :as j]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.physical-edit :as physical-edit]
             [js.react-native.helper-theme :as helper-theme]
             [js.react-native.helper-theme-default :as helper-theme-default]]})

(defn.js swiperTransform
  "transforms swiper position to screen"
  {:added "4.0"}
  [#{posEnabled
     posFull
     posThreshold
     posThresholdInit
     negEnabled
     negFull
     negThreshold
     negThresholdInit}]
  (return
   (fn [#{[(:= position 0)]}]
     (cond (or  (and (not posEnabled)
                     (< 0 position))
                (and (not negEnabled)
                     (> 0 position))
                (and (< position posThresholdInit)
                     (> position negThresholdInit)))
           (return {:style {:transform [{:translateX 0}]}})

           :else
           (return {:style {:transform [{:translateX (j/max negFull
                                                            (j/min position posFull))}]}})))))

(defn.js swiperTheme
  "creates the swiper theme"
  {:added "4.0"}
  [#{[theme
      themePipeline
      (:.. rprops)]}]
  (var __theme (j/assign {} helper-theme-default/ButtonDefaultTheme theme))
  (var __themePipeline (j/assign {}
                                 helper-theme-default/PressDefaultPipeline
                                 themePipeline))
  (var [styleStatic transformFn]
       (helper-theme/prepThemeCombined
        #{[:theme __theme
           :themePipeline __themePipeline
           (:.. rprops)]}))
  (return [styleStatic transformFn]))

(defn.js useSwiperPosition
  "helper function to connect swiper position"
  {:added "4.0"}
  [#{posEnabled
     posFull
     posThreshold
     posThresholdInit
     negEnabled
     negFull
     negThreshold
     negThresholdInit
     fixedRef
     direction
     setDirection
     opened
     setOpened
     onOpened
     onClosed}]
  (var directionRef (r/useFollowRef direction))
  (var openedRef    (r/useFollowRef opened))
  (var position      (a/val 0))
  (r/init []
    (a/addListener
     position
     (fn []
       (var #{_value} position)
       (var _direction "none")
       (var _opened    false)
       (cond (< _value negThresholdInit)
             (:= _direction "negative")
             
             (> _value posThresholdInit)
             (:= _direction "positive"))
       (cond (and (< _value negThreshold)
                  negEnabled)
             (do (:= _opened true)
                 (r/curr:set fixedRef negFull))
             
             (and (> _value posThreshold)
                  posEnabled)
             (do (:= _opened true)
                 (r/curr:set fixedRef posFull))
             
             :else
             (r/curr:set fixedRef 0))
       (when (not= (r/curr openedRef) _opened)
         (setOpened _opened)
         (r/curr:set openedRef _opened))
       (when (not= (r/curr directionRef) _direction)
         (setDirection _direction)
         (r/curr:set directionRef _direction)))))
  (return position))

(defn.js Swiper
  "creates a slim swiper"
  {:added "0.1"}
  [#{[theme
      themePipeline
      disabled
      posView
      posEnabled
      (:= posFull 150)
      (:= posThresholdInit 10)
      (:= posThreshold 100)
      negView
      negEnabled
      (:= negFull -150)
      (:= negThresholdInit -10)
      (:= negThreshold -100)
      style
      styleContainer
      chord
      onHoverIn
      onHoverOut
      addons
      onOpened
      onClosed
      (:.. rprops)]}]
  (var [opened setOpened] (r/local false))
  (var [direction setDirection] (r/local "none"))
  (var fixedRef (r/ref 0))
  (var position (-/useSwiperPosition #{posEnabled
                                       posFull
                                       posThreshold
                                       posThresholdInit
                                       negEnabled
                                       negFull
                                       negThreshold
                                       negThresholdInit
                                       fixedRef
                                       direction
                                       setDirection
                                       opened
                                       setOpened
                                       onOpened
                                       onClosed}))
  (var #{touchable
         panHandlers} (physical-edit/usePanTouchable
                       #{[disabled
                          :chord (j/assign #{opened
                                             direction}
                                           chord)
                          :onPressOut (fn []
                                        (var v (r/curr fixedRef))
                                        (. (a/spring
                                            position
                                            {:toValue v
                                             :duration 300
                                             :useNativeDriver false})
                                           (start (fn []
                                                    (cond (and (== v 0)
                                                               onClosed)
                                                          (onClosed)

                                                          (and (not= v 0)
                                                               onOpened)
                                                          (onOpened))))))
                          (:.. rprops)]}
                       "horizontal"
                       position
                       true))
  (var [styleStatic transformFn]
       (-/swiperTheme #{[theme
                         themePipeline
                         :transformations {:bg (r/const
                                                (-/swiperTransform
                                                 #{posEnabled
                                                   posFull
                                                   posThreshold
                                                   posThresholdInit
                                                   negEnabled
                                                   negFull
                                                   negThreshold
                                                   negThresholdInit}))}
                         (:.. rprops)]}
                      #{posFull
                        posThreshold
                        posThresholdInit
                        negFull
                        negThreshold
                        negThresholdInit}))
  (var  #{setPressing
          pressing
          hovering
          setHovering}    touchable)
  (return
   [:% n/View
    {:style [(:.. (j/arrayify styleContainer))]}
    [:% physical-base/Box
     #{[theme
        themePipeline
        :indicators touchable.indicators
        :chord      touchable.chord
        :onMouseEnter  (fn [e] (setHovering true)
                         (if onHoverIn (onHoverIn e)))
        :onMouseLeave  (fn [e] (setHovering false)
                         (if onHoverOut (onHoverOut e)))
        :onMouseUp     (fn []
                         (setPressing false))
        :style   [styleStatic
                  (:.. (j/arrayify style))]
        :addons  [{:component n/View
                   :key "neg"
                   :style {:position "absolute"
                           :overflow "hidden"
                           :right 0
                           :zIndex -10
                           :height (:? (== direction "negative")
                                       "100%"
                                       0)
                           :width  (:? (== direction "negative")

                                       (- negFull)
                                       0)}
                   :children [negView]}
                  {:component n/View
                   :key "pos"
                   :style {:position "absolute"
                           :overflow "hidden"
                           :left 0
                           :zIndex -10
                           :height (:? (== direction "positive")
                                       "100%"
                                       0)
                           :width  (:? (== direction "positive")
                                       posFull
                                       0)}
                   :children [posView]}
                  (:.. (j/arrayify addons))]
        :transformations transformFn
        (:.. (j/assign touchable
                       panHandlers))]}]]))

