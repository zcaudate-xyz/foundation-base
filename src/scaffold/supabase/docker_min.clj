(ns scaffold.supabase.docker-min
  (:require [std.config :as config]
            [std.lib.os :as os]
            [scaffold.supabase.event-host-util :as event-host-util]))

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
    (loop [attempt 0]
      (let [response (try
                       (event-host-util/http-get base-url headers)
                       (catch Throwable _
                         nil))
            ready? (and response
                        (= 200 (:status response)))]
        (cond ready?
              response

              (or (nil? timeout)
                  (< (- (System/currentTimeMillis) started) timeout))
              (do (Thread/sleep (long pause))
                  (recur (inc attempt)))

              :else
              (throw (ex-info "Timed out waiting for Supabase HTTP readiness"
                              {:url base-url
                               :attempts attempt
                               :response response})))))))

(defn start-supabase
  ([]
   (start-supabase nil))
  ([_opts]
   (let [opts (or _opts {})]
     (os/sh {:args ["docker-compose" "-f" "docker/supabase-min/docker-compose.yml" "up" "-d"]
             :output-errors true})
     (when (not (= false (:wait-http opts)))
       (wait-for-http-ready! (select-keys opts [:timeout :pause :path :headers])))
     true)))

(defn stop-supabase
  [_]
  (os/sh {:args ["docker-compose" "-f" "docker/supabase-min/docker-compose.yml" "stop"]
          :output-errors true})
  (os/sh {:args ["docker-compose" "-f" "docker/supabase-min/docker-compose.yml" "down"]
          :output-errors true}))
