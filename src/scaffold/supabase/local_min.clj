(ns scaffold.supabase.local-min
  (:require [std.config :as config]
            [std.lib.os :as os]
            [std.lib.network :as network]))

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

(defn start-supabase
  ([]
   (start-supabase nil))
  ([_opts]
   (let [opts (or _opts {})]
     (os/sh {:args ["supabase" "start" "--workdir" "docker/local-min"]
             :output-errors true})
     (when (not (= false (:wait-http opts)))
       (network/wait-for-port
        (get-in +config+ [:api :hostname])
        (get-in +config+ [:api :port])
        {:timeout 60000})
       (Thread/sleep 2000))
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
  (os/sh {:args ["supabase" "stop" "--workdir" "docker/local-min" "--no-backup"]
          :output-errors true}))


