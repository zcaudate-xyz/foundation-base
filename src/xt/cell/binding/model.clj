(ns xt.cell.binding.model
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.cell.binding.trigger :as binding-trigger]
             [xt.cell.service.db-query :as db-query]
             [xt.cell.service.db-remote :as db-remote]
             [xt.cell.service.db-supabase :as db-supabase]
             [xt.cell.service.db-sync :as db-sync]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt unwrap-result
  "unwraps a service-layer [ok result] tuple"
  {:added "4.0"}
  [[ok result]]
  (when (not ok)
    (xt/x:throw result))
  (return result))

(defn.xt make-view-context
  "creates a view execution context"
  {:added "4.0"}
  [prepared db link args]
  (return {"args" args
           "db" db
           "link" link
           "model-id" (xt/x:get-key prepared "model_id")
           "view-id" (xt/x:get-key prepared "view_id")}))

(defn.xt compile-main-handler
  "compiles the local query handler"
  {:added "4.0"}
  [prepared]
  (var query (xt/x:get-key prepared "query"))
  (when (xt/x:nil? query)
    (return nil))
  (var target (xt/x:get-key query "target"))
  (when (and (xt/x:not-nil? target)
             (not= target "local"))
    (return nil))
  (var db (xt/x:get-key query "db"))
  (return
   (fn [link ...args]
     (return (-/unwrap-result
              (db-query/run-query db
                                  query
                                  (-/make-view-context prepared db link args)))))))

(defn.xt compile-remote-handler
  "compiles the remote query handler"
  {:added "4.0"}
  [prepared]
  (var query (xt/x:get-key prepared "query"))
  (var remote (xt/x:get-key prepared "remote"))
  (when (xt/x:nil? query)
    (return nil))
  (var target (xt/x:get-key query "target"))
  (cond (== target "supabase")
        (do (var db (xt/x:get-key query "db"))
            (return
             (fn [link ...args]
               (return (-/unwrap-result
                        (db-supabase/run-supabase-query
                         db
                         query
                         (-/make-view-context prepared db link args)))))))

        remote
        (do (var remote-db (or (xt/x:get-key remote "db")
                               (xt/x:get-key query "db")))
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

(defn.xt compile-sync-pipeline
  "compiles the sync pipeline section"
  {:added "4.0"}
  [prepared]
  (var sync (xt/x:get-key prepared "sync"))
  (when (xt/x:nil? sync)
    (return nil))
  (var remote (xt/x:get-key prepared "remote"))
  (var handler nil)
  (if remote
    (do (var remote-db (or (xt/x:get-key remote "db")
                           (xt/x:get-key sync "db")))
        (:= handler
            (fn [link ...args]
              (return (-/unwrap-result
                       (db-remote/run-remote-sync
                        remote-db
                        remote
                        sync
                        (-/make-view-context prepared remote-db link args)))))))
    (do (var db (xt/x:get-key sync "db"))
        (:= handler
            (fn [link ...args]
              (return (-/unwrap-result
                       (db-sync/run-sync
                        db
                        sync
                        (-/make-view-context prepared db link args))))))))
  (return {"sync" {"handler" handler}}))

(defn.xt compile-view-spec
  "compiles a prepared descriptor into a kernel view spec"
  {:added "4.0"}
  [prepared]
  (var hooks (binding-trigger/compile-view-hooks prepared))
  (var options
       (xtd/obj-assign-nested
        {"context" {"modelId" (xt/x:get-key prepared "model_id")
                     "viewId" (xt/x:get-key prepared "view_id")
                     "resolve" (xt/x:get-key prepared "resolve")
                     "stream" (xt/x:get-key prepared "stream")}}
        (or (xt/x:get-key prepared "options") {})
        (or (xt/x:get-key hooks "options") {})))
  (return {"handler" (-/compile-main-handler prepared)
           "remoteHandler" (-/compile-remote-handler prepared)
           "pipeline" (-/compile-sync-pipeline prepared)
           "defaultArgs" (xt/x:get-key prepared "default_args")
           "defaultOutput" (xt/x:get-key prepared "default_output")
           "defaultProcess" (xt/x:get-key prepared "default_process")
           "defaultInit" (xt/x:get-key prepared "default_init")
           "trigger" (or (xt/x:get-key hooks "trigger")
                         (xt/x:get-key prepared "trigger"))
           "options" options
           "deps" (or (xt/x:get-key hooks "deps")
                      (xt/x:get-key prepared "deps"))}))
