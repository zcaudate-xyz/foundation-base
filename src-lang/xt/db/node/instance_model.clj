(ns xt.db.node.instance-model
  (:refer-clojure :exclude [remove sync])
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-query :as schema-query]
             [xt.db.node.schema-spec :as spec]
             [xt.db.node.schema-state :as schema-state]
             [xt.db.node.instance-query :as instance-query]
             [xt.db.node.instance-state :as instance-state]
             [xt.db.node.instance-sync :as instance-sync]
             [xt.db.node.instance-util :as util]
             [xt.event.node :as event-node]
             [xt.event.node-request :as node-request]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt install
  "installs xt.db.node handlers and triggers on a node"
  {:added "4.1"}
  [node opts]
  (util/set-node-opts node opts)
  (event-node/register-handler node spec/ACTION_QUERY -/handle-query nil)
  (event-node/register-handler node spec/ACTION_QUERY_REFRESH -/handle-query-refresh nil)
  (event-node/register-handler node spec/ACTION_SYNC -/handle-sync nil)
  (event-node/register-handler node spec/ACTION_REMOVE -/handle-remove nil)
  (event-node/register-handler node spec/ACTION_CLEAR -/handle-clear nil)
  (event-node/register-handler node spec/ACTION_SNAPSHOT -/handle-snapshot nil)
  (event-node/register-trigger node spec/SIGNAL_CACHE_CHANGED instance-sync/handle-cache-changed nil)
  (event-node/register-trigger node spec/SIGNAL_CACHE_INVALIDATED instance-sync/handle-cache-invalidated nil)
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
  (return (instance-state/ensure-state space node)))

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
  (return (instance-state/put-model state model-id model-spec)))

(defn.xt view-put
  "registers a single view on a node space"
  {:added "4.1"}
  [node space-id model-id view-id view-spec]
  (var state (-/ensure-space-state node space-id))
  (return (instance-state/put-view state model-id view-id view-spec)))

(defn.xt model-get
  "gets a model from a node space"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (return (schema-state/get-model state model-id)))

(defn.xt view-get
  "gets a view from a node space"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (return (schema-state/get-view state model-id view-id)))

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

(defn.xt normalize-remote
  "normalizes remote settings"
  {:added "4.1"}
  [state remote-spec view-context]
  (return
   (xt/x:obj-assign
    {}
    (or (xt/x:get-key state "remote") {})
    (or remote-spec {})
    (or (xt/x:get-key view-context "remote") {}))))

(defn.xt request-remote
  "issues a remote node request"
  {:added "4.1"}
  [node space-id remote action payload]
  (return (event-node/request
           node
           (or (xt/x:get-key remote "space")
               (xt/x:get-key remote "target")
               space-id)
           action
           [payload]
           (or (xt/x:get-key remote "meta") {}))))

(defn.xt run-remote-query
  "runs a query against a remote xt.db.node"
  {:added "4.1"}
  [node space-id state query-spec view-context remote-spec model-id view-id]
  (var [ok prepared] (schema-query/prepare-query state query-spec view-context))
  (when (not ok)
    (return (promise/x:promise (fn [_ reject]
                                 (reject prepared)))))
  (var remote (-/normalize-remote state remote-spec view-context))
  (return
   (promise/x:promise-then
    (-/request-remote node
                      space-id
                      remote
                      spec/ACTION_QUERY
                      {:query query-spec
                       :view view-context})
    (fn [response]
      (when (and (xt/x:is-object? response)
                 (or (xt/x:has-key? response "db/sync")
                     (xt/x:has-key? response "db/remove")))
        (instance-sync/apply-sync-request
         state
         {"db/sync" (xt/x:get-key response "db/sync")
          "db/remove" (xt/x:get-key response "db/remove")}))
      (return
       (instance-query/attach-query-entry
        state
        prepared
        (util/response-value response)
        (or (xt/x:get-key response "tables")
            (xt/x:get-key prepared "tables"))
        model-id
        view-id))))))

(defn.xt run-remote-sync
  "runs a sync request against a remote xt.db.node"
  {:added "4.1"}
  [node space-id state sync-spec view-context remote-spec]
  (var [ok request] (instance-sync/prepare-sync sync-spec view-context))
  (when (not ok)
    (return (promise/x:promise (fn [_ reject]
                                 (reject request)))))
  (var remote (-/normalize-remote state remote-spec view-context))
  (return
   (promise/x:promise-then
    (-/request-remote node
                      space-id
                      remote
                      spec/ACTION_SYNC
                      {:sync request
                       :view view-context})
    (fn [response]
      (var mirrored (or (xt/x:get-key response "result")
                        request))
      (instance-sync/apply-sync-request state mirrored)
      (return response)))))

(defn.xt view-refresh
  "refreshes a single registered view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (instance-state/ensure-db state)
  (var view (schema-state/ensure-view state model-id view-id))
  (var query-spec (xt/x:get-key view "query"))
  (when (xt/x:nil? query-spec)
    (xt/x:err (xt/x:cat "query not configured for view - "
                        (xt/x:json-encode [model-id view-id]))))
  (instance-state/set-view-pending state model-id view-id)
  (var view-context {:model-id model-id
                     :view-id view-id
                     :args (or (xt/x:get-key view "input") [])})
  (var remote-spec (xt/x:get-key view "remote"))
  (if (xt/x:not-nil? remote-spec)
    (return
     (promise/x:promise-catch
      (-/run-remote-query
       node
       space-id
       state
       query-spec
       view-context
       remote-spec
       model-id
       view-id)
      (fn [err]
        (instance-state/set-view-error state model-id view-id err)
        (xt/x:throw err))))
    (do
      (var [ok result] (instance-query/run-local-query
                        state
                        query-spec
                        view-context
                        model-id
                        view-id))
      (when (not ok)
        (instance-state/set-view-error state model-id view-id result)
        (xt/x:throw result))
      (return result))))

(defn.xt model-refresh
  "refreshes all views in a registered model"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (var model (schema-state/ensure-model state model-id))
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
  (instance-state/set-view-input state model-id view-id input)
  (return (-/view-refresh node space-id model-id view-id)))

(defn.xt handle-query
  "handles a local query request"
  {:added "4.1"}
  [current-space args request node]
  (var payload (util/request-payload args))
  (var state (instance-state/ensure-state current-space node))
  (instance-state/ensure-db state)
  (var query-spec (or (xt/x:get-key payload "query")
                      payload))
  (var view-context (or (xt/x:get-key payload "view")
                        {}))
  (var model-id (xt/x:get-key view-context "model-id"))
  (var view-id (xt/x:get-key view-context "view-id"))
  (var [ok result] (instance-query/run-local-query
                    state
                    query-spec
                    view-context
                    model-id
                    view-id))
  (when (not ok)
    (xt/x:throw result))
  (return result))

(defn.xt handle-query-refresh
  "handles a refresh request for a cached query"
  {:added "4.1"}
  [current-space args request node]
  (var payload (util/request-payload args))
  (var state (instance-state/ensure-state current-space node))
  (var query-key (xt/x:get-key payload "query_key"))
  (when (xt/x:not-nil? query-key)
    (return (instance-query/refresh-query-entry state query-key)))
  (return (-/handle-query current-space args request node)))

(defn.xt handle-sync
  "handles a local sync request"
  {:added "4.1"}
  [current-space args request node]
  (var payload (util/request-payload args))
  (var state (instance-state/ensure-state current-space node))
  (instance-state/ensure-db state)
  (var sync-spec (or (xt/x:get-key payload "sync")
                     payload))
  (var view-context (or (xt/x:get-key payload "view")
                        {}))
  (var [ok result] (instance-sync/run-sync-local state sync-spec view-context))
  (when (not ok)
    (xt/x:throw result))
  (var summary (instance-sync/process-cache-payload
                state
                (xt/x:get-key result "event")
                true))
  (return
   (promise/x:promise-then
    (event-node/publish node
                        (xt/x:get-key current-space "id")
                        spec/SIGNAL_CACHE_CHANGED
                        (xt/x:get-key result "event")
                        {:origin_node (xt/x:get-key node "id")
                         :queries (xt/x:get-key summary "queries")})
    (fn [_]
      (return (xt/x:obj-assign result
                               {:queries (xt/x:get-key summary "queries")}))))))

(defn.xt handle-remove
  "handles a local db/remove request"
  {:added "4.1"}
  [current-space args request node]
  (var payload (util/request-payload args))
  (return (-/handle-sync
           current-space
           [{"db/remove" (or (xt/x:get-key payload "db/remove")
                             (xt/x:get-key payload "remove")
                             payload)}]
           request
           node)))

(defn.xt handle-clear
  "handles a cache clear request"
  {:added "4.1"}
  [current-space args request node]
  (var state (instance-state/ensure-state current-space node))
  (instance-sync/clear-state-cache state)
  (return
   (promise/x:promise-then
    (event-node/publish node
                        (xt/x:get-key current-space "id")
                        spec/SIGNAL_CACHE_INVALIDATED
                        {"tables" {"*" true}}
                        {:origin_node (xt/x:get-key node "id")})
    (fn [_]
      (return true)))))

(defn.xt handle-snapshot
  "returns a snapshot of the current cache state"
  {:added "4.1"}
  [current-space args request node]
  (var state (instance-state/ensure-state current-space node))
  (var db (instance-state/ensure-db state))
  (return {:dbtype (xt/x:get-key db "::")
           :queries (xt/x:get-key state "queries")
           :models (xt/x:get-key state "models")
           :watch (xt/x:get-key state "watch")
           :rows (xt/x:get-key (xt/x:get-key db "instance") "rows")}))
