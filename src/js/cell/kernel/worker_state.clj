(ns js.cell.kernel.worker-state
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [js.cell.kernel.base-util :as util]
             [xt.lang.base-runtime :as rt :with [defvar.js]]
             [xt.lang.base-lib :as k]]})

(defvar.js ^{:ns "@worker"}
  WORKER_STATE
  "gets worker state
 
   (base-fn/WORKER_STATE)
   => map?"
  {:added "4.0"}
  []
  (return  {:eval true}))

(defvar.js ^{:ns "@worker"}
  WORKER_ACTIONS
  "gets worker actions
 
   (base-fn/WORKER_ACTIONS)
   => map?"
  {:added "4.0"}
  []
  (return  {}))

(defn.js get-state
  "gets cell state"
  {:added "4.0"}
  [worker]
  (return (-/WORKER_STATE)))

(defn.js get-actions
  "gets cell actions"
  {:added "4.0"}
  [worker]
  (return (or (and worker (. worker actions))
              (-/WORKER_ACTIONS))))

(defn.js set-actions
  "initiates the base actions"
  {:added "4.0"}
  [actions worker]
  (cond worker
        (do (k/set-key worker "actions" actions)
            (return worker))
        
        :else
        (return (-/WORKER_ACTIONS-reset actions))))

(defn.js fn-self
  "applies arguments along with `self`"
  {:added "4.0"}
  [f]
  (return (fn [...args]
            (return (f self ...args)))))

(defn.js ^{:cell/action "@worker/trigger"
           :cell/static false}
  fn-trigger
  "triggers an event"
  {:added "4.0"}
  [worker op signal status body]
  (return (j/postMessage worker {:op op
                                 :signal signal
                                 :status status
                                 :body body})))

(defn.js ^{:cell/action "@worker/trigger-async"
           :cell/static false
           :cell/async  true}
  fn-trigger-async
  "triggers an event after a delay"
  {:added "4.0"}
  [worker op signal status body ms]
  (return (j/future-delayed [ms]
            (return (-/fn-trigger worker op signal status body)))))

(defn.js fn-set-state
  "helper to set the state and emit event"
  {:added "4.0"}
  [worker state set-fn suppress]
  (cond (k/get-key state "final")
        (throw "Worker State is Final.")
        
        :else
        (do (set-fn state)
            (when (not suppress)
              (j/postMessage worker
                             {:op "stream"
                              :signal util/EV_STATE
                              :status "ok"
                              :body  state}))
            (return state))))

(defn.js ^{:cell/action "@worker/set-final-status"
           :cell/static false}
  fn-set-final-status
  "sets the worker state to final"
  {:added "4.0"}
  [worker suppress]
  (return (-/fn-set-state worker
                          (-/WORKER_STATE)
                          (fn [state]
                            (k/set-key state "final" true))
                          suppress)))

(defn.js ^{:cell/action "@worker/get-final-status"
           :cell/static false}
  fn-get-final-status
  "gets the final status"
  {:added "4.0"}
  [worker]
  (return (. (-/WORKER_STATE) ["final"])))

(defn.js ^{:cell/action "@worker/set-eval-status"
           :cell/static false}
  fn-set-eval-status
  "enables eval"
  {:added "4.0"}
  [worker status suppress]
  (return (-/fn-set-state worker
                          (-/WORKER_STATE)
                          (fn [state]
                            (k/set-key state "eval" status))
                          suppress)))

(defn.js ^{:cell/action "@worker/get-eval-status"
           :cell/static true}
  fn-get-eval-status
  "gets the eval status"
  {:added "4.0"}
  []
  (return (. (-/WORKER_STATE) ["eval"])))

(defn.js ^{:cell/action "@worker/get-action-list"
           :cell/static true}
  fn-get-action-list
  "gets the actions list"
  {:added "4.0"}
  []
  (return (Object.keys (-/WORKER_ACTIONS))))

(defn.js ^{:cell/action "@worker/get-action-entry"
           :cell/static true}
  fn-get-action-entry
  "gets a action entry"
  {:added "4.0"}
  [name]
  (return (. (-/WORKER_ACTIONS)
             [name])))

(defn.js ^{:cell/action "@worker/ping"
           :cell/static true}
  fn-ping
  "pings the worker"
  {:added "4.0"}
  []
  (return ["pong" (k/now-ms)]))

(defn.js ^{:cell/action "@worker/ping.async"
           :cell/static true
           :cell/async  true}
  fn-ping-async
  "pings after a delay"
  {:added "4.0"}
  [ms]
  (return (j/future-delayed [ms]
            (return (-/fn-ping)))))

(defn.js ^{:cell/action "@worker/echo"
           :cell/static true}
  fn-echo
  "echos the first arg"
  {:added "4.0"}
  [arg]
  (return [arg (k/now-ms)]))

(defn.js ^{:cell/action "@worker/echo.async"
           :cell/static true
           :cell/async  true}
  fn-echo-async
  "echos the first arg after delay"
  {:added "4.0"}
  [arg ms]
  (return(j/future-delayed [ms]
           (return (-/fn-echo arg)))))

(defn.js ^{:cell/action "@worker/error"
           :cell/static true}
  fn-error
  "throws an error"
  {:added "4.0"}
  []
  (throw ["error" (k/now-ms)]))

(defn.js ^{:cell/action "@worker/error.async"
           :cell/static true
           :cell/async  true}
  fn-error-async
  "throws an error after delay"
  {:added "4.0"}
  [ms]
  (return (j/future-delayed [ms]
            (-/fn-error))))
