(ns scaffold.supabase.local-min
  (:require [std.config :as config]
            [std.lib.os :as os]
            [std.lib.network :as network]))

(def +config-file+
  "config/scaffold/supabase-min.edn")

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

(defn wait-for-http-ready!
  "waits for the Supabase HTTP endpoint to return a 200 health response"
  [opts]
  (let [{:keys [timeout pause path headers]
         :or {timeout 30000
              pause 1000
              path "/auth/v1/health"
              headers {}}} (or opts {})
        base-url (str (get-in +config+ [:api :protocol] "http")
                      "://"
                      (get-in +config+ [:api :hostname] "127.0.0.1")
                      ":"
                      (get-in +config+ [:api :port] 55121)
                      path)
        started (System/currentTimeMillis)]
    ))

(defn start-supabase
  ([]
   (start-supabase nil))
  ([_opts]
   (let [opts (or _opts {})]
     (os/sh {:args ["supabase" "start" "--workdir" "docker/supabase-min"]
             :output-errors true})
     (when (not (= false (:wait-http opts)))
       (network/wait-for-port
        (get-in +config+ [:api :hostname])
        (get-in +config+ [:api :port]))
       (Thread/sleep 2000))
     true)))

(defn stop-supabase
  [_]
  (os/sh {:args ["supabase" "start" "--workdir" "docker/supabase-min"]
          :output-errors true}))
