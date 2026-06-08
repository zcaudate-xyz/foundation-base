(ns xt.db.node.adaptor-base
  (:require [hara.lang :as l]
            [scaffold.supabase.event-host-util :as live]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.lang.common-notify :as notify]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.substrate :as substrate]
             [xt.db.text.sql-call :as call]
             [xt.db.system.impl-sqlite :as impl-sqlite]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.impl-supabase :as impl-supabase]]})

(l/script :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.substrate :as substrate]
             [xt.db.text.sql-call :as call]
             [xt.db.system.impl-sqlite :as impl-sqlite]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.impl-supabase :as impl-supabase]]})



;;
;; The xt.db.node.adaptor-base 
;;

(comment
  
  (live/refresh-live-supabase-config!)
  
  
  (notify/wait-on [:js 10000]
    (var settings (xt/x:obj-clone (. (@! live/+live-supabase-config+) ["client"])))
    (xt/x:set-key settings "transport" (js-fetch/client {}))
    (promise/x:promise-then
     (impl/client-supabase-init
      (impl/client-supabase
       (@! fixtures/+schema+)
       (@! fixtures/+lookup+)
       {}
       settings))
     (fn [client]
       (repl/notify
        {"tag" (. client ["::"])
         "instance" (supabase/client? (. client ["instance"]))}))))
  
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
  set-sqlite-service
  [node service-id driver schema lookup settings]
  (return
   (-> (impl-sqlite/client-sqlite schema lookup settings)
       (impl-sqlite/client-sqlite-init driver)
       (promise/x:promise-then
        (fn [client]
          (return
           (substrate/set-service service-id client)))))))

(defn.xt ^{:substrate/fn true}
  set-memory-service
  [node service-id driver schema lookup settings]
  (return
   (promise/x:promise-run
    (substrate/set-service
     service-id
     (impl-memory/client-memory schema lookup)))))

(defn.xt ^{:substrate/fn true}
  set-supabase-service
  [node service-id driver schema lookup settings]
  (return
   (promise/x:promise-run
    (substrate/set-service service-id
                           (impl-memory/client-memory schema lookup)))))

(impl-supabase/client-supabase)



(!.js
  (impl-sqlite/client-sqlite))


(defn.xt ^{:substrate/fn true}
  db-init
  [node schema lookup
   ]
  (-> (impl-sqlite/client-sqlite
       (@! fixtures/+schema+)
       (@! fixtures/+lookup+)
       nil
       {"filename" ":memory:"})
      (impl-sqlite/client-sqlite-init
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
              (impl-sqlite/pull (main/get-service node "db/caching")
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
