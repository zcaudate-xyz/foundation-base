(ns xt.db.node.view-model
  (:refer-clojure :exclude [remove sync])
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-spec :as spec]
             [xt.db.node.view-query :as view-query]
             [xt.db.node.view-state :as view-state]
             [xt.db.node.view-sync :as view-sync]
             [xt.db.node.view-util :as util]
             [xt.substrate :as event-node]
             [xt.substrate.base-space :as node-space]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]]})

(defn.xt ensure-space-state
  "ensures the target node space has xt.db view state"
  {:added "4.1"}
  [node space-id]
  (var space (node-space/ensure-space node space-id nil))
  (var state (xt/x:get-key space "state"))
  (when (not (util/state? state))
    (:= state (view-state/base-state (util/node-opts node)))
    (xt/x:set-key space "state" state))
  (return state))

(defn.xt query
  "issues a node query request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              spec/ACTION_QUERY
                              [payload]
                              nil)))

(defn.xt query-refresh
  "issues a cached query refresh request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              spec/ACTION_QUERY_REFRESH
                              [payload]
                              nil)))

(defn.xt sync
  "issues a node sync request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              spec/ACTION_SYNC
                              [payload]
                              nil)))

(defn.xt remove
  "issues a node remove request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              spec/ACTION_REMOVE
                              [payload]
                              nil)))

(defn.xt clear
  "issues a node clear request"
  {:added "4.1"}
  [node space-id]
  (return (event-node/request node
                              space-id
                              spec/ACTION_CLEAR
                              [{}]
                              nil)))

(defn.xt snapshot
  "requests a node state snapshot"
  {:added "4.1"}
  [node space-id]
  (return (event-node/request node
                              space-id
                              spec/ACTION_SNAPSHOT
                              [{}]
                              nil)))

(defn.xt model-put
  "registers a model and its views on a node space"
  {:added "4.1"}
  [node space-id model-id model-spec]
  (var state (-/ensure-space-state node space-id))
  (return (view-state/put-model state model-id model-spec)))

(defn.xt view-put
  "registers a single view on a node space"
  {:added "4.1"}
  [node space-id model-id view-id view-spec]
  (var state (-/ensure-space-state node space-id))
  (return (view-state/put-view state model-id view-id view-spec)))

(defn.xt model-get
  "gets a model from a node space"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (return (view-state/get-model state model-id)))

(defn.xt view-get
  "gets a view from a node space"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (return (view-state/get-view state model-id view-id)))

(defn.xt view-val
  "gets the current value for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (xt/x:get-key view "value")
              nil)))

(defn.xt view-input
  "gets the current input for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (or (xt/x:get-key view "input") [])
              [])))

(defn.xt view-pending
  "gets the pending flag for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (or (xt/x:get-key view "pending") false)
              false)))

(defn.xt view-error
  "gets the current error for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (xt/x:get-key view "error")
              nil)))

(defn.xt view-dependents
  "gets dependent views for the given source view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (return (view-state/get-view-dependents state model-id view-id)))

(defn.xt model-dependents
  "gets dependent models for the given source model"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (return (view-state/get-model-dependents state model-id)))

(defn.xt view-refresh
  "marks a single registered view as ready"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (var view (view-state/set-view-ready state model-id view-id))
  (return (promise/x:promise-run (view-query/refresh-result view))))

(defn.xt model-refresh
  "marks all views in a registered model as ready"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (var model (view-state/ensure-model state model-id))
  (var running [])
  (xt/for:object [[view-id _] (or (xt/x:get-key model "views") {})]
    (xt/x:arr-push running (-/view-refresh node space-id model-id view-id)))
  (return (promise/x:promise-all running)))

(defn.xt view-set-input
  "sets view input and marks the view ready"
  {:added "4.1"}
  [node space-id model-id view-id input]
  (var state (-/ensure-space-state node space-id))
  (view-state/set-view-input state model-id view-id input)
  (return (-/view-refresh node space-id model-id view-id)))

(defn.xt handle-query
  "handles a query request for the clean view-based implementation"
  {:added "4.1"}
  [current-space args request node]
  (var payload (util/request-payload args))
  (return (view-query/not-implemented payload)))

(defn.xt handle-query-refresh
  "handles a cached query refresh request"
  {:added "4.1"}
  [current-space args request node]
  (return (-/handle-query current-space args request node)))

(defn.xt handle-sync
  "handles a sync request for the clean view-based implementation"
  {:added "4.1"}
  [current-space args request node]
  (var payload (util/request-payload args))
  (return (view-sync/not-implemented payload)))

(defn.xt handle-remove
  "handles a remove request for the clean view-based implementation"
  {:added "4.1"}
  [current-space args request node]
  (var payload (util/request-payload args))
  (return (view-sync/not-implemented {"db/remove" payload})))

(defn.xt handle-clear
  "clears the local view state caches"
  {:added "4.1"}
  [current-space args request node]
  (var state (-/ensure-space-state node (xt/x:get-key current-space "id")))
  (view-state/clear-state state)
  (return true))

(defn.xt handle-snapshot
  "returns a snapshot of the current view state"
  {:added "4.1"}
  [current-space args request node]
  (var state (-/ensure-space-state node (xt/x:get-key current-space "id")))
  (return (view-state/snapshot-state state)))

(defn.xt install
  "installs xt.db.node view handlers on a node"
  {:added "4.1"}
  [node opts]
  (util/set-node-opts node opts)
  (event-node/register-handler node spec/ACTION_QUERY -/handle-query nil)
  (event-node/register-handler node spec/ACTION_QUERY_REFRESH -/handle-query-refresh nil)
  (event-node/register-handler node spec/ACTION_SYNC -/handle-sync nil)
  (event-node/register-handler node spec/ACTION_REMOVE -/handle-remove nil)
  (event-node/register-handler node spec/ACTION_CLEAR -/handle-clear nil)
  (event-node/register-handler node spec/ACTION_SNAPSHOT -/handle-snapshot nil)
  (return node))

(defn.xt uninstall
  "removes xt.db.node view handlers from a node"
  {:added "4.1"}
  [node]
  (event-node/unregister-handler node spec/ACTION_QUERY)
  (event-node/unregister-handler node spec/ACTION_QUERY_REFRESH)
  (event-node/unregister-handler node spec/ACTION_SYNC)
  (event-node/unregister-handler node spec/ACTION_REMOVE)
  (event-node/unregister-handler node spec/ACTION_CLEAR)
  (event-node/unregister-handler node spec/ACTION_SNAPSHOT)
  (return node))
