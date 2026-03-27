(ns js.cell.service.db-sync
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[js.cell.service.db-view :as db-view]
             [xt.db :as xdb]
             [xt.db.base-flatten :as flatten]
             [xt.lang.base-lib :as k]]
   :export  [MODULE]})

(defn.xt sync-capable?
  "checks that the db descriptor can process sync requests"
  {:added "4.0"}
  [db]
  (return (and (k/obj? db)
               (k/has-key? db "schema"))))

(defn.xt normalize-sync
  "normalizes a sync spec into db/sync and db/remove keys"
  {:added "4.0"}
  [db sync-spec view-context]
  (return
   {"db/sync"   (or (k/get-key sync-spec "db/sync")
                    (k/get-key sync-spec "sync")
                    (k/get-key view-context "db/sync")
                    (k/get-key view-context "sync"))
    "db/remove" (or (k/get-key sync-spec "db/remove")
                    (k/get-key sync-spec "remove")
                    (k/get-key view-context "db/remove")
                    (k/get-key view-context "remove"))}))

(defn.xt prepare-sync
  "prepares a sync request"
  {:added "4.0"}
  [db sync-spec view-context]
  (var request (-/normalize-sync db sync-spec view-context))
  (var db-sync (k/get-key request "db/sync"))
  (var db-remove (k/get-key request "db/remove"))
  (when (and (k/nil? db-sync)
             (k/nil? db-remove))
    (return [false {:status "error"
                    :tag "db/sync-empty-request"}]))
  (when (and (k/not-nil? db-sync)
             (not (k/obj? db-sync)))
    (return [false {:status "error"
                    :tag "db/sync-invalid"
                    :data {:input db-sync}}]))
  (when (and (k/not-nil? db-remove)
             (not (k/obj? db-remove)))
    (return [false {:status "error"
                    :tag "db/remove-invalid"
                    :data {:input db-remove}}]))
  (when (k/obj? db-sync)
    (k/for:object [[table entries] db-sync]
      (when (not (k/arr? entries))
        (return [false {:status "error"
                        :tag "db/sync-invalid-entries"
                        :data {:table table
                               :input entries}}]))))
  (when (k/obj? db-remove)
    (k/for:object [[table ids] db-remove]
      (when (not (k/arr? ids))
        (return [false {:status "error"
                        :tag "db/remove-invalid-ids"
                        :data {:table table
                               :input ids}}]))))
  (return [true request]))

(defn.xt execute-sync
  "executes a prepared sync request against a local db"
  {:added "4.0"}
  [db sync-request view-context]
  (var local-db (k/get-key view-context "db"))
  (when (k/nil? local-db)
    (return [false {:status "error"
                    :tag "db/local-db-not-provided"}]))
  (var db-sync (k/get-key sync-request "db/sync"))
  (var db-remove (k/get-key sync-request "db/remove"))
  (when (and (k/obj? db-sync)
             (k/not-empty? db-sync))
    (xdb/sync-event local-db ["add" db-sync]))
  (when (and (k/obj? db-remove)
             (k/not-empty? db-remove))
    (k/for:object [[table ids] db-remove]
      (xdb/db-delete-sync local-db
                          (db-view/get-schema db)
                          table
                          ids)))
  (return [true sync-request]))

(defn.xt result->update
  "converts a sync request into an update payload"
  {:added "4.0"}
  [db sync-result view-context]
  (var db-sync (k/get-key sync-result "db/sync"))
  (var db-remove (k/get-key sync-result "db/remove"))
  (var sync-changes (:? (k/not-nil? db-sync)
                        (-> (flatten/flatten-bulk (db-view/get-schema db)
                                                  db-sync)
                            (k/obj-map k/obj-keys))
                        nil))
  (var body {"db/sync" sync-changes
             "db/remove" db-remove})
  (var update-mode (or (k/get-key view-context "update-mode")
                       "sync"))
  (cond (== update-mode "refresh-local")
        (return {"type" "refresh"
                 "view_id" (k/get-key view-context "view-id")
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
