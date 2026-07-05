^{:no-test true}
(ns scaffold.supabase.local-min-support
  {:added "4.1"}
  (:require [clojure.string :as str]
            [lib.supabase.common :as common]
            [scaffold.supabase.local-min :as local-min]
            [std.json :as json]))

(defn base-url
  "Returns the base URL for the local-min Supabase stack."
  {:added "4.1"}
  []
  (or (System/getenv "SUPABASE_LOCAL_MIN_URL")
      "http://localhost:8000"))

(defn service-opts
  "Returns service_role API options for local-min."
  {:added "4.1"}
  []
  {:host (base-url)
   :key  (or (System/getenv "SUPABASE_SERVICE_KEY")
             "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoic2VydmljZV9yb2xlIn0.M5dJKSDhAfzClaWKQMzBpK-sQsVfwIeRafvza1TfiLI")})

(defn anon-opts
  "Returns anon API options for local-min."
  {:added "4.1"}
  []
  {:host (base-url)
   :key  (or (System/getenv "SUPABASE_ANON_KEY")
             "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiJ9.M5dJKSDhAfzClaWKQMzBpK-sQsVfwIeRafvza1TfiLI")})

(defn auth-opts
  "Returns authenticated API options for local-min."
  {:added "4.1"}
  [access-token]
  (merge (anon-opts)
         {:token access-token}))

(defn start!
  "Starts the local-min Supabase stack if available."
  {:added "4.1"}
  []
  (try
    (local-min/start-supabase)
    (catch Throwable _
      nil)))

(defn stop!
  "Stops the local-min Supabase stack if available."
  {:added "4.1"}
  []
  (try
    (local-min/shutdown-supabase nil)
    (catch Throwable _
      nil)))

(defn random-email
  "Generates a random test email."
  {:added "4.1"}
  []
  (str "test-" (java.util.UUID/randomUUID) "@example.com"))

(defn random-log-message
  "Generates a random log message."
  {:added "4.1"}
  []
  (str "msg-" (java.util.UUID/randomUUID)))

(def ^:const +scratch-v0-schema+
  "scratch_v0")

(def ^:const +log-table+
  "scratch_v0.Log")

(def ^:const +ping-fn+
  "scratch_v0.ping")

(def ^:const +log-append-fn+
  "scratch_v0.log_append")

(defn log-message
  "Extracts the message from a log row."
  {:added "4.1"}
  [row]
  (get row "message"))

(defn seed-log!
  "Seeds a log message via direct Postgres (placeholder)."
  {:added "4.1"}
  [message]
  nil)

(defn clear-log!
  "Clears log messages via direct Postgres (placeholder)."
  {:added "4.1"}
  ([message]
   nil)
  ([]
   nil))

(defn list-logs
  "Lists log rows via direct Postgres (placeholder)."
  {:added "4.1"}
  []
  [])

(defn create-auth-user!
  "Creates an auth user via Supabase Auth API (live only)."
  {:added "4.1"}
  []
  (throw (ex-info "Live Supabase Auth API not available in this environment"
                  {:reason :live-api-disabled})))

(defn create-auth-session!
  "Creates an authenticated session via Supabase Auth API (live only)."
  {:added "4.1"}
  []
  (throw (ex-info "Live Supabase Auth API not available in this environment"
                  {:reason :live-api-disabled})))

(defn delete-auth-user!
  "Deletes an auth user via Supabase Auth API (live only)."
  {:added "4.1"}
  [uid]
  (throw (ex-info "Live Supabase Auth API not available in this environment"
                  {:reason :live-api-disabled
                   :uid uid})))
