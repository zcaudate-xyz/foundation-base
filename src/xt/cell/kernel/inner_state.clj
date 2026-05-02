(ns xt.cell.kernel.inner-state
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.cell.kernel.base-util :as util]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-space :as rt :with [defsingleton.xt]]]})

(defspec.xt INNER_STATE
  [:fn [] xt.cell.kernel.spec/InnerState])

(defspec.xt INNER_ACTIONS
  [:fn [] xt.cell.kernel.spec/InnerActionMap])

(defspec.xt get-state
  [:fn [:xt/any] xt.cell.kernel.spec/InnerState])

(defspec.xt get-actions
  [:fn [:xt/any] xt.cell.kernel.spec/InnerActionMap])

(defspec.xt set-actions
  [:fn [xt.cell.kernel.spec/InnerActionMap [:xt/maybe :xt/any]] :xt/any])

(defspec.xt fn-trigger
  [:fn [:xt/any :xt/str :xt/str :xt/str :xt/any] :xt/any])

(defspec.xt fn-trigger-async
  [:fn [:xt/any :xt/str :xt/str :xt/str :xt/any :xt/int] :xt/any])

(defspec.xt fn-set-state
  [:fn [:xt/any xt.cell.kernel.spec/InnerState [:fn [xt.cell.kernel.spec/InnerState] :xt/any] [:xt/maybe :xt/bool]]
   xt.cell.kernel.spec/InnerState])

(defspec.xt fn-set-final-status
  [:fn [:xt/any [:xt/maybe :xt/bool]]
   xt.cell.kernel.spec/InnerState])

(defspec.xt fn-get-final-status
  [:fn [:xt/any] [:xt/maybe :xt/bool]])

(defspec.xt fn-set-eval-status
  [:fn [:xt/any :xt/bool [:xt/maybe :xt/bool]]
   xt.cell.kernel.spec/InnerState])

(defspec.xt fn-get-eval-status
  [:fn [] :xt/bool])

(defspec.xt fn-get-action-list
  [:fn [] xt.cell.kernel.spec/StringList])

(defspec.xt fn-get-action-entry
  [:fn [:xt/str] [:xt/maybe xt.cell.kernel.spec/InnerActionEntry]])

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

(defsingleton.xt ^{:ns "@cell"}
  INNER_STATE
  "gets inner state
 
   (base-fn/INNER_STATE)
   => map?"
  {:added "4.0"}
  []
  (return  {:eval true}))

(defsingleton.xt ^{:ns "@cell"}
  INNER_ACTIONS
  "gets inner actions
 
   (base-fn/INNER_ACTIONS)
   => map?"
  {:added "4.0"}
  []
  (return  {}))

(defn.xt get-state
  "gets cell state"
  {:added "4.0"}
  [inner]
  (return (-/INNER_STATE)))

(defn.xt get-actions
  "gets cell actions"
  {:added "4.0"}
  [inner]
  (when (xt/x:not-nil? inner)
    (var actions (xt/x:get-key inner "actions"))
    (when (xt/x:not-nil? actions)
      (return actions)))
  (return (-/INNER_ACTIONS)))

(defn.xt set-actions
  "initiates the base actions"
  {:added "4.0"}
  [actions inner]
  (if (xt/x:not-nil? inner)
    (do (xt/x:set-key inner "actions" actions)
        (return inner))
    (return (-/INNER_ACTIONS-reset actions))))

(defn.xt ^{:cell/action "@cell/trigger"
            :cell/static false}
  fn-trigger
  "triggers an event"
  {:added "4.0"}
  [inner op signal status body]
  (var postMessage (xt/x:get-key inner "postMessage"))
  (return (xt/x:apply postMessage [{:op op
                                    :signal signal
                                    :status status
                                    :body body}])))

(defn.xt ^{:cell/action "@cell/trigger-async"
           :cell/static false
           :cell/is-async  true}
  fn-trigger-async
  "triggers an event after a delay"
  {:added "4.0"}
  [inner op signal status body ms]
  (return (spec-promise/x:with-delay
           ms
           (fn []
             (return (-/fn-trigger inner op signal status body))))))

(defn.xt fn-set-state
  "helper to set the state and emit event"
  {:added "4.0"}
  [inner state set-fn suppress]
  (cond (xt/x:get-key state "final")
        (throw "Inner State is Final.")
        
         :else
         (do (set-fn state)
             (when (not suppress)
               (var postMessage (xt/x:get-key inner "postMessage"))
               (xt/x:apply postMessage [(util/resp-stream util/EV_STATE state)]))
              (return state))))

(defn.xt ^{:cell/action "@cell/set-final-status"
           :cell/static false}
  fn-set-final-status
  "sets the inner state to final"
  {:added "4.0"}
  [inner suppress]
  (return (-/fn-set-state inner
                          (-/INNER_STATE)
                          (fn [state]
                            (xt/x:set-key state "final" true))
                          suppress)))

(defn.xt ^{:cell/action "@cell/get-final-status"
           :cell/static false}
  fn-get-final-status
  "gets the final status"
  {:added "4.0"}
  [inner]
  (return (xt/x:get-key (-/INNER_STATE) "final")))

(defn.xt ^{:cell/action "@cell/set-eval-status"
           :cell/static false}
  fn-set-eval-status
  "enables eval"
  {:added "4.0"}
  [inner status suppress]
  (return (-/fn-set-state inner
                          (-/INNER_STATE)
                          (fn [state]
                            (xt/x:set-key state "eval" status))
                          suppress)))

(defn.xt ^{:cell/action "@cell/get-eval-status"
           :cell/static true}
  fn-get-eval-status
  "gets the eval status"
  {:added "4.0"}
  []
  (return (xt/x:get-key (-/INNER_STATE) "eval")))

(defn.xt ^{:cell/action "@cell/get-action-list"
           :cell/static true}
  fn-get-action-list
  "gets the actions list"
  {:added "4.0"}
  []
  (return (xt/x:obj-keys (-/INNER_ACTIONS))))

(defn.xt ^{:cell/action "@cell/get-action-entry"
           :cell/static true}
  fn-get-action-entry
  "gets a action entry"
  {:added "4.0"}
  [name]
  (return (xt/x:get-key (-/INNER_ACTIONS) name)))

(defn.xt ^{:cell/action "@cell/ping"
           :cell/static true}
  fn-ping
  "pings the inner"
  {:added "4.0"}
  []
  (return ["pong" (xt/x:now-ms)]))

(defn.xt ^{:cell/action "@cell/ping.async"
           :cell/static true
           :cell/is-async  true}
  fn-ping-async
  "pings after a delay"
  {:added "4.0"}
  [ms]
  (return (spec-promise/x:with-delay
           ms
           (fn []
             (return (-/fn-ping))))))

(defn.xt ^{:cell/action "@cell/echo"
           :cell/static true}
  fn-echo
  "echos the first arg"
  {:added "4.0"}
  [arg]
  (return [arg (xt/x:now-ms)]))

(defn.xt ^{:cell/action "@cell/echo.async"
           :cell/static true
           :cell/is-async  true}
  fn-echo-async
  "echos the first arg after delay"
  {:added "4.0"}
  [arg ms]
  (return (spec-promise/x:with-delay
           ms
           (fn []
             (return (-/fn-echo arg))))))

(defn.xt ^{:cell/action "@cell/error"
           :cell/static true}
  fn-error
  "throws an error"
  {:added "4.0"}
  []
  (throw ["error" (xt/x:now-ms)]))

(defn.xt ^{:cell/action "@cell/error.async"
           :cell/static true
           :cell/is-async  true}
  fn-error-async
  "throws an error after delay"
  {:added "4.0"}
  [ms]
  (return (spec-promise/x:with-delay
           ms
           (fn []
             (return (-/fn-error))))))
