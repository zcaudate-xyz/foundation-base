(ns xt.db.node.adaptor-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.substrate :as substrate]
             [xt.db.text.sql-call :as call]
             [xt.db.system.impl-sqlite :as impl-sqlite]
             [xt.db.system.impl-postgres :as impl-postgres]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.impl-supabase :as impl-supabase]
             [xt.db.system.main :as sys-main]]})

;;
;; The xt.db.node.adaptor-base 
;;

(defn.xt
  set-memory-impl
  [node service-id client schema lookup]
  (substrate/set-service
   service-id
   (impl-memory/impl-memory schema lookup))
  (return (promise/x:promise-run node)))

(defn.xt
  set-sqlite-impl
  [node service-id client schema lookup]
  (return
   (-> (impl-sqlite/impl-sqlite client schema lookup)
       (impl-sqlite/impl-sqlite-init)
       (promise/x:promise-then
        (fn [client]
          (substrate/set-service service-id client)
          (return node))))))

(defn.xt
  set-postgres-impl
  [node service-id client schema lookup]
  (return
   (-> (impl-postgres/impl-postgres client schema lookup)
       (impl-postgres/impl-postgres-init)
       (promise/x:promise-then
        (fn [client]
          (substrate/set-service service-id client)
          (return node))))))

(defn.xt
  set-supabase-impl
  [node service-id client schema lookup]
  (substrate/set-service
   service-id
   (impl-supabase/impl-supabase client schema lookup))
  (return (promise/x:promise-run node)))

(defn.xt ^{:substrate/fn true}
  set-impl
  [node service-id type defaults schema lookup]
  (var client (sys-main/create-client type defaults))
  (cond (== type "memory")
        (return
         (-/set-memory-impl node service-id client schema lookup))

        (== type "sqlite")
        (return
         (-/set-sqlite-impl node service-id client schema lookup))

        (== type "postgres")
        (return
         (-/set-postgres-impl node service-id client schema lookup))

        (== type "supabase")
        (return
         (-/set-supabase-impl node service-id client schema lookup))))


(defn.xt ^{:substrate/fn true}
  init-db
  [node config schema lookup]
  (var #{primary
         caching} config)
  (-> (-/set-impl node
                  "db/primary"
                  (xt/x:get-key primary "type")
                  (xt/x:get-key primary "defaults")
                  schema
                  lookup)
      (promise/x:promise-then
       (fn [out]
         (return
          (-/set-impl node
                      "db/caching"
                      (xt/x:get-key caching "type")
                      (xt/x:get-key caching "defaults")
                      schema
                      lookup))))
      (promise/x:promise-then
       (fn [out]
         (return node)))))



(defn.xt call-primary-handler
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


;;
;;
;;



(defn.xt ^{:substrate/fn true}
  init-handlers
  [node db-map])




(comment
  (impl-supabase/impl-supabase)
  (!.js
    (impl-sqlite/impl-sqlite)))

(comment
  
  (!.js
    (-> (xt.substrate/node-create {})
        (-/init-db {:primary {:type "supabase"
                              :defaults {}}
                    :caching {:type "sqlite"
                              :defaults {}}}
                   schema
                   lookup)
        (-/init-handlers)))
  {"services"
   {"db/common"   {"schema"  {}
                   "lookup"  {}}
    "db/primary"  'client
    "db/caching"  'client}}
  
  )

(comment

  (!.js
    (-> (xt.substrate/node-create {})
        (-/db-init {:primary {}
                    :caching {}})))
  {"services"
   {"db/common"   {"schema"  {}
                   "lookup"  {}}
    "db/primary"  'client
    "db/caching"  'client}}
  
  )
