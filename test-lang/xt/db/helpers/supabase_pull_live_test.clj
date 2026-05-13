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

(def +shell+
  "/bin/bash")

(def +supabase-config-path+
  "docker/supabase/config.toml")

(def +postgres-host+
  "127.0.0.1")

(def +postgres-port+
  55122)

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

(def +postgres-runtime+
  nil)

(def +postgres-module+
  'postgres.sample.scratch-v1)

(defn shell-program-exists?
  [program]
  (let [out @(os/sh {:args [+shell+ "-lc"
                            (str "command -v " program " >/dev/null 2>&1 && echo ok || true")]
                     :inherit false})]
    (= "ok" out)))

(defn shell-command
  [& parts]
  (str/join " " parts))

(def +supabase-command+
  ["npx" "supabase"])

(defn supabase-shell-command
  [& parts]
  (apply shell-command (concat +supabase-command+ parts)))

(defn startup-shell-command
  []
  (str
   "set -e\n"
   (supabase-shell-command "stop" "--no-backup" "--yes") " >/dev/null 2>&1 || true\n"
   (supabase-shell-command "start") "\n"
   "for i in $(seq 1 120); do\n"
   "  if (echo > /dev/tcp/" +postgres-host+ "/" (str +postgres-port+) ") >/dev/null 2>&1; then\n"
   "    exit 0\n"
   "  fi\n"
   "  sleep 1\n"
   "done\n"
   "echo 'Timed out waiting for postgres on " +postgres-host+ ":" (str +postgres-port+) "' >&2\n"
   "exit 1"))

(def CANARY-SUPABASE-LIVE
  (and (shell-program-exists? "npx")
       (shell-program-exists? "docker")
       (.exists (java.io.File. ^String +supabase-config-path+))))

(defn init-live-postgres-runtime!
  []
  (let [rt (script/script-test
            :postgres
            {:runtime :jdbc.client
             :require '[[postgres.sample.scratch-v1 :as scratch]]
             :config {:host +postgres-host+
                      :port +postgres-port+
                      :user "postgres"
                      :pass "postgres"
                      :dbname "postgres"
                      :startup {:args [+shell+ "-lc" (startup-shell-command)]
                                :root "docker"
                                :ignore-errors false}
                      :teardown {:args [+shell+ "-lc" (supabase-shell-command "stop" "--no-backup" "--yes")]
                                 :root "docker"
                                 :ignore-errors true}}})]
    (alter-var-root #'+postgres-runtime+ (constantly rt))
    rt))

(defn pg-rt
  []
  +postgres-runtime+)

(defn supabase-status-env
  []
  @(os/sh {:args [+shell+ "-lc" (supabase-shell-command "status" "-o" "env")]
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

(defn reload-postgrest!
  []
  (pg-exec-best-effort! "NOTIFY pgrst, 'reload schema'"))

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
