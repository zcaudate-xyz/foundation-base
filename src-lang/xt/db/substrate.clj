(ns xt.db.substrate
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.substrate :as substrate]
             [xt.db.runtime :as db-runtime]
             [xt.db.text.sql-call :as call]
             [xt.db.node.schema-query :as schema-query]]})

(defn.xt call-db-handler
  [driver-fn service-id]
  (return
   (fn [space args request node]
     (var opts (xt/x:first args))
     (var fn-template (. opts ["template"]))
     (var fn-args     (. opts ["args"]))
     (return
      (-> (sql/connect
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
     (var [ok prepared] (schema-query/prepare-query desc
                                                    query-spec
                                                    view-context))
     (when (not ok)
       (return prepared))
     (return
      (db-runtime/db-pull db
                          schema
                          (. prepared ["plan"]))))))


;; 1. discover how transport works
;; 2. figure out how to load models and views with only data 
;; 3. figure out how to load/unload routes through only the server
