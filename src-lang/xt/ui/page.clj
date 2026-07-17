(ns xt.ui.page
  "Lifecycle and subscription primitive for UI-independent page controllers."
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defspec.xt PageController :xt/any)
(defspec.xt PageState :xt/any)
(defspec.xt PageActions :xt/any)

(defn.xt controller-create
  "creates a controller whose state is plain data and whose handlers own effects"
  [initial-state handlers lifecycle deps]
  (return {"state" (or initial-state {})
           "revision" 0
           "handlers" (or handlers {})
           "lifecycle" (or lifecycle {})
           "deps" (or deps {})
           "listeners" {}
           "opened" false}))

(defn.xt snapshot
  [controller]
  (return (xt/x:get-key controller "state")))

(defn.xt revision
  [controller]
  (return (xt/x:get-key controller "revision")))

(defn.xt notify!
  [controller]
  (xt/for:object [[_ listener] (xt/x:get-key controller "listeners")]
    (listener (-/snapshot controller) (-/revision controller)))
  (return controller))

(defn.xt set-state!
  [controller state]
  (xt/x:set-key controller "state" (or state {}))
  (xt/x:set-key controller "revision" (+ 1 (-/revision controller)))
  (-/notify! controller)
  (return state))

(defn.xt update-state!
  [controller update-fn]
  (return (-/set-state! controller (update-fn (-/snapshot controller)))))

(defn.xt subscribe!
  [controller listener-id listener]
  (xt/x:set-key (xt/x:get-key controller "listeners") listener-id listener)
  (return listener-id))

(defn.xt unsubscribe!
  [controller listener-id]
  (xt/x:del-key (xt/x:get-key controller "listeners") listener-id)
  (return true))

(defn.xt dispatch!
  "dispatches a page intent; handlers receive controller, payload and dependencies"
  [controller action-id payload]
  (var handler (xt/x:get-key (xt/x:get-key controller "handlers") action-id))
  (when (not (xt/x:is-function? handler))
    (return (promise/x:promise-run
             {"status" "unavailable" "action" action-id})))
  (return
   (promise/x:promise-run
    (handler controller payload (xt/x:get-key controller "deps")))))

(defn.xt actions-create
  "creates the callback facade supplied to a pure page view"
  [controller action-ids]
  (var actions {})
  (xt/for:array [action-id (or action-ids [])]
    (xt/x:set-key actions action-id
                  (fn [payload]
                    (return (-/dispatch! controller action-id payload)))))
  (return actions))

(defn.xt open!
  [controller]
  (when (== true (xt/x:get-key controller "opened"))
    (return (promise/x:promise-run controller)))
  (xt/x:set-key controller "opened" true)
  (var handler (xt/x:get-key (xt/x:get-key controller "lifecycle") "open"))
  (when (not (xt/x:is-function? handler))
    (return (promise/x:promise-run controller)))
  (return
   (promise/x:promise-then
    (promise/x:promise-run
     (handler controller (xt/x:get-key controller "deps")))
    (fn [_] (return controller)))))

(defn.xt close!
  [controller]
  (when (not= true (xt/x:get-key controller "opened"))
    (return (promise/x:promise-run true)))
  (xt/x:set-key controller "opened" false)
  (var handler (xt/x:get-key (xt/x:get-key controller "lifecycle") "close"))
  (var finish (fn [_]
                (xt/x:set-key controller "listeners" {})
                (return true)))
  (when (not (xt/x:is-function? handler))
    (return (promise/x:promise-then (promise/x:promise-run nil) finish)))
  (return
   (promise/x:promise-then
    (promise/x:promise-run
     (handler controller (xt/x:get-key controller "deps")))
    finish)))
