(ns lang-demos.shared-log.backbone
  (:require [lang.core :as l]
            [lang-demos.shared-log.entries]
            [scaffold.supabase.local-min :as local-min]))

(def +local-supabase-url+
  (str "http://"
       (-> local-min/+config+ :api :hostname)
       ":"
       (-> local-min/+config+ :api :port)))

(def +local-anon-key+
  (-> local-min/+config+ :api :anon-key))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.db.node.client-base :as client-base]
             [xt.db.system.impl-supabase-realtime :as impl-realtime]
             [xt.db.node.runtime :as db-runtime]
             [xt.db.system.main :as db-main]
             [lang-demos.shared-log.entries :as entries]]})

(def.js LocalSupabaseUrl
  (@! lang-demos.shared-log.backbone/+local-supabase-url+))

(def.js LocalAnonKey
  (@! lang-demos.shared-log.backbone/+local-anon-key+))

(defn.js supabase-defaults
  [url anon-key]
  (return {"host" (. url hostname)
           "port" (:? (== "" (. url port))
                      (:? (== "https:" (. url protocol)) 443 80)
                      (Number (. url port)))
           "secured" (== "https:" (. url protocol))
           "basepath" (:? (== "/" (. url pathname))
                          ""
                          (. url pathname))
           "apikey" anon-key
           "token" anon-key}))

(defn.js runtime-config
  [defaults]
  (return {"primary" {"type" "supabase"
                      "defaults" defaults}
           "caching" {"type" "memory"
                      "defaults" {}}}))

(defn.js install-rpc-service
  [node defaults]
  (substrate/set-service
   node
   "auth/supabase"
   (db-main/create-impl "supabase" defaults nil nil))
  (db-runtime/init-server node)
  (return node))

(defn.js live-context
  [node config]
  (return {"node" node
           "runtime" {"config" config
                      "schema" entries/Schema
                      "lookup" entries/SchemaLookup}}))

(defn.js table-impl
  []
  (var query entries/LogQueries)
  (return {"base"
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
