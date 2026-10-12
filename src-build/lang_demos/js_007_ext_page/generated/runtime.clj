(ns lang-demos.js-007-ext-page.generated.runtime
  (:require [lang.core :as l]
            [postgres.core :as pg]
            [postgres.gen.bind-macro :as pg-bind]
            [postgres.sample.scratch-v0]
            [scaffold.supabase.local-min :as local-min]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.db.node.client-base :as client-base]
             [xt.db.system.impl-supabase-realtime :as impl-realtime]
             [xt.db.node.runtime :as db-runtime]
             [xt.db.system.main :as db-main]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def.js SupabaseUrl
  (new URL (or (. process env NEXT_PUBLIC_SUPABASE_URL)
               (@! (str "http://"
                        (-> local-min/+config+ :api :hostname)
                        ":"
                        (-> local-min/+config+ :api :port))))))

(def.js AnonKey
  (or (. process env NEXT_PUBLIC_SUPABASE_ANON_KEY)
      (@! (-> local-min/+config+ :api :anon-key))))

(def.js SupabaseDefaults
  {"host" (. -/SupabaseUrl hostname)
   "port" (:? (== "" (. -/SupabaseUrl port))
              (:? (== "https:" (. -/SupabaseUrl protocol)) 443 80)
              (Number (. -/SupabaseUrl port)))
   "secured" (== "https:" (. -/SupabaseUrl protocol))
   "basepath" (:? (== "/" (. -/SupabaseUrl pathname))
                  ""
                  (. -/SupabaseUrl pathname))
   "apikey" -/AnonKey
   "token" -/AnonKey})

(def.js RuntimeConfig
  {"primary" {"type" "supabase"
              "defaults" -/SupabaseDefaults}
   "caching" {"type" "memory"
              "defaults" {}}})

(def.js LogSelectEntry
  (@! (pg-bind/bind-view
       (var-get #'postgres.sample.scratch-v0/log-all))))

(def.js LogReturnEntry
  (@! (pg-bind/bind-view
       (var-get #'postgres.sample.scratch-v0/log-default))))

(def.js LogQueries
  {"list" {"table" "Log"
           "select_entry" -/LogSelectEntry}
   "data" {"table" "Log"
           "return_entry" -/LogReturnEntry}})

(defn.js install-rpc-service
  [node]
  (substrate/set-service
   node
   "auth/supabase"
   (db-main/create-impl "supabase" -/SupabaseDefaults nil nil))
  (db-runtime/init-server node)
  (return node))

(defn.js live-context
  [node]
  (return {"node" node
           "runtime" {"config" -/RuntimeConfig
                      "schema" -/Schema
                      "lookup" -/SchemaLookup}}))

(defn.js table-impl
  []
  (var query -/LogQueries)
  (return
   {"base"
    {"list" {"spec" (. query ["list"])}
     "data" {"spec" (. query ["data"])}}
    "call" {}
    "cached" {}}))

(defn.js subscribe-log
  [node context connection-id on-change]
  (var runtime-config (. context ["runtime"]))
  (var topics ["realtime:scratch_v0:Log"])
  (return
   (-> (client-base/kernel-init
        node
        (. runtime-config ["config"])
        (. runtime-config ["schema"])
        (. runtime-config ["lookup"])
        {})
       (promise/x:promise-then
        (fn [_]
          (var primary (substrate/get-service node "db/primary"))
          (when (not primary)
            (xt/x:err "The shared log database did not initialise."))
          (var callback-id (+ connection-id "/refresh"))
          (impl-realtime/add-realtime-callback
           primary
           connection-id
           callback-id
           (fn [event]
             (when (and (== "realtime:scratch_v0:Log" (. event ["topic"]))
                        (or (xt/x:has-key? event "db/sync")
                            (xt/x:has-key? event "db/remove")))
               (on-change event))))
          (return
           (-> (client-base/subscribe-db
                node "db/primary" connection-id topics {})
               (promise/x:promise-then
                (fn [results]
                  (when (not (== true (. results [0])))
                    (xt/x:err "Supabase Realtime could not subscribe to scratch_v0.Log."))
                  (on-change nil)
                  (var active true)
                  (return
                   (fn []
                     (when active
                       (:= active false)
                       (impl-realtime/remove-realtime-callback
                        primary connection-id callback-id)
                       (return
                        (client-base/unsubscribe-db
                         node "db/primary" connection-id topics {})))))))
               (promise/x:promise-catch
                (fn [error]
                  (impl-realtime/remove-realtime-callback
                   primary connection-id callback-id)
                  (xt/x:err (or (. error message)
                                "Supabase Realtime could not subscribe to scratch_v0.Log.")))))))))))
