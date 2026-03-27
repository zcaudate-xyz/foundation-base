(ns js.cell.kernel.worker-fn
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-runtime :as rt :with [defvar.js]]
             [xt.lang.base-lib :as k]]})

(def$.js EV_STATE   "@/::STATE")

(defvar.js ^{:ns "@"}
  WORKER_STATE
  "gets worker state"
  {:added "4.0"}
  []
  (return {:eval true}))

(defvar.js ^{:ns "@"}
  WORKER_ACTIONS
  "gets worker actions"
  {:added "4.0"}
  []
  (return {}))

(defn.js get-state
  "gets cell state"
  {:added "4.0"}
  [worker]
  (return (-/WORKER_STATE)))

(defn.js get-actions
  "gets cell actions"
  {:added "4.0"}
  [worker]
  (return (or (and worker (. worker ["actions"]))
              (-/WORKER_ACTIONS))))

(defn.js fn-self
  "applies arguments along with `self`"
  {:added "4.0"}
  [f]
  (return (fn [...args]
            (return (f self ...args)))))

(defn.js ^{:api/route "@/trigger"
           :api/static false}
  fn-trigger
  "triggers an event"
  {:added "4.0"}
  [worker op signal status body]
  (return (j/postMessage worker {:op op
                                 :signal signal
                                 :status status
                                 :body body})))

(defn.js ^{:api/route "@/trigger-async"
           :api/static false
           :api/is-async true}
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
              (j/postMessage worker {:op "stream"
                                     :signal -/EV_STATE
                                     :status "ok"
                                     :body state}))
            (return state))))

(defn.js ^{:api/route "@/final-set"
           :api/static false}
  fn-final-set
  "sets the worker state to final"
  {:added "4.0"}
  [worker suppress]
  (return (-/fn-set-state worker
                          (-/WORKER_STATE)
                          (fn [state]
                            (k/set-key state "final" true))
                          suppress)))

(defn.js ^{:api/route "@/final-status"
           :api/static false}
  fn-final-status
  "gets the final status"
  {:added "4.0"}
  [worker]
  (return (. (-/WORKER_STATE) ["final"])))

(defn.js ^{:api/route "@/eval-enable"
           :api/static false}
  fn-eval-enable
  "enables eval"
  {:added "4.0"}
  [worker suppress]
  (return (-/fn-set-state worker
                          (-/WORKER_STATE)
                          (fn [state]
                            (k/set-key state "eval" true))
                          suppress)))

(defn.js ^{:api/route "@/eval-disable"
           :api/static false}
  fn-eval-disable
  "disables eval"
  {:added "4.0"}
  [worker suppress]
  (return (-/fn-set-state worker
                          (-/WORKER_STATE)
                          (fn [state]
                            (k/set-key state "eval" false))
                          suppress)))

(defn.js ^{:api/route "@/eval-status"
           :api/static true}
  fn-eval-status
  "gets the eval status"
  {:added "4.0"}
  []
  (return (. (-/WORKER_STATE) ["eval"])))

(defn.js ^{:api/route "@/action-list"
           :api/static true}
  fn-action-list
  "gets the actions list"
  {:added "4.0"}
  []
  (return (Object.keys (-/WORKER_ACTIONS))))

(defn.js ^{:api/route "@/action-entry"
           :api/static true}
  fn-action-entry
  "gets an action entry"
  {:added "4.0"}
  [name]
  (return (. (-/WORKER_ACTIONS) [name])))

(defn.js ^{:api/route "@/ping"
           :api/static true}
  fn-ping
  "pings the worker"
  {:added "4.0"}
  []
  (return ["pong" (k/now-ms)]))

(defn.js ^{:api/route "@/ping-async"
           :api/static true
           :api/is-async true}
  fn-ping-async
  "pings after a delay"
  {:added "4.0"}
  [ms]
  (return (j/future-delayed [ms]
            (return (-/fn-ping)))))

(defn.js ^{:api/route "@/echo"
           :api/static true}
  fn-echo
  "echos the first arg"
  {:added "4.0"}
  [arg]
  (return [arg (k/now-ms)]))

(defn.js ^{:api/route "@/echo-async"
           :api/static true
           :api/is-async true}
  fn-echo-async
  "echos the first arg after delay"
  {:added "4.0"}
  [arg ms]
  (return (j/future-delayed [ms]
            (return (-/fn-echo arg)))))

(defn.js ^{:api/route "@/error"
           :api/static true}
  fn-error
  "throws an error"
  {:added "4.0"}
  []
  (throw ["error" (k/now-ms)]))

(defn.js ^{:api/route "@/error-async"
           :api/static true
           :api/is-async true}
  fn-error-async
  "throws an error after delay"
  {:added "4.0"}
  [ms]
  (return (j/future-delayed [ms]
            (-/fn-error))))

(defn tmpl-local-action
  "templates a local function"
  {:added "4.0"}
  [{:api/keys [route static is-async]
    :as entry}]
  (let [handler (cond->> (l/sym-full entry)
                  (not static) (list 'js.cell.kernel.worker-fn/fn-self))
        args    (nth (:form entry) 2)]
    [route {:handler handler
            :async  (true? is-async)
            :args   (mapv str (if static
                                args
                                (rest args)))}]))

(def +locals+
  (mapv tmpl-local-action
        (l/module-entries :js 'js.cell.kernel.worker-fn
                          :api/route)))

(defn.js actions-base
  "returns the base actions"
  {:added "4.0"}
  []
  (return (@! (cons 'tab +locals+))))

(defn.js actions-init
  "initiates the base actions"
  {:added "4.0"}
  [actions worker]
  (cond worker
        (do (k/set-key worker "actions" actions)
            (return worker))

        :else
        (return (-/WORKER_ACTIONS-reset
                 (k/obj-assign (-/actions-base)
                               actions)))))
