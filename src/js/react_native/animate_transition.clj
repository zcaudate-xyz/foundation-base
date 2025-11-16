(ns js.react-native.animate-transition
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :suppress true :as k]
             [js.core :as j]
             [js.react-native.animate :as a]]})

;;
;; Transitions
;;

(defn.js unit
  "gets the unit transform"
  {:added "4.0"}
  [indicator outputRange inputRange]
  (:= inputRange (or inputRange [0 1]))
  (return (a/interpolate indicator
                         #{inputRange
                           outputRange})))

(defn.js LinearFn
  "linear transition function"
  {:added "4.0"}
  ([k enter exit label]
   (return
    (fn [#{current
           next
           closing
           layouts}]
      (cond (and closing next) 
            (return  {:backfaceVisibility "none"
                      :cardStyle
                      {:opacity  (-/unit next.progress
                                         [1 0.3 0]
                                         [0 0.3 1])
                       :transform
                       [{k (-/unit next.progress
                                   [0 (:? (k/is-function? exit) (exit layouts) exit)])}]}})
            current
            (return  {:backfaceVisibility "none"
                      :cardStyle
                      {:opacity  current.progress
                       :transform
                       [{k (-/unit current.progress
                                   [(:? (k/is-function? enter) (enter layouts) enter)
                                    0])}]}})
            :else (return {}))))))

(def.js Dissolve
  (-/LinearFn "opacity" 0.5 0.5))

(def.js FlipHorizontal
  (-/LinearFn "rotateY" (- j/PI) j/PI))

(def.js FlipVertical
  (-/LinearFn "rotateX" (- j/PI) j/PI))

(def.js SlideLeft
  (-/LinearFn "translateX"
              (fn:> [l] l.screen.width)
              (fn:> [l] (- l.screen.width))
              "SLIDE LEFT"))

(def.js SlideRight
  (-/LinearFn "translateX"
              (fn:> [l] (- l.screen.width))
              (fn:> [l] l.screen.width)
              "SLIDE RIGHT"))

(def.js SlideDown
  (-/LinearFn "translateY"
              (fn:> [l] (- l.screen.height))
              (fn:> [l] l.screen.height)
              "SLIDE DOWN"))

(def.js SlideUp
  (-/LinearFn "translateY"
              (fn:> [l] l.screen.height)
              (fn:> [l] (- l.screen.height))
              "SLIDE UP"))

(defglobal.js TransitionCache
  {:current -/SlideLeft
   :default -/SlideLeft})

(defelem.js Transition
  [k enter exit label]
  (let [f (. -/TransitionCache ["current"])
        
        _ (:= (. -/TransitionCache ["current"])
              (. -/TransitionCache ["default"]))]
    (return (f k enter exit label))))

(def.js setCurrentTransition
  (fn [transition]
    (:= (. -/TransitionCache ["current"])
        transition)))

(def.js setDefaultTransition
  (fn [transition]
    (:= (. -/TransitionCache ["default"])
        transition)))

