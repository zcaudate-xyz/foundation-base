(ns postgres.sample.scratch-v0.route-entries
  (:require [hara.lang :as l]))

(l/script :xtalk)

(def.xt ping
  {:input []
   :return "text"
   :schema "scratch_v0"
   :id "ping"
   :flags {}
   :url "api/scratch-v0/ping"})

(def.xt log-append
  {:input [{:symbol "i_message" :type "text"}]
   :return "jsonb"
   :schema "scratch_v0"
   :id "log_append"
   :flags {}
   :url "api/scratch-v0/log-append"})
