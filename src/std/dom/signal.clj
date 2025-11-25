(ns std.dom.signal
  (:require [std.dom.sync :as sync]
            [std.dom.update :as update]
            [std.dom.common :as base]))

(defmulti process-signal
  "processes a signal (delta or event)

   (process-signal client {:type :delta :payload [[:set :a 1]]})
   (process-signal server {:type :event :payload {:id :click}})"
  {:added "4.0"}
  (fn [target signal] (:type signal)))

(defmethod process-signal :delta
  [client {:keys [payload]}]
  (sync/apply-patch client payload))

(defmethod process-signal :event
  [server {:keys [payload]}]
  (sync/receive-event server payload))

(defn create-delta-signal
  "creates a signal carrying dom deltas"
  {:added "4.0"}
  [ops]
  {:type :delta :payload ops})

(defn create-event-signal
  "creates a signal carrying an event"
  {:added "4.0"}
  [event]
  {:type :event :payload event})
