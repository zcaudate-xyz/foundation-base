(ns std.dom.sync
  (:require [std.dom.common :as base]
            [std.dom.diff :as diff]
            [std.dom.update :as update]
            [std.dom.event :as event]
            [std.lib :as h]))

(defprotocol ISyncClient
  (apply-patch [client ops] "Applies patch ops from server")
  (emit-event  [client event] "Sends event to server"))

(defprotocol ISyncServer
  (receive-event [server event] "Handles event from client")
  (generate-diff [server last-synced-dom] "Calculates diff against a last known state")
  (get-snapshot  [server] "Returns the current state of the server DOM"))

;; Simple in-memory implementations for testing logic
(defrecord LocalClient [shadow-atom server-ref]
  ISyncClient
  (apply-patch [_ ops]
    (swap! shadow-atom (fn [dom]
                         (update/dom-apply dom ops))))
  (emit-event [_ event]
    (receive-event server-ref event)))

(defrecord LocalServer [dom-atom client-ref]
  ISyncServer
  (receive-event [_ event]
    ;; For this simple sync, we assume event contains enough info to trigger local handler
    ;; In a real app, we'd have a router here.
    (let [{:keys [id data]} event]
      ;; This is where we'd look up the component and call its handler
      ;; For now, let's assume we just trigger on the root
      (base/dom-trigger @dom-atom event)))

  (generate-diff [_ last-synced-dom]
    (let [current @dom-atom]
      (diff/dom-diff last-synced-dom current)))

  (get-snapshot [_]
    @dom-atom))

(defn sync-step
  "Performs one sync cycle: Server generates diff -> Client applies it"
  [server client last-synced-atom]
  (let [last    @last-synced-atom
        ops     (generate-diff server last)]
    (when (seq ops)
      (apply-patch client ops)
      ;; Update our 'last-synced' to match the server's current state
      (reset! last-synced-atom (get-snapshot server)))
    ops))
