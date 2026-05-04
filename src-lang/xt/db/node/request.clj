(ns xt.db.node.request
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.query :as query]
             [xt.db.node.spec :as spec]
             [xt.db.node.state :as state]
             [xt.db.node.sync :as sync]
             [xt.db.node.trigger :as trigger]
             [xt.event.node :as event-node]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]]})

(defn.xt request-payload
  "gets the first request payload"
  {:added "4.1"}
  [args]
  (return (or (xt/x:get-idx args 0)
              {})))

(defn.xt handle-query
  "handles a local query request"
  {:added "4.1"}
  [current-space args request node]
  (var payload (-/request-payload args))
  (var state (state/ensure-state current-space node))
  (state/ensure-db state)
  (var query-spec (or (xt/x:get-key payload "query")
                      payload))
  (var view-context (or (xt/x:get-key payload "view")
                        {}))
  (var model-id (xt/x:get-key view-context "model-id"))
  (var view-id (xt/x:get-key view-context "view-id"))
  (var [ok result] (query/run-local-query
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
  (var payload (-/request-payload args))
  (var state (state/ensure-state current-space node))
  (var query-key (xt/x:get-key payload "query_key"))
  (when (xt/x:not-nil? query-key)
    (return (query/refresh-query-entry state query-key)))
  (return (-/handle-query current-space args request node)))

(defn.xt handle-sync
  "handles a local sync request"
  {:added "4.1"}
  [current-space args request node]
  (var payload (-/request-payload args))
  (var state (state/ensure-state current-space node))
  (state/ensure-db state)
  (var sync-spec (or (xt/x:get-key payload "sync")
                     payload))
  (var view-context (or (xt/x:get-key payload "view")
                        {}))
  (var [ok result] (sync/run-sync-local state sync-spec view-context))
  (when (not ok)
    (xt/x:throw result))
  (var summary (trigger/process-cache-payload
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
  (var payload (-/request-payload args))
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
  (var state (state/ensure-state current-space node))
  (sync/clear-state-cache state)
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
  (var state (state/ensure-state current-space node))
  (var db (state/ensure-db state))
  (return {:dbtype (xt/x:get-key db "::")
           :queries (xt/x:get-key state "queries")
           :models (xt/x:get-key state "models")
           :watch (xt/x:get-key state "watch")
           :rows (xtd/get-in db ["instance" "rows"])}))
