(ns xt.db.node.sync
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.instance :as instance]
             [xt.db.node.state :as state]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt normalize-sync
  "normalizes a sync spec into db/sync and db/remove keys"
  {:added "4.1"}
  [sync-spec view-context]
  (return {"db/sync" (or (xt/x:get-key sync-spec "db/sync")
                         (xt/x:get-key sync-spec "sync")
                         (xt/x:get-key view-context "db/sync")
                         (xt/x:get-key view-context "sync"))
           "db/remove" (or (xt/x:get-key sync-spec "db/remove")
                           (xt/x:get-key sync-spec "remove")
                           (xt/x:get-key view-context "db/remove")
                           (xt/x:get-key view-context "remove"))}))

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
  (var db (state/ensure-db state))
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
  (instance/db-clear (state/ensure-db state))
  (xt/x:set-key state "queries" {})
  (xt/x:set-key state "watch" {})
  (xt/for:object [[model-id model] (or (xt/x:get-key state "models") {})]
    (xt/for:object [[view-id _] (or (xt/x:get-key model "views") {})]
      (state/set-view-stale state model-id view-id "cleared")))
  (return true))
