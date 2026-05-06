(ns xt.event.node-router
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.node-frame :as frame]]})

(def$.xt KIND_SUBSCRIBE   "subscribe")
(def$.xt KIND_UNSUBSCRIBE "unsubscribe")

(defn.xt subscribe-frame
  "constructs a router subscribe control frame"
  {:added "4.1"}
  [space signal subscription-id meta]
  (return
   {:kind -/KIND_SUBSCRIBE
    :id (or subscription-id
            (frame/rand-id "sub-" 6))
    :space (or space frame/SPACE_NODE)
    :signal signal
    :meta (or meta {})}))

(defn.xt unsubscribe-frame
  "constructs a router unsubscribe control frame"
  {:added "4.1"}
  [space signal subscription-id meta]
  (return
   {:kind -/KIND_UNSUBSCRIBE
    :id (or subscription-id
            (frame/rand-id "sub-" 6))
    :space (or space frame/SPACE_NODE)
    :signal signal
    :meta (or meta {})}))

(defn.xt ensure-router
  "ensures router state is present on a node"
  {:added "4.1"}
  [node]
  (var router (xt/x:get-key node "router"))
  (when (xt/x:nil? router)
    (:= router {:connections {}
                :subscriptions {}})
    (xt/x:set-key node "router" router))
  (return router))

(defn.xt get-connections
  "gets registered router connections"
  {:added "4.1"}
  [node]
  (return (xt/x:get-key (-/ensure-router node)
                        "connections")))

(defn.xt get-subscriptions
  "gets router subscriptions"
  {:added "4.1"}
  [node]
  (return (xt/x:get-key (-/ensure-router node)
                        "subscriptions")))

(defn.xt register-connection
  "registers a connection with the router"
  {:added "4.1"}
  [node transport-id meta]
  (var entry {:id transport-id
              :meta (or meta {})})
  (xt/x:set-key (-/get-connections node)
                transport-id
                entry)
  (return entry))

(defn.xt prune-subscription-signal-loop
  "removes a connection from all signals within a space"
  {:added "4.1"}
  [space-subs signal-ids transport-id index]
  (when (>= index (xt/x:len signal-ids))
    (return nil))
  (var signal (xt/x:get-idx signal-ids index))
  (var signal-subs (xt/x:get-key space-subs signal))
  (when (xt/x:not-nil? signal-subs)
    (xt/x:del-key signal-subs transport-id)
    (when (== 0 (xt/x:len (xt/x:obj-keys signal-subs)))
      (xt/x:del-key space-subs signal)))
  (return (-/prune-subscription-signal-loop space-subs
                                            signal-ids
                                            transport-id
                                            (+ index 1))))

(defn.xt prune-subscription-space-loop
  "removes a connection from all router subscriptions"
  {:added "4.1"}
  [subscriptions space-ids transport-id index]
  (when (>= index (xt/x:len space-ids))
    (return nil))
  (var space (xt/x:get-idx space-ids index))
  (var space-subs (xt/x:get-key subscriptions space))
  (when (xt/x:not-nil? space-subs)
    (-/prune-subscription-signal-loop space-subs
                                      (xt/x:obj-keys space-subs)
                                      transport-id
                                      0)
    (when (== 0 (xt/x:len (xt/x:obj-keys space-subs)))
      (xt/x:del-key subscriptions space)))
  (return (-/prune-subscription-space-loop subscriptions
                                           space-ids
                                           transport-id
                                           (+ index 1))))

(defn.xt unregister-connection
  "unregisters a connection and all of its subscriptions"
  {:added "4.1"}
  [node transport-id]
  (var connections (-/get-connections node))
  (var prev (xt/x:get-key connections transport-id))
  (xt/x:del-key connections transport-id)
  (var subscriptions (-/get-subscriptions node))
  (-/prune-subscription-space-loop subscriptions
                                   (xt/x:obj-keys subscriptions)
                                   transport-id
                                   0)
  (return prev))

(defn.xt ensure-space-subscriptions
  "ensures the per-space subscription table exists"
  {:added "4.1"}
  [node space]
  (var subscriptions (-/get-subscriptions node))
  (var space-id (or space frame/SPACE_NODE))
  (var space-subs (xt/x:get-key subscriptions space-id))
  (when (xt/x:nil? space-subs)
    (:= space-subs {})
    (xt/x:set-key subscriptions space-id space-subs))
  (return space-subs))

(defn.xt ensure-signal-subscriptions
  "ensures the per-signal subscription table exists"
  {:added "4.1"}
  [node space signal]
  (var space-subs (-/ensure-space-subscriptions node space))
  (var signal-subs (xt/x:get-key space-subs signal))
  (when (xt/x:nil? signal-subs)
    (:= signal-subs {})
    (xt/x:set-key space-subs signal signal-subs))
  (return signal-subs))

(defn.xt add-subscription
  "adds a router subscription for a connection"
  {:added "4.1"}
  [node transport-id space signal subscription-id meta]
  (var signal-subs (-/ensure-signal-subscriptions node space signal))
  (var entry {:id (or subscription-id
                      (frame/rand-id "sub-" 6))
              :meta (or meta {})})
  (xt/x:set-key signal-subs transport-id entry)
  (return entry))

(defn.xt remove-subscription
  "removes a router subscription for a connection"
  {:added "4.1"}
  [node transport-id space signal]
  (var subscriptions (-/get-subscriptions node))
  (var space-subs (xt/x:get-key subscriptions (or space frame/SPACE_NODE)))
  (when (xt/x:nil? space-subs)
    (return nil))
  (var signal-subs (xt/x:get-key space-subs signal))
  (when (xt/x:nil? signal-subs)
    (return nil))
  (var prev (xt/x:get-key signal-subs transport-id))
  (xt/x:del-key signal-subs transport-id)
  (when (== 0 (xt/x:len (xt/x:obj-keys signal-subs)))
    (xt/x:del-key space-subs signal))
  (when (== 0 (xt/x:len (xt/x:obj-keys space-subs)))
    (xt/x:del-key subscriptions (or space frame/SPACE_NODE)))
  (return prev))

(defn.xt list-subscriptions
  "lists router subscriptions"
  {:added "4.1"}
  [node space signal]
  (var subscriptions (-/get-subscriptions node))
  (if (xt/x:nil? space)
    (return subscriptions)
    (do
      (var space-subs (xt/x:get-key subscriptions (or space frame/SPACE_NODE)))
      (if (xt/x:nil? signal)
        (return (or space-subs {}))
        (if (xt/x:nil? space-subs)
          (return [])
          (return (xtd/arr-sort (xt/x:obj-keys (or (xt/x:get-key space-subs signal)
                                                   {}))
                                (fn [x] (return x))
                                xt/x:str-lt)))))))

(defn.xt target-ids
  "lists transport ids subscribed for a stream"
  {:added "4.1"}
  [node space signal]
  (return (-/list-subscriptions node space signal)))

(defn.xt receive-subscribe
  "processes an inbound subscribe control frame"
  {:added "4.1"}
  [node event ctx]
  (var transport-id (xt/x:get-key ctx "transport-id"))
  (when (xt/x:not-nil? transport-id)
    (-/add-subscription node
                        transport-id
                        (xt/x:get-key event "space")
                        (xt/x:get-key event "signal")
                        (xt/x:get-key event "id")
                        (xt/x:get-key event "meta")))
  (return (promise/x:promise-run event)))

(defn.xt receive-unsubscribe
  "processes an inbound unsubscribe control frame"
  {:added "4.1"}
  [node event ctx]
  (var transport-id (xt/x:get-key ctx "transport-id"))
  (when (xt/x:not-nil? transport-id)
    (-/remove-subscription node
                           transport-id
                           (xt/x:get-key event "space")
                           (xt/x:get-key event "signal")))
  (return (promise/x:promise-run event)))
