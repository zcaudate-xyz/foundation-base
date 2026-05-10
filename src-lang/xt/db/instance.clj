(ns xt.db.instance
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.text.base-schema :as base-schema]
             [xt.db.text.base-scope :as scope]
             [xt.db.runtime.cache :as impl-cache]
             [xt.db.runtime.supabase-pull :as impl-supabase-pull]
             [xt.db.runtime.sql :as impl-sql]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.event.util-throttle :as th]]})

(defn.xt unsupported-op
  "signals that the requested backend operation is unavailable"
  {:added "4.1"}
  [op dbtype]
  (xt/x:throw {:status "error"
               :tag "db/op-not-available"
               :data {:op op
                      :dbtype dbtype}}))

(def.xt IMPL
  {"db.cache"    {"create"      (fn [m]
                                  (return {:rows {}}))
                  "add"         impl-cache/cache-process-event-sync
                  "remove"      impl-cache/cache-process-event-remove
                  "pull_sync"   impl-cache/cache-pull-sync
                  "delete_sync" impl-cache/cache-delete-sync
                  "clear"       impl-cache/cache-clear}
    "db.supabase" {"create"    (fn [m]
                                 (return m))
                   "pull_sync" impl-supabase-pull/supabase-pull-sync}
    "db.sql"      {"create"      (fn [m]
                                   (return (xt/x:get-key m "instance")))
                  "exec_sync"   (fn [instance raw-input _opts]
                                   (return (sql/query-sync instance raw-input)))
                  "add"         impl-sql/sql-process-event-sync
                  "remove"      impl-sql/sql-process-event-remove
                  "pull_sync"   impl-sql/sql-pull-sync
                  "delete_sync" impl-sql/sql-delete-sync
                  "clear"       impl-sql/sql-clear}})

(defn.xt get-dbtype
  [db]
  (return (or (xt/x:get-key db "::")
              "db.sql")))

(defn.xt process-event
  "processes an event"
  {:added "4.0"}
  [db event schema lookup opts]
  (var dbtype (-/get-dbtype db))
  (var #{instance} db)
  (var tag (xt/x:first event))
  (var data (xt/x:second event))
  (var as-input (:? (> (xt/x:len event) 2)
                    (xt/x:get-idx event (xt/x:offset 2))
                    nil))
  (var event-fn (xtd/get-in -/IMPL [dbtype tag]))
  (when (xt/x:nil? event-fn)
    (return (-/unsupported-op tag dbtype)))
  (var input-tag (:? as-input "input" tag))
  (return (event-fn instance input-tag data schema lookup opts)))

(defn.xt process-triggers
  "process triggers"
  {:added "4.0"}
  [db triggers tables]
  (var out [])
  (xt/for:object [[id trigger] triggers]
    (var #{listen callback} trigger)
    (var update? (xt/x:arr-some listen (fn [key] (return (xt/x:has-key? tables key)))))
    (when update?
      (xt/x:arr-push out id)
      (if (xt/x:get-key trigger "async")
        (callback db trigger)
        (callback db trigger))))
  (return out))

(defn.xt add-trigger
  "adds a trigger to db"
  {:added "4.0"}
  [db id trigger]
  (var #{triggers} db)
  (xt/x:set-key triggers id trigger)
  (return id))

(defn.xt remove-trigger
  "removes the trigger"
  {:added "4.0"}
  [db id]
  (var #{triggers} db)
  (var curr (xt/x:get-key triggers id))
  (xt/x:del-key triggers id)
  (return curr))

(defn.xt db-trigger
  "performs the trigger"
  {:added "4.0"}
  [db tables]
  (var #{instance
         triggers} db)
  (return (-/process-triggers db triggers tables)))

(defn.xt db-create
  "creates the db"
  {:added "4.0"}
  [m schema lookup opts]
  (var dbtype (-/get-dbtype m))
  (var create-fn (xtd/get-in -/IMPL [dbtype "create"]))
  (when (xt/x:nil? create-fn)
    (return (-/unsupported-op "create" dbtype)))
  (var instance (or (xt/x:get-key m "instance")
                    (create-fn m)))
  (var db {"::" dbtype
           :instance instance
           :events []
           :triggers {}
           :opts opts})
  (var handler (fn [_]
                 (var #{events
                        triggers} db)
                 (xt/x:set-key db "events" [])
                 (var tables (-> (xtd/arr-mapcat
                                  events
                                  (fn [e]
                                    (return (-/process-event db e schema lookup opts))))
                                 (xtd/arr-lookup)))
                 (return (-/process-triggers db triggers tables))))
  (var throttle (th/throttle-create handler nil))
  (var sync-handler (fn [e]
                      (return (-/process-event db e schema lookup opts))))
  (xt/x:set-key db "throttle" throttle)
  (xt/x:set-key db "handler" handler)
  (xt/x:set-key db "sync_handler" sync-handler)
  (return db))

(defn.xt queue-event
  "queues an event to the db"
  {:added "4.0"}
  [db event]
  (var #{throttle events} db)
  (xt/x:arr-push events event)
  (return (th/throttle-run throttle "main")))

(defn.xt sync-event
  "syncs an event to the db"
  {:added "4.0"}
  [db event]
  (var #{sync-handler instance triggers} db)
  (var output (sync-handler event))
  (cond (or (xt/x:is-string? output)
            (xt/x:nil? output))
        (return output)

        :else
        (do (var tables (xtd/arr-lookup output))
            (return [(-/process-triggers db triggers tables)
                     tables]))))

(defn.xt db-exec-sync
  "runs a raw statement"
  {:added "4.0"}
  [db raw-input]
  (var dbtype (-/get-dbtype db))
  (var #{instance opts} db)
  (var exec-fn (xtd/get-in -/IMPL [dbtype "exec_sync"]))
  (when (xt/x:nil? exec-fn)
    (return (-/unsupported-op "exec_sync" dbtype)))
  (return (exec-fn instance raw-input opts)))

(defn.xt db-pull-sync
  "runs a pull statement"
  {:added "4.0"}
  [db schema tree]
  (var dbtype (-/get-dbtype db))
  (var #{instance opts} db)
  (var pull-fn (xtd/get-in -/IMPL [dbtype "pull_sync"]))
  (when (xt/x:nil? pull-fn)
    (return (-/unsupported-op "pull_sync" dbtype)))
  (return (pull-fn instance schema tree opts)))

(defn.xt db-delete-sync
  "deletes rows from the db"
  {:added "4.0"}
  [db schema table-name ids]
  (var dbtype (-/get-dbtype db))
  (var #{instance opts} db)
  (var delete-fn (xtd/get-in -/IMPL [dbtype "delete_sync"]))
  (when (xt/x:nil? delete-fn)
    (return (-/unsupported-op "delete_sync" dbtype)))
  (return (delete-fn instance schema table-name ids opts)))

(defn.xt db-clear
  "clears the db"
  {:added "4.0"}
  [db]
  (var dbtype (-/get-dbtype db))
  (var #{instance opts} db)
  (var clear-fn (xtd/get-in -/IMPL [dbtype "clear"]))
  (when (xt/x:nil? clear-fn)
    (return (-/unsupported-op "clear" dbtype)))
  (return (clear-fn instance)))

(defn.xt add-view-trigger
  "adds a view trigger to the db"
  {:added "4.0"}
  [db id schema view view-fn]
  (var view-rec (xt/x:get-key view "view"))
  (var #{table query} view-rec)
  (var listen (xt/x:obj-keys (scope/get-linked-tables schema
                                                      table
                                                      query)))
  (var callback
       (fn []
         (view-fn (-/db-pull-sync db
                                  schema
                                  [table query]))))
  (return (-/add-trigger db id
                         {:id id
                          :listen listen
                          :callback callback})))
