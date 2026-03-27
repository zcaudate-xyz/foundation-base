(ns js.cell-v2.event
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.cell.kernel.base-util :as base-util]]})

(def$.js EV_INIT "@/::INIT")

(def$.js EV_STATE "@/::STATE")

(def$.js EV_LOCAL "cell/::LOCAL")

(def$.js EV_REMOTE "cell/::REMOTE")

(def$.js EV_DB_SYNC "db/::SYNC")

(def$.js EV_DB_REMOVE "db/::REMOVE")

(def$.js EV_DB_VIEW "db/::VIEW")

(defn.js event-group
  "returns the generic group for a topic"
  {:added "4.0"}
  [topic]
  (cond (or (== topic -/EV_INIT)
            (== topic -/EV_STATE))
        (return "lifecycle")

        (== topic -/EV_LOCAL)
        (return "local")

        (== topic -/EV_REMOTE)
        (return "remote")

        (or (== topic -/EV_DB_SYNC)
            (== topic -/EV_DB_REMOVE)
            (== topic -/EV_DB_VIEW))
        (return "store")

        :else
        (return "custom")))

(defn.js event-lifecycle?
  "checks if topic is a lifecycle event"
  {:added "4.0"}
  [topic]
  (return (== "lifecycle" (-/event-group topic))))

(defn.js event-local?
  "checks if topic is a local event"
  {:added "4.0"}
  [topic]
  (return (== "local" (-/event-group topic))))

(defn.js event-remote?
  "checks if topic is a remote event"
  {:added "4.0"}
  [topic]
  (return (== "remote" (-/event-group topic))))

(defn.js event-store?
  "checks if topic is a store event"
  {:added "4.0"}
  [topic]
  (return (== "store" (-/event-group topic))))

(defn.js event
  "constructs an event map"
  {:added "4.0"}
  [signal body meta]
  (return {:signal signal
           :topic signal
           :status "ok"
           :group (-/event-group signal)
           :body body
           :meta (or meta {})}))

(defn.js signal-event
  "constructs a signal event map"
  {:added "4.0"}
  [signal status body meta]
  (return {:signal signal
           :topic signal
           :status (or status "ok")
           :group (-/event-group signal)
           :body body
           :meta (or meta {})}))

(defn.js signal
  "constructs a signal event"
  {:added "4.0"}
  [signal body meta]
  (return (-/event signal body meta)))

(defn.js event-signal
  "gets the signal from an event"
  {:added "4.0"}
  [event]
  (return (or (k/get-key event "signal")
              (k/get-key event "topic"))))

(defn.js event-topic
  "gets the topic from an event"
  {:added "4.0"}
  [event]
  (return (-/event-signal event)))

(defn.js make-bus
  "creates a new event bus"
  {:added "4.0"}
  []
  (return {"::" "cell-v2.bus"
           :listeners {}}))

(defn.js add-listener
  "adds a listener to the bus"
  {:added "4.0"}
  [bus listener-id pred f]
  (var prev (. bus ["listeners"] [listener-id]))
  (:= (. bus ["listeners"] [listener-id])
      {:id listener-id
       :pred pred
       :handler f})
  (return prev))

(defn.js remove-listener
  "removes a listener from the bus"
  {:added "4.0"}
  [bus listener-id]
  (var prev (. bus ["listeners"] [listener-id]))
  (del (. bus ["listeners"] [listener-id]))
  (return prev))

(defn.js list-listeners
  "lists all listeners"
  {:added "4.0"}
  [bus]
  (return (k/obj-keys (. bus ["listeners"]))))

(defn.js emit
  "dispatches an event to matching listeners"
  {:added "4.0"}
  [bus event]
  (var signal (-/event-signal event))
  (var out [])
  (k/for:object [[listener-id entry] (. bus ["listeners"])]
    (var #{pred handler} entry)
    (when (base-util/check-event pred signal event bus)
      (try
        (handler event signal bus)
        (x:arr-push out listener-id)
        (catch err
            (k/LOG! {:stack   (. err ["stack"])
                     :message (. err ["message"])})))))
  (return out))
