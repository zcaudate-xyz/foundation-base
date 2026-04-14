(ns js.cell.binding.model
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.cell.binding.trigger :as binding-trigger]
             [js.cell.service.db-query :as db-query]
             [js.cell.service.db-remote :as db-remote]
             [js.cell.service.db-supabase :as db-supabase]
             [js.cell.service.db-sync :as db-sync]
             [xt.lang.common-lib :as k]]})

(defn.js unwrap-result
  "unwraps a service-layer [ok result] tuple"
  {:added "4.0"}
  [[ok result]]
  (when (not ok)
    (k/throw result))
  (return result))

(defn.js make-view-context
  "creates a view execution context"
  {:added "4.0"}
  [prepared db link args]
  (return {"args" args
           "db" db
           "link" link
           "model-id" (k/get-key prepared "model_id")
           "view-id" (k/get-key prepared "view_id")}))

(defn.js compile-main-handler
  "compiles the local query handler"
  {:added "4.0"}
  [prepared]
  (var query (k/get-key prepared "query"))
  (when (k/nil? query)
    (return nil))
  (var target (k/get-key query "target"))
  (when (and (k/not-nil? target)
             (not= target "local"))
    (return nil))
  (var db (k/get-key query "db"))
  (return
   (fn [link ...args]
     (return (-/unwrap-result
              (db-query/run-query db
                                  query
                                  (-/make-view-context prepared db link args)))))))

(defn.js compile-remote-handler
  "compiles the remote query handler"
  {:added "4.0"}
  [prepared]
  (var query (k/get-key prepared "query"))
  (var remote (k/get-key prepared "remote"))
  (when (k/nil? query)
    (return nil))
  (var target (k/get-key query "target"))
  (cond (== target "supabase")
        (do (var db (k/get-key query "db"))
            (return
             (fn [link ...args]
               (return (-/unwrap-result
                        (db-supabase/run-supabase-query
                         db
                         query
                         (-/make-view-context prepared db link args)))))))

        remote
        (do (var remote-db (or (k/get-key remote "db")
                               (k/get-key query "db")))
            (return
             (fn [link ...args]
               (return (-/unwrap-result
                        (db-remote/run-remote-query
                         remote-db
                         remote
                         query
                         (-/make-view-context prepared remote-db link args)))))))

        :else
        (return nil)))

(defn.js compile-sync-pipeline
  "compiles the sync pipeline section"
  {:added "4.0"}
  [prepared]
  (var sync (k/get-key prepared "sync"))
  (when (k/nil? sync)
    (return nil))
  (var remote (k/get-key prepared "remote"))
  (var handler nil)
  (if remote
    (do (var remote-db (or (k/get-key remote "db")
                           (k/get-key sync "db")))
        (:= handler
            (fn [link ...args]
              (return (-/unwrap-result
                       (db-remote/run-remote-sync
                        remote-db
                        remote
                        sync
                        (-/make-view-context prepared remote-db link args)))))))
    (do (var db (k/get-key sync "db"))
        (:= handler
            (fn [link ...args]
              (return (-/unwrap-result
                       (db-sync/run-sync
                        db
                        sync
                        (-/make-view-context prepared db link args))))))))
  (return {"sync" {"handler" handler}}))

(defn.js compile-view-spec
  "compiles a prepared descriptor into a kernel view spec"
  {:added "4.0"}
  [prepared]
  (var hooks (binding-trigger/compile-view-hooks prepared))
  (var options
       (k/obj-assign-nested
        {"context" {"modelId" (k/get-key prepared "model_id")
                     "viewId" (k/get-key prepared "view_id")
                     "resolve" (k/get-key prepared "resolve")
                     "stream" (k/get-key prepared "stream")}}
        (or (k/get-key prepared "options") {})
        (or (k/get-key hooks "options") {})))
  (return {"handler" (-/compile-main-handler prepared)
           "remoteHandler" (-/compile-remote-handler prepared)
           "pipeline" (-/compile-sync-pipeline prepared)
           "defaultArgs" (k/get-key prepared "default_args")
           "defaultOutput" (k/get-key prepared "default_output")
           "defaultProcess" (k/get-key prepared "default_process")
           "defaultInit" (k/get-key prepared "default_init")
           "trigger" (or (k/get-key hooks "trigger")
                         (k/get-key prepared "trigger"))
           "options" options
           "deps" (or (k/get-key hooks "deps")
                      (k/get-key prepared "deps"))}))
