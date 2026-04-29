(ns xt.cell.kernel.inner-state
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-async :as async]
             [xt.cell.kernel.base-util :as util]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-space :as rt :with [defsingleton.xt]]]})


(defspec.xt WORKER_STATE
  [:fn [] xt.cell.kernel.spec/WorkerState])

(defspec.xt WORKER_ACTIONS
  [:fn [] xt.cell.kernel.spec/WorkerActionMap])

(defspec.xt get-state
  [:fn [:xt/any] xt.cell.kernel.spec/WorkerState])

(defspec.xt get-actions
  [:fn [:xt/any] xt.cell.kernel.spec/WorkerActionMap])

(defspec.xt set-actions
  [:fn [xt.cell.kernel.spec/WorkerActionMap [:xt/maybe :xt/any]] :xt/any])

(defspec.xt fn-self
  [:fn [[:fn [:xt/any] :xt/any]] :xt/any])

(defspec.xt fn-bind
  [:fn [:xt/any [:fn [:xt/any] :xt/any]] :xt/any])

(defspec.xt fn-trigger
  [:fn [:xt/any :xt/str :xt/str :xt/str :xt/any] :xt/any])

(defspec.xt fn-trigger-async
  [:fn [:xt/any :xt/str :xt/str :xt/str :xt/any :xt/int] :xt/any])

(defspec.xt fn-set-state
  [:fn [:xt/any xt.cell.kernel.spec/WorkerState [:fn [xt.cell.kernel.spec/WorkerState] :xt/any] [:xt/maybe :xt/bool]]
   xt.cell.kernel.spec/WorkerState])

(defspec.xt fn-set-final-status
  [:fn [:xt/any [:xt/maybe :xt/bool]]
   xt.cell.kernel.spec/WorkerState])

(defspec.xt fn-get-final-status
  [:fn [:xt/any] [:xt/maybe :xt/bool]])

(defspec.xt fn-set-eval-status
  [:fn [:xt/any :xt/bool [:xt/maybe :xt/bool]]
   xt.cell.kernel.spec/WorkerState])

(defspec.xt fn-get-eval-status
  [:fn [] :xt/bool])

(defspec.xt fn-get-action-list
  [:fn [] xt.cell.kernel.spec/StringList])

(defspec.xt fn-get-action-entry
  [:fn [:xt/str] [:xt/maybe xt.cell.kernel.spec/WorkerActionEntry]])

(defspec.xt fn-ping
  [:fn [] [:tuple :xt/str :xt/int]])

(defspec.xt fn-ping-async
  [:fn [:xt/int] :xt/any])

(defspec.xt fn-echo
  [:fn [:xt/any] [:tuple :xt/any :xt/int]])

(defspec.xt fn-echo-async
  [:fn [:xt/any :xt/int] :xt/any])

(defspec.xt fn-error
  [:fn [] :xt/any])

(defspec.xt fn-error-async
  [:fn [:xt/int] :xt/any])

(defsingleton.xt ^{:ns "@worker"}
  WORKER_STATE
  "gets worker state
 
   (base-fn/WORKER_STATE)
   => map?"
  {:added "4.0"}
  []
  (return  {:eval true}))

(defsingleton.xt ^{:ns "@worker"}
  WORKER_ACTIONS
  "gets worker actions
 
   (base-fn/WORKER_ACTIONS)
   => map?"
  {:added "4.0"}
  []
  (return  {}))

(defn.xt get-state
  "gets cell state"
  {:added "4.0"}
  [worker]
  (return (-/WORKER_STATE)))

(defn.xt get-actions
  "gets cell actions"
  {:added "4.0"}
  [worker]
  (return (or (and worker (. worker actions))
              (-/WORKER_ACTIONS))))

(defn.xt set-actions
  "initiates the base actions"
  {:added "4.0"}
  [actions worker]
  (cond worker
        (do (xt/x:set-key worker "actions" actions)
            (return worker))
        
        :else
        (return (-/WORKER_ACTIONS-reset actions))))

(defn.xt fn-self
  "applies arguments along with `self`"
  {:added "4.0"}
  [f]
  (return (fn [...args]
            (return (f self ...args)))))

(defn.xt fn-bind
  "applies arguments along with an explicit worker instance"
  {:added "4.0"}
  [worker f]
  (return (fn [...args]
            (return (f worker ...args)))))

(defn.xt ^{:cell/action "@worker/trigger"
           :cell/static false}
  fn-trigger
  "triggers an event"
  {:added "4.0"}
  [worker op signal status body]
  (return (. worker (postMessage {:op op
                                  :signal signal
                                  :status status
                                  :body body}))))

(defn.xt ^{:cell/action "@worker/trigger-async"
           :cell/static false
           :cell/is-async  true}
  fn-trigger-async
  "triggers an event after a delay"
  {:added "4.0"}
  [worker op signal status body ms]
  (return (async/promise-delay
           ms
           (fn []
             (return (-/fn-trigger worker op signal status body))))))

(defn.xt fn-set-state
  "helper to set the state and emit event"
  {:added "4.0"}
  [worker state set-fn suppress]
  (cond (xt/x:get-key state "final")
        (throw "Worker State is Final.")
        
         :else
         (do (set-fn state)
             (when (not suppress)
               (. worker (postMessage (util/resp-stream util/EV_STATE state))))
             (return state))))

(defn.xt ^{:cell/action "@worker/set-final-status"
           :cell/static false}
  fn-set-final-status
  "sets the worker state to final"
  {:added "4.0"}
  [worker suppress]
  (return (-/fn-set-state worker
                          (-/WORKER_STATE)
                          (fn [state]
                            (xt/x:set-key state "final" true))
                          suppress)))

(defn.xt ^{:cell/action "@worker/get-final-status"
           :cell/static false}
  fn-get-final-status
  "gets the final status"
  {:added "4.0"}
  [worker]
  (return (. (-/WORKER_STATE) ["final"])))

(defn.xt ^{:cell/action "@worker/set-eval-status"
           :cell/static false}
  fn-set-eval-status
  "enables eval"
  {:added "4.0"}
  [worker status suppress]
  (return (-/fn-set-state worker
                          (-/WORKER_STATE)
                          (fn [state]
                            (xt/x:set-key state "eval" status))
                          suppress)))

(defn.xt ^{:cell/action "@worker/get-eval-status"
           :cell/static true}
  fn-get-eval-status
  "gets the eval status"
  {:added "4.0"}
  []
  (return (. (-/WORKER_STATE) ["eval"])))

(defn.xt ^{:cell/action "@worker/get-action-list"
           :cell/static true}
  fn-get-action-list
  "gets the actions list"
  {:added "4.0"}
  []
  (return (xt/x:obj-keys (-/WORKER_ACTIONS))))

(defn.xt ^{:cell/action "@worker/get-action-entry"
           :cell/static true}
  fn-get-action-entry
  "gets a action entry"
  {:added "4.0"}
  [name]
  (return (. (-/WORKER_ACTIONS)
             [name])))

(defn.xt ^{:cell/action "@worker/ping"
           :cell/static true}
  fn-ping
  "pings the worker"
  {:added "4.0"}
  []
  (return ["pong" (xt/x:now-ms)]))

(defn.xt ^{:cell/action "@worker/ping.async"
           :cell/static true
           :cell/is-async  true}
  fn-ping-async
  "pings after a delay"
  {:added "4.0"}
  [ms]
  (return (async/promise-delay
           ms
           (fn []
             (return (-/fn-ping))))))

(defn.xt ^{:cell/action "@worker/echo"
           :cell/static true}
  fn-echo
  "echos the first arg"
  {:added "4.0"}
  [arg]
  (return [arg (xt/x:now-ms)]))

(defn.xt ^{:cell/action "@worker/echo.async"
           :cell/static true
           :cell/is-async  true}
  fn-echo-async
  "echos the first arg after delay"
  {:added "4.0"}
  [arg ms]
  (return (async/promise-delay
           ms
           (fn []
             (return (-/fn-echo arg))))))

(defn.xt ^{:cell/action "@worker/error"
           :cell/static true}
  fn-error
  "throws an error"
  {:added "4.0"}
  []
  (throw ["error" (xt/x:now-ms)]))

(defn.xt ^{:cell/action "@worker/error.async"
           :cell/static true
           :cell/is-async  true}
  fn-error-async
  "throws an error after delay"
  {:added "4.0"}
  [ms]
  (return (async/promise-delay
           ms
           (fn []
             (return (-/fn-error))))))
