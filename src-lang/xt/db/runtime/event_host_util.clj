(ns xt.db.runtime.event-host-util
  (:require [clojure.string :as str]
            [hara.lang :as l]
            [hara.lang.script :as script]
            [hara.runtime.postgres :as pg]
            [postgres.sample.scratch-v1]
            [std.json :as json]
            [std.lib.os :as os])
  (:import (java.net URI)
           (java.net.http HttpClient HttpRequest HttpResponse$BodyHandlers)))

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

(def +public-schema+
  "public")

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

(def +live-realtime-entry-name+
  "copilot_supabase_realtime_live")

(def +live-realtime-entry-tags+
  ["copilot" "supabase" "realtime"])

(def +live-realtime-entry-query+
  ["Entry"
   {"name" +live-realtime-entry-name+}
   ["name"
    "tags"]])

(def +live-supabase-config+
  nil)

(def +postgres-runtime+
  nil)

(def +postgres-module+
  'postgres.sample.scratch-v1)

(defonce +http-client+
  (HttpClient/newHttpClient))

(defn request-event
  "Returns the canonical xt.db event name for a host-side xt.db request."
  {:added "4.1.4"}
  [request]
  (cond (contains? request "db/sync")
        "db/sync"

        (contains? request "db/remove")
        "db/remove"

        :else
        nil))

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
   "if (echo > /dev/tcp/" +postgres-host+ "/" (str +postgres-port+) ") >/dev/null 2>&1; then\n"
   "  exit 0\n"
   "fi\n"
   "start_output=$(" (supabase-shell-command "start") " 2>&1) || start_status=$?\n"
   "if [ -n \"${start_status:-}\" ]; then\n"
   "  if ! printf '%s\\n' \"$start_output\" | grep -q 'already running'; then\n"
   "    printf '%s\\n' \"$start_output\" >&2\n"
   "    exit \"$start_status\"\n"
   "  fi\n"
   "fi\n"
   "for i in $(seq 1 120); do\n"
   "  if (echo > /dev/tcp/" +postgres-host+ "/" (str +postgres-port+) ") >/dev/null 2>&1; then\n"
   "    exit 0\n"
   "  fi\n"
   "  sleep 1\n"
   "done\n"
   "printf '%s\\n' \"$start_output\" >&2\n"
   "echo 'Timed out waiting for postgres on " +postgres-host+ ":" (str +postgres-port+) "' >&2\n"
   "exit 1"))

(defn init-live-postgres-runtime!
  []
  (let [rt (script/script-test
            :postgres
            {:runtime :jdbc.client
            :require '[[postgres.sample.scratch-v1 :as scratch]
                       [postgres.sample.scratch-v0 :as scratch-v0]]
            :config {:host +postgres-host+
                      :port +postgres-port+
                      :user "postgres"
                      :pass "postgres"
                      :dbname "postgres"
                      :startup {:args [+shell+ "-lc" (startup-shell-command)]
                                :root "docker"
                                :ignore-errors false}
                      :teardown {:args [+shell+ "-lc" "true"]
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

(defn http-get
  [url headers]
  (let [builder (HttpRequest/newBuilder (URI/create url))]
    (doseq [[k v] headers]
      (.header builder k v))
    (let [request (.build (.GET builder))
          response (.send +http-client+
                         request
                         (HttpResponse$BodyHandlers/ofString))]
      {:status (.statusCode response)
       :body (.body response)})))

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

(defn ensure-scratch-entry-table!
  []
  (pg-exec!
   (str "CREATE SCHEMA IF NOT EXISTS \"" +scratch-schema+ "\";\n"
        "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";\n"
        "CREATE TABLE IF NOT EXISTS \"" +scratch-schema+ "\".\"" +scratch-entry-table+ "\" (\n"
        "  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),\n"
        "  name text NOT NULL UNIQUE,\n"
        "  tags jsonb NOT NULL DEFAULT '[]'::jsonb,\n"
        "  op_created uuid,\n"
        "  op_updated uuid,\n"
        "  time_created bigint,\n"
        "  time_updated bigint,\n"
        "  __deleted__ boolean NOT NULL DEFAULT false\n"
        ");")))

(defn grant-scratch-schema!
  []
  (ensure-scratch-entry-table!)
  (doseq [sql [(str "GRANT USAGE ON SCHEMA \"" +scratch-schema+
                    "\" TO anon, authenticated, service_role")
               (str "GRANT ALL ON ALL TABLES IN SCHEMA \"" +scratch-schema+
                    "\" TO anon, authenticated, service_role")
               (str "ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA \""
                    +scratch-schema+
                    "\" GRANT ALL ON TABLES TO anon, authenticated, service_role")]]
    (pg-exec-best-effort! sql)))

(defn enable-scratch-entry-realtime!
  []
  (pg-exec!
   (str "DO $$\n"
        "BEGIN\n"
        "  IF NOT EXISTS (\n"
        "    SELECT 1\n"
        "    FROM pg_publication_tables\n"
        "    WHERE pubname = 'supabase_realtime'\n"
        "      AND schemaname = '" +scratch-schema+ "'\n"
        "      AND tablename = '" +scratch-entry-table+ "'\n"
        "  ) THEN\n"
        "    EXECUTE 'ALTER PUBLICATION supabase_realtime ADD TABLE \"" +scratch-schema+ "\".\"" +scratch-entry-table+ "\"';\n"
        "  END IF;\n"
        "END $$;")))

(declare wait-postgrest-schema!)

(defn reload-postgrest!
  ([] (reload-postgrest! +scratch-schema+ +scratch-entry-table+))
  ([schema-name]
   (reload-postgrest! schema-name +scratch-entry-table+))
  ([schema-name table-name]
   (pg-exec-best-effort! "NOTIFY pgrst, 'reload schema'")
   (wait-postgrest-schema! schema-name table-name)))

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

(defn ensure-public-entry-table!
  []
  (pg-exec!
   (str "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";\n"
        "CREATE TABLE IF NOT EXISTS \"" +public-schema+ "\".\"" +scratch-entry-table+ "\" (\n"
        "  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),\n"
        "  name text NOT NULL UNIQUE,\n"
        "  tags jsonb NOT NULL DEFAULT '[]'::jsonb\n"
        ");\n"
        "GRANT ALL ON TABLE \"" +public-schema+ "\".\"" +scratch-entry-table+ "\" TO anon, authenticated, service_role;")))

(defn enable-public-entry-realtime!
  []
  (pg-exec!
   (str "DO $$\n"
        "BEGIN\n"
        "  IF NOT EXISTS (\n"
        "    SELECT 1\n"
        "    FROM pg_publication_tables\n"
        "    WHERE pubname = 'supabase_realtime'\n"
        "      AND schemaname = '" +public-schema+ "'\n"
        "      AND tablename = '" +scratch-entry-table+ "'\n"
        "  ) THEN\n"
        "    EXECUTE 'ALTER PUBLICATION supabase_realtime ADD TABLE \"" +public-schema+ "\".\"" +scratch-entry-table+ "\"';\n"
        "  END IF;\n"
        "END $$;")))

(defn wait-postgrest-schema!
  ([schema-name]
   (wait-postgrest-schema! schema-name +scratch-entry-table+))
  ([schema-name table-name]
   (let [status (parse-shell-env (supabase-status-env))
         base-url (get status "API_URL")
         api-key (get status "SERVICE_ROLE_KEY")
         headers {"apikey" api-key
                  "Authorization" (str "Bearer " api-key)
                  "Accept-Profile" schema-name
                  "Content-Profile" schema-name}
         url (str base-url "/rest/v1/" table-name "?select=*&limit=1")]
     (loop [attempt 0]
       (let [response (try
                        (http-get url headers)
                        (catch Throwable _
                          nil))
             ready? (and response
                         (== 200 (:status response)))]
         (cond ready?
               true

               (< attempt 29)
               (do (Thread/sleep 1000)
                   (recur (inc attempt)))

               :else
               (throw (ex-info "Timed out waiting for PostgREST schema cache"
                               {:schema schema-name
                                :table table-name
                                :response response}))))))))

(defn cleanup-public-entry!
  [name]
  (pg-exec-best-effort!
   (str "DELETE FROM \"" +public-schema+ "\".\"" +scratch-entry-table+
        "\" WHERE name = " (sql-literal name))))

(defn setup-public-entry!
  [name tags]
  (cleanup-public-entry! name)
  (pg-exec!
   (str "INSERT INTO \"" +public-schema+ "\".\"" +scratch-entry-table+
        "\" (name, tags)"
        " VALUES (" (sql-literal name)
        ", '" (str/replace (json/write tags) "'" "''") "'::jsonb)"))
  (Thread/sleep 200))

(defn send-realtime-broadcast!
  [topic event payload]
  (pg-exec!
   (str "SELECT realtime.send("
        (sql-literal (json/write payload))
        "::jsonb, "
        (sql-literal event)
        ", "
        (sql-literal topic)
        ", false);")))

(defn send-realtime-request!
  [topic request]
  (let [event (request-event request)]
    (when-not event
      (throw (ex-info "Unsupported xt.db realtime request"
                      {:request request})))
    (send-realtime-broadcast! topic event request)))
