(ns js.cell.base-fn
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[js.core :as j]
             [js.cell.base-util :as util]
             [xt.lang.base-runtime :as rt :with [defvar.js]]
             [xt.lang.base-lib :as k]]})

(defvar.js ^{:ns "@"}
  CELL_STATE
  "gets worker state
 
   (base-fn/CELL_STATE)
   => map?"
  {:added "4.0"}
  []
  (return  {:eval true}))

(defvar.js ^{:ns "@"}
  CELL_ROUTES
  "gets worker routes
 
   (base-fn/CELL_ROUTES)
   => map?"
  {:added "4.0"}
  []
  (return  {}))

(defn.js get-state
  "gets cell state"
  {:added "4.0"}
  [worker]
  (return (-/CELL_STATE)))

(defn.js get-routes
  "gets cell routes"
  {:added "4.0"}
  [worker]
  (return (or (and worker (. worker routes))
              (-/CELL_ROUTES))))

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
  [worker op topic status body]
  (return (j/postMessage worker {:op op
                                 :topic topic
                                 :status status
                                 :body body})))

(defn.js ^{:api/route "@/trigger-async"
           :api/static false
           :api/async  true}
  fn-trigger-async
  "triggers an event after a delay"
  {:added "4.0"}
  [worker op topic status body ms]
  (return (j/future-delayed [ms]
            (return (-/fn-trigger worker op topic status body)))))

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
                              :topic util/EV_STATE
                              :status "ok"
                              :body  state}))
            (return state))))

(defn.js ^{:api/route "@/final-set"
           :api/static false}
  fn-final-set
  "sets the worker state to final"
  {:added "4.0"}
  [worker suppress]
  (return (-/fn-set-state worker
                          (-/CELL_STATE)
                          (fn [state]
                            (k/set-key state "final" true))
                          suppress)))

(defn.js ^{:api/route "@/final-status"
           :api/static false}
  fn-final-status
  "gets the final status"
  {:added "4.0"}
  [worker]
  (return (. (-/CELL_STATE) ["final"])))

(defn.js ^{:api/route "@/eval-enable"
           :api/static false}
  fn-eval-enable
  "enables eval"
  {:added "4.0"}
  [worker suppress]
  (return (-/fn-set-state worker
                          (-/CELL_STATE)
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
                          (-/CELL_STATE)
                          (fn [state]
                            (k/set-key state "eval" false))
                          suppress)))

(defn.js ^{:api/route "@/eval-status"
           :api/static true}
  fn-eval-status
  "gets the eval status"
  {:added "4.0"}
  []
  (return (. (-/CELL_STATE) ["eval"])))

(defn.js ^{:api/route "@/route-list"
           :api/static true}
  fn-route-list
  "gets the routes list"
  {:added "4.0"}
  []
  (return (Object.keys (-/CELL_ROUTES))))

(defn.js ^{:api/route "@/route-entry"
           :api/static true}
  fn-route-entry
  "gets a route entry"
  {:added "4.0"}
  [name]
  (return (. (-/CELL_ROUTES)
             [name])))

(defn.js ^{:api/route "@/ping"
           :api/static true}
  fn-ping
  "pings the worker"
  {:added "4.0"}
  []
  (return ["pong" (k/now-ms)]))

(defn.js ^{:api/route "@/ping-async"
           :api/static true
           :api/async  true}
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
           :api/async  true}
  fn-echo-async
  "echos the first arg after delay"
  {:added "4.0"}
  [arg ms]
  (return(j/future-delayed [ms]
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
           :api/async  true}
  fn-error-async
  "throws an error after delay"
  {:added "4.0"}
  [ms]
  (return (j/future-delayed [ms]
            (-/fn-error))))

(defn tmpl-local-route
  "templates a local function"
  {:added "4.0"}
  [{:api/keys [route static async]
    :as entry}]
  (let [handler (cond->> (l/sym-full entry)
                  (not static) (list 'js.cell.base-fn/fn-self))
        args    (nth (:form entry) 2)]
    [route {:handler handler
            :async (true? async)
            :args  (mapv str (if static
                                args
                                (rest args)))}]))

(def +locals+
  (mapv tmpl-local-route
        (l/module-entries :js 'js.cell.base-fn
                          :api/route)))

(defn.js routes-base
  "returns the base routes"
  {:added "4.0"}
  []
  (return (@! (cons 'tab +locals+))))

(defn.js routes-init
  "initiates the base routes"
  {:added "4.0"}
  [routes worker]
  (cond worker
        (do (k/set-key worker "routes" routes)
            (return worker))

        :else
        (return (-/CELL_ROUTES-reset
                 (k/obj-assign (-/routes-base)
                               routes)))))

