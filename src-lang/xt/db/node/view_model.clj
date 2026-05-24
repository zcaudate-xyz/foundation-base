(ns xt.db.node.view-model
  (:refer-clojure :exclude [remove sync])
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-spec :as spec]
             [xt.db.node.schema-query :as schema-query]
             [xt.db.node.view-state :as view-state]
             [xt.db.runtime :as db-runtime]
             [xt.db.node.view-util :as util]
             [xt.substrate :as event-node]
             [xt.substrate.base-space :as node-space]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt not-implemented
  "returns a standard not-implemented payload"
  {:added "4.1"}
  [tag payload]
  (return {:status "error"
           :tag tag
           :data payload}))

(defn.xt view-refresh-result
  "returns the public refresh result from the current view state"
  {:added "4.1"}
  [view]
  (return {:query_key (xt/x:get-key view "query_key")
           :source (xt/x:get-key view "source")
           :value (xt/x:get-key view "value")
           :status (xt/x:get-key view "status")}))

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

(defn.xt source-get
  "gets a model source binding"
  {:added "4.1"}
  [node space-id model-id source-id]
  (var state (-/ensure-space-state node space-id))
  (return (view-state/get-source state model-id source-id)))

(defn.xt source-put
  "stores source data on a model source binding"
  {:added "4.1"}
  [node space-id model-id source-id data]
  (var state (-/ensure-space-state node space-id))
  (return (view-state/set-source-data state model-id source-id data)))

(defn.xt source-refresh
  "refreshes a live source from its declared query"
  {:added "4.1"}
  [node space-id model-id source-id]
  (var state (-/ensure-space-state node space-id))
  (var source (view-state/ensure-source state model-id source-id))
  (var query (xt/x:get-key source "query"))
  (var db (xt/x:get-key source "db"))
  (when (or (not (== true (xt/x:get-key source "query_live")))
            (not (== true (xt/x:get-key source "live")))
            (xt/x:nil? db)
            (xt/x:nil? query))
    (return (promise/x:promise-run source)))
  (var [ok prepared]
       (schema-query/prepare-query
        state
        query
        {"model_id" model-id
         "source_id" source-id
         "args" (or (xt/x:get-key source "input") [])}))
  (when (not ok)
    (return (promise/x:promise-run prepared)))
  (xt/x:set-key source "query_key" (xt/x:get-key prepared "key"))
  (return
   (promise/x:promise-then
    (db-runtime/db-pull
     db
     (xt/x:get-key state "schema")
     (xt/x:get-key prepared "plan"))
    (fn [value]
      (view-state/set-source-data state model-id source-id value)
      (return (view-state/ensure-source state model-id source-id))))))

(defn.xt source-sync
  "synchronizes a model source from its configured upstream source"
  {:added "4.1"}
  [node space-id model-id source-id]
  (var state (-/ensure-space-state node space-id))
  (var source (view-state/ensure-source state model-id source-id))
  (var upstream-id (or (xt/x:get-key source "sync_from")
                       (xt/x:get-key source "sync-from")))
  (when (or (xt/x:nil? upstream-id)
            (== upstream-id source-id))
    (return (promise/x:promise-run source)))
  (var upstream (view-state/ensure-source state model-id upstream-id))
  (var upstream-refresh
       (:? (and (== true (xt/x:get-key upstream "query_live"))
                (== true (xt/x:get-key upstream "live"))
                (xt/x:not-nil? (xt/x:get-key upstream "db"))
                (xt/x:not-nil? (xt/x:get-key upstream "query")))
           (-/source-refresh node space-id model-id upstream-id)
           (promise/x:promise-run upstream)))
  (return
   (promise/x:promise-then
    upstream-refresh
    (fn [_]
      (view-state/sync-source state model-id source-id)
      (var synced (view-state/ensure-source state model-id source-id))
      (var db (xt/x:get-key synced "db"))
      (when (and (== true (xt/x:get-key synced "query_live"))
                 (== true (xt/x:get-key synced "live"))
                 (xt/x:not-nil? db))
        (db-runtime/db-clear db)
        (var data (or (xt/x:get-key synced "data") []))
        (var query (xt/x:get-key synced "query"))
        (var table (or (xt/x:get-key synced "table")
                       (:? (xt/x:not-nil? query)
                           (xt/x:get-key query "table")
                           nil)))
        (when (> (xt/x:len data) 0)
          (db-runtime/sync-event
           db
           ["add" (:? (and (xt/x:is-array? data)
                           (xt/x:not-nil? table))
                     {table data}
                     data)])))
      (return synced)))))

(defn.xt model-sync
  "synchronizes all structural sources for a model"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (var model (view-state/ensure-model state model-id))
  (var source-ids [])
  (var running [])
  (xt/for:object [[source-id source] (or (xt/x:get-key model "sources") {})]
    (when (xt/x:not-nil? (or (xt/x:get-key source "sync_from")
                             (xt/x:get-key source "sync-from")))
      (xt/x:arr-push source-ids source-id)
      (xt/x:arr-push running (-/source-sync node space-id model-id source-id))))
  (if (> (xt/x:len running) 0)
    (return
     (promise/x:promise-then
      (promise/x:promise-all running)
      (fn [results]
        (var out {})
        (xt/for:index [idx [0 (xt/x:len source-ids)]]
          (var source-id (xt/x:get-idx source-ids idx))
          (xt/x:set-key out source-id (xt/x:get-idx results idx)))
        (return out))))
    (return (promise/x:promise-run {}))))

(defn.xt view-refresh
  "refreshes a view from its declared structural source role"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (var view (view-state/ensure-view state model-id view-id))
  (var source-id (or (xt/x:get-key view "source")
                     "caching"))
  (var source (view-state/ensure-source state model-id source-id))
  (var query-live? (xt/x:get-key source "query_live"))
  (var live? (xt/x:get-key source "live"))
  (var db (xt/x:get-key source "db"))
  (cond (and (== true query-live?)
             (xt/x:is-boolean? live?)
             live?)
        (do (var [ok prepared]
                 (schema-query/prepare-query
                  state
                  (xt/x:get-key view "query")
                  {"model_id" model-id
                   "view_id" view-id
                   "args" (or (xt/x:get-key view "input") [])}))
            (when (not ok)
              (view-state/set-view-error state model-id view-id prepared)
              (return (promise/x:promise-run prepared)))
            (xt/x:set-key view "query_key" (xt/x:get-key prepared "key"))
            (return
             (promise/x:promise-then
              (db-runtime/db-pull
               db
               (xt/x:get-key state "schema")
               (xt/x:get-key prepared "plan"))
              (fn [value]
                (view-state/set-source-data state model-id source-id value)
                (view-state/set-view-value state model-id view-id source-id value)
                (return (-/view-refresh-result view))))))

        :else
        (do (var value (xtd/clone-nested (xt/x:get-key source "data")))
            (view-state/set-view-value state model-id view-id source-id value)
            (return (promise/x:promise-run (-/view-refresh-result view))))))

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
  (return (-/not-implemented
           "xt.db.node.view/query-not-implemented"
           payload)))

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
  (return (-/not-implemented
           "xt.db.node.view/sync-not-implemented"
           payload)))

(defn.xt handle-remove
  "handles a remove request for the clean view-based implementation"
  {:added "4.1"}
  [current-space args request node]
  (var payload (util/request-payload args))
  (return (-/not-implemented
           "xt.db.node.view/remove-not-implemented"
           {"db/remove" payload})))

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
