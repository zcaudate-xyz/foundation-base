(ns xt.ui.state.core
  "Lifecycle, subscriptions and action dispatch for UI-independent state sources."
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defspec.xt StateSource :xt/any)
(defspec.xt UiState :xt/any)
(defspec.xt UiActions :xt/any)

(defn.xt controller-create
  "creates a headless state source whose handlers own effects"
  [initial-state handlers lifecycle deps]
  (return {"state" (or initial-state {})
           "revision" 0
           "handlers" (or handlers {})
           "lifecycle" (or lifecycle {})
           "deps" (or deps {})
           "listeners" {}
           "opened" false}))

(defn.xt snapshot [controller]
  (return (. controller ["state"])))

(defn.xt revision [controller]
  (return (. controller ["revision"])))

(defn.xt notify! [controller]
  (xt/for:object [[_ listener] (. controller ["listeners"])]
    (listener (-/snapshot controller) (-/revision controller)))
  (return controller))

(defn.xt set-state! [controller state]
  (xt/x:set-key controller "state" (or state {}))
  (xt/x:set-key controller "revision" (+ 1 (-/revision controller)))
  (-/notify! controller)
  (return state))

(defn.xt update-state! [controller update-fn]
  (return (-/set-state! controller (update-fn (-/snapshot controller)))))

(defn.xt subscribe! [controller listener-id listener]
  (xt/x:set-key (. controller ["listeners"]) listener-id listener)
  (return listener-id))

(defn.xt unsubscribe! [controller listener-id]
  (xt/x:del-key (. controller ["listeners"]) listener-id)
  (return true))

(defn.xt dispatch!
  "dispatches an intent; handlers receive controller, payload and dependencies"
  [controller action-id payload]
  (var handler (xt/x:get-key (. controller ["handlers"]) action-id))
  (when (not (xt/x:is-function? handler))
    (return (promise/x:promise-run
             {"status" "unavailable" "action" action-id})))
  (return
   (promise/x:promise-run
    (handler controller payload (. controller ["deps"])))))

(defn.xt actions-create [controller action-ids]
  (var actions {})
  (xt/for:array [action-id (or action-ids [])]
    (xt/x:set-key actions action-id
                  (fn [payload]
                    (return (-/dispatch! controller action-id payload)))))
  (return actions))

(defn.xt open! [controller]
  (when (== true (. controller ["opened"]))
    (return (promise/x:promise-run controller)))
  (xt/x:set-key controller "opened" true)
  (var handler (. controller ["lifecycle"] ["open"]))
  (when (not (xt/x:is-function? handler))
    (return (promise/x:promise-run controller)))
  (return
   (promise/x:promise-then
    (promise/x:promise-run
     (handler controller (. controller ["deps"])))
    (fn [_] (return controller)))))

(defn.xt close! [controller]
  (when (not= true (. controller ["opened"]))
    (return (promise/x:promise-run true)))
  (xt/x:set-key controller "opened" false)
  (var handler (. controller ["lifecycle"] ["close"]))
  (var finish (fn [_]
                (xt/x:set-key controller "listeners" {})
                (return true)))
  (when (not (xt/x:is-function? handler))
    (return (promise/x:promise-then (promise/x:promise-run nil) finish)))
  (return
   (promise/x:promise-then
    (promise/x:promise-run
     (handler controller (. controller ["deps"])))
    finish)))
