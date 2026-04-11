(ns xt.cell.service.db-sync
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.cell.service.db-view :as db-view]
             [xt.db :as xdb]
             [xt.db.base-flatten :as flatten]
             [xt.lang.common-spec :as xt]]
   :export  [MODULE]})

(defn.xt sync-capable?
  "checks that the db descriptor can process sync requests"
  {:added "4.0"}
  [db]
  (return (and (xt/x:is-object? db)
               (xt/x:has-key? db "schema"))))

(defn.xt normalize-sync
  "normalizes a sync spec into db/sync and db/remove keys"
  {:added "4.0"}
  [db sync-spec view-context]
  (return
   {"db/sync"   (or (xt/x:get-key sync-spec "db/sync")
                    (xt/x:get-key sync-spec "sync")
                    (xt/x:get-key view-context "db/sync")
                    (xt/x:get-key view-context "sync"))
    "db/remove" (or (xt/x:get-key sync-spec "db/remove")
                    (xt/x:get-key sync-spec "remove")
                    (xt/x:get-key view-context "db/remove")
                    (xt/x:get-key view-context "remove"))}))

(defn.xt prepare-sync
  "prepares a sync request"
  {:added "4.0"}
  [db sync-spec view-context]
  (var request (-/normalize-sync db sync-spec view-context))
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

(defn.xt execute-sync
  "executes a prepared sync request against a local db"
  {:added "4.0"}
  [db sync-request view-context]
  (var local-db (xt/x:get-key view-context "db"))
  (when (xt/x:nil? local-db)
    (return [false {:status "error"
                    :tag "db/local-db-not-provided"}]))
  (var db-sync (xt/x:get-key sync-request "db/sync"))
  (var db-remove (xt/x:get-key sync-request "db/remove"))
  (when (and (xt/x:is-object? db-sync)
             (xt/x:not-empty? db-sync))
    (xdb/sync-event local-db ["add" db-sync]))
  (when (and (xt/x:is-object? db-remove)
             (xt/x:not-empty? db-remove))
    (xt/for:object [[table ids] db-remove]
      (xdb/db-delete-sync local-db
                          (db-view/get-schema db)
                          table
                          ids)))
  (return [true sync-request]))

(defn.xt result->update
  "converts a sync request into an update payload"
  {:added "4.0"}
  [db sync-result view-context]
  (var db-sync (xt/x:get-key sync-result "db/sync"))
  (var db-remove (xt/x:get-key sync-result "db/remove"))
  (var sync-changes (:? (xt/x:not-nil? db-sync)
                        (-> (flatten/flatten-bulk (db-view/get-schema db)
                                                  db-sync)
                            (xt/x:obj-map k/obj-keys))
                        nil))
  (var body {"db/sync" sync-changes
             "db/remove" db-remove})
  (var update-mode (or (xt/x:get-key view-context "update-mode")
                       "sync"))
  (cond (== update-mode "refresh-local")
        (return {"type" "refresh"
                 "view_id" (xt/x:get-key view-context "view-id")
                 "body" body})

        (== update-mode "patch")
        (return {"type" "patch"
                 "body" body})

        :else
        (return {"type" "sync"
                 "body" body})))

(defn.xt run-sync
  "prepares, executes, and summarizes a sync request"
  {:added "4.0"}
  [db sync-spec view-context]
  (var [ok request] (-/prepare-sync db sync-spec view-context))
  (when (not ok)
    (return [ok request]))
  (var [e-ok result] (-/execute-sync db request view-context))
  (when (not e-ok)
    (return [e-ok result]))
  (return [true {"result" result
                 "update" (-/result->update db result view-context)}]))

(def.xt MODULE (!:module))
