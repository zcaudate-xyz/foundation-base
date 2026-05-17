(ns xt.event.node-transport-json
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.event.node-main :as main]
             [xt.event.node-json :as node-json]]})

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
