(ns xt.substrate.transport-memory
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
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

(defspec.xt write-fn
  [:fn [:xt/any] [:xt/maybe TextEndpointWriteFn]])

(defspec.xt start-fn
  [:fn [:xt/any] [:xt/maybe TextEndpointStartFn]])

(defspec.xt stop-fn
  [:fn [:xt/any] [:xt/maybe TextEndpointStopFn]])

(defspec.xt create-fn
  [:fn [:xt/any] [:xt/maybe TextEndpointCreateFn]])

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

(defn.xt write-fn
  "gets the write_fn for a raw text endpoint"
  {:added "4.1"}
  [endpoint]
  (return (xt/x:get-key endpoint "write_fn")))

(defn.xt start-fn
  "gets the start_fn for a raw text endpoint"
  {:added "4.1"}
  [endpoint]
  (return (xt/x:get-key endpoint "start_fn")))

(defn.xt stop-fn
  "gets the stop_fn for a raw text endpoint"
  {:added "4.1"}
  [endpoint]
  (return (xt/x:get-key endpoint "stop_fn")))

(defn.xt create-fn
  "gets the create_fn for a raw text endpoint source"
  {:added "4.1"}
  [endpoint]
  (return (xt/x:get-key endpoint "create_fn")))

(defn.xt text-endpoint
  "adapts a line-oriented text endpoint to the node transport contract"
  {:added "4.1"}
  [endpoint-source]
  (var current-endpoint nil)
  (var current-listener nil)
  (var current-callback nil)
  (var source-create-fn (-/create-fn endpoint-source))
  (var send-fn
       (fn [frame]
         (var endpoint current-endpoint)
         (when (xt/x:nil? endpoint)
          (when (not (xt/x:is-function? source-create-fn))
            (:= endpoint endpoint-source)))
         (when (xt/x:nil? endpoint)
           (xt/x:err "json endpoint not started"))
         (var raw-write-fn (-/write-fn endpoint))
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
              (var raw-start-fn (-/start-fn current-endpoint))
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
          (:= raw-stop-fn (-/stop-fn endpoint)))
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
