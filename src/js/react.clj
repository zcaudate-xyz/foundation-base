^{:no-test true}
(ns js.react
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.lib.walk :as walk]
            [js.react.compile :as compile])
  (:refer-clojure :exclude [> ref derive sync get set]))

(l/script :js
  {:import [["react" :as React]
            ["react-dom/client" :as ReactDOM]
            ["react-nil" :as ReactNIL]
            ["react" :as React]]
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]]})

(defrun.js __init__
  (:# "eslint-disable react-hooks/rules-of-hooks"))

;;
;; React
;;

(def +react+
  '[Children Component Fragment Profiler PureComponent StrictMode Suspense
    cloneElement createContext createElement createFactory
    createRef forwardRef isValidElement
    lazy memo
    useCallback useContext useDebugValue useEffect
    useImperativeHandle useLayoutEffect useMemo
    useReducer useRef useState useTransition
    version])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "React"
                                   :tag "js"}]
  +react+)

(def$.js % React.createElement)

(def$.js createDOMRoot ReactDOM.createRoot)


;;
;;
;;

(defmacro.js ^{:style/indent 0}
  curr
  "shortcut for `<ref>.current`"
  {:added "4.0"}
  ([ref]
   (list '. ref 'current)))

(defmacro.js curr:set
  "shortcut for `<ref>.current` = val"
  {:added "4.0"}
  ([ref val]
   (list := (list '. ref 'current) val)))

(defmacro.js ref
  "shortcut for useRef"
  {:added "4.0"}
  ([& [init :as args]]
   (apply list 'React.useRef args)))

;;
;; local
;;

(defmacro.js ^{:style/indent 1} local
  "shortcut for useState"
  {:added "4.0"}
  ([& [init]]
   (if init
     (list 'React.useState init)
     '(React.useState))))

(defmacro.js const
  "returns a constant val"
  {:added "4.0"}
  ([val]
   (list '. (list 'React.useRef val)
         'current)))

(defmacro.js ^{:style/indent 1}
  derive
  "derives value from watches and expression"
  {:added "4.0"}
  ([watch val]
   (list 'React.useCallback val watch)))

(defmacro.js ^{:style/indent 1}
  init
  "shorcut for useEffect []"
  {:added "4.0"}
  ([_ & body]
   (list 'React.useEffect (apply list 'fn [] body) [])))

(defmacro.js ^{:style/indent 1}
  run
  "shortcut for useEffect"
  {:added "4.0"}
  ([_ & body]
   (list 'React.useEffect (apply list 'fn [] body))))

(defmacro.js ^{:style/indent 1}
  watch
  "shortcut for useEffect [...watches]"
  {:added "4.0"}
  ([watch & body]
   (list 'React.useEffect (apply list 'fn [] body)
         watch)))

(defmacro.js sync
  "shortcut for useEffect [var] (f var)"
  {:added "4.0"}
  ([local setFn & [expr]]
   (list 'React.useEffect (list 'fn [] (list 'if setFn
                                             (list setFn (list 'or expr local))))
         [local])))

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactDOM"
                                   :tag "js"}]
  [[renderDom render]
   [hydrateDom hydrate]
   [flushDom flushSync]
   [unmountDom unmountComponentAtNode]
   [createPortalDom createPortal]
   [findDom findDOMNode]
   [versionDOM version]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactNIL"
                                   :tag "js"}]
  [[renderNil render]])

(defmacro.js
  get
  [state]
  (list '. (symbol (str/camel-case (name state)))))

(defmacro.js
  set
  [state]
  (list '. (symbol (str/camel-case (str "set-" (name state))))))

(defmacro.js ^{:style/indent 1
               :static/deps ['React]}
  ui
  "shortcut for useEffect [var] (f var)"
  {:added "4.0"}
  ([layout components]
   (:# 'React)
   (compile/compile-layout layout components)))

(defmacro.js ^{:static/deps ['React]}
  return-ui
  "shortcut for useEffect [var] (f var)"
  {:added "4.0"}
  ([{:keys [layout
            states
            actions
            components]
     :as input}]
   (:# 'React)
   (compile/compile-full input)))

;;
;;

(defn.js useStateFor
  [controls key]
  (var setterKey (+ "set" (k/capitalize key)))
  (return
   [(. controls [key])
    (. controls [setterKey])]))

(defclass.js Try
  [:- -/Component]
  
  (fn constructor [props]
    (super props)
    (:= (. this state)
        {:hasError false
         :error nil}))
  
  ^{:- [:static]}
  (fn getDerivedStateFromError [error]
    (return #{error {:hasError true}}))
  
  (fn render []
    (if (. this state hasError)
      (return (. this props fallback))
      (return (. this props children)))))

(defn.js id
  "make as random id"
  {:added "4.0"}
  [n]
  (:# "eslint-disable-next-line react-hooks/rules-of-hooks")
  (return (-/const (j/randomId (or n 6)))))

;;
;; useStep
;;

(defn.js useStep
  "method takes a `setDone` function, usually for watching a previous step"
  {:added "4.0"}
  [f]
    (var [done setDone] (-/local false))
    (-/run []
      (when (not done)
        (return (f setDone))))
    (return [done setDone]))

(defn.js makeLazy
  "makes the lazy component"
  {:added "4.0"}
  [component]
  (if (k/fn? component)
    (return component)
    (return (-/lazy (fn:> component)))))

(defn.js useLazy
  "constructs a lazy component in function"
  {:added "4.0"}
  [component]
  (if (k/fn? component)
    (return component)
    (do (:# "eslint-disable-next-line react-hooks/rules-of-hooks")
        (return (-/const (-/lazy (fn:> component)))))))

;;
;; useRef
;;

(defn.js useRefresh
  "constructs refresh function"
  {:added "4.0"}
  []
  (var [flag setFlag] (-/local true))
  (var refresh (fn:> (setFlag (not flag))))
  (return refresh))

(defn.js useGetCount
  "function to get counter"
  {:added "4.0"}
  [n]
  (var counterRef (-/ref (or n 0)))
  (-/run []
    (-/curr:set counterRef (+ 1 (-/curr counterRef))))
  (var getCount   (-/const (fn:> (-/curr counterRef))))
  (return getCount))

(defn.js useFollowRef
  "ref follower"
  {:added "4.0"}
  [value f]
  (:= f (or f k/identity))
  (:# "eslint-disable-next-line react-hooks/rules-of-hooks")
  (var valueRef (-/ref (f value)))
  (:# "eslint-disable-next-line react-hooks/rules-of-hooks")
  (-/watch [value]
    (-/curr:set valueRef (f value)))
  (return valueRef))

;;
;; useIsMounted
;;

(defn.js useIsMounted
  "checks if component is mounted"
  {:added "4.0"}
  ([]
   (var mountedRef  (-/ref   true))
   (var isMounted  (-/const (fn:> (return (-/curr mountedRef)))))
   (-/init []
     (return (fn []
               (-/curr:set mountedRef false))))
   (return isMounted)))

(defn.js useIsMountedWrap
  "wrapper for function to run only if mounted"
  {:added "4.0"}
  ([]
   (var isMounted (-/useIsMounted))
   (return
    (fn [f]
      (return (fn [...args]
                (when (isMounted)
                  (f ...args))))))))

(defn.js useMountedCallback
  "runs a mounted callback"
  {:added "4.0"}
  [cb]
  (var cbRef (-/useFollowRef cb))
  (-/init []
    (when (-/curr cbRef)
      ((-/curr cbRef) true))
    (return (fn []
              (when (-/curr cbRef)
                ((-/curr cbRef) false))))))

(defn.js useFollowDelayed
  "sets a value after a given delay"
  {:added "4.0"}
  [value delay isMounted]
  (when (== 0 delay)
    (return [value k/noop]))
  (:# "eslint-disable-next-line react-hooks/rules-of-hooks")
  (var [delayed setDelayed] (-/local value))
  (:# "eslint-disable-next-line react-hooks/rules-of-hooks")
  (-/watch [value]
    (j/future-delayed [delay]
      (when (isMounted)
        (setDelayed value))))
  (return [delayed setDelayed]))

(defn.js useStablized
  [input isStabilized]
  (var [output setOutput] (-/local input))
  (-/watch [input]
    (when (and isStabilized
               (k/not-nil? input)
               (k/eq-nested input output))
      (setOutput input)))
  (return (:? isStabilized
              output
              input)))

;;
;; useInterval
;;

(defn.js runIntervalStop
  "inner function of useInterval"
  {:added "4.0"}
  [intervalRef]
  (var interval (-/curr intervalRef))
  (when (k/not-nil? interval)
    (j/clearInterval interval)
    (-/curr:set intervalRef nil))
  (return interval))

(defn.js runIntervalStart
  "inner function of useInterval"
  {:added "4.0"}
  [fRef msRef intervalRef]
  (var prev (-/runIntervalStop intervalRef))
  (when (not= nil (-/curr msRef))
    (var curr (j/repeating [(-/curr msRef)]
                ((-/curr fRef))))
    (-/curr:set intervalRef curr)
    (return [prev curr]))
  
  (return [prev]))

(defn.js useInterval
  "performs a task at a given iterval"
  {:added "4.0"}
  ([f ms]
   (var fRef  (-/useFollowRef f))
   (var msRef (-/useFollowRef ms))
   (var intervalRef (-/ref nil))
   (var stopInterval
        (-/const
         (fn:> (-/runIntervalStop intervalRef))))
   (var startInterval
        (-/const
         (fn:> (-/runIntervalStart fRef msRef intervalRef))))
   (-/watch [ms]
     (startInterval)
     (return stopInterval))
   (return #{stopInterval
             startInterval})))

;;
;; useTimeout
;;

(defn.js runTimeoutStop
  "inner function of useTimeout"
  {:added "4.0"}
  [timeoutRef]
  (var timeout (-/curr timeoutRef))
  (when (k/not-nil? timeout)
    (j/clearTimeout timeout)
    (-/curr:set timeoutRef nil))
  (return timeout))

(defn.js runTimeoutStart
  "inner function of useTimeout"
  {:added "4.0"}
  [fRef msRef timeoutRef]
  (var prev (-/runTimeoutStop timeoutRef))
  (var curr (j/delayed [(or (-/curr msRef)
                            0)]
              ((-/curr fRef))))
  (-/curr:set timeoutRef curr)
  (return [prev curr]))

(defn.js useTimeout
  "performs a task after a given interval"
  {:added "4.0"}
  ([f ms init]
   (var fRef  (-/useFollowRef f))
   (var msRef (-/useFollowRef ms))
   (var timeoutRef (-/ref nil))
   (var stopTimeout  (-/const
                      (fn:> (-/runTimeoutStop timeoutRef))))
   (var startTimeout (-/const
                      (fn:> (-/runTimeoutStart fRef msRef timeoutRef))))
   (-/init []
     (when (not= false init)
       (startTimeout))
     (return stopTimeout))
   (return #{stopTimeout
             startTimeout})))

;;
;; useCountdown
;;

(defn.js useCountdown
  "countdown value every second"
  {:added "4.0"}
  [initial onComplete opts]
  (var #{[(:= interval 1000)
          (:= step 1)
          (:= to  0)]} (or opts {}))
  (var [current setCurrent] (-/local initial))
  (var #{stopInterval
         startInterval} (-/useInterval
                        (fn []
                          (cond (> current to)
                                (setCurrent (- current step))

                                :else
                                (do (stopInterval)
                                    (when onComplete
                                      (onComplete current)))))
                        interval))
  (return [current
           setCurrent
           {:startCountdown startInterval
            :stopCountdown  stopInterval}]))

;;
;; useNow
;;

(defn.js useNow
  "uses the current time"
  {:added "4.0"}
  [interval]
  (var [now setNow] (-/local (k/now-ms)))
  (var #{stopInterval
         startInterval} (-/useInterval
                         (fn []
                           (setNow (k/now-ms)))
                         (or interval 1000)))
  (return [now {:startNow startInterval
                :stopNow stopInterval}]))

;;
;; useSubmit
;;

(defn.js useSubmit
  "uses the submit option"
  {:added "4.0"}
  [#{[result
      (:= delay 200)
      (:= setResult (fn:>))
      (:= onSubmit  (fn:>))
      (:= onError   (fn [res]
                      (k/LOG! "ERRORED" res)
                      (return (. res ["body"]))))
      (:= onSuccess k/identity)
      (:= isMounted (fn:> true))]}]
  (var [waiting setWaiting] (-/local (fn:> false)))
  (var onAction (fn []
                  (setWaiting true)
                  (. (j/future
                       (when onSubmit
                         (return (onSubmit))))
                     (then (fn [res]
                             (when (isMounted)
                               (setResult (onSuccess res)))
                             (j/future-delayed [delay]
                               (when (isMounted)
                                 (setWaiting false)))))
                     (catch (fn [err]
                              (j/future-delayed [delay]
                                (when (isMounted)
                                  (setWaiting false)))
                              (when (isMounted)
                                (setResult (onError err))))))))
  (var errored (and result (== "error" (k/get-key result ["status"]))))
  (return
   #{waiting setWaiting
     onAction errored}))

(defn.js useSubmitResult
  "uses the submit option with error result"
  {:added "4.0"}
  [#{onResult
     onSubmit
     onError
     onSuccess
     result
     setResult}]
  (var isMounted (-/useIsMounted))
  (:# "eslint-disable-next-line react-hooks/rules-of-hooks")
  (:= [result setResult] (:? (k/nil? setResult)
                             (-/local)
                             [result setResult]))
  (-/watch [result]
    (when onResult (onResult result)))
  (var #{waiting setWaiting
         onAction
         errored} (-/useSubmit #{onSubmit
                                 onError
                                 onSuccess
                                 result
                                 setResult
                                 isMounted}))
  (return #{waiting
            setWaiting
            onAction
            errored
            isMounted
            result
            setResult
            {:onActionPress (fn:> (:? errored
                                      (setResult nil)
                                      (onAction)))
             :onActionReset (fn:> (setResult nil))}}))

;;
;;

(defn.js convertIndex
  "converts list value to list index"
  {:added "4.0"}
  [#{[data
      value
      setValue
      allowNotFound
      (:= valueFn j/identity)]}]
  (var forwardFn (fn [idx]
                   (var out  (and data (. data [(or idx 0)])))
                   (return (:? out (valueFn out)))))
  (var reverseFn (fn [label]
                   (var idx (j/indexOf (j/map data valueFn)
                                       label))
                   (return (:? allowNotFound idx (j/max 0 idx)))))
  (var setIndex (fn [idx] (setValue (forwardFn idx))))
  (var index    (reverseFn value))
  (var items    (j/map data valueFn))
  (return #{setIndex
            items
            index}))

(defn.js convertModular
  "converts index to modular
 
   (!.js
    (r/convertModular {:values [\"A\"]
                       :setValues k/identity
                       :data [\"A\" \"B\" \"C\"]
                       :indexFn (fn:> -10)}))
   => {\"index\" -9, \"items\" [\"A\" \"B\" \"C\"]}"
  {:added "4.0"}
  [#{[data
      value
      setValue
      (:= valueFn j/identity)
      indexFn]}]
  (var forwardFn (fn [idx]
                   (var out (and data (. data [(k/mod-pos (or idx 0)
                                                          (k/len data))])))
                   (return (:? out (valueFn out)))))
  (var reverseFn (fn [label]
                   (var pval (indexFn))
                   (var nval (j/max 0 (j/indexOf (j/map data valueFn)
                                                 label)))
                   (var offset (k/mod-offset pval nval (k/len data)))
                   (return (+ pval offset))))
  (var setIndex (fn [idx] (setValue (forwardFn idx))))
  (var index    (reverseFn value))
  (var items    (j/map data valueFn))
  (return #{setIndex
            items
            index}))

(defn.js convertIndices
  "converts indices to values"
  {:added "4.0"}
  [#{[data
      values
      setValues
      (:= valueFn j/identity)]}]
  (var forwardFn (fn [indices]
                   (var out [])
                   (k/for:array [[i e] indices]
                     (when e
                       (x:arr-push out (. data [i]))))
                   (return out)))
  (var reverseFn  (fn:> [values] (j/map data (fn:> [e] (<= 0 (j/indexOf values e))))))
  (var setIndices (fn [indices] (setValues (forwardFn indices))))
  (var indices    (reverseFn values))
  (var items      (j/map data valueFn))
  (return #{items
            setIndices
            indices}))

(defn.js convertPosition
  "converts index to position"
  {:added "4.0"}
  [#{length max min step}]
  (var divisions (j/floor (/ (- max min) step)))
  (var unit      (/ length divisions))
  (var forwardFn
       (fn [value]
         (var n (j/floor (/ (- value min)
                            step)))
         (return (* n unit))))
  (var reverseFn
       (fn [pos]
         (var relative  (j/max 0 (j/min length pos)))
         (var n         (j/round (/ relative unit)))
         (var out (+ min (* n step)))
         (return out)))
  (return #{forwardFn
            reverseFn}))

(defn.js useChanging
  "uses value and setValue that may be influenced by available data"
  {:added "4.0"}
  [data f state]
  (:= f (or f k/first))
  (:= data (or data []))
  (:# "eslint-disable-next-line react-hooks/rules-of-hooks")
  (var [value setValue] (or state (-/local (f data))))
  (-/watch [(k/json-encode data)]
    (when (and (k/not-empty? data)
               (or (k/nil? value)
                   (> 0 (j/indexOf data value))))
      (setValue (f data))))
  (return [value setValue]))

(defn.js useTree
  "tree function helper"
  {:added "4.0"}
  [#{tree
     root
     parents
     initial
     setInitial
     branchesFn
     targetFn
     formatFn
     displayFn}]
  (:= branchesFn (or branchesFn
                     (fn [tree _parents _root]
                       (if tree
                         (return (k/sort (k/obj-keys tree)))
                         (return [])))))
  (:= targetFn   (or targetFn
                     (fn [tree branch _parents _root]
                       (if tree
                         (return (k/get-key tree branch))
                         (return nil)))))
  (var branches  (branchesFn tree parents root))
  (var [branch setBranch] (-/local (or initial (k/first branches))))
  (var target (:? (and tree branch)
                  (targetFn tree branch parents root)))
  (-/watch [branch initial]
    (when (and (k/not-nil? branch)
               (k/nil? target)
               (k/not-empty? branches)
               (targetFn tree (k/first branches) parents root))
      (setBranch (k/first branches)))
    (when (and (k/not-nil? branch)
               setInitial
               (not= initial branch))
      (setInitial branch)))
  (var view (displayFn target branch parents root))
  (return #{branch
            setBranch
            branches
            view}))
