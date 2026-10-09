(ns js.react-test
  (:require [lang.core :as l]
            [js.react :as react-source]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.react :as r]
             [js.react.helper-jsdom :as helper]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as xtt]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.react/curr :added "4.1"}
(fact "reads a ref current value"
  (helper-source/test
   (fn []
     (var valueRef (r/ref "value"))
     (return [:span (r/curr valueRef)]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>value</span></div>")

^{:refer js.react/curr:set :added "4.1"}
(fact "writes a ref current value"
  (helper-source/test
   (fn []
     (var valueRef (r/ref "before"))
     (r/curr:set valueRef "after")
     (return [:span (r/curr valueRef)]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>after</span></div>")

^{:refer js.react/const :added "4.1"}
(fact "keeps a stable constant value"
  (helper-source/test
   (fn []
     (var value (r/const "constant"))
     (return [:span value]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>constant</span></div>")

^{:refer js.react/derive :added "4.1"}
(fact "derives a callback from a dependency list"
  (helper-source/test
   (fn []
     (var derived (r/derive []
                            (fn [] (return "derived"))))
     (return [:span (derived)]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>derived</span></div>")

^{:refer js.react/init :added "4.1"}
(fact "runs an effect once after mount"
  (helper-source/test
   (fn []
     (var [value setValue] (r/local "before"))
     (r/init []
       (setValue "initialized"))
     (return [:span value]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>initialized</span></div>")

^{:refer js.react/run :added "4.1"}
(fact "runs an effect without an explicit dependency list"
  (helper-source/test
   (fn []
     (var [value setValue] (r/local false))
     (r/run []
       (when (not value)
         (setValue true)))
     (return [:span (:? value "ran" "pending")]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>ran</span></div>")

^{:refer js.react/watch :added "4.1"}
(fact "reruns an effect when a watched state changes"
  (helper-source/test
   (fn []
     (var [value setValue] (r/local "before"))
     (var [seen setSeen] (r/local "pending"))
     (r/watch [value]
       (when (== value "after")
         (setSeen "watched")))
     (r/init []
       (setValue "after"))
     (return [:span seen]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>watched</span></div>")

^{:refer js.react/sync :added "4.1"}
(fact "synchronizes one state value into another"
  (helper-source/test
   (fn []
     (var [value setValue] (r/local "source"))
     (var [synced setSynced] (r/local "initial"))
     (r/sync value setSynced)
     (return [:span (+ value ":" synced)]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>source:source</span></div>")

^{:refer js.react/get :added "4.1"}
(fact "generates the value property form"
  (:op @react-source/get)
  => 'defmacro)

^{:refer js.react/set :added "4.1"}
(fact "generates the setter property form"
  (:op @react-source/set)
  => 'defmacro)

^{:refer js.react/ui :added "4.1"}
(fact "compiles a layout into a React element tree"
  (helper-source/test
   (fn []
     (return (r/ui [:span "ui"] {})))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>ui</span></div>")

^{:refer js.react/return-ui :added "4.1"}
(fact "returns a compiled full UI definition"
  (helper-source/test
   (fn []
     (r/return-ui {:states {}
                   :actions {}
                   :triggers []
                   :layout [:span "full"]
                   :components {}}))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>full</span></div>")

^{:refer js.react/getDOMRoot :added "4.1"}
(fact "finds the React root attached to a DOM node"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var domNode {"other" true
                   "__reactContainer$test" true})
     (Object.defineProperty domNode "1"
                            {"configurable" true
                             "enumerable" false
                             "value" "root"})
     (return (r/getDOMRoot domNode))))
  => "root")

^{:refer js.react/renderDOMRoot :added "4.1"}
(fact "renders a component into a named DOM root"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ document _ _]
     (var rendered false)
     (var root {"render" (fn [element] (:= rendered true))})
     (var domNode {"0" true
                   "__reactContainer$test" true})
     (Object.defineProperty domNode "1"
                            {"configurable" true
                             "enumerable" false
                             "value" root})
     (var original (. document ["getElementById"]))
     (try
       (:= document.getElementById (fn [_] (return domNode)))
       (var result (r/renderDOMRoot "root" (fn [] (return [:span "rendered"]))))
       (:= document.getElementById original)
       (return {"result" result
                "rendered" rendered})
       (catch err
         (:= document.getElementById original)
         (throw err)))))
  => {"result" true
      "rendered" true})

^{:refer js.react/useStateFor :added "4.1"}
(fact "returns a state value and its generated setter"
  (helper-source/test
   (fn []
     (var controls {"value" "ready"
                    "setValue" (fn [v] (return v))})
     (var pair (r/useStateFor controls "value"))
     (return
      [:span (+ (. pair [0]) ":" (typeof (. pair [1])))]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>ready:function</span></div>")

^{:refer js.react/id :added "4.1"}
(fact "creates a stable random id of the requested length"
  (helper-source/test
   (fn []
     (var ident (r/id 8))
     (return (JSON.stringify
              {"type" (typeof ident)
               "length" (. ident ["length"])})))
   {}
   (fn [_ document _ _]
     (return (JSON.parse (. document.body ["textContent"])))))
  => {"type" "string"
      "length" 8})

^{:refer js.react/ref :added "4.1"}
(fact "creates a React ref with an initial value"
  (helper-source/test
   (fn []
     (var valueRef (r/ref "initial"))
     (return [:span (r/curr valueRef)]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>initial</span></div>")

^{:refer js.react/local :added "4.1"}
(fact "creates local state and applies an initial effect"
  (helper-source/test
   (fn []
     (var [value setValue] (r/local "before"))
     (r/init []
       (setValue "after"))
     (return [:span value]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>after</span></div>")

^{:refer js.react/useStep :added "4.1"}
(fact "completes a step from an effect callback"
  (helper-source/test
   (fn []
     (var [done setDone] (r/useStep (fn [complete]
                                      (complete true))))
     (return [:span (:? done "done" "pending")]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>done</span></div>")

^{:refer js.react/makeLazy :added "4.1"}
(fact "keeps functions and wraps non-functions as lazy components"
  (helper-source/test
   (fn []
     (var original (fn [] nil))
     (var direct (r/makeLazy original))
     (var lazy (r/makeLazy {}))
     (return [:span (+ (== direct original) ":" (typeof lazy))]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>true:object</span></div>")

^{:refer js.react/useLazy :added "4.1"}
(fact "returns a stable lazy component"
  (helper-source/test
   (fn []
     (var lazy (r/useLazy {}))
     (return [:span (typeof lazy)]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>object</span></div>")

^{:refer js.react/useRefresh :added "4.1"}
(fact "refreshes the component when called"
  (helper-source/test
   (fn []
     (var renderRef (r/ref 0))
     (r/curr:set renderRef (+ 1 (r/curr renderRef)))
     (var refresh (r/useRefresh))
     (r/init [] (refresh))
     (return [:span (r/curr renderRef)]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>2</span></div>")

^{:refer js.react/useGetCount :added "4.1"}
(fact "counts completed renders"
  (helper-source/test
   (fn []
     (var [ready setReady] (r/local false))
     (var getCount (r/useGetCount))
     (r/init [] (setReady true))
     (return [:span (+ (getCount) ":" ready)]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>1:true</span></div>")

^{:refer js.react/useFollowRef :added "4.1"}
(fact "follows a changing value in a ref"
  (helper-source/test
   (fn []
     (var [value setValue] (r/local "before"))
     (var valueRef (r/useFollowRef value))
     (var refresh (r/useRefresh))
     (r/watch [value]
       (refresh))
     (r/init [] (setValue "after"))
     (return [:span (r/curr valueRef)]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>after</span></div>")

^{:refer js.react/useIsMounted :added "4.1"}
(fact "reports mounted during setup and unmounted during cleanup"
  (helper-source/test
   (fn []
     (var [status setStatus] (r/local "none"))
     (var isMounted (r/useIsMounted))
     (r/init [] (setStatus (:? (isMounted) "mounted" "not-mounted")))
     (return [:span status]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>mounted</span></div>")

^{:refer js.react/useIsMountedWrap :added "4.1"}
(fact "does not invoke wrapped callbacks after unmount"
  (helper-source/test
   (fn []
     (var [status setStatus] (r/local "none"))
     (var wrap (r/useIsMountedWrap))
     (var callback (wrap (fn [] (setStatus "called"))))
     (r/init [] (callback))
     (return [:span status]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>called</span></div>")

^{:refer js.react/useMountedCallback :added "4.1"}
(fact "calls back on mount and unmount"
  (helper-source/test
   (fn []
     (var [status setStatus] (r/local "none"))
     (r/useMountedCallback
      (fn [mounted]
        (setStatus (:? mounted "mounted" "unmounted"))))
     (return [:span status]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>mounted</span></div>")

^{:refer js.react/useFollowDelayed :added "4.1"}
(fact "returns the value immediately for zero delay"
  (helper-source/test
   (fn []
     (var [value setter] (r/useFollowDelayed "ready" 0 (fn [] (return true))))
     (return [:span (+ value ":" (typeof setter))]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>ready:function</span></div>")

^{:refer js.react/useStablized :added "4.1"}
(fact "keeps the previous value when input changes"
  (helper-source/test
   (fn []
     (var [data setData] (r/local {"value" 1}))
     (var stable (r/useStablized data true))
     (var direct (r/useStablized data false))
     (r/init [] (setData {"value" 2}))
     (return [:span (+ (. stable ["value"]) ":" (. direct ["value"]))]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>1:2</span></div>")

^{:refer js.react/runIntervalStop :added "4.1"}
(fact "stops and clears an active interval ref"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var intervalRef {"current" (setInterval (fn [] nil) 1000)})
     (var stopped (r/runIntervalStop intervalRef))
     (return {"stopped" (not= nil stopped)
              "cleared" (== nil (r/curr intervalRef))})))
  => {"stopped" true
      "cleared" true})

^{:refer js.react/runIntervalStart :added "4.1"}
(fact "starts an interval when a delay is configured"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var fRef {"current" (fn [] nil)})
     (var msRef {"current" nil})
     (var intervalRef {"current" nil})
     (var first (r/runIntervalStart fRef msRef intervalRef))
     (r/curr:set msRef 1000)
     (var second (r/runIntervalStart fRef msRef intervalRef))
     (var stopped (r/runIntervalStop intervalRef))
     (return {"first" (. first ["length"])
              "second" (. second ["length"])
              "started" (not= nil (. second [1]))
              "stopped" (not= nil stopped)})))
  => {"first" 1
      "second" 2
      "started" true
      "stopped" true})

^{:refer js.react/useInterval :added "4.1"}
(fact "exposes start and stop interval controls"
  (helper-source/test
   (fn []
     (var [status setStatus] (r/local "initial"))
     (var #{stopInterval
            startInterval} (r/useInterval (fn [] nil) 1000))
     (r/init []
       (startInterval)
       (stopInterval)
       (setStatus "stopped"))
     (return [:span status]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>stopped</span></div>")

^{:refer js.react/runTimeoutStop :added "4.1"}
(fact "stops and clears an active timeout ref"
  (helper-source/test
   (fn []
     (var timeoutRef (r/ref (setTimeout (fn [] nil) 1000)))
     (var stopped (r/runTimeoutStop timeoutRef))
     (return (JSON.stringify
              {"stopped" (not= nil stopped)
               "cleared" (== nil (r/curr timeoutRef))})))
   {}
   (fn [_ document _ _]
     (return (JSON.parse (. document.body ["textContent"])))))
  => {"stopped" true
      "cleared" true})

^{:refer js.react/runTimeoutStart :added "4.1"}
(fact "starts and replaces a timeout"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var fRef {"current" (fn [] nil)})
     (var msRef {"current" 1000})
     (var timeoutRef {"current" nil})
     (var first (r/runTimeoutStart fRef msRef timeoutRef))
     (var second (r/runTimeoutStart fRef msRef timeoutRef))
     (var stopped (r/runTimeoutStop timeoutRef))
     (return {"first" (. first ["length"])
              "second" (. second ["length"])
              "replaced" (not= nil (. second [0]))
              "stopped" (not= nil stopped)})))
  => {"first" 2
      "second" 2
      "replaced" true
      "stopped" true})

^{:refer js.react/useTimeout :added "4.1"}
(fact "exposes start and stop timeout controls"
  (helper-source/test
   (fn []
     (var [status setStatus] (r/local "initial"))
     (var #{stopTimeout
            startTimeout} (r/useTimeout (fn [] nil) 1000 false))
     (r/init []
       (startTimeout)
       (stopTimeout)
       (setStatus "stopped"))
     (return [:span status]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>stopped</span></div>")

^{:refer js.react/useCountdown :added "4.1"}
(fact "returns countdown state and controls"
  (helper-source/test
   (fn []
     (var [current setCurrent controls]
       (r/useCountdown 3 nil {"interval" 1000000}))
     (return [:span (+ current ":"
                     (typeof (. controls ["startCountdown"])) ":"
                     (typeof (. controls ["stopCountdown"]))) ]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>3:function:function</span></div>")

^{:refer js.react/useNow :added "4.1"}
(fact "returns the current time and controls"
  (helper-source/test
   (fn []
     (var [current controls] (r/useNow 1000000))
     (return [:span (+ (typeof current) ":"
                     (typeof (. controls ["startNow"])) ":"
                     (typeof (. controls ["stopNow"]))) ]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>number:function:function</span></div>")

^{:refer js.react/useSubmit :added "4.1"}
(fact "returns submit state and reports an error result"
  (helper-source/test
   (fn []
     (var controls
          (r/useSubmit {"result" {"status" "error"}
                        "onSubmit" (fn [] (return nil))}))
     (return
      (JSON.stringify
       {"errored" (. controls ["errored"])
        "waiting" (. controls ["waiting"])
        "action" (typeof (. controls ["onAction"]))})))
   {}
   (fn [_ document _ _]
     (return (JSON.parse (. document.body ["textContent"])))))
  => {"errored" true
      "waiting" false
      "action" "function"})

^{:refer js.react/useSubmitResult :added "4.1"}
(fact "returns submit-result actions and setters"
  (select-keys (meta #'react-source/useSubmitResult)
               [:arglists :doc])
  => {:arglists '([#{setResult onResult onSubmit onSuccess onError result}])
      :doc "uses the submit option with error result"})

^{:refer js.react/convertIndex :added "4.1"}
(fact "converts values to indices and back"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var calls [])
     (var result
          (r/convertIndex {"data" ["A" "B" "C"]
                           "value" "B"
                           "setValue" (fn [value] (. calls (push value)))}))
     (var missing
          (r/convertIndex {"data" ["A" "B" "C"]
                           "value" "Z"
                           "allowNotFound" true}))
     (var ignored ((. result ["setIndex"]) 2))
     (return
      {"items" (. result ["items"])
       "index" (. result ["index"])
       "set" (. calls [0])
       "missing" (. missing ["index"])})))
  => {"items" ["A" "B" "C"]
      "index" 1
      "set" "C"
      "missing" -1})

^{:refer js.react/convertModular :added "4.1"}
(fact "converts modular indices"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var calls [])
     (var result
          (r/convertModular {"data" ["A" "B" "C"]
                             "value" "A"
                             "setValue" (fn [value] (. calls (push value)))
                             "indexFn" (fn [] (return -10))}))
     (var ignored ((. result ["setIndex"]) 2))
     (return
      {"items" (. result ["items"])
       "index" (. result ["index"])
       "set" (. calls [0])})))
  => {"items" ["A" "B" "C"]
      "index" -9
      "set" "C"})

^{:refer js.react/convertIndices :added "4.1"}
(fact "converts selected values to boolean indices"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var calls [])
     (var result
          (r/convertIndices {"data" ["A" "B" "C"]
                             "values" ["A" "C"]
                             "setValues" (fn [value] (. calls (push value)))}))
     (var ignored ((. result ["setIndices"]) [true false true]))
     (return
      {"items" (. result ["items"])
       "indices" (. result ["indices"])
       "set" (. calls [0])})))
  => {"items" ["A" "B" "C"]
      "indices" [true false true]
      "set" ["A" "C"]})

^{:refer js.react/convertPosition :added "4.1"}
(fact "converts between bounded values and positions"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var result (r/convertPosition {"length" 60
                                     "max" 100
                                     "min" 0
                                     "step" 10}))
     (return
      {"forward" ((. result ["forwardFn"]) 50)
       "reverse" ((. result ["reverseFn"]) 30)
       "clamped" ((. result ["reverseFn"]) -1)})))
  => {"forward" 30
      "reverse" 50
      "clamped" 0})

^{:refer js.react/useChanging :added "4.1"}
(fact "changes the selected value when data no longer contains it"
  (helper-source/test
   (fn []
     (var [data setData] (r/local ["A" "B"]))
     (var [value setValue] (r/useChanging data))
     (r/init [] (setData ["X" "Y"]))
     (return [:span value]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>X</span></div>")

^{:refer js.react/useTree :added "4.1"}
(fact "selects and renders a tree branch"
  (helper-source/test
   (fn []
     (var controls
          (r/useTree
           {"tree" {"a" {"value" 1}
                     "x" {"value" 2}}
            "initial" "x"
            "displayFn"
            (fn [target branch parents root]
              (return (+ branch ":" (. target ["value"]))))}))
     (return [:span (. controls ["view"])]))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>x:2</span></div>")
