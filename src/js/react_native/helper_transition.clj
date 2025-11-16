(ns js.react-native.helper-transition
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react-native.model-context :as model-context]]})

(def.js DefaultTransitions
  {:centered "from_top"
   :top "from_top"
   :top-right "from_right"
   :top-left  "from_top"
   :left "from_left"
   :right "from_right"
   :bottom-right "from_bottom"
   :bottom-left "from_left"
   :bottom "from_bottom"})

(defn.js absoluteAnimateFn
  "creates the animate function"
  {:added "4.0"}
  [#{[transition
      effect
      position
      margin
      (:= height 0)
      (:= width 0)]}]
  (:= transition (or transition
                     (. -/DefaultTransitions [position])))
  (var [translateKey
        magnitude] (model-context/getTranslationOffset
                    #{transition
                      position
                      margin
                      height
                      width}))
  (var zoomFn  (model-context/getScalarFn effect "zoom"))
  (var fadeFn  (model-context/getScalarFn effect "fade"))
  (return
   (fn [visible]
     (var style {:opacity  (:? fadeFn
                               (fadeFn visible)
                               visible)
                 :transform [(:.. (:? translateKey
                                      [{translateKey (* (- 1 visible)
                                                        magnitude)}]
                                      []))
                             (:.. (:? zoomFn
                                      [{:scale (zoomFn visible)}]
                                      []))]})
     (return {:style style}))))

(defn.js absoluteAnimateProgress
  "applies the animate function"
  {:added "4.0"}
  [#{[transition
      (:= effect {})
      position
      margin
      height
      width]}
   visible]
  (:= transition (or transition
                     (. -/DefaultTransitions [position])))
  (var [translateKey
        magnitude] (model-context/getTranslationOffset
                    #{transition
                      position
                      margin
                      height
                      width}))
  (var opacity (k/mix (or (. effect fade) 0) 1 visible))
  (var style {:opacity   (* opacity opacity)
              :transform [(:.. (:? translateKey
                                   [{translateKey (* (- 1 visible)
                                                     magnitude)}]
                                   []))
                          (:.. (:? (. effect zoom)
                                   [{:scale (k/mix (. effect zoom) 1 visible)}]
                                   []))]})
  (return {:style style}))

;;
;;
;;

(defn.js relativeAnimateProgress
  "applies the relative animate function"
  {:added "4.0"}
  [#{[transition
      (:= effect {})
      xOffset
      yOffset]}
   visible]
  (var style {:opacity  (k/mix (or (. effect fade) 0) 1 visible)
              :transform [{:translateX (* (- 1 visible)
                                          xOffset)}
                          {:translateY (* (- 1 visible)
                                          yOffset)}
                          (:.. (:? (. effect zoom)
                                   [{:scale (k/mix (. effect zoom) 1 visible)}]
                                   []))]})
  (return {:style style}))

