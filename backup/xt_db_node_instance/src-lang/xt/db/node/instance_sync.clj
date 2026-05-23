(ns xt.db.node.instance-sync
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.runtime :as instance]
             [xt.db.node.instance-state :as instance-state]
             [xt.db.node.instance-query :as instance-query]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt normalize-sync
  "normalizes a sync spec into db/sync and db/remove keys"
  {:added "4.1"}
  [sync-spec view-context]
  (var db-sync (or (xt/x:get-key sync-spec "db/sync")
                   (xt/x:get-key view-context "db/sync")))
  (var db-remove (or (xt/x:get-key sync-spec "db/remove")
                     (xt/x:get-key view-context "db/remove")))
  (var out {})
  (when (xt/x:not-nil? db-sync)
    (xt/x:set-key out "db/sync" db-sync))
  (when (xt/x:not-nil? db-remove)
    (xt/x:set-key out "db/remove" db-remove))
  (return out))

(defn.xt prepare-sync
  "prepares a sync request"
  {:added "4.1"}
  [sync-spec view-context]
  (var request (-/normalize-sync sync-spec view-context))
  (var db-sync (xt/x:get-key request "db/sync"))
  (var db-remove (xt/x:get-key request "db/remove"))
  (when (and (xt/x:nil? db-sync)
             (xt/x:nil? db-remove))
    (return [false {:status "error"
                    :tag "db/sync-empty-request"}]))
  (when (and (xt/x:not-nil? db-sync)
             (not (xt/x:is-object? db-sync)))
    (return [false {:status "error"
                    :tag "db/sync-invalid"
                    :data {:input db-sync}}]))
  (when (and (xt/x:not-nil? db-remove)
             (not (xt/x:is-object? db-remove)))
    (return [false {:status "error"
                    :tag "db/remove-invalid"
                    :data {:input db-remove}}]))
  (when (xt/x:is-object? db-sync)
    (xt/for:object [[table entries] db-sync]
      (when (not (xt/x:is-array? entries))
        (return [false {:status "error"
                        :tag "db/sync-invalid-entries"
                        :data {:table table
                               :input entries}}]))))
  (when (xt/x:is-object? db-remove)
    (xt/for:object [[table ids] db-remove]
      (when (not (xt/x:is-array? ids))
        (return [false {:status "error"
                        :tag "db/remove-invalid-ids"
                        :data {:table table
                               :input ids}}]))))
  (return [true request]))

(defn.xt payload-tables
  "gets the affected tables for a sync or invalidation payload"
  {:added "4.1"}
  [payload]
  (cond (xt/x:is-array? payload)
        (return (xtd/arr-lookup payload))

        (xt/x:is-object? payload)
        (do (var out {})
            (xt/for:object [[table _] (or (xt/x:get-key payload "db/sync") {})]
              (xt/x:set-key out table true))
            (xt/for:object [[table _] (or (xt/x:get-key payload "db/remove") {})]
              (xt/x:set-key out table true))
            (when (and (== 0 (xt/x:len (xt/x:obj-keys out)))
                       (xt/x:has-key? payload "tables"))
              (:= out (or (xt/x:get-key payload "tables") {})))
            (return out))

        :else
        (return {})))

(defn.xt apply-sync-request
  "applies a sync request to the local db instance"
  {:added "4.1"}
  [state sync-request]
  (var db (instance-state/ensure-db state))
  (var schema (xt/x:get-key state "schema"))
  (var db-sync (xt/x:get-key sync-request "db/sync"))
  (var db-remove (xt/x:get-key sync-request "db/remove"))
  (when (and (xt/x:is-object? db-sync)
             (xtd/not-empty? db-sync))
    (instance/sync-event db ["add" db-sync]))
  (when (and (xt/x:is-object? db-remove)
             (xtd/not-empty? db-remove))
    (xt/for:object [[table ids] db-remove]
      (instance/db-delete-sync db schema table ids)))
  (return {:result sync-request
           :tables (-/payload-tables sync-request)
           :event sync-request}))

(defn.xt run-sync-local
  "prepares and applies a local sync request"
  {:added "4.1"}
  [state sync-spec view-context]
  (var [ok request] (-/prepare-sync sync-spec view-context))
  (when (not ok)
    (return [ok request]))
  (return [true (-/apply-sync-request state request)]))

(defn.xt clear-state-cache
  "clears the local cache and query indexes"
  {:added "4.1"}
  [state]
  (instance/db-clear (instance-state/ensure-db state))
  (xt/x:set-key state "queries" {})
  (xt/x:set-key state "watch" {})
  (xt/x:set-key state "view_watch" {})
  (xt/for:object [[model-id model] (or (xt/x:get-key state "models") {})]
    (xt/for:object [[view-id _] (or (xt/x:get-key model "views") {})]
      (instance-state/set-view-stale state model-id view-id "cleared")))
  (return true))

(defn.xt process-cache-payload
  "applies or invalidates cache state for a payload"
  {:added "4.1"}
  [state payload skip-apply]
  (when (not skip-apply)
    (-/apply-sync-request state payload))
  (var tables (-/payload-tables payload))
  (var query-keys (instance-state/affected-query-ids state tables))
  (var view-bindings (instance-state/affected-view-bindings state tables))
  (var opts (or (xt/x:get-key state "opts") {}))
  (var auto-refresh true)
  (when (xt/x:has-key? opts "auto_refresh")
    (:= auto-refresh (xt/x:get-key opts "auto_refresh")))
  (var refreshed nil)
  (if auto-refresh
    (do (instance-query/mark-query-stale-many state query-keys payload)
        (:= refreshed (instance-query/refresh-view-bindings-local state view-bindings)))
    (do (:= refreshed (instance-query/mark-view-bindings-stale state view-bindings payload))
        (instance-query/mark-query-stale-many state query-keys payload)))
  (return {:tables tables
            :queries query-keys
            :views view-bindings
            :refreshed refreshed}))

(defn.xt handle-cache-changed
  "handles an inbound cache.changed stream"
  {:added "4.1"}
  [current-space stream node]
  (var state (instance-state/ensure-state current-space node))
  (var payload (or (xt/x:get-key stream "data") {}))
  (var origin-node (xtd/get-in stream ["meta" "origin_node"]))
  (return (-/process-cache-payload
           state
           payload
           (== origin-node (xt/x:get-key node "id")))))

(defn.xt handle-cache-invalidated
  "handles an inbound cache.invalidated stream"
  {:added "4.1"}
  [current-space stream node]
  (var state (instance-state/ensure-state current-space node))
  (var payload (or (xt/x:get-key stream "data") {}))
  (return (-/process-cache-payload
           state
           {"tables" (-/payload-tables payload)}
           true)))
