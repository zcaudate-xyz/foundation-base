(ns xt.db.helpers.supabase-pull-live-test
  (:require [clojure.string :as str]
            [hara.lang :as l]
            [hara.lang.script :as script]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.postgres :as pg]
            [postgres.sample.scratch-v1]
            [std.json :as json]
            [std.lib.os :as os]))

(def +supabase-cli-root+
  "docker")

(def +supabase-config-path+
  "docker/supabase/config.toml")

(def +scratch-schema+
  "scratch")

(def +scratch-entry-table+
  "Entry")

(def +live-entry-name+
  "copilot_supabase_pull_live")

(def +live-entry-tags+
  ["copilot" "supabase" "pull"])

(def +live-entry-query+
  ["Entry"
   {"name" +live-entry-name+}
   ["name"
    "tags"]])

(def +live-supabase-config+
  nil)

(def CANARY-SUPABASE-LIVE
  (and (common/program-exists? "supabase")
       (common/program-exists? "docker")
       (.exists (java.io.File. +supabase-config-path+))))

(defn init-live-postgres-runtime!
  []
  (script/script-test
   :postgres
   {:runtime :jdbc.client
    :require '[[postgres.sample.scratch-v1 :as scratch]]
    :config {:host "127.0.0.1"
             :port 55122
             :user "postgres"
             :pass "postgres"
             :dbname "postgres"
             :startup {:args ["supabase" "start"]
                       :root "docker"
                       :ignore-errors true}
             :teardown {:args ["supabase" "stop"]
                        :root "docker"
                        :ignore-errors true}}}))

(defn pg-rt
  []
  (l/rt:space :postgres))

(defn supabase-status-env
  []
  @(os/sh {:args ["supabase" "status" "-o" "env"]
           :root +supabase-cli-root+}))

(defn parse-shell-env
  [s]
  (reduce (fn [m line]
            (if (str/blank? line)
              m
              (let [[k v] (str/split line #"=" 2)]
                (assoc m k (str/replace (or v "") #"^\"|\"$" "")))))
          {}
          (str/split-lines s)))

(defn refresh-live-supabase-config!
  []
  (let [status (parse-shell-env (supabase-status-env))
        config {"::" "db.supabase"
                "client" {"base_url" (get status "API_URL")
                          "schema_name" +scratch-schema+
                          "api_key" (get status "SERVICE_ROLE_KEY")
                          "auth_token" (get status "SERVICE_ROLE_KEY")}}]
    (alter-var-root #'+live-supabase-config+ (constantly config))
    config))

(defn pg-exec!
  [sql]
  (pg/raw-eval (pg-rt) sql))

(defn pg-exec-best-effort!
  [sql]
  (try
    (pg-exec! sql)
    (catch Throwable _
      nil)))

(defn sql-literal
  [s]
  (str "'" (str/replace s "'" "''") "'"))

(defn grant-scratch-schema!
  []
  (doseq [sql [(str "GRANT USAGE ON SCHEMA \"" +scratch-schema+
                    "\" TO anon, authenticated, service_role")
               (str "GRANT ALL ON ALL TABLES IN SCHEMA \"" +scratch-schema+
                    "\" TO anon, authenticated, service_role")
               (str "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \""
                    +scratch-schema+
                    "\" GRANT ALL ON TABLES TO anon, authenticated, service_role")]]
    (pg-exec-best-effort! sql)))

(defn cleanup-scratch-entry!
  [name]
  (pg-exec-best-effort!
   (str "DELETE FROM \"" +scratch-schema+ "\".\"" +scratch-entry-table+
        "\" WHERE name = " (sql-literal name))))

(defn setup-scratch-entry!
  [name tags]
  (cleanup-scratch-entry! name)
  (pg-exec!
   (str "INSERT INTO \"" +scratch-schema+ "\".\"" +scratch-entry-table+
        "\" (name, tags)"
        " VALUES (" (sql-literal name)
        ", '" (str/replace (json/write tags) "'" "''") "'::jsonb)"))
  (Thread/sleep 200))
