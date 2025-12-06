(ns indigo.server.context
  (:require [org.httpkit.server :as http]))

(defonce repl-clients (atom #{}))

(defn broadcast! [msg]
  (doseq [channel @repl-clients]
    (http/send! channel msg)))
