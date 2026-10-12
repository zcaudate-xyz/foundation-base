(ns lang-demos.js-007-ext-page.generated.runtime
  (:require [lang.core :as l]
            [lang-demos.shared-log.backbone]))

(l/script :js
  {:require [[lang-demos.shared-log.backbone :as backbone]]})

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

(defn.js install-rpc-service
  [node]
  (return (backbone/install-rpc-service node -/SupabaseDefaults)))

(defn.js live-context
  [node]
  (return (backbone/live-context node -/RuntimeConfig)))

(defn.js table-impl
  []
  (return (backbone/table-impl)))

(defn.js subscribe-log
  [node context connection-id on-change]
  (return (backbone/subscribe-log node context connection-id on-change)))
