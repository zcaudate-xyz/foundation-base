(ns xt.db.node.main
  (:refer-clojure :exclude [remove sync])
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.query :as query]
             [xt.db.node.remote :as remote]
             [xt.db.node.request :as request]
             [xt.db.node.spec :as spec]
             [xt.db.node.state :as state]
             [xt.db.node.trigger :as trigger]
             [xt.event.node :as event-node]
             [xt.event.node-request :as node-request]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt install
  "installs xt.db.node handlers and triggers on a node"
  {:added "4.1"}
  [node opts]
  (state/set-node-opts node opts)
  (event-node/register-handler node spec/ACTION_QUERY request/handle-query nil)
  (event-node/register-handler node spec/ACTION_QUERY_REFRESH request/handle-query-refresh nil)
  (event-node/register-handler node spec/ACTION_SYNC request/handle-sync nil)
  (event-node/register-handler node spec/ACTION_REMOVE request/handle-remove nil)
  (event-node/register-handler node spec/ACTION_CLEAR request/handle-clear nil)
  (event-node/register-handler node spec/ACTION_SNAPSHOT request/handle-snapshot nil)
  (event-node/register-trigger node spec/SIGNAL_CACHE_CHANGED trigger/handle-cache-changed nil)
  (event-node/register-trigger node spec/SIGNAL_CACHE_INVALIDATED trigger/handle-cache-invalidated nil)
  (return node))

(defn.xt uninstall
  "removes xt.db.node handlers and triggers from a node"
  {:added "4.1"}
  [node]
  (event-node/unregister-handler node spec/ACTION_QUERY)
  (event-node/unregister-handler node spec/ACTION_QUERY_REFRESH)
  (event-node/unregister-handler node spec/ACTION_SYNC)
  (event-node/unregister-handler node spec/ACTION_REMOVE)
  (event-node/unregister-handler node spec/ACTION_CLEAR)
  (event-node/unregister-handler node spec/ACTION_SNAPSHOT)
  (event-node/unregister-trigger node spec/SIGNAL_CACHE_CHANGED)
  (event-node/unregister-trigger node spec/SIGNAL_CACHE_INVALIDATED)
  (return node))

(defn.xt ensure-space-state
  "ensures the target node space has db.node state"
  {:added "4.1"}
  [node space-id]
  (var space (event-node/ensure-space node space-id nil))
  (return (state/ensure-state space node)))

(defn.xt query
  "issues a local node query request"
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
  "issues a local node sync request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              spec/ACTION_SYNC
                              [payload]
                              nil)))

(defn.xt remove
  "issues a local node remove request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              spec/ACTION_REMOVE
                              [payload]
                              nil)))

(defn.xt clear
  "issues a local node clear request"
  {:added "4.1"}
  [node space-id]
  (return (event-node/request node
                              space-id
                              spec/ACTION_CLEAR
                              [{}]
                              nil)))

(defn.xt snapshot
  "requests a node cache snapshot"
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
  (return (state/put-model state model-id model-spec)))

(defn.xt view-put
  "registers a single view on a node space"
  {:added "4.1"}
  [node space-id model-id view-id view-spec]
  (var state (-/ensure-space-state node space-id))
  (return (state/put-view state model-id view-id view-spec)))

(defn.xt model-get
  "gets a model from a node space"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (return (state/get-model state model-id)))

(defn.xt view-get
  "gets a view from a node space"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (return (state/get-view state model-id view-id)))

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
              (xt/x:get-key view "input")
              nil)))

(defn.xt view-pending
  "gets the pending flag for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (xt/x:get-key view "pending")
              false)))

(defn.xt view-error
  "gets the current error for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (xt/x:get-key view "error")
              nil)))

(defn.xt view-refresh
  "refreshes a single registered view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (state/ensure-db state)
  (var view (state/ensure-view state model-id view-id))
  (var query-spec (xt/x:get-key view "query"))
  (when (xt/x:nil? query-spec)
    (xt/x:err (xt/x:cat "query not configured for view - "
                        (xt/x:json-encode [model-id view-id]))))
  (state/set-view-pending state model-id view-id)
  (var view-context {:model-id model-id
                     :view-id view-id
                     :args (or (xt/x:get-key view "input") [])})
  (var remote-spec (xt/x:get-key view "remote"))
  (if (xt/x:not-nil? remote-spec)
    (return
     (promise/x:promise-catch
      (remote/run-remote-query
       node
       space-id
       state
       query-spec
       view-context
       remote-spec
       model-id
       view-id)
      (fn [err]
        (state/set-view-error state model-id view-id err)
        (xt/x:throw err))))
    (do
      (var [ok result] (query/run-local-query
                        state
                        query-spec
                        view-context
                        model-id
                        view-id))
      (when (not ok)
        (state/set-view-error state model-id view-id result)
        (xt/x:throw result))
      (return result))))

(defn.xt model-refresh
  "refreshes all views in a registered model"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (var model (state/ensure-model state model-id))
  (var running [])
  (xt/for:object [[view-id _] (or (xt/x:get-key model "views") {})]
    (xt/x:arr-push running
                   (node-request/ensure-promise
                    (-/view-refresh node space-id model-id view-id))))
  (return (promise/x:promise-all running)))

(defn.xt view-set-input
  "sets view input and refreshes the view"
  {:added "4.1"}
  [node space-id model-id view-id input]
  (var state (-/ensure-space-state node space-id))
  (state/set-view-input state model-id view-id input)
  (return (-/view-refresh node space-id model-id view-id)))
