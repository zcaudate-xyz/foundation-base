(ns xt.db.substrate
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.substrate :as substrate]
             [xt.db.text.sql-call :as call]]})

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
     (var request {"action" action-id
                   "space"  (get space "id")
                   "args"   args})
     (var node    (. context ["node"]))
     (return
      (substrate/request node
                         (. opts ["id"])
                         handler-id
                         [{"args" (. context ["args"])
                           "template" template}]
                         meta)))))


