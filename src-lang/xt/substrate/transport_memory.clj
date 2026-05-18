(ns xt.substrate.transport-memory
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as main]
             [xt.substrate.base-json :as node-json]]})

(defspec.xt event-text
  [:fn [:xt/any] :xt/any])

(defspec.xt TextEndpointListener
  [:fn [:xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/any])

(defspec.xt TextEndpointWriteFn
  [:fn [:xt/str] :xt/any])

(defspec.xt TextEndpointStartFn
  [:fn [TextEndpointListener] :xt/any])

(defspec.xt TextEndpointStopFn
  [:fn [:xt/any] :xt/any])

(defspec.xt TextEndpointCreateFn
  [:fn [TextEndpointListener] :xt/any])

(defspec.xt text-endpoint
  [:fn [:xt/any] main/NodeTransport])

(defspec.xt MemoryWireEndpoint
  [:xt/record
   ["meta" [:xt/dict :xt/str :xt/any]]
   ["write_fn" TextEndpointWriteFn]
   ["start_fn" TextEndpointStartFn]
   ["stop_fn" TextEndpointStopFn]])

(defspec.xt MemoryWirePair
  [:xt/record
   ["left" MemoryWireEndpoint]
   ["right" MemoryWireEndpoint]])

(defspec.xt memory-endpoint
  [:fn [[:xt/dict :xt/str :xt/any]] MemoryWireEndpoint])

(defspec.xt memory-pair
  [:fn [[:xt/maybe [:xt/dict :xt/str :xt/any]]] MemoryWirePair])

(defspec.xt MemoryWireNetwork
  [:xt/dict :xt/str MemoryWireEndpoint])

(defspec.xt memory-network
  [:fn [[:xt/maybe [:xt/dict :xt/str :xt/any]]] MemoryWireNetwork])

(defn.xt event-text
  "normalizes inbound endpoint events into text payloads"
  {:added "4.1"}
  [event]
  (return (:? (and (xt/x:is-object? event)
                   (xt/x:has-key? event "data"))
               (xt/x:get-key event "data")
               (:? (and (xt/x:is-object? event)
                        (xt/x:has-key? event "text"))
                   (xt/x:get-key event "text")
                   event))))

(defn.xt network-targets
  "normalizes network link config to an array of peer ids"
  {:added "4.1"}
  [value]
  (cond (xt/x:nil? value)
        (return [])

        (xt/x:is-array? value)
        (return value)

        :else
        (return [value])))

(defn.xt ensure-network-state
  "ensures shared network state exists for an endpoint id"
  {:added "4.1"}
  [network endpoint-id]
  (var states (xt/x:get-key network "states"))
  (var state (xt/x:get-key states endpoint-id))
  (when (xt/x:nil? state)
    (:= state {"id" endpoint-id
               "listener" nil
               "peers" []
               "network" network})
    (xt/x:set-key states endpoint-id state))
  (return state))

(defn.xt ensure-network-targets-loop
  "ensures shared network state exists for each configured peer"
  {:added "4.1"}
  [network peer-ids index]
  (when (>= index (xt/x:len peer-ids))
    (return nil))
  (-/ensure-network-state network
                          (xt/x:get-idx peer-ids (xt/x:offset index)))
  (return (-/ensure-network-targets-loop network
                                         peer-ids
                                         (+ index 1))))

(defn.xt configure-network-links-loop
  "configures endpoint links on a shared memory network"
  {:added "4.1"}
  [network links endpoint-ids index]
  (when (>= index (xt/x:len endpoint-ids))
    (return network))
  (var endpoint-id (xt/x:get-idx endpoint-ids (xt/x:offset index)))
  (var peer-ids (-/network-targets (xt/x:get-key links endpoint-id)))
  (var state (-/ensure-network-state network endpoint-id))
  (xt/x:set-key state "peers" peer-ids)
  (-/ensure-network-targets-loop network peer-ids 0)
  (return (-/configure-network-links-loop network
                                          links
                                          endpoint-ids
                                          (+ index 1))))

(defn.xt create-network-endpoints-loop
  "materializes transport endpoints for a shared network"
  {:added "4.1"}
  [network endpoint-ids out index]
  (when (>= index (xt/x:len endpoint-ids))
    (return out))
  (var endpoint-id (xt/x:get-idx endpoint-ids (xt/x:offset index)))
  (xt/x:set-key out
                endpoint-id
                (-/memory-endpoint (-/ensure-network-state network endpoint-id)))
  (return (-/create-network-endpoints-loop network
                                           endpoint-ids
                                           out
                                           (+ index 1))))

(defn.xt deliver-network-loop
  "delivers text to each configured peer in order"
  {:added "4.1"}
  [network state peer-ids text index]
  (when (>= index (xt/x:len peer-ids))
    (return (promise/x:promise-run true)))
  (var peer-id (xt/x:get-idx peer-ids (xt/x:offset index)))
  (var peer (xt/x:get-key (xt/x:get-key network "states")
                          peer-id))
  (when (xt/x:nil? peer)
    (xt/x:err (xt/x:cat "wire peer not found - " peer-id)))
  (var listener (xt/x:get-key peer "listener"))
  (when (xt/x:nil? listener)
    (xt/x:err (xt/x:cat "wire peer not started - " peer-id)))
  (var output
       (listener
        {"text" text}
        {"wire" (xt/x:get-key state "id")
         "peer" peer-id}))
  (return
   (promise/x:promise-then
    (:? (promise/x:promise-native? output)
        output
        (promise/x:promise-run output))
    (fn [_]
      (return (-/deliver-network-loop network
                                      state
                                      peer-ids
                                      text
                                      (+ index 1)))))))

(defn.xt text-endpoint
  "adapts a line-oriented text endpoint to the node transport contract"
  {:added "4.1"}
  [endpoint-source]
  (var current-endpoint nil)
  (var current-listener nil)
  (var current-callback nil)
  (var source-create-fn (xt/x:get-key endpoint-source "create_fn"))
  (var send-fn
       (fn [frame]
         (var endpoint current-endpoint)
         (when (xt/x:nil? endpoint)
          (when (not (xt/x:is-function? source-create-fn))
            (:= endpoint endpoint-source)))
         (when (xt/x:nil? endpoint)
           (xt/x:err "json endpoint not started"))
         (var raw-write-fn (xt/x:get-key endpoint "write_fn"))
         (when (not (xt/x:is-function? raw-write-fn))
           (xt/x:err "json endpoint missing write implementation"))
         (return (raw-write-fn (node-json/encode-frame frame)))))
  (var start-fn
       (fn [listener]
         (var callback
              (fn [event ctx]
                (var text (-/event-text event))
                (var frame (node-json/decode-frame text))
                (:= ctx (or ctx {}))
                (xt/x:set-key ctx "raw" event)
                (xt/x:set-key ctx "payload" text)
                (return (listener frame ctx))))
         (:= current-callback callback)
         (if (xt/x:is-function? source-create-fn)
           (do (:= current-endpoint (source-create-fn callback))
              (:= current-listener current-endpoint)
              (return current-endpoint))
           (do (:= current-endpoint endpoint-source)
              (var raw-start-fn (xt/x:get-key current-endpoint "start_fn"))
              (when (xt/x:nil? raw-start-fn)
                (:= current-listener current-endpoint)
                (return current-endpoint))
              (:= current-listener (raw-start-fn callback))
              (when (xt/x:not-nil? current-listener)
                (return current-listener))
              (return current-endpoint)))))
  (var stop-fn
       (fn [_]
         (var endpoint current-endpoint)
         (when (xt/x:nil? endpoint)
          (:= endpoint endpoint-source))
         (var raw-stop-fn nil)
         (when (xt/x:is-object? endpoint)
          (:= raw-stop-fn (xt/x:get-key endpoint "stop_fn")))
         (when (xt/x:is-function? raw-stop-fn)
          (raw-stop-fn current-listener))
         (:= current-endpoint nil)
         (:= current-listener nil)
         (:= current-callback nil)
         (return true)))
  (return
   {"meta" {"kind" "json"}
    "send_fn" send-fn
    "start_fn" start-fn
    "stop_fn" stop-fn}))

(defn.xt memory-endpoint
  "creates an in-memory text endpoint that forwards writes to its peer listener"
  {:added "4.1"}
  [state]
  (var write-fn
       (fn [text]
         (var network (xt/x:get-key state "network"))
         (var peer-ids (xt/x:get-key state "peers"))
         (when (xt/x:not-nil? network)
          (when (== 0 (xt/x:len peer-ids))
            (xt/x:err "wire endpoint missing peers"))
          (return (-/deliver-network-loop network
                                          state
                                          peer-ids
                                          text
                                          0)))
         (var peer (xt/x:get-key state "peer"))
         (when (xt/x:nil? peer)
           (xt/x:err "wire endpoint missing peer"))
         (var listener (xt/x:get-key peer "listener"))
         (when (xt/x:nil? listener)
           (xt/x:err "wire peer not started"))
         (return
          (listener
           {"text" text}
           {"wire" (xt/x:get-key state "id")
            "peer" (xt/x:get-key peer "id")}))))
  (var start-fn
       (fn [listener]
         (xt/x:set-key state "listener" listener)
         (return state)))
  (var stop-fn
       (fn [_]
         (xt/x:set-key state "listener" nil)
         (return true)))
  (return
   {"meta" {"kind" "wire.memory"
            "id" (xt/x:get-key state "id")}
    "write_fn" write-fn
    "start_fn" start-fn
    "stop_fn" stop-fn}))

(defn.xt memory-pair
  "creates a bidirectional in-memory text wire"
  {:added "4.1"}
  [opts]
  (var config (or opts {}))
  (var left-state {"id" (or (xt/x:get-key config "left_id")
                            "left")
                   "listener" nil
                   "peer" nil})
  (var right-state {"id" (or (xt/x:get-key config "right_id")
                             "right")
                    "listener" nil
                    "peer" nil})
  (xt/x:set-key left-state "peer" right-state)
  (xt/x:set-key right-state "peer" left-state)
  (return
   {"left" (-/memory-endpoint left-state)
    "right" (-/memory-endpoint right-state)}))

(defn.xt memory-network
  "creates named in-memory endpoints for arbitrary routing topologies"
  {:added "4.1"}
  [opts]
  (var config (or opts {}))
  (var links (:? (xt/x:has-key? config "links")
                (xt/x:get-key config "links")
                config))
  (var network {"states" {}})
  (-/configure-network-links-loop network
                                 links
                                 (xt/x:obj-keys links)
                                 0)
  (return (-/create-network-endpoints-loop network
                                          (xt/x:obj-keys (xt/x:get-key network "states"))
                                          {}
                                          0)))
