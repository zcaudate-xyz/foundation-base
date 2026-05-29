(ns demo-xtdb-backbone.app.config
  (:require [std.config :as config]))

(def +config-file+
  "src-build/demo_xtdb_backbone/config.edn")

(defn load-config
  []
  (config/load +config-file+))

(def +supabase-config+
  (let [{:keys [supabase]} (load-config)]
    {"protocol" (:protocol supabase)
     "hostname" (:hostname supabase)
     "port" (:port supabase)
     "base_url" (:base_url supabase)
     "schema_name" (:schema_name supabase)
     "api_key" (:api_key supabase)
     "auth_token" (:auth_token supabase)}))

(defn supabase-config
  []
  +supabase-config+)

(defn supabase-base-url
  []
  (or (get +supabase-config+ "base_url")
      (str (or (get +supabase-config+ "protocol")
               "http")
           "://"
           (or (get +supabase-config+ "hostname")
               "127.0.0.1")
           ":"
           (or (get +supabase-config+ "port")
               55121))))

(defn supabase-api-key
  []
  (get +supabase-config+ "api_key"))

(defn supabase-auth-token
  []
  (or (get +supabase-config+ "auth_token")
      (get +supabase-config+ "api_key")))
