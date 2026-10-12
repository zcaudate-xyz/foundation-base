(ns lang-demos.js-009-log-sharedworker.generated.runtime
  (:require [lang.core :as l]
            [lang-demos.shared-log.backbone]
            [lang-demos.shared-log.entries]))

(l/script :js
  {:require [[xt.substrate :as substrate]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.runtime :as db-runtime]
             [lang-demos.shared-log.backbone :as backbone]
             [lang-demos.shared-log.entries :as entries]]})

(def.js SupabaseUrl
  (new URL (or (. process env NEXT_PUBLIC_SUPABASE_URL)
               backbone/LocalSupabaseUrl)))

(def.js AnonKey
  (or (. process env NEXT_PUBLIC_SUPABASE_ANON_KEY)
      backbone/LocalAnonKey))

(def.js SupabaseDefaults
  (backbone/supabase-defaults -/SupabaseUrl -/AnonKey))

(def.js RuntimeConfig
  (backbone/runtime-config -/SupabaseDefaults))

(defn.js sharedworker-source
  []
  (return
   (browser-transport/sharedworker-url-source
    "/workers/lang-demos-js-009-log-sharedworker.js"
    {"type" "classic"})))

(defn.js connect-client
  [client]
  (var connected
       (db-runtime/sharedworker-connect-transport
        client
        (-/sharedworker-source)
        "log-sharedworker-transport"))
  (return
   (. connected
      (then
       (fn [state]
         (var connection (. state ["connection"]))
         (return
          (. (substrate/request
              client
              nil
              "@xt.db/kernel-init"
              [-/RuntimeConfig entries/Schema entries/SchemaLookup]
              {"transport_id" (. connection ["transport_id"])})
             (then
              (fn [init]
                (return {"connection" connection
                         "init" init}))))))))))

(defn.js disconnect-client
  [state]
  (return (db-runtime/sharedworker-disconnect state)))

(defn.js live-context
  [node]
  (return {"node" node}))

(defn.js table-impl
  []
  (return (backbone/table-impl)))
