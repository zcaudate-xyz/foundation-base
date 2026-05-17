(ns xt.event.node-sync
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.node :as event-node]
             [xt.event.node-router :as router]
             [xt.event.node-space :as node-space]]})

(def$.xt META_KEY  "node.sync")
(def$.xt STATE_TAG "node.sync.state")

(def$.xt ACTION_OPEN        "node.sync/open")
(def$.xt ACTION_SNAPSHOT    "node.sync/snapshot")
(def$.xt ACTION_RESUME      "node.sync/resume")
(def$.xt ACTION_UNSUBSCRIBE "node.sync/unsubscribe")

(def$.xt SIGNAL_DELTA "node.sync/delta")
(def$.xt SIGNAL_RESET "node.sync/reset")
(def$.xt SIGNAL_ERROR "node.sync/error")

(def$.xt MODE_OPEN        "open")
(def$.xt MODE_SNAPSHOT    "snapshot")
(def$.xt MODE_RESUME      "resume")
(def$.xt MODE_RESET       "reset")
(def$.xt MODE_UNSUBSCRIBE "unsubscribe")

(defspec.xt node-opts
  [:fn [:xt/any] [:xt/dict :xt/str :xt/any]])

(defspec.xt set-node-opts
  [:fn [:xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   [:xt/dict :xt/str :xt/any]])

(defspec.xt merge-node-opts
  [:fn [:xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   [:xt/dict :xt/str :xt/any]])

(defspec.xt request-payload
  [:fn [[:xt/maybe [:xt/array :xt/any]]] [:xt/dict :xt/str :xt/any]])

(defspec.xt ensure-promise
  [:fn [:xt/any] :xt/promise])

(defspec.xt sync-state
  [:fn [node-space/NodeSpace]
   [:xt/maybe [:xt/dict :xt/str :xt/any]]])

(defspec.xt ensure-sync-state
  [:fn [node-space/NodeSpace] [:xt/dict :xt/str :xt/any]])

(defspec.xt get-cursor
  [:fn [:xt/any
        [:xt/maybe :xt/str]]
   :xt/int])

(defspec.xt set-cursor
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        :xt/int]
   :xt/int])

(defspec.xt next-cursor
  [:fn [:xt/any
        [:xt/maybe :xt/str]]
   :xt/int])

(defspec.xt subscribe-signals
  [:fn [:xt/any] [:xt/array :xt/str]])

(defspec.xt snapshot-value
  [:fn [node-space/NodeSpace :xt/any :xt/any] :xt/promise])

(defspec.xt subscribe-transport
  [:fn [:xt/any
        :xt/str
        [:xt/maybe :xt/str]
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   [:xt/maybe :xt/str]])

(defspec.xt unsubscribe-transport
  [:fn [:xt/any
        :xt/str
        [:xt/maybe :xt/str]]
   :xt/str])

(defspec.xt handle-open
  [:fn [node-space/NodeSpace
        [:xt/maybe [:xt/array :xt/any]]
        :xt/any
        :xt/any]
   :xt/promise])

(defspec.xt handle-snapshot
  [:fn [node-space/NodeSpace
        [:xt/maybe [:xt/array :xt/any]]
        :xt/any
        :xt/any]
   :xt/promise])

(defspec.xt handle-resume
  [:fn [node-space/NodeSpace
        [:xt/maybe [:xt/array :xt/any]]
        :xt/any
        :xt/any]
   :xt/promise])

(defspec.xt handle-unsubscribe
  [:fn [node-space/NodeSpace
        [:xt/maybe [:xt/array :xt/any]]
        :xt/any
        :xt/any]
   [:xt/dict :xt/str :xt/any]])

(defspec.xt install
  [:fn [:xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/any])

(defspec.xt install-triggers
  [:fn [:xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/any])

(defspec.xt uninstall
  [:fn [:xt/any] :xt/any])

(defspec.xt open
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt snapshot
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt resume
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt unsubscribe
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt publish-delta
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        :xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt publish-reset
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        :xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt publish-error
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        :xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt apply-stream
  [:fn [:xt/any :xt/any] :xt/promise])

(defspec.xt handle-delta
  [:fn [node-space/NodeSpace :xt/any :xt/any] :xt/promise])

(defspec.xt handle-reset
  [:fn [node-space/NodeSpace :xt/any :xt/any] :xt/promise])

(defspec.xt handle-error
  [:fn [node-space/NodeSpace :xt/any :xt/any] :xt/promise])

(defn.xt node-opts
  "gets node.sync options from node metadata"
  {:added "4.1"}
  [node]
  (return (or (xtd/get-in node ["meta" -/META_KEY])
              {})))

(defn.xt set-node-opts
  "stores node.sync options on node metadata"
  {:added "4.1"}
  [node opts]
  (xtd/set-in node ["meta" -/META_KEY] (or opts {}))
  (return (-/node-opts node)))

(defn.xt merge-node-opts
  "merges node.sync options into node metadata"
  {:added "4.1"}
  [node opts]
  (var current (-/node-opts node))
  (xt/for:object [[k v] (or opts {})]
    (xt/x:set-key current k v))
  (-/set-node-opts node current)
  (return current))

(defn.xt request-payload
  "gets the first request payload"
  {:added "4.1"}
  [args]
  (return (or (:? (and (xt/x:is-array? args)
                       (> (xt/x:len args) 0))
                    (xt/x:first args)
                    nil)
              {})))

(defn.xt ensure-promise
  "wraps sync values in a native host promise"
  {:added "4.1"}
  [value]
  (if (promise/x:promise-native? value)
    (return value)
    (return (promise/x:promise-run value))))

(defn.xt sync-state
  "gets node.sync state from space metadata"
  {:added "4.1"}
  [space]
  (return (xtd/get-in space ["meta" -/META_KEY])))

(defn.xt ensure-sync-state
  "ensures node.sync state exists on a space"
  {:added "4.1"}
  [space]
  (var current (-/sync-state space))
  (when (xt/x:nil? current)
    (:= current {"::" -/STATE_TAG
                 "cursor" 0
                 "last_error" nil})
    (xtd/set-in space ["meta" -/META_KEY] current))
  (return current))

(defn.xt get-cursor
  "gets the current sync cursor for a node space"
  {:added "4.1"}
  [node space-id]
  (var space (node-space/ensure-space node space-id nil))
  (var state (-/ensure-sync-state space))
  (return (or (xt/x:get-key state "cursor")
              0)))

(defn.xt set-cursor
  "sets the sync cursor for a node space"
  {:added "4.1"}
  [node space-id cursor]
  (var space (node-space/ensure-space node space-id nil))
  (var state (-/ensure-sync-state space))
  (xt/x:set-key state "cursor" cursor)
  (return cursor))

(defn.xt next-cursor
  "increments the sync cursor for a node space"
  {:added "4.1"}
  [node space-id]
  (var next (+ (-/get-cursor node space-id) 1))
  (-/set-cursor node space-id next)
  (return next))

(defn.xt subscribe-signals
  "gets the signals used by the sync protocol"
  {:added "4.1"}
  [node]
  (var opts (-/node-opts node))
  (return (or (xt/x:get-key opts "subscribe_signals")
              (xt/x:get-key opts "subscribe-signals")
              [-/SIGNAL_DELTA -/SIGNAL_RESET -/SIGNAL_ERROR])))

(defn.xt snapshot-value
  "gets the snapshot value for a sync response"
  {:added "4.1"}
  [space request node]
  (var opts (-/node-opts node))
  (var getter (xt/x:get-key opts "get_snapshot"))
  (if (xt/x:is-function? getter)
    (return (-/ensure-promise (getter space request node)))
    (return (promise/x:promise-run (xt/x:get-key space "state")))))

(defn.xt subscribe-transport
  "subscribes a transport to the sync stream signals for a space"
  {:added "4.1"}
  [node transport-id space-id subscription-id meta]
  (xt/for:array [signal (-/subscribe-signals node)]
    (router/add-subscription node
                             transport-id
                             space-id
                             signal
                             subscription-id
                             meta))
  (return subscription-id))

(defn.xt unsubscribe-transport
  "removes a transport from the sync stream signals for a space"
  {:added "4.1"}
  [node transport-id space-id]
  (xt/for:array [signal (-/subscribe-signals node)]
    (router/remove-subscription node
                                transport-id
                                space-id
                                signal))
  (return transport-id))

(defn.xt handle-open
  "handles a node sync open request"
  {:added "4.1"}
  [space args request node]
  (var payload (-/request-payload args))
  (var space-id (xt/x:get-key space "id"))
  (var transport-id (xtd/get-in request ["meta" "transport_id"]))
  (var subscribe? (not= false (xt/x:get-key payload "subscribe")))
  (var subscription-id (or (xt/x:get-key payload "subscription_id")
                           (xt/x:get-key request "id")))
  (return
   (promise/x:promise-then
    (-/snapshot-value space request node)
    (fn [snapshot]
      (when (and subscribe? (xt/x:not-nil? transport-id))
        (-/subscribe-transport
         node
         transport-id
         space-id
         subscription-id
         {"request_id" (xt/x:get-key request "id")}))
      (return {"protocol" -/META_KEY
               "mode" -/MODE_OPEN
               "space" space-id
               "cursor" (-/get-cursor node space-id)
               "snapshot" snapshot
               "subscribed" (and subscribe?
                                 (xt/x:not-nil? transport-id))
               "subscription_id" subscription-id})))))

(defn.xt handle-snapshot
  "handles a node sync snapshot request"
  {:added "4.1"}
  [space args request node]
  (var space-id (xt/x:get-key space "id"))
  (return
   (promise/x:promise-then
    (-/snapshot-value space request node)
    (fn [snapshot]
      (return {"protocol" -/META_KEY
               "mode" -/MODE_SNAPSHOT
               "space" space-id
               "cursor" (-/get-cursor node space-id)
               "snapshot" snapshot})))))

(defn.xt handle-resume
  "handles a node sync resume request"
  {:added "4.1"}
  [space args request node]
  (var payload (-/request-payload args))
  (var requested-cursor (xt/x:get-key payload "cursor"))
  (var space-id (xt/x:get-key space "id"))
  (var current-cursor (-/get-cursor node space-id))
  (var transport-id (xtd/get-in request ["meta" "transport_id"]))
  (var subscribe? (not= false (xt/x:get-key payload "subscribe")))
  (var subscription-id (or (xt/x:get-key payload "subscription_id")
                           (xt/x:get-key request "id")))
  (if (== requested-cursor current-cursor)
    (do
      (when (and subscribe? (xt/x:not-nil? transport-id))
        (-/subscribe-transport
         node
         transport-id
         space-id
         subscription-id
         {"request_id" (xt/x:get-key request "id")}))
      (return
       (promise/x:promise-run
        {"protocol" -/META_KEY
         "mode" -/MODE_RESUME
         "space" space-id
         "cursor" current-cursor
         "subscribed" (and subscribe?
                           (xt/x:not-nil? transport-id))
         "subscription_id" subscription-id})))
    (return
     (promise/x:promise-then
      (-/handle-open space args request node)
      (fn [response]
        (xt/x:set-key response "mode" -/MODE_RESET)
        (return response))))))

(defn.xt handle-unsubscribe
  "handles a node sync unsubscribe request"
  {:added "4.1"}
  [space args request node]
  (var payload (-/request-payload args))
  (var transport-id (xtd/get-in request ["meta" "transport_id"]))
  (when (xt/x:not-nil? transport-id)
    (-/unsubscribe-transport node transport-id (xt/x:get-key space "id")))
  (return {"protocol" -/META_KEY
           "mode" -/MODE_UNSUBSCRIBE
           "space" (xt/x:get-key space "id")
           "transport_id" transport-id
           "subscription_id" (or (xt/x:get-key payload "subscription_id")
                                 (xt/x:get-key request "id"))
           "ok" true}))

(defn.xt install
  "installs node sync request handlers on a node"
  {:added "4.1"}
  [node opts]
  (-/merge-node-opts node opts)
  (event-node/register-handler node -/ACTION_OPEN -/handle-open nil)
  (event-node/register-handler node -/ACTION_SNAPSHOT -/handle-snapshot nil)
  (event-node/register-handler node -/ACTION_RESUME -/handle-resume nil)
  (event-node/register-handler node -/ACTION_UNSUBSCRIBE -/handle-unsubscribe nil)
  (return node))

(defn.xt install-triggers
  "installs node sync stream triggers on a node"
  {:added "4.1"}
  [node opts]
  (-/merge-node-opts node opts)
  (event-node/register-trigger node -/SIGNAL_DELTA -/handle-delta nil)
  (event-node/register-trigger node -/SIGNAL_RESET -/handle-reset nil)
  (event-node/register-trigger node -/SIGNAL_ERROR -/handle-error nil)
  (return node))

(defn.xt uninstall
  "removes node sync handlers and triggers from a node"
  {:added "4.1"}
  [node]
  (event-node/unregister-handler node -/ACTION_OPEN)
  (event-node/unregister-handler node -/ACTION_SNAPSHOT)
  (event-node/unregister-handler node -/ACTION_RESUME)
  (event-node/unregister-handler node -/ACTION_UNSUBSCRIBE)
  (event-node/unregister-trigger node -/SIGNAL_DELTA)
  (event-node/unregister-trigger node -/SIGNAL_RESET)
  (event-node/unregister-trigger node -/SIGNAL_ERROR)
  (return node))

(defn.xt open
  "issues a node sync open request"
  {:added "4.1"}
  [node space-id payload meta]
  (return (event-node/request node
                              space-id
                              -/ACTION_OPEN
                              [(or payload {})]
                              meta)))

(defn.xt snapshot
  "issues a node sync snapshot request"
  {:added "4.1"}
  [node space-id payload meta]
  (return (event-node/request node
                              space-id
                              -/ACTION_SNAPSHOT
                              [(or payload {})]
                              meta)))

(defn.xt resume
  "issues a node sync resume request"
  {:added "4.1"}
  [node space-id payload meta]
  (return (event-node/request node
                              space-id
                              -/ACTION_RESUME
                              [(or payload {})]
                              meta)))

(defn.xt unsubscribe
  "issues a node sync unsubscribe request"
  {:added "4.1"}
  [node space-id payload meta]
  (return (event-node/request node
                              space-id
                              -/ACTION_UNSUBSCRIBE
                              [(or payload {})]
                              meta)))

(defn.xt publish-delta
  "publishes a node sync delta stream"
  {:added "4.1"}
  [node space-id delta meta]
  (var cursor (-/next-cursor node space-id))
  (return
   (event-node/publish
    node
    space-id
    -/SIGNAL_DELTA
    {"cursor" cursor
     "delta" delta}
    (xt/x:obj-assign {"protocol" -/META_KEY
                      "cursor" cursor
                      "origin_node" (xt/x:get-key node "id")}
                     (or meta {})))))

(defn.xt publish-reset
  "publishes a node sync reset stream"
  {:added "4.1"}
  [node space-id snapshot meta]
  (var cursor (-/next-cursor node space-id))
  (return
   (event-node/publish
    node
    space-id
    -/SIGNAL_RESET
    {"cursor" cursor
     "snapshot" snapshot}
    (xt/x:obj-assign {"protocol" -/META_KEY
                      "cursor" cursor
                      "origin_node" (xt/x:get-key node "id")}
                     (or meta {})))))

(defn.xt publish-error
  "publishes a node sync error stream"
  {:added "4.1"}
  [node space-id error meta]
  (var cursor (-/get-cursor node space-id))
  (return
   (event-node/publish
    node
    space-id
    -/SIGNAL_ERROR
    {"cursor" cursor
     "error" error}
    (xt/x:obj-assign {"protocol" -/META_KEY
                      "cursor" cursor
                      "origin_node" (xt/x:get-key node "id")}
                     (or meta {})))))

(defn.xt apply-stream
  "applies an inbound sync stream using configured node.sync handlers"
  {:added "4.1"}
  [node stream]
  (var signal (xt/x:get-key stream "signal"))
  (var payload (or (xt/x:get-key stream "data")
                   {}))
  (var space-id (xt/x:get-key stream "space"))
  (var space (node-space/ensure-space node space-id nil))
  (var sync-state (-/ensure-sync-state space))
  (var opts (-/node-opts node))
  (var current-state (xt/x:get-key space "state"))
  (var cursor (or (xt/x:get-key payload "cursor")
                  (xtd/get-in stream ["meta" "cursor"])))
  (cond (== signal -/SIGNAL_DELTA)
        (do
          (var apply-delta (xt/x:get-key opts "apply_delta"))
          (var handler (:? (xt/x:is-function? apply-delta)
                           apply-delta
                           (fn [state data _stream _node]
                             (return (or (xt/x:get-key data "delta")
                                         data)))))
          (return
           (promise/x:promise-then
            (-/ensure-promise
             (handler current-state payload stream node))
            (fn [next-state]
              (xt/x:set-key sync-state "last_error" nil)
              (when (xt/x:is-number? cursor)
                (-/set-cursor node space-id cursor))
              (return (event-node/set-space-state node
                                                  space-id
                                                  next-state))))))

        (== signal -/SIGNAL_RESET)
        (do
          (var apply-reset (xt/x:get-key opts "apply_reset"))
          (var handler (:? (xt/x:is-function? apply-reset)
                           apply-reset
                           (fn [_state data _stream _node]
                             (return (or (xt/x:get-key data "snapshot")
                                         data)))))
          (return
           (promise/x:promise-then
            (-/ensure-promise
             (handler current-state payload stream node))
            (fn [next-state]
              (xt/x:set-key sync-state "last_error" nil)
              (when (xt/x:is-number? cursor)
                (-/set-cursor node space-id cursor))
              (return (event-node/set-space-state node
                                                  space-id
                                                  next-state))))))

        (== signal -/SIGNAL_ERROR)
        (do
          (var apply-error (xt/x:get-key opts "apply_error"))
          (var handler (:? (xt/x:is-function? apply-error)
                           apply-error
                           (fn [_state data _stream _node]
                             (return (or (xt/x:get-key data "error")
                                         data)))))
          (return
           (promise/x:promise-then
            (-/ensure-promise
             (handler current-state payload stream node))
            (fn [out]
              (xt/x:set-key sync-state "last_error" out)
              (when (xt/x:is-number? cursor)
                (-/set-cursor node space-id cursor))
              (return out)))))

        :else
        (return (promise/x:promise-run nil))))

(defn.xt handle-delta
  "handles a node sync delta stream"
  {:added "4.1"}
  [space stream node]
  (return (-/apply-stream node stream)))

(defn.xt handle-reset
  "handles a node sync reset stream"
  {:added "4.1"}
  [space stream node]
  (return (-/apply-stream node stream)))

(defn.xt handle-error
  "handles a node sync error stream"
  {:added "4.1"}
  [space stream node]
  (return (-/apply-stream node stream)))
