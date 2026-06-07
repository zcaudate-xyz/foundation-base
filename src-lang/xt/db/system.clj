(ns xt.db.system
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.client-memory :as impl-memory]
             [xt.db.system.client-postgres :as impl-postgres]
             [xt.db.system.base-sql :as base-sql]
             [xt.db.system.client-sqlite :as impl-sqlite]
             [xt.db.system.client-supabase :as impl-supabase]
             [xt.db.text.base-scope :as scope]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.protocol.impl.graphdb :as graphdb]
             [xt.event.util-throttle :as th]]})

(defn.xt unsupported-op
  "signals that the requested backend operation is unavailable"
  {:added "4.1"}
  [op dbtype]
  (xt/x:throw {:status "error"
               :tag "db/op-not-available"
               :data {:op op
                      :dbtype dbtype}}))

(defn.xt client-input
  "builds the client creation record with stored db context"
  {:added "4.1"}
  [source instance schema lookup opts]
  (var out (xt/x:obj-clone (or source {})))
  (when (xt/x:not-nil? instance)
    (xt/x:set-key out "instance" instance))
  (xt/x:set-key out
                "schema"
                (or (xt/x:get-key out "schema")
                    schema
                    {}))
  (xt/x:set-key out
                "lookup"
                (or (xt/x:get-key out "lookup")
                    lookup
                    {}))
  (xt/x:set-key out
                "opts"
                (or (xt/x:get-key out "opts")
                    opts
                    {}))
  (return out))

(defn.xt ensure-client-memory
  ([instance]
   (return (-/ensure-client-memory instance nil)))
  ([instance input]
   (if (impl-memory/client? instance)
     (return instance)
     (return (impl-memory/client (-/client-input input instance nil nil nil))))))

(defn.xt ensure-sql-client
  ([instance]
   (return (-/ensure-sql-client instance nil)))
  ([instance input]
   (if (base-sql/client? instance)
     (return instance)
     (return (base-sql/client (-/client-input input instance nil nil nil))))))

(defn.xt ensure-client-postgres
  ([instance]
   (return (-/ensure-client-postgres instance nil)))
  ([instance input]
   (if (impl-postgres/client? instance)
     (return instance)
     (return (impl-postgres/client (-/client-input input instance nil nil nil))))))

(defn.xt ensure-client-base
  ([instance]
   (return (-/ensure-client-base instance nil)))
  ([instance input]
   (if (impl-sqlite/client? instance)
     (return instance)
     (return (impl-sqlite/client (-/client-input input instance nil nil nil))))))

(defn.xt ensure-client-supabase
  ([instance]
   (return (-/ensure-client-supabase instance nil)))
  ([instance input]
   (if (impl-supabase/client? instance)
     (return instance)
     (return (impl-supabase/client (-/client-input input instance nil nil nil))))))

(defn.xt ensure-driver
  [entry]
  (cond (xt/x:nil? entry)
        (return nil)

        (graphdb/driver? entry)
        (return entry)

        :else
        (return (graphdb/driver-create entry))))

(defn.xt get-driver
  [dbtype]
  (var entry (xt/x:get-key -/DRIVERS dbtype))
  (var driver (-/ensure-driver entry))
  (when (and (xt/x:not-nil? dbtype)
             (xt/x:not-nil? driver)
             (xt/x:neq entry driver))
    (xt/x:set-key -/DRIVERS dbtype driver))
  (return driver))

(defn.xt wrap-instance
  [dbtype instance driver input]
  (cond (xt/x:nil? instance)
        (return nil)

        (== dbtype "db.cache")
        (return (-/ensure-client-memory instance input))

        (== dbtype "db.supabase")
        (return (-/ensure-client-supabase instance input))

        (== dbtype "db.postgres")
        (return (-/ensure-client-postgres instance input))

        (== dbtype "db.sqlite")
        (return (-/ensure-client-base instance input))

        (== dbtype "db.sql")
        (return (-/ensure-sql-client instance input))

        :else
        (return (graphdb/db-create instance driver))))

(defn.xt ensure-db
  [db]
  (when (xt/x:nil? db)
    (return nil))
  (var instance (xt/x:get-key db "instance"))
  (when (graphdb/db? instance)
    (return instance))
  (var dbtype (-/get-dbtype db))
  (var driver (-/get-driver dbtype))
  (when (xt/x:nil? driver)
    (return nil))
  (when (xt/x:nil? instance)
    (return nil))
  (var input (-/client-input db instance
                             (xt/x:get-key db "schema")
                             (xt/x:get-key db "lookup")
                             (xt/x:get-key db "opts")))
  (var client (-/wrap-instance dbtype instance driver input))
  (xt/x:set-key db "instance" client)
  (return client))

(def.xt DRIVERS
  {"db.cache" (graphdb/driver-create
              {"create"      (fn [m]
                               (return (impl-memory/client m)))
               "add"         (fn [instance tag data schema lookup opts]
                               (return (impl-memory/process-event-sync instance tag data schema lookup opts)))
               "remove"      (fn [instance tag data schema lookup opts]
                               (return (impl-memory/process-event-remove instance tag data schema lookup opts)))
               "delete_sync" (fn [instance schema table-name ids opts]
                               (return (impl-memory/record-delete-sync instance schema table-name ids opts)))
               "clear"       (fn [instance]
                               (return (impl-memory/clear instance)))})
  "db.supabase" impl-supabase/DRIVER
  "db.postgres" impl-postgres/DRIVER
  "db.sqlite" impl-sqlite/DRIVER
  "db.sql" (graphdb/driver-create
            {"create"      (fn [m]
                             (return (base-sql/client m)))
             "add"         (fn [instance tag data schema lookup opts]
                             (return (base-sql/process-event-sync instance tag data schema lookup opts)))
             "remove"      (fn [instance tag data schema lookup opts]
                             (return (base-sql/process-event-remove instance tag data schema lookup opts)))
             "delete_sync" (fn [instance schema table-name ids opts]
                             (return (base-sql/record-delete-sync instance schema table-name ids opts)))
             "exec_sync"   (fn [instance raw-input]
                             (return (base-sql/exec-sync instance raw-input)))})})

(defn.xt get-dbtype
  [db]
  (return (xt/x:get-key db "::")))

(defn.xt process-event
  "processes an event"
  {:added "4.0"}
  [db event schema lookup opts]
  (var dbtype (-/get-dbtype db))
  (var instance (-/ensure-db db))
  (var driver (-/get-driver dbtype))
  (var tag (xt/x:first event))
  (var data (xt/x:second event))
  (var as-input (:? (> (xt/x:len event) 2)
                    (xt/x:get-idx event (xt/x:offset 2))
                    nil))
  (when (xt/x:nil? instance)
    (return (-/unsupported-op tag dbtype)))
  (var input-tag (:? as-input "input" tag))
  (cond (== tag "add")
        (do (when (or (xt/x:nil? driver)
                      (xt/x:nil? (graphdb/driver-op driver "add")))
              (return (-/unsupported-op tag dbtype)))
            (return ((graphdb/driver-op driver "add")
                     instance
                     input-tag
                     data
                     schema
                     lookup
                     opts)))

        (== tag "remove")
        (do (when (or (xt/x:nil? driver)
                      (xt/x:nil? (graphdb/driver-op driver "remove")))
              (return (-/unsupported-op tag dbtype)))
            (return ((graphdb/driver-op driver "remove")
                     instance
                     input-tag
                     data
                     schema
                     lookup
                     opts)))

        :else
        (return (-/unsupported-op tag dbtype))))

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
  (var driver (-/get-driver dbtype))
  (when (or (xt/x:nil? driver)
           (xt/x:nil? (graphdb/driver-op driver "create")))
    (return (-/unsupported-op "create" dbtype)))
  (var input (-/client-input m
                             (xt/x:get-key m "instance")
                             schema
                             lookup
                             opts))
  (var instance (or (xt/x:get-key input "instance")
                   (graphdb/create driver input)))
  (:= instance (-/wrap-instance dbtype instance driver input))
  (var db {"::" dbtype
           :instance instance
           :events []
           :triggers {}
           :schema (xt/x:get-key input "schema")
           :lookup (xt/x:get-key input "lookup")
           :opts (xt/x:get-key input "opts")})
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
  (var instance (-/ensure-db db))
  (var driver (-/get-driver dbtype))
  (when (or (xt/x:nil? instance)
            (xt/x:nil? driver)
            (xt/x:nil? (graphdb/driver-op driver "exec_sync")))
    (return (-/unsupported-op "exec_sync" dbtype)))
  (return ((graphdb/driver-op driver "exec_sync")
           instance
           raw-input)))

(defn.xt db-pull-sync
  "runs a pull statement"
  {:added "4.0"}
  [db schema tree]
  (var dbtype (-/get-dbtype db))
  (var instance (-/ensure-db db))
  (var opts (xt/x:get-key db "opts"))
  (when (or (xt/x:nil? instance)
            (xt/x:nil? (graphdb/db-op instance "pull_sync")))
    (return (-/unsupported-op "pull_sync" dbtype)))
  (return (graphdb/pull-sync instance schema tree opts)))

(defn.xt db-pull
  "runs a pull statement with async semantics"
  {:added "4.1"}
  [db schema tree]
  (var dbtype (-/get-dbtype db))
  (var instance (-/ensure-db db))
  (var opts (xt/x:get-key db "opts"))
  (if (and (xt/x:not-nil? instance)
           (xt/x:not-nil? (graphdb/db-op instance "pull")))
    (return (graphdb/pull instance schema tree opts))
    (return
     (promise/x:promise-run
      (-/db-pull-sync db schema tree)))))

(defn.xt db-delete-sync
  "deletes rows from the db"
  {:added "4.0"}
  [db schema table-name ids]
  (var dbtype (-/get-dbtype db))
  (var instance (-/ensure-db db))
  (var opts (xt/x:get-key db "opts"))
  (var driver (-/get-driver dbtype))
  (when (or (xt/x:nil? instance)
            (xt/x:nil? driver)
            (xt/x:nil? (graphdb/driver-op driver "delete_sync")))
    (return (-/unsupported-op "delete_sync" dbtype)))
  (return ((graphdb/driver-op driver "delete_sync")
           instance
           schema
           table-name
           ids
           opts)))

(defn.xt db-clear
  "clears the db"
  {:added "4.0"}
  [db]
  (var dbtype (-/get-dbtype db))
  (var instance (-/ensure-db db))
  (var driver (-/get-driver dbtype))
  (when (or (xt/x:nil? instance)
            (xt/x:nil? driver)
            (xt/x:nil? (graphdb/driver-op driver "clear")))
    (return (-/unsupported-op "clear" dbtype)))
  (return ((graphdb/driver-op driver "clear")
           instance)))

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
