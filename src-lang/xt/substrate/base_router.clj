(ns xt.substrate.base-router
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]]})

(defspec.xt RouterConnectionEntry
  [:xt/record
   ["id" :xt/str]
   ["meta" [:xt/maybe [:xt/dict :xt/str :xt/any]]]])

(defspec.xt RouterSubscriptionEntry
  [:xt/record
   ["id" :xt/str]
   ["meta" [:xt/maybe [:xt/dict :xt/str :xt/any]]]])

(defspec.xt RouterSignalSubscriptions
  [:xt/dict :xt/str RouterSubscriptionEntry])

(defspec.xt RouterSpaceSubscriptions
  [:xt/dict :xt/str RouterSignalSubscriptions])

(defspec.xt RouterSubscriptions
  [:xt/dict :xt/str RouterSpaceSubscriptions])

(defspec.xt RouterState
  [:xt/record
   ["connections" [:xt/dict :xt/str RouterConnectionEntry]]
   ["subscriptions" RouterSubscriptions]])

(defspec.xt subscribe-frame
  [:fn [[:xt/maybe :xt/str]
        :xt/str
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       frame/NodeFrame])

(defspec.xt unsubscribe-frame
  [:fn [[:xt/maybe :xt/str]
        :xt/str
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       frame/NodeFrame])

(defspec.xt ensure-router
  [:fn [:xt/any] RouterState])

(defspec.xt get-connections
  [:fn [:xt/any] [:xt/dict :xt/str RouterConnectionEntry]])

(defspec.xt get-subscriptions
  [:fn [:xt/any] RouterSubscriptions])

(defspec.xt register-connection
  [:fn [:xt/any
        :xt/str
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       RouterConnectionEntry])

(defspec.xt prune-subscription-signal-loop
  [:fn [RouterSpaceSubscriptions
        [:xt/array :xt/str]
        :xt/str
        :xt/int]
       :xt/nil])

(defspec.xt prune-subscription-space-loop
  [:fn [RouterSubscriptions
        [:xt/array :xt/str]
        :xt/str
        :xt/int]
       :xt/nil])

(defspec.xt unregister-connection
  [:fn [:xt/any :xt/str] [:xt/maybe RouterConnectionEntry]])

(defspec.xt ensure-space-subscriptions
  [:fn [:xt/any
        [:xt/maybe :xt/str]]
       RouterSpaceSubscriptions])

(defspec.xt ensure-signal-subscriptions
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        :xt/str]
       RouterSignalSubscriptions])

(defspec.xt add-subscription
  [:fn [:xt/any
        :xt/str
        [:xt/maybe :xt/str]
        :xt/str
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       RouterSubscriptionEntry])

(defspec.xt remove-subscription
  [:fn [:xt/any
        :xt/str
        [:xt/maybe :xt/str]
        :xt/str]
       [:xt/maybe RouterSubscriptionEntry]])

(defspec.xt list-subscriptions
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        [:xt/maybe :xt/str]]
       [:or RouterSubscriptions
            RouterSpaceSubscriptions
            [:xt/array :xt/str]]])

(defspec.xt target-ids
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        :xt/str]
       [:xt/array :xt/str]])

(defspec.xt receive-subscribe
  [:fn [:xt/any
        frame/NodeFrame
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt receive-unsubscribe
  [:fn [:xt/any
        frame/NodeFrame
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

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
  (var #{router} node)
  (when (xt/x:nil? router)
    (:= router {:connections {}
                :subscriptions {}})
    (xt/x:set-key node "router" router))
  (return router))

(defn.xt get-connections
  "gets registered router connections"
  {:added "4.1"}
  [node]
  (return (. (-/ensure-router node) ["connections"])))

(defn.xt get-subscriptions
  "gets router subscriptions"
  {:added "4.1"}
  [node]
  (return (. (-/ensure-router node) ["subscriptions"])))

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
  (var signal (xt/x:get-idx signal-ids (xt/x:offset index)))
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
  (var space (xt/x:get-idx space-ids (xt/x:offset index)))
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
  (var #{transport-id} ctx)
  (when (xt/x:not-nil? transport-id)
    (-/add-subscription node
                        transport-id
                        (. event ["space"])
                        (. event ["signal"])
                        (. event ["id"])
                        (. event ["meta"])))
  (return (promise/x:promise-run event)))

(defn.xt receive-unsubscribe
  "processes an inbound unsubscribe control frame"
  {:added "4.1"}
  [node event ctx]
  (var #{transport-id} ctx)
  (when (xt/x:not-nil? transport-id)
    (-/remove-subscription node
                           transport-id
                           (. event ["space"])
                           (. event ["signal"])))
  (return (promise/x:promise-run event)))
