(ns js.react-native.animate
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [delay sequence loop val derive]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react-native :as n]
             [xt.lang.event-animate :as event-animate]]
   :bundle {:default  [["react-native" :as [* ReactNative]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactNative.Animated"
                                   :tag "js"}]
  [decay
   timing
   spring
   add
   subtract
   divide
   multiply
   modulo
   diffClamp
   delay
   sequence
   parallel
   stagger
   loop
   event
   forkEvent
   unforkEvent

   Value
   ValueXY
   Interpolation
   Node
   createAnimatedComponent
   attachNativeEvent

   [Box View]
   Image
   ScrollView
   Text
   FlatList
   SectionList])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactNative.Easing"
                                   :tag "js"}]
  [step0
   step1
   linear
   ease
   quad
   cubic
   poly
   sin
   circle
   exp
   elastic
   back
   bounce
   bezier
   [easeIn in]
   [easeOut out]
   [easeInOut inOut]])

(h/template-entries [l/tmpl-macro {:base "ReactNative.Animated.Value"
                                   :inst "val"
                                   :tag "js"}]
  [[start [] {:optional [cb]}]
   [stop []]
   [reset []]
   [setValue  [num]]
   [setOffset [offset]]
   [flattenOffset []]
   [extractOffset []]
   [addListener [cb]]
   [removeListener [id]]
   [removeAllListeners []]
   [stopAnimation []  {:optional [cb]}]
   [resetAnimation [] {:optional [cb]}]
   [interpolate [config]]
   [animate [animation cb]]
   [stopTracking]
   [track [trackting]]
   [getLayout []]
   [getTranslateTransform]])

(defmacro.js ^{:standalone true} val
  "shortcut for Animated.Value"
  {:added "4.0"}
  ([init]
   (h/$ (React.useCallback (new ReactNative.Animated.Value ~init) []))))

(defn.js isAnimatedValue
  "checks that value is animated"
  {:added "4.0"}
  [x]
  (return (and (not= nil x)
               (k/obj? (. x ["_listeners"]))
               #_(== (. x
                      ["constructor"]
                      ["name"])
                   "AnimatedValue"))))

(defn.js createTransition
  "creates a transition from params"
  {:added "4.0"}
  ([indicator tparams [prev curr] tf]
   (return (fn [callback]
             (let [_ (when (== (. indicator _value) curr)
                       (return))
                   fparams   (or (. tparams [[prev curr]])
                                 (. tparams ["default"])
                                 {})
                   #{[(:= type "timing")
                      onChange]} fparams
                   f        (. {:timing -/timing
                                :spring -/spring
                                :decay  -/decay}
                               [type])
                   params   (j/assign {:toValue (tf curr)
                                       :useNativeDriver false}
                                       fparams)
                   anim     (f indicator params)]
               (-/start anim callback)
               (return anim))))))

(defn.js setPropsAll
  "sets props for all the elements"
  {:added "4.0"}
  [elem props]
  (cond (and elem
             elem.setNativeProps
             (k/fn? elem.setNativeProps))
        (try (elem.setNativeProps props)
             (catch e))
        
        (== "web" (. n/Platform OS))
        (k/for:object [[k0 v0] props]
          (when (props.hasOwnProperty k0)
            (cond (and (== k0 "style")
                       (== "object" (typeof v0)))
                  (:= elem.style
                      (k/arr-foldl [elem.style v0]
                                   k/obj-assign
                                   {}))
                  
                  (and (== k0 "text")
                       (or (== "INPUT" elem.tagName)
                           (== "TEXTAREA" elem.tagName)))
                  (:= elem.v0ue v0)
                  
                  :else
                  (:= (. elem [k0])
                      (. props [k0])))))))

(def.js IMPL
  {:create-val        (fn:> [v]
                        (-/val v))
   
   :add-listener      (fn:> [aval f]
                        (-/addListener aval f))
   :get-value         (:? (n/isWeb)
                          (fn [aval]
                            (cond (j/isInteger aval._value)
                                  (return aval._value)
                                  
                                  :else
                                  (return
                                   (/ (j/round (* aval._value 10))
                                      10))))
                          (fn [aval]
                            (cond (j/isInteger aval._value)
                                  (return aval._value)
                                  
                                  :else
                                  (return
                                   (/ (j/round (* aval._value 20))
                                      20)))))
   :set-value         (fn:> [aval v] (-/setValue aval v))
   :set-props         -/setPropsAll
   :is-animated       -/isAnimatedValue
   :create-transition -/createTransition
   :stop-transition   (fn:> [anim] (-/stop anim))})

(defn.js derive
  "derives a value from one or more Animated.Value"
  {:added "4.0"}
  [f arr]
  (return (event-animate/new-derived -/IMPL f arr)))

(defn.js listenSingle
  "listens to indicator and sets function"
  {:added "4.0"}
  [ref
   ind
   f := (fn:> {})]
  (return (event-animate/listen-single -/IMPL ref ind f)))

(defn.js useListenSingle
  "listens to a single indicator to set ref"
  {:added "4.0"}
  [ind f]
  (let [ref (r/ref)]
    (r/init [] (-/listenSingle ref ind f))
    (return ref)))

(defn.js listenArray
  "listen to ref for array"
  {:added "4.0"}
  [ref arr f]
  (return (event-animate/listen-array -/IMPL ref arr f)))

(defn.js useListenArray
  "creates a ref as well as animated value listeners"
  {:added "4.0"}
  [arr f]
  (let [ref (r/ref)]
    (r/init []
      (-/listenArray ref arr f))
    (return ref)))

(defn.js listenMap
  "listen to ref for map"
  {:added "4.0"}
  [ref m f]
  (return (event-animate/listen-map -/IMPL ref m f)))

(defn.js listenTransformations
  "listens to a transformation"
  {:added "4.0"}
  [ref indicators transformations getChord]
  (return (event-animate/listen-transformations -/IMPL ref indicators transformations getChord)))

(defn.js runWithCancel
  "runs a function, cancelling the animation if too slow"
  {:added "4.0"}
  [animateFn progressing progressFn]
  (return (event-animate/run-with-cancel -/IMPL animateFn progressing progressFn)))

(defn.js runWithChained
  "runs with chained"
  {:added "4.0"}
  [type animateFn progressing progressFn]
  (return (event-animate/run-with-chained -/IMPL type animateFn progressing progressFn)))

(defn.js runWith
  "generic runWith function for animations"
  {:added "4.0"}
  [type animateFn progressing progressFn]
  (return (event-animate/run-with -/IMPL type animateFn progressing progressFn)))

;;
;; Indicators
;;

(defn.js useProgess
  "creates a progress result and a progress function"
  {:added "4.0"}
  [callback]
  (var progressing  (r/const (event-animate/new-progressing)))
  (var progressFn   (r/const (fn [info]
                               (when callback
                                 (callback progressing info)))))
  (return [progressing progressFn]))

(defn.js useBinaryIndicator
  "creates a binary indicator from state"
  {:added "4.0"}
  [flag
   tparams
   callback
   type]
  (:= tparams (or tparams {}))
  (var [progressing progressFn] (-/useProgess callback))
  (var #{indicator
         trigger-fn} (r/const (event-animate/make-binary-indicator -/IMPL
                                                                flag
                                                                tparams
                                                                (or type "cancel")
                                                                progressing
                                                                progressFn)))
  (r/watch [flag] (trigger-fn flag))    
  (return indicator))

(defn.js usePressIndicator
  "accentuates the press"
  {:added "4.0"}
  [flag
   tparams
   callback]
  (return (-/useBinaryIndicator flag tparams callback "chained-one")))

(defn.js useLinearIndicator
  "uses the linear indicator"
  {:added "4.0"}
  [value
   tparams
   callback
   type
   checkFn]
  (:= tparams (or tparams {}))
  (var [progressing progressFn] (-/useProgess callback))
  (var prev          (r/ref value))
  (var #{indicator
         trigger-fn} (r/const (event-animate/make-linear-indicator
                               -/IMPL
                               value
                               (fn:> (r/curr prev))
                               (fn:> [value] (r/curr:set prev value))
                               tparams
                               (or type "chained-all")
                               progressing
                               progressFn
                               checkFn)))
  (r/watch [value] (trigger-fn value))
  (return indicator))

(defn.js useIndexIndicator
  "creates a index indicator from state"
  {:added "4.0"}
  [value
   tparams
   callback]
  (return (-/useLinearIndicator value tparams callback "cancel")))

(defn.js useCircularIndicator
  "constructs a circular indicator"
  {:added "4.0"}
  [value
   tparams
   callback
   type
   modulo
   checkFn]
  (:= tparams (or tparams {}))
  (var [progressing progressFn] (-/useProgess callback))
  (var prev           (r/ref value))
  (var #{indicator
         trigger-fn} (r/const (event-animate/make-circular-indicator
                               -/IMPL
                               value
                               (fn:> (r/curr prev))
                               (fn:> [value] (r/curr:set prev value))
                               tparams
                               (or type "cancel")
                               modulo
                               progressing
                               progressFn
                               checkFn)))
  (r/watch [value] (trigger-fn value))
  (return indicator))

(defn.js usePosition
  "constructs position indicator for slider and pan"
  {:added "4.0"}
  [#{[length
      (:= max 10)
      (:= min  0)
      (:= step 1)
      value
      setValue
      flip]}]
  (var #{forwardFn
         reverseFn} (r/convertPosition
                     (j/assign #{length step}
                               (:? flip
                                   {:max min
                                    :min max}
                                   #{max min}))))
  (var position  (-/val (forwardFn value)))
  (var prev (r/ref))
  
  (r/init []
    (-/addListener position
                   (fn [e]
                     (var nvalue (reverseFn position._value))
                     (when (not= (r/curr prev) nvalue)
                       (setValue nvalue)
                       (r/curr:set prev nvalue)))))
  (return #{position
            forwardFn
            reverseFn}))

(defn.js useRange
  "constructs lower and upper bound indicator for range"
  {:added "4.0"}
  [#{[length
      (:= max 10)
      (:= min  0)
      (:= step 1)
      (:= lower 0)
      setLower
      (:= upper 1)
      setUpper]}]
  (var #{forwardFn
         reverseFn} (r/convertPosition #{length max min step}))
  (when (< upper lower)
    (:= upper (+ lower step)))
  (var positionUpper  (-/val (forwardFn upper)))
  (var prevUpper (r/ref))
  (var positionLower  (-/val (forwardFn lower)))
  (var prevLower (r/ref))
  (r/init []
    (-/addListener positionUpper
                   (fn [e]
                     (var nupper (reverseFn positionUpper._value))
                     (when (and (not= (r/curr prevUpper) nupper)
                                (<= lower nupper))
                       (setUpper nupper)
                       (r/curr:set prevUpper nupper))))
    (-/addListener positionLower
                   (fn [e]
                     (var nlower (reverseFn positionLower._value))
                     (when (and (not= (r/curr prevLower) nlower)
                                (<= nlower upper))
                       (setLower nlower)
                       (r/curr:set prevLower nlower)))))
  (return #{positionUpper
            positionLower
            forwardFn
            reverseFn}))

(defn.js useShowing
  "constructs a function that removes"
  {:added "4.0"}
  [visible indicatorParams isMounted onComplete]
  (var vindicator (-/useBinaryIndicator visible indicatorParams))
  (var [showing setShowing] (r/local visible))
  (var showingRef (r/useFollowRef showing))
  (r/watch [visible]
    (when visible (setShowing true)))
  (r/init []
    (-/addListener vindicator
                   (fn []
                     (when (isMounted)
                       (when (== vindicator._value 0)
                         (when onComplete (onComplete 0))
                         (when (r/curr showingRef)
                           (r/curr:set showingRef false)
                           (j/delayed [100]
                             (when (isMounted)
                               (setShowing false)))))
                       (when (== vindicator._value 1)
                         (when onComplete (onComplete 1)))))))
  (return [(or visible showing)
           vindicator]))

