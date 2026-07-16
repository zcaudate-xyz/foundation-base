^{:no-test true}
(ns scaffold.supabase.local-min
  (:require [std.config :as config]
            [std.lib.os :as os]
            [std.lib.network :as network]
            [std.lib.env :as env]
            [net.http :as net.http]))

(def +config-file+
  "config/scaffold/supabase-local.edn")

(def +config+
  (config/load +config-file+))

(def +config-supabase-anon+
  {:host  (-> +config+ :api :hostname)
   :port (-> +config+ :api :port)
   :secured false
   :basepath ""
   :apikey (-> +config+ :api :anon-key)})

(def +config-supabase-service+
  {:host  (-> +config+ :api :hostname)
   :port (-> +config+ :api :port)
   :secured false
   :basepath ""
   :apikey (-> +config+ :api :service-key)})

(defn supabase-available?
  "Returns true if the supabase CLI is installed."
  []
  (env/program-exists? "supabase"))

(defn- http-get
  "Performs a synchronous GET and returns {:status ... :body ...}."
  [url headers]
  (try (net.http/get url {:headers headers
                          :timeout 1000})
       (catch Throwable t
         {:status 0
          :body (str t)})))

(defn wait-for-auth-ready
  "Polls the GoTrue auth endpoint until it returns HTTP 200."
  ([]
   (wait-for-auth-ready 60000))
  ([timeout-ms]
   (let [{:keys [host port apikey]} +config-supabase-anon+
         url (str "http://" host ":" port "/auth/v1/health")
         deadline (+ (System/currentTimeMillis) timeout-ms)]
     (loop []
       (let [{:keys [status body]} (http-get url {"apikey" apikey})]
         (if (== status 200)
           true
           (if (>= (System/currentTimeMillis) deadline)
             (throw (ex-info "Auth service not ready"
                             {:status status :body body}))
             (do (Thread/sleep 500)
                 (recur)))))))))

(defn wait-for-postgrest-ready
  "Polls PostgREST for a specific schema/table until it returns HTTP 200.
   Defaults to scratch_v0.Log to match the existing poc tests."
  ([]
   (wait-for-postgrest-ready "scratch_v0" "Log"))
  ([schema table]
   (wait-for-postgrest-ready schema table 60000))
  ([schema table timeout-ms]
   (let [{:keys [host port apikey]} +config-supabase-anon+
         url (str "http://" host ":" port "/rest/v1/" table "?select=*&limit=1")
         deadline (+ (System/currentTimeMillis) timeout-ms)]
     (loop []
       (let [{:keys [status body]}
             (http-get url {"apikey" apikey
                            "Accept-Profile" schema
                            "Content-Profile" schema})]
         (if (== status 200)
           true
           (if (>= (System/currentTimeMillis) deadline)
             (throw (ex-info "PostgREST schema cache not ready"
                             {:schema schema :table table :status status :body body}))
             (do (Thread/sleep 500)
                 (recur)))))))))

(defn refresh-postgrest-schema
  "Tells PostgREST to reload its schema cache and waits until the probe
   endpoint returns HTTP 200."
  ([]
   (refresh-postgrest-schema "scratch_v0" "Log"))
  ([schema table]
   (let [{:keys [host port user password database]} (:db +config+)]
     (let [process (os/sh {:args ["psql"
                                  (str "postgresql://" user ":" password "@" host ":" port "/" database)
                                  "-v" "ON_ERROR_STOP=1"
                                  "-c" "NOTIFY pgrst, 'reload schema';"]
                           :wait true
                           :output false})
           {:keys [exit out err]} (os/sh-output process)]
       (when (not= exit 0)
         (throw (ex-info "PostgREST schema reload failed"
                         {:exit exit :out out :err err}))))
     (wait-for-postgrest-ready schema table))))

(defn start-supabase
  ([]
   (start-supabase nil))
  ([_opts]
   (let [opts (or _opts {})
         _ (println "[supabase] starting local-min...")
         process (os/sh {:args ["supabase" "start" "--workdir" "docker/local-min"]
                         :wait true
                         :output false})
         {:keys [exit out err] :as start} (os/sh-output process)
         api {:host (get-in +config+ [:api :hostname])
              :port (get-in +config+ [:api :port])}]
     (when (not= exit 0)
       (println "[supabase] start failed (exit" exit ")")
       (when (seq out) (println out))
       (when (seq err) (println err))
       (throw (ex-info "supabase start failed"
                       {:exit exit :out out :err err})))
     (println "[supabase] start command completed")
     (when (not (= false (:wait-http opts)))
       (try
         (network/wait-for-port (:host api) (:port api) {:timeout 60000})
         (println "[supabase] waiting for auth service...")
         (wait-for-auth-ready)
         (println "[supabase] kong/api ready")
         (catch Throwable t
           (throw (ex-info "Supabase local-min API did not become ready"
                           {:api api
                            :start start
                            :hint "Run `supabase status --workdir docker/local-min`; Kong/API must listen on the configured port."}
                           t)))))
     true)))

(defn restart-postgrest
  "Restarts only the supabase_rest_local-min Docker container and waits for the
   PostgREST API port to become available again."
  []
  (os/sh {:args ["docker" "restart" "supabase_rest_local-min"]
          :output-errors true})
  (network/wait-for-port
   (get-in +config+ [:api :hostname])
   (get-in +config+ [:api :port])
   {:timeout 60000})
  (Thread/sleep 2000)
  true)

(defn stop-supabase
  [_])

(defn shutdown-supabase
  [_]
  (println "[supabase] stopping local-min...")
  (let [process (os/sh {:args ["supabase" "stop" "--workdir" "docker/local-min" "--no-backup"]
                        :wait true
                        :output false})
        {:keys [exit out err]} (os/sh-output process)]
    (when (not= exit 0)
      (println "[supabase] stop failed (exit" exit ")")
      (when (seq out) (println out))
      (when (seq err) (println err))))
  (println "[supabase] stop command completed"))
