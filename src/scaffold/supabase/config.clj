(ns scaffold.supabase.config
  (:require [std.config :as config]))

(def +config-file+
  "config/scaffold/supabase.edn")

(defn load-config
  []
  (config/load +config-file+))

(defn supabase-config
  []
  (:supabase (load-config)))

(defn db-config
  []
  (:db (supabase-config)))

(defn api-config
  []
  (:api (supabase-config)))

(defn shell
  []
  (:shell (supabase-config)))

(defn setup-config
  []
  (:setup (supabase-config)))

(defn setup-type
  []
  (:type (setup-config)))

(defn cli-command
  []
  (:cli_command (setup-config)))

(defn compose-file
  []
  (:compose_file (setup-config)))

(defn env-file
  []
  (:env_file (setup-config)))

(defn project-name
  []
  (:project_name (setup-config)))

(defn startup-command
  []
  (:startup_command (setup-config)))

(defn status-command
  []
  (:status_command (setup-config)))

(defn cli-root
  []
  (:root (setup-config)))

(defn config-path
  []
  (:config_path (setup-config)))

(defn postgres-host
  []
  (:host (db-config)))

(defn postgres-port
  []
  (:port (db-config)))

(defn postgres-user
  []
  (:user (db-config)))

(defn postgres-password
  []
  (:password (db-config)))

(defn postgres-database
  []
  (:database (db-config)))

(defn api-base-url
  []
  (:base_url (api-config)))

(defn resolved-api-base-url
  []
  (let [{:keys [base_url protocol hostname port]} (api-config)]
    (or base_url
        (str (or protocol "http")
             "://"
             (or hostname "127.0.0.1")
             ":"
             (or port 55121)))))

(defn anon-key
  []
  (:anon_key (api-config)))

(defn service-key
  []
  (:service_key (api-config)))
