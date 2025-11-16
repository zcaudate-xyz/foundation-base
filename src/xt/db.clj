(ns xt.db
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.db.base-flatten :as f]
             [xt.db.base-schema :as base-schema]
             [xt.db.base-scope :as scope]
             [xt.db.impl-cache :as impl-cache]
             [xt.db.impl-sql :as impl-sql]
             [xt.lang.base-lib :as k]
             [xt.lang.util-throttle :as th]
             [xt.sys.conn-dbsql :as conn-dbsql]]})

(def.xt IMPL
  {"db.cache" {:create  (fn:> {:rows {}})
               :add     impl-cache/cache-process-event-sync  
               :remove  impl-cache/cache-process-event-remove
               :pull-sync    impl-cache/cache-pull-sync
               :delete-sync  impl-cache/cache-delete-sync}
   "db.sql"   {:create  conn-dbsql/connect
               :add     impl-sql/sql-process-event-sync
               :remove  impl-sql/sql-process-event-remove
               :pull-sync    impl-sql/sql-pull-sync
               :delete-sync  impl-sql/sql-delete-sync}})

(defn.xt process-event
  "processes an event"
  {:added "4.0"}
  [db event schema lookup opts]
  (var dbtype (or (. db ["::"])
                  "db.sql"))
  (var #{instance} db)
  (var [tag data as-input] event)
  (var event-fn (k/get-in -/IMPL [dbtype tag]))
  (var input-tag (:? as-input "input" tag))
  (return (event-fn instance input-tag data schema lookup opts)))

(defn.xt process-triggers
  "process triggers"
  {:added "4.0"}
  [db triggers tables]
  (var out [])
  (k/for:object [[id trigger] triggers]
    (var #{listen callback} trigger)
    (var update? (k/arr-some listen (fn [key] (return (k/has-key? tables key)))))
    (when update?
      (x:arr-push out id)
      (if (k/get-key trigger "async")
        (k/for:async [[ok err] (callback db trigger)]
          {:success (return ok)
           :error   (return err)})
        (callback db trigger))))
  (return out))

(defn.xt add-trigger
  "adds a trigger to db"
  {:added "4.0"}
  [db id trigger]
  (var #{triggers} db)
  (k/set-key triggers id trigger)
  (return id))

(defn.xt remove-trigger
  "removes the trigger"
  {:added "4.0"}
  [db id]
  (var #{triggers} db)
  (var curr (k/get-key triggers id))
  (k/del-key triggers id)
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
  (var dbtype (or (. m ["::"])
                  "db.sql"))
  (var create-fn (k/get-in -/IMPL [dbtype "create"]))
  (var instance (or (k/get-key m "instance")
                    (create-fn m)))
  (var db      {"::" dbtype
                :instance instance
                :events   []
                :triggers {}
                :opts opts})
  (var handler  (fn [_]
                  (var #{events
                         triggers} db)
                  (k/set-key db "events" [])
                  (var tables (-> (k/arr-mapcat
                                   events
                                   (fn [e]
                                     (return (-/process-event db e schema lookup opts))))
                                  (k/arr-lookup)))
                  (return (-/process-triggers db triggers tables))))
  (var throttle (th/throttle-create handler nil))
  (var sync-handler (fn [e]
                      (return (-/process-event db e schema lookup opts))))
  (k/set-key db "throttle" throttle)
  (k/set-key db "handler" handler)
  (k/set-key db "sync_handler" sync-handler)
  (return db))

(defn.xt queue-event
  "queues an event to the db"
  {:added "4.0"}
  [db event]
  (var #{throttle events} db)
  (x:arr-push events event)
  (return (th/throttle-run throttle "main")))

(defn.xt sync-event
  "syncs an event to the db"
  {:added "4.0"}
  [db event]
  (var #{sync-handler instance triggers} db)
  (var output (sync-handler event))
  (cond (or (k/is-string? output)
            (k/nil? output))
        (return output)
        
        :else
        (do (var tables (k/arr-lookup output))
            (return [(-/process-triggers db triggers tables)
                     tables]))))

(defn.xt db-exec-sync
  "runs a raw statement"
  {:added "4.0"}
  [db raw-input]
  (var dbtype (or (. db ["::"])
                  "db.sql"))
  (var #{instance} db)
  (when (== dbtype "db.sql")
    (return (conn-dbsql/query-sync instance
                                   raw-input))))

(defn.xt db-pull-sync
  "runs a pull statement"
  {:added "4.0"}
  [db schema tree]
  (var dbtype (or (. db ["::"])
                  "db.sql"))
  (var #{instance opts} db)
  (cond (== dbtype "db.sql")
        (return (impl-sql/sql-pull-sync instance schema tree opts))

        (== dbtype "db.cache")
        (return (impl-cache/cache-pull-sync instance schema tree opts))))

(defn.xt db-delete-sync
  "deletes rows from the db"
  {:added "4.0"}
  [db schema table-name ids]
  (var dbtype (or (. db ["::"])
                  "db.sql"))
  (var #{instance opts} db)
  (cond (== dbtype "db.sql")
        (return (impl-sql/sql-delete-sync instance schema table-name ids opts))
        
        (== dbtype "db.cache")
        (return (impl-cache/cache-delete-sync instance schema table-name ids opts))))

(defn.xt db-clear
  "clears the db"
  {:added "4.0"}
  [db]
  (var dbtype (or (. db ["::"])
                  "db.sql"))
  (var #{instance opts} db)
  (cond (== dbtype "db.sql")
        (return (impl-sql/sql-clear instance))
        
        (== dbtype "db.cache")
        (return (impl-cache/cache-clear instance))))

(defn.xt add-view-trigger
  "adds a view trigger to the db"
  {:added "4.0"}
  [db id schema view view-fn]
  (var view-rec (k/get-key view "view"))
  (var #{table query} view-rec)
  (var listen (k/obj-keys (scope/get-linked-tables schema
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

