(ns xt.db.node.trigger
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.query :as query]
             [xt.db.node.spec :as spec]
             [xt.db.node.state :as state]
             [xt.db.node.sync :as sync]
             [xt.db.node.watch :as watch]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt process-cache-payload
  "applies or invalidates cache state for a payload"
  {:added "4.1"}
  [state payload skip-apply]
  (when (not skip-apply)
    (sync/apply-sync-request state payload))
  (var tables (sync/payload-tables payload))
  (var query-keys (watch/affected-query-ids state tables))
  (var auto-refresh (or (xtd/get-in state ["opts" "auto_refresh"])
                        true))
  (var refreshed (:? auto-refresh
                     (query/refresh-query-keys state query-keys)
                     (query/mark-query-stale-many state query-keys payload)))
  (return {:tables tables
           :queries query-keys
           :refreshed refreshed}))

(defn.xt handle-cache-changed
  "handles an inbound cache.changed stream"
  {:added "4.1"}
  [current-space stream node]
  (var state (state/ensure-state current-space node))
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
  (var state (state/ensure-state current-space node))
  (var payload (or (xt/x:get-key stream "data") {}))
  (return (-/process-cache-payload
           state
           {"tables" (sync/payload-tables payload)}
           true)))
