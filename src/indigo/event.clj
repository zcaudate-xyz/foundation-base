(ns indigo.event
  (:require [std.concurrent.bus :as bus]
            [clojure.core.async :as async :refer [chan put!]]
            [std.lib :as h]))

(defonce ^:dynamic *event-bus* (atom nil))

(defrecord EventBus [bus subscribers])

(extend-protocol std.protocol.component/IComponent
  EventBus
  (-start [this]
    (bus/start-bus (:bus this))
    this)
  (-stop [this]
    (bus/stop-bus (:bus this))
    this))

(defn create-event-bus []
  (let [bus (bus/bus:create)]
    (map->EventBus {:bus bus
                    :subscribers (atom {})})))

(defn subscribe
  [event-bus topic]
  (let [bus (:bus event-bus)
        subscribers (:subscribers event-bus)
        sub-chan (chan)]
    (swap! subscribers update topic (fnil conj []) sub-chan)
    sub-chan))

(defn publish
  [event-bus topic message]
  (let [bus (:bus event-bus)
        subscribers (:subscribers event-bus)]
    (doseq [sub-chan (get @subscribers topic)]
      (put! sub-chan message))))

(defn start-event-bus! []
  (reset! *event-bus* (create-event-bus))
  (h/start @*event-bus*))

(defn stop-event-bus! []
  (when @*event-bus*
    (h/stop @*event-bus*))
  (reset! *event-bus* nil))
