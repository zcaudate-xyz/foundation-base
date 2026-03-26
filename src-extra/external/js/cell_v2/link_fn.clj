(ns js.cell-v2.link-fn
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.cell-v2.control :as control]
             [js.cell-v2.link :as link]]})

(defn.js control-action
  "calls a control action over the link"
  {:added "4.0"}
  [link-ref action-id input]
  (return (link/call-action link-ref action-id input nil)))

(defn.js action
  "calls an action over the link"
  {:added "4.0"}
  [link-ref action-id input]
  (return (link/call-action link-ref action-id input nil)))

(defn.js eval
  "evaluates code over the link control surface"
  {:added "4.0"}
  [link-ref code]
  (return (-/control-action link-ref control/ACT_EVAL [code])))

(defn.js trigger
  "triggers a signal over the link control surface"
  {:added "4.0"}
  [link-ref op signal status body]
  (return (-/control-action link-ref control/ACT_TRIGGER [op signal status body])))

(defn.js trigger-async
  "triggers a signal after a delay"
  {:added "4.0"}
  [link-ref op signal status body ms]
  (return (-/control-action link-ref control/ACT_TRIGGER_ASYNC [op signal status body ms])))

(defn.js final-set
  "sets the remote worker state to final"
  {:added "4.0"}
  [link-ref suppress]
  (return (-/control-action link-ref control/ACT_FINAL_SET [suppress])))

(defn.js final-status
  "gets the remote final status"
  {:added "4.0"}
  [link-ref]
  (return (-/control-action link-ref control/ACT_FINAL_STATUS [])))

(defn.js eval-enable
  "enables remote eval"
  {:added "4.0"}
  [link-ref suppress]
  (return (-/control-action link-ref control/ACT_EVAL_ENABLE [suppress])))

(defn.js eval-disable
  "disables remote eval"
  {:added "4.0"}
  [link-ref suppress]
  (return (-/control-action link-ref control/ACT_EVAL_DISABLE [suppress])))

(defn.js eval-status
  "gets the remote eval status"
  {:added "4.0"}
  [link-ref]
  (return (-/control-action link-ref control/ACT_EVAL_STATUS [])))

(defn.js route-list
  "lists remote public routes"
  {:added "4.0"}
  [link-ref]
  (return (-/control-action link-ref control/ACT_ROUTE_LIST [])))

(defn.js route-entry
  "gets remote metadata for a route"
  {:added "4.0"}
  [link-ref route-id]
  (return (-/control-action link-ref control/ACT_ROUTE_ENTRY [route-id])))

(defn.js ping
  "pings the remote worker"
  {:added "4.0"}
  [link-ref]
  (return (-/control-action link-ref control/ACT_PING [])))

(defn.js ping-async
  "pings the remote worker after a delay"
  {:added "4.0"}
  [link-ref ms]
  (return (-/control-action link-ref control/ACT_PING_ASYNC [ms])))

(defn.js echo
  "echos the input value"
  {:added "4.0"}
  [link-ref arg]
  (return (-/control-action link-ref control/ACT_ECHO [arg])))

(defn.js echo-async
  "echos the input value after a delay"
  {:added "4.0"}
  [link-ref arg ms]
  (return (-/control-action link-ref control/ACT_ECHO_ASYNC [arg ms])))

(defn.js error
  "triggers a remote error"
  {:added "4.0"}
  [link-ref]
  (return (-/control-action link-ref control/ACT_ERROR [])))

(defn.js error-async
  "triggers a remote error after a delay"
  {:added "4.0"}
  [link-ref ms]
  (return (-/control-action link-ref control/ACT_ERROR_ASYNC [ms])))
