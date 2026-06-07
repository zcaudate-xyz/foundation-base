(ns xt.db.node.adaptor-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.substrate :as substrate]
             [xt.db.text.sql-call :as call]
             [xt.db.system.impl-sqlite :as impl-sqlite]
             [xt.db.system.impl-memory :as impl-memory]]})

;;
;; The xt.db.node.adaptor-base 
;;

(comment

  {"services"
   {"db/common"   {"schema"  {}
                   "lookup"  {}}
    "db/primary"  'client
    "db/caching"  'client}})

(defn.xt ^{:substrate/fn true
           :substrate/global true}
  db-get-common
  [node])

(defn.xt ^{:substrate/fn true}
  db-init-sqlite
  [node driver schema lookup])

(defn.xt ^{:substrate/fn true}
  db-init-sqlite
  [node driver schema lookup]
  (sql-))

(defn.xt ^{:substrate/fn true}
  db-init-superbase
  [space args request node])

(defn.xt ^{:substrate/fn true}
  pair-init-sup
  [node schema lookup]
  (impl-sqlite/init-client)
  
  (-> (sqlite-impl/client-sqlite
       (@! fixtures/+schema+)
       (@! fixtures/+lookup+)
       nil
       {"filename" ":memory:"})
      (sqlite-impl/client-sqlite-init
       (js-sqlite/driver))
      (promise/x:promise-then
       (fn [caching-db]
         (var node
              (main/node-create
               {"services"
                {"db/primary"
                 (pg-impl/client-postgres
                  (@! fixtures/+schema+)
                  (@! fixtures/+lookup+)
                  {}
                  {"schema_name" "scratch"})
                 "db/caching" caching-db}}))
         (var result
              (sqlite-impl/pull (main/get-service node "db/caching")
                                ["Entry" ["name"]]))
         (repl/notify
          {"value" result
           "count" (xt/x:len result)
           "dbtype" (. (main/get-service node "db/caching") ["::"])})))))







(defn.xt ^{:substrate/fn true}
  set-db-primary
  [space args request node])

(defn.xt ^{:substrate/fn true}
  set-db-caching
  [space args request node])


(defn.xt install-schema
  [node schema])

(defn.xt install-sqllite
  [node schema])

(defn.xt install-sqllite
  [node schema])


(defn.xt install-sql-lite
  [space args request node]
  )

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
