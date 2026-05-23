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
             [xt.event.base-view :as event-view]
             [xt.event.util-throttle :as throttle]
             [xt.substrate :as event-node]
             [xt.substrate.base-space :as node-space]
             [xt.substrate.base-request :as node-request]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
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
  (var space (node-space/ensure-space node space-id nil))
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
              (event-view/get-current view nil)
              nil)))

(defn.xt view-input
  "gets the current input for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (when (xt/x:nil? view)
    (return nil))
  (var current (xt/x:get-key (event-view/get-input view) "current"))
  (cond (xt/x:is-object? current)
        (return (or (xt/x:get-key current "data")
                    []))

        (xt/x:is-array? current)
        (return current)

        :else
        (return [])))

(defn.xt view-pending
  "gets the pending flag for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (event-view/is-pending view nil)
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
  (return (instance-state/get-view-dependents state model-id view-id)))

(defn.xt model-dependents
  "gets dependent models for the given source model"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (return (instance-state/get-model-dependents state model-id)))

(defn.xt view-remote-spec
  "gets the configured remote spec for a view, if present"
  {:added "4.1"}
  [view]
  (var remote (xt/x:get-key view "remote"))
  (if (and (xt/x:is-object? remote)
           (or (xt/x:has-key? remote "space")
               (xt/x:has-key? remote "target")
               (xt/x:has-key? remote "transport")
               (xt/x:has-key? remote "meta")))
    (return remote)
    (return nil)))

(defn.xt refresh-seen?
  "checks whether a view has already been visited in a refresh chain"
  {:added "4.1"}
  [visited model-id view-id]
  (return (== true (xtd/get-in visited [model-id view-id]))))

(defn.xt mark-refresh-seen
  "marks a view as visited in a refresh chain"
  {:added "4.1"}
  [visited model-id view-id]
  (xtd/set-in visited [model-id view-id] true)
  (return visited))

(defn.xt ensure-model-throttle
  "ensures a per-model dependent refresh throttle exists"
  {:added "4.1"}
  [node space-id state model-id]
  (var model (schema-state/ensure-model state model-id))
  (var model-throttle (xt/x:get-key model "throttle"))
  (when (xt/x:nil? model-throttle)
    (:= model-throttle
        (throttle/throttle-create
         (fn [view-id opts]
           (return (-/view-refresh-impl node space-id model-id view-id opts)))
         xt/x:now-ms))
    (xt/x:set-key model "throttle" model-throttle))
  (return model-throttle))

(defn.xt normalize-remote
  "normalizes remote settings"
  {:added "4.1"}
  [state remote-spec view-context]
  (return
   (xt/x:obj-assign
    (xt/x:obj-assign
     (xt/x:obj-assign
      {}
      (or (xt/x:get-key state "remote") {}))
     (or remote-spec {}))
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
    (return (promise/x:promise (fn []
                                 (xt/x:throw prepared)))))
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
    (return (promise/x:promise (fn []
                                 (xt/x:throw request)))))
  (var remote (-/normalize-remote state remote-spec view-context))
  (return
   (promise/x:promise-then
    (-/request-remote node
                      space-id
                      remote
                      spec/ACTION_SYNC
                      (xt/x:obj-assign request
                                       {:view view-context}))
     (fn [response]
       (var mirrored (or (xt/x:get-key response "result")
                         request))
       (instance-sync/apply-sync-request state mirrored)
       (return response)))))

(defn.xt view-refresh-result
  "returns the public refresh result from the updated view state"
  {:added "4.1"}
  [view]
  (return {:query_key (xt/x:get-key view "query_key")
           :value (event-view/get-current view nil)
           :tables (or (xt/x:get-key view "tables")
                       {})}))

(defn.xt run-view-main
  "runs the local query stage for a db view"
  {:added "4.1"}
  [context]
  (var #{state model-id view-id args view} context)
  (var query-spec (xt/x:get-key view "query"))
  (var view-context {:model-id model-id
                     :view-id view-id
                     :args (or args [])})
  (var [ok result] (instance-query/run-local-query
                    state
                    query-spec
                    view-context
                    model-id
                    view-id))
  (when (not ok)
    (xt/x:throw result))
  (return (xt/x:obj-assign {"key" nil}
                           result)))

(defn.xt run-view-remote
  "runs the remote query stage for a db view"
  {:added "4.1"}
  [context]
  (var #{node space-id state model-id view-id args view} context)
  (var query-spec (xt/x:get-key view "query"))
  (var remote-spec (-/view-remote-spec view))
  (var view-context {:model-id model-id
                     :view-id view-id
                     :args (or args [])})
  (return
   (-/run-remote-query
    node
    space-id
    state
    query-spec
    view-context
    remote-spec
    model-id
    view-id)))

(defn.xt configure-view-pipeline
  "installs the db-node pipeline handlers onto a base view"
  {:added "4.1"}
  [view]
  (xtd/set-in view ["pipeline" "main" "handler"] -/run-view-main)
  (xtd/set-in view ["pipeline" "remote" "handler"] -/run-view-remote)
  (return view))

(defn.xt pipeline-run-async
  "adapts xt.db.node handlers to the base-view async callback contract"
  {:added "4.1"}
  [handler-fn context callbacks]
  (return
   (promise/x:promise-catch
    (promise/x:promise-then
     (node-request/ensure-promise (handler-fn context))
     (fn [result]
       (return (xt/x:apply (xt/x:get-key callbacks "success")
                           [result]))))
    (fn [err]
      (return (xt/x:apply (xt/x:get-key callbacks "error")
                          [err]))))))

(defn.xt refresh-view-dependents
  "refreshes dependent views for a source view"
  {:added "4.1"}
  [node space-id state model-id view-id opts]
  (when (xt/x:nil? opts)
    (:= opts {}))
  (var visited (or (xt/x:get-key opts "visited") {}))
  (var dependents (instance-state/get-view-dependents state model-id view-id))
  (var running [])
  (xt/for:object [[dmodel-id dview-ids] dependents]
    (xt/for:array [dview-id dview-ids]
      (when (not (-/refresh-seen? visited dmodel-id dview-id))
        (var entry (throttle/throttle-run
                    (-/ensure-model-throttle node space-id state dmodel-id)
                    dview-id
                    [{"visited" (xtd/clone-nested visited)
                      "refresh_deps" true}]))
        (xt/x:arr-push running
                       (node-request/ensure-promise
                        (xt/x:get-key entry "promise")))))) 
  (return (promise/x:promise-all running)))

(defn.xt view-refresh-impl
  "refreshes a single registered view with internal refresh-chain options"
  {:added "4.1"}
  [node space-id model-id view-id opts]
  (when (xt/x:nil? opts)
    (:= opts {}))
  (var state (-/ensure-space-state node space-id))
  (instance-state/ensure-db state)
  (var view (schema-state/ensure-view state model-id view-id))
  (var visited (or (xt/x:get-key opts "visited") {}))
  (when (-/refresh-seen? visited model-id view-id)
    (return (promise/x:promise-run (-/view-refresh-result view))))
  (-/mark-refresh-seen visited model-id view-id)
  (var refresh-deps (:? (and (xt/x:not-nil? opts)
                             (xt/x:has-key? opts "refresh_deps"))
                         (xt/x:get-key opts "refresh_deps")
                         true))
  (var query-spec (xt/x:get-key view "query"))
  (when (xt/x:nil? query-spec)
    (xt/x:err (xt/x:cat "query not configured for view - "
                        (xt/x:json-encode [model-id view-id]))))
  (-/configure-view-pipeline view)
  (var [context disabled] (event-view/pipeline-prep
                           view
                           {:node node
                            :state state
                            :space-id space-id
                            :model-id model-id
                            :view-id view-id}))
  (if disabled
    (do
      (return (promise/x:promise-run (-/view-refresh-result view))))
    (do
      (var remote-spec (-/view-remote-spec view))
      (var refresh-p (:? (xt/x:not-nil? remote-spec)
                         (event-view/pipeline-run-remote
                          context
                          false
                          -/pipeline-run-async
                          nil
                          (fn [_]
                            (return (-/view-refresh-result view))))
                         (event-view/pipeline-run
                          context
                          false
                          -/pipeline-run-async
                          nil
                          (fn [_]
                            (return (-/view-refresh-result view)))
                          nil)))
      (return
       (promise/x:promise-catch
        (promise/x:promise-then
         refresh-p
         (fn [_]
            (var result (-/view-refresh-result view))
            (if refresh-deps
             (return
              (promise/x:promise-then
               (-/refresh-view-dependents
                node
                space-id
                state
                model-id
                view-id
                {"visited" visited})
               (fn [_]
                 (return result))))
             (return result))))
        (fn [err]
          (instance-state/set-view-error state model-id view-id err)
          (xt/x:throw err)))))))

(defn.xt view-refresh
  "refreshes a single registered view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (return (-/view-refresh-impl node space-id model-id view-id nil)))

(defn.xt model-refresh
  "refreshes all views in a registered model"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (var model (schema-state/ensure-model state model-id))
  (var running [])
  (xt/for:array [view-id (xt/x:obj-keys (or (xt/x:get-key model "views") {}))]
    (xt/x:arr-push running
                   (node-request/ensure-promise
                    (-/view-refresh-impl
                     node
                     space-id
                     model-id
                     view-id
                     {"visited" {}
                      "refresh_deps" false}))))
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
  (var model-id (xt/x:get-key view-context "model_id"))
  (var view-id (xt/x:get-key view-context "view_id"))
  (return
  (promise/x:promise-then
   (instance-query/run-local-query-async
    state
    query-spec
    view-context
    model-id
    view-id)
   (fn [out]
     (var [ok result] out)
     (when (not ok)
       (xt/x:throw result))
     (return result)))))

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
  (var sync-spec payload)
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
