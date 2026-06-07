(ns scaffold.supabase.docker-min
  (:require [std.config :as config]))

(def +config-file+
  "config/scaffold/supabase-min.edn")

(config/load +config-file+)

(defn load-config
  []
  (config/load +config-file+))


(defn compose-command
  [& parts]
  (apply shell-command
         (concat ["docker-compose"]
                 (when +project-name+
                   ["-p" +project-name+])
                 (when +env-file+
                   ["--env-file" +env-file+])
                 (when +compose-file+
                   ["-f" +compose-file+])
                 parts)))
