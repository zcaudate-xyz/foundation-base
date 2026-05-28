(ns lib.supabase.support
  (:require [hara.lang :as l]
            [lib.supabase :as supabase]
            [std.lib.os :as os]
            [xt.db.runtime.event-host-util :as live])
  (:import (java.util UUID)))

(def +scratch-v0-module+
  'postgres.sample.scratch-v0)

(def +scratch-v0-schema+
  "scratch_v0")

(def +log-table-name+
  "Log")

(def +log-table+
  (atom {:id 'Log
         :static/schema +scratch-v0-schema+}))

(def +ping-fn+
  (atom {:id 'ping
         :static/schema +scratch-v0-schema+}))

(def +log-append-fn+
  (atom {:id 'log-append
         :static/schema +scratch-v0-schema+}))

(declare clear-log!)

(defn- port-open?
  [host port]
  (try
    (with-open [socket (java.net.Socket. host (int port))]
      true)
    (catch Throwable _
      false)))

(defn start!
  []
  (when-not (port-open? live/+postgres-host+ live/+postgres-port+)
    @(os/sh {:args [live/+shell+ "-lc" (live/supabase-shell-command "start" "-x" "logflare" "--ignore-health-check")]
             :root live/+supabase-cli-root+
             :ignore-errors false}))
  (l/rt:restart)
  (live/init-live-postgres-runtime!)
  (l/rt:setup (live/pg-rt) live/+postgres-module+)
  (l/rt:setup (live/pg-rt) +scratch-v0-module+)
  (live/grant-scratch-schema!)
  (live/ensure-scratch-entry-table!)
  (live/pg-exec-best-effort!
   (str "GRANT USAGE ON SCHEMA \"" +scratch-v0-schema+ "\" TO anon, authenticated, service_role"))
  (live/reload-postgrest!)
  (live/reload-postgrest! +scratch-v0-schema+ +log-table-name+)
  (live/refresh-live-supabase-config!)
  (live/pg-exec!
   (str "CREATE OR REPLACE FUNCTION \"" live/+scratch-schema+ "\".\"echo_name\"(input text)\n"
        "RETURNS TABLE(name text)\n"
        "LANGUAGE sql\n"
        "STABLE\n"
        "AS $$\n"
        "  SELECT input::text AS name;\n"
        "$$;\n"
        "GRANT EXECUTE ON FUNCTION \"" live/+scratch-schema+ "\".\"echo_name\"(text) "
        "TO anon, authenticated, service_role;"))
  (live/reload-postgrest!)
  true)

(defn stop!
  []
  (try
    (l/rt:teardown (live/pg-rt) +scratch-v0-module+)
    (catch Throwable _))
  (try
    (l/rt:teardown (live/pg-rt) live/+postgres-module+)
    (catch Throwable _))
  (alter-var-root #'live/+postgres-runtime+ (constantly nil))
  (alter-var-root #'live/+live-supabase-config+ (constantly nil))
  (l/rt:stop)
  true)

(defn base-url
  []
  (get-in (live/refresh-live-supabase-config!) ["client" "base_url"]))

(defn service-key
  []
  (get-in (live/refresh-live-supabase-config!) ["client" "api_key"]))

(defn anon-key
  []
  (let [status (live/parse-shell-env (live/supabase-status-env))]
    (get status "ANON_KEY")))

(defn service-opts
  []
  {:host (base-url)
   :key (service-key)
   :type :service})

(defn anon-opts
  []
  {:host (base-url)
   :key (anon-key)
   :type :anon})

(defn auth-opts
  [access-token]
  (assoc (anon-opts) :auth access-token))

(defn service-client
  []
  (supabase/create-client (base-url)
                          (service-key)
                          {:schema_name live/+scratch-schema+
                           :auth_token (service-key)}))

(defn anon-client
  []
  (supabase/create-client (base-url)
                          (anon-key)
                          {:schema_name live/+scratch-schema+}))

(defn random-email
  []
  (str "copilot+" (UUID/randomUUID) "@example.com"))

(defn random-log-message
  []
  (str "copilot-log-" (UUID/randomUUID)))

(defn create-auth-user!
  ([] (create-auth-user! (random-email) "pass123456"))
  ([email password]
   (let [created (supabase/api-signup-create {"email" email
                                              "password" password
                                              "email_confirm" true}
                                             (service-opts))
         uid (or (get-in created [:body "user" "id"])
                 (get-in created [:body "id"]))]
     {:created created
      :uid uid
      :email email
      :password password})))

(defn sign-in-user!
  [{:keys [email password]}]
  (supabase/api-signin {"email" email
                        "password" password}
                       (anon-opts)))

(defn create-auth-session!
  ([] (create-auth-session! (random-email) "pass123456"))
  ([email password]
   (let [user (create-auth-user! email password)
         signed-in (sign-in-user! user)]
     (assoc user
            :signed-in signed-in
            :access-token (get-in signed-in [:body "access_token"])))))

(defn delete-auth-user!
  [uid]
  (when uid
    (supabase/api-signup-delete uid (service-opts)))
  true)

(defn seed-entry!
  [name tags]
  (live/setup-scratch-entry! name tags)
  true)

(defn clear-entry!
  [name]
  (live/cleanup-scratch-entry! name)
  true)

(defn seed-log!
  [message]
  (clear-log! message)
  (live/pg-exec!
   (str "INSERT INTO \"" +scratch-v0-schema+ "\".\"" +log-table-name+ "\" (message)"
        " VALUES (" (live/sql-literal message) ")"))
  (Thread/sleep 200)
  true)

(defn clear-log!
  [message]
  (live/pg-exec-best-effort!
   (str "DELETE FROM \"" +scratch-v0-schema+ "\".\"" +log-table-name+ "\""
        " WHERE message = " (live/sql-literal message)))
  true)

(defn list-logs
  [& [opts]]
  (:body (supabase/api-select-all +log-table+ (merge (anon-opts) opts))))
