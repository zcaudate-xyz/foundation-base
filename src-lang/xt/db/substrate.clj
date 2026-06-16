(ns xt.db.substrate
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.conn-sql :as dbsql]
             [xt.substrate :as substrate]
             [xt.db.text.sql-call :as call]]})


;; 1. discover how transport works
;; 2. figure out how to load models and views with only data 
;; 3. figure out how to load/unload routes through only the server

(defn.xt call-db-handler
  [driver-fn service-id]
  (return
   (fn [space args request node]
     (var opts (xt/x:first args))
     (var fn-template (. opts ["template"]))
     (var fn-args     (. opts ["args"]))
     (return
      (-> (dbsql/connect
           (driver-fn)
           (substrate/get-service node service-id))
          (promise/x:promise-then
           (fn [conn]
             (return (call/call-raw conn fn-template fn-args)))))))))

(defn.xt call-view-request
  [template handler-id action-id meta]
  (return
   (fn [context]
     (var space   (. context ["space"]))
     (var args    (. context ["args"]))
     (var node    (. context ["node"]))
     (return
      (substrate/request node
                         (. space ["id"])
                         handler-id
                         [{"args" args
                           "template" template}]
                         (xt/x:obj-assign {"action_id" action-id}
                                          (or meta {})))))))


;;
;;
                                        ;
(comment

  (defn.xt query-db-handler
    [service-id desc opts]
    (return
     (fn [space args request node]
       (var payload       (or (xt/x:first args) {}))
       (var query-spec    (or (. payload ["query"])
                              payload))
       (var view-context  (or (. payload ["view"])
                              {"args" []}))
       (var schema        (. desc ["schema"]))
       (var views         (. desc ["views"]))
       (var db            (substrate/get-service node service-id))
       (var [ok prepared] (dataview/prepare-query desc
                                                  query-spec
                                                  view-context))
       (when (not ok)
         (return prepared))
       (return
        (db-system/db-pull db
                           schema
                           (. prepared ["plan"]))))))

  (defn.xt service-dbtype
    [service]
    (var tag (xt/x:get-key service "::"))
    (when (xt/x:not-nil? tag)
      (return tag))
    (var kind (xt/x:get-key service "kind"))
    (cond (or (== kind "cache")
              (== kind "memory"))
          (return "db.cache")

          (== kind "supabase")
          (return "db.supabase")

          (== kind "postgres")
          (return "db.postgres")

          (== kind "sqlite")
          (return "db.sqlite")

          :else
          (return "db.sql")))

  (defn.xt init-db-service
    [node service-id]
    (var service (substrate/get-service node service-id))
    (when (xt/x:nil? service)
      (return nil))
    (when (xt/x:has-key? service "events")
      (return service))
    (var schema (or (xt/x:get-key service "schema")
                    (xt/x:get-key node "schema")
                    {}))
    (var lookup (or (xt/x:get-key service "lookup")
                    (xt/x:get-key node "lookup")
                    {}))
    (var db-opts (or (xt/x:get-key service "db_opts")
                     nil))
    (var db (db-system/db-create
             (xt/x:obj-assign {"::" (-/service-dbtype service)}
                              service)
             schema
             lookup
             db-opts))
    (substrate/set-service node service-id db)
    (return db))

  (defn.xt init-db-services
    [node]
    (-/init-db-service node "db/primary")
    (-/init-db-service node "db/caching")
    (return node)))

