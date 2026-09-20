(ns js.react-native.animate
  (:require [lang.core :as l]
            [std.lib.foundation :as f]
            [std.lib.template :as template])
  (:refer-clojure :exclude [delay sequence loop val derive]))

(l/script :js
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [js.react :as r]
             [js.react-native :as n]
             [xt.event.base-animate :as event-animate]]
   :import [["react-native" :as [* ReactNative]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
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

(f/template-entries [l/tmpl-entry {:type :fragment
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

(f/template-entries [l/tmpl-macro {:base "ReactNative.Animated.Value"
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
   (template/$ (React.useCallback (new ReactNative.Animated.Value ~init) []))))

(defn.js isAnimatedValue
  "checks that value is animated"
  {:added "4.0"}
  [x]
  (return (and (not= nil x)
               (k/is-object? (. x ["_listeners"]))
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
                   params   (xt/x:obj-assign {:toValue (tf curr)
                                       :useNativeDriver false}
                                       fparams)
                   anim     (f indicator params)]
               (-/start anim callback)
               (return anim))))))

(defn.js webUnitlessStyle
  "checks whether a web style property accepts unitless numbers"
  {:added "4.1.6"}
  [key]
  (return (or (== key "aspectRatio")
              (== key "flex")
              (== key "flexGrow")
              (== key "flexShrink")
              (== key "fontWeight")
              (== key "lineHeight")
              (== key "opacity")
              (== key "order")
              (== key "zIndex"))))

(defn.js webTransformValue
  "converts a React Native transform value into CSS"
  {:added "4.1.6"}
  [key value]
  (cond (k/is-array? value)
        (do (var out [])
            (xt/for:array [v value]
              (xt/x:arr-push out (-/webTransformValue key v)))
            (return (xt/x:str-join " " out)))
        
        (and (== "number" (typeof value))
             (or (== key "translate")
                 (== key "translate3d")
                 (== key "translateX")
                 (== key "translateY")
                 (== key "translateZ")
                 (== key "perspective")))
        (return (+ value "px"))
        
        (and (== "number" (typeof value))
             (or (== key "rotate")
                 (== key "rotateX")
                 (== key "rotateY")
                 (== key "rotateZ")
                 (== key "skewX")
                 (== key "skewY")))
        (return (+ value "deg"))
        
        :else
        (return value)))

(defn.js webTransform
  "converts React Native transform entries into a CSS transform string"
  {:added "4.1.6"}
  [value]
  (var out [])
  (xt/for:array [entry (xtd/arrayify value)]
    (when (k/is-object? entry)
      (xt/for:object [[key v] entry]
        (xt/x:arr-push out
                       (xt/x:cat key
                                 "("
                                 (-/webTransformValue key v)
                                 ")")))))
  (return (xt/x:str-join " " out)))

(defn.js webStyleValue
  "converts an animated style value to a DOM-compatible value"
  {:added "4.1.6"}
  [key value]
  (cond (== key "transform")
        (return (-/webTransform value))
        
        (and (== "number" (typeof value))
             (not (-/webUnitlessStyle key)))
        (return (+ value "px"))
        
        :else
        (return value)))

(defn.js webStyle
  "flattens and converts a React Native style value for the DOM"
  {:added "4.1.6"}
  [value]
  (var out {})
  (xt/for:array [entry (xtd/arrayify value)]
    (when (k/is-object? entry)
      (xtd/obj-assign out entry)))
  (xt/for:object [[key v] out]
    (xt/x:set-key out key (-/webStyleValue key v)))
  (return out))

(defn.js setPropsWeb
  "sets props on a React Native Web host element"
  {:added "4.1.6"}
  [elem props]
  (when (and elem elem.style)
    (xt/for:object [[k0 v0] (or props {})]
      (when (and props.hasOwnProperty
                 (props.hasOwnProperty k0))
        (cond (and (== k0 "style")
                   (or (k/is-object? v0)
                       (k/is-array? v0)))
              (xtd/obj-assign elem.style
                              (-/webStyle v0))
              
              (and (== k0 "text")
                   (or (== "INPUT" elem.tagName)
                       (== "TEXTAREA" elem.tagName)))
              (:= elem.value v0)
              
              (== k0 "text")
              (:= elem.textContent v0)
              
              :else
              (:= (. elem [k0]) v0))))
    (return true))
  (return false))

(defn.js getNativePropsTarget
  "resolves a host ref from a wrapper ref"
  {:added "4.1.6"}
  [elem]
  (var target elem)
  (when (and target
             target.getNativeRef
             (k/is-function? target.getNativeRef))
    (try
      (var nativeRef (target.getNativeRef))
      (when nativeRef
        (:= target nativeRef))
      (catch e)))
  (when (and target
             (not (and target.setNativeProps
                       (k/is-function? target.setNativeProps)))
             target.getNode
             (k/is-function? target.getNode))
    (try
      (var nativeRef (target.getNode))
      (when nativeRef
        (:= target nativeRef))
      (catch e)))
  (when (and target
             (not (and target.setNativeProps
                       (k/is-function? target.setNativeProps)))
             target._component)
    (:= target target._component))
  (return target))

(defn.js callNativeProps
  "calls setNativeProps when available"
  {:added "4.1.6"}
  [elem props]
  (when (and elem
             elem.setNativeProps
             (k/is-function? elem.setNativeProps))
    (try
      (elem.setNativeProps props)
      (return true)
      (catch e)))
  (return false))

(defn.js setPropsNative
  "sets props on a React Native host ref"
  {:added "4.1.6"}
  [elem props]
  (var target (-/getNativePropsTarget elem))
  (when (-/callNativeProps target props)
    (return true))
  (when (and target
             (not= target elem)
             (-/callNativeProps elem props))
    (return true))
  (return false))

(defn.js setPropsAll
  "sets props for all the elements"
  {:added "4.0"}
  [elem props]
  (cond (and (== "web" (. n/Platform OS))
             elem
             elem.style)
        (-/setPropsWeb elem props)
        
        :else
        (-/setPropsNative elem props)))

(def.js IMPL
  {:create-val        (fn:> [v]
                        (-/val v))
   
   :add-listener      (fn:> [aval f]
                        (-/addListener aval f))
   :get-value         (:? (n/isWeb)
                          (fn [aval]
                            (cond (Number.isInteger aval._value)
                                  (return aval._value)
                                  
                                  :else
                                  (return
                                   (/ (Math.round (* aval._value 10))
                                      10))))
                          (fn [aval]
                            (cond (Number.isInteger aval._value)
                                  (return aval._value)
                                  
                                  :else
                                  (return
                                   (/ (Math.round (* aval._value 20))
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
                     (xt/x:obj-assign #{length step}
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
                           (setTimeout
                            (fn []
                              (when (isMounted)
                                (setShowing false)))
                            100)))
                       (when (== vindicator._value 1)
                         (when onComplete (onComplete 1)))))))
  (return [(or visible showing)
           vindicator]))