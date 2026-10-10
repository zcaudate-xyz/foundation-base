(ns lang-demos.js-007-ext-page.generated.runtime
  (:require [lang.core :as l]
            [postgres.core :as pg]
            [postgres.gen.bind-macro :as pg-bind]
            [postgres.sample.scratch-v0]
            [scaffold.supabase.local-min :as local-min]))

(l/script :js
  {:require [[xt.substrate :as substrate]
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
