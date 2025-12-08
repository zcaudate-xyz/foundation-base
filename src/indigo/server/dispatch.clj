(ns indigo.server.dispatch
  (:require [cheshire.core :as json]
            [std.lib :as h]
            [org.httpkit.server :as http]))

(defonce ^:dynamic *clients* (atom #{}))

(defn register! [channel]
  (swap! *clients* conj channel))

(defn unregister! [channel]
  (swap! *clients* disj channel))

(defn send! [ch message]
  (let [message (if (string? message)
                  message
                  (json/generate-string message))]
    (http/send! ch message)))

(defn broadcast! [message]
  (let [json-msg (if (string? message)
                   message
                   (json/generate-string message))]
    (doseq [ch @*clients*]
      (http/send! ch json-msg))))