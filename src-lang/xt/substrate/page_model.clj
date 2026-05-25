(ns xt.substrate.page-model
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.substrate :as event-node]
             [xt.substrate.base-space :as node-space]
             [xt.substrate.page-spec :as page-spec]
             [xt.substrate.page-state :as page-state]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]]})

(defn.xt ensure-space-state
  "ensures the target node space has page model state"
  {:added "4.1"}
  [node space-id]
  (var space (node-space/ensure-space node space-id nil))
  (var state (xt/x:get-key space "state"))
  (when (not (and (xt/x:not-nil? state)
                  (== (xt/x:get-key state "::") page-spec/STATE_TAG)))
    (:= state (page-state/base-state nil))
    (xt/x:set-key space "state" state))
  (return state))

(defn.xt model-put
  "registers a model on a node space"
  {:added "4.1"}
  [node space-id model-id model-spec]
  (var state (-/ensure-space-state node space-id))
  (return (page-state/put-model state model-id model-spec)))

(defn.xt view-put
  "registers a single view on a node space"
  {:added "4.1"}
  [node space-id model-id view-id view-spec]
  (var state (-/ensure-space-state node space-id))
  (return (page-state/put-view state model-id view-id view-spec)))

(defn.xt model-get
  "gets a model from a node space"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (return (page-state/get-model state model-id)))

(defn.xt view-get
  "gets a view from a node space"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (return (page-state/get-view state model-id view-id)))

(defn.xt model-set-state
  "sets model local state"
  {:added "4.1"}
  [node space-id model-id path value]
  (var state (-/ensure-space-state node space-id))
  (return (page-state/set-model-state state model-id path value)))

(defn.xt view-set-input
  "sets view input"
  {:added "4.1"}
  [node space-id model-id view-id input]
  (var state (-/ensure-space-state node space-id))
  (return (page-state/set-view-input state model-id view-id input)))

(defn.xt view-refresh-result
  "returns the public refresh result from the current view state"
  {:added "4.1"}
  [view]
  (return {"value" (xt/x:get-key view "value")
           "status" (xt/x:get-key view "status")
           "error" (xt/x:get-key view "error")}))

(defn.xt resolver-pre-trigger
  "gets the pre trigger for a resolver"
  {:added "4.1"}
  [resolver]
  (return (page-spec/resolver-trigger-pre resolver)))

(defn.xt resolver-post-trigger
  "gets the post trigger for a resolver"
  {:added "4.1"}
  [resolver]
  (return (page-spec/resolver-trigger-post resolver)))

(defn.xt resolver-fn
  "gets the executable function for a resolver"
  {:added "4.1"}
  [resolver]
  (return (page-spec/resolver-fn resolver)))

(defn.xt apply-pre-trigger
  "applies a resolver pre trigger to a context"
  {:added "4.1"}
  [resolver ctx]
  (var trigger (-/resolver-pre-trigger resolver))
  (if (xt/x:is-function? trigger)
    (do (var out (trigger ctx))
        (return (:? (xt/x:is-object? out)
                    out
                    ctx)))
    (return ctx)))

(defn.xt apply-post-trigger
  "applies a resolver post trigger to a result"
  {:added "4.1"}
  [resolver ctx result]
  (var trigger (-/resolver-post-trigger resolver))
  (if (xt/x:is-function? trigger)
    (do (var out (trigger ctx result))
        (return (:? (xt/x:not-nil? out)
                    out
                    result)))
    (return result)))

(defn.xt finalize-result
  "applies post processing and updates the view state"
  {:added "4.1"}
  [state model-id view-id resolver ctx result]
  (:= result (-/apply-post-trigger resolver ctx result))
  (if (and (xt/x:is-object? result)
           (== (xt/x:get-key result "status") "error"))
    (do (page-state/set-view-error state model-id view-id result)
        (return (page-state/get-view state model-id view-id)))
    (do (page-state/set-view-value state model-id view-id "resolver" result)
        (return (page-state/get-view state model-id view-id)))))

(defn.xt run-api-resolver
  "runs an api resolver through substrate request"
  {:added "4.1"}
  [resolver ctx]
  (var action (page-spec/resolver-action resolver))
  (when (xt/x:nil? action)
    (return {"status" "error"
             "tag" "substrate.page/resolver-action-not-found"
             "data" {"type" "fn/api"}}))
  (return
   (event-node/request
    (xt/x:get-key ctx "node")
    (page-spec/resolver-target resolver)
    action
    (page-spec/resolver-args resolver ctx)
    (page-spec/resolver-meta resolver))))

(defn.xt resolve-view
  "executes a generic page resolver"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (var model (page-state/ensure-model state model-id))
  (var view (page-state/ensure-view state model-id view-id))
  (var resolver (page-spec/view-resolver view))
  (var type (page-spec/resolver-type resolver))
  (var ctx {"node" node
            "space_id" space-id
            "state" state
            "model_id" model-id
            "view_id" view-id
            "model" model
            "view" view
            "resolver" resolver
            "input" (or (xt/x:get-key view "input") [])})
  (:= ctx (-/apply-pre-trigger resolver ctx))
  (var exec (-/resolver-fn resolver))
  (var result nil)
  (cond (or (== type page-spec/RESOLVER_TYPE_LOCAL)
            (== type page-spec/RESOLVER_TYPE_CUSTOM)
            false)
        (if (xt/x:is-function? exec)
          (:= result (exec ctx))
          (:= result {"status" "error"
                      "tag" "substrate.page/resolver-fn-not-found"
                      "data" {"type" type}}))

        (== type page-spec/RESOLVER_TYPE_API)
        (:= result (-/run-api-resolver resolver ctx))

        :else
        (if (xt/x:is-function? exec)
          (:= result (exec ctx))
          (:= result {"status" "error"
                      "tag" "substrate.page/resolver-type-not-supported"
                      "data" {"type" type}})))
  (if (== type page-spec/RESOLVER_TYPE_API)
    (return
     (promise/x:promise-then
      (promise/x:promise-run result)
      (fn [output]
        (return (-/finalize-result state model-id view-id resolver ctx output)))))
    (if (promise/x:promise-native? result)
    (return
     (promise/x:promise-then
      result
      (fn [output]
        (return (-/finalize-result state model-id view-id resolver ctx output)))))
      (return (-/finalize-result state model-id view-id resolver ctx result)))))

(defn.xt view-refresh
  "refreshes a page view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var result (-/resolve-view node space-id model-id view-id))
  (if (promise/x:promise-native? result)
    (return
     (promise/x:promise-then
      result
      (fn [view]
        (return (-/view-refresh-result view)))))
    (return (-/view-refresh-result result))))
