(ns indigo.server.dispatch
  (:require [cheshire.core :as json]
            [std.lib :as h]
            [org.httpkit.server :as http]))

(defonce ^:dynamic *clients* (atom {}))

(defn register! [channel]
  (let [id (str (h/uuid))]
    (swap! *clients* assoc id channel)
    id))

(defn unregister! [id]
  (swap! *clients* dissoc id))

(defn send! [client-id message]
  (when-let [ch (get @*clients* client-id)]
    (let [message (if (string? message)
                    message
                    (json/generate-string message))]
      (http/send! ch message))))

(defn broadcast! [message]
  (let [json-msg (if (string? message)
                   message
                   (json/generate-string message))]
    (doseq [ch (vals @*clients*)]
      (http/send! ch json-msg))))