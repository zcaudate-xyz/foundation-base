(ns scaffold.supabase.docker-min
  (:require [std.config :as config]
            [std.lib.os :as os]))

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

(defn start-supabase
  [_]
  (os/sh {:args ["docker-compose" "-f" "docker/supabase-min/docker-compose.yml" "up" "-d"]
          :output-errors true}))

(defn stop-supabase
  [_]
  (os/sh {:args ["docker-compose" "-f" "docker/supabase-min/docker-compose.yml" "stop"]
          :output-errors true})
  (os/sh {:args ["docker-compose" "-f" "docker/supabase-min/docker-compose.yml" "down"]
          :output-errors true}))
