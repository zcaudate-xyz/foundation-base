(ns xt.event.node-main
  (:require [hara.lang :as l]
            [hara.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-common]
             [xt.event.node-frame :as frame]
             [xt.event.node-router :as router]
             [xt.event.node-space :as node-space]
             [xt.event.node-request :as node-request]
             [xt.event.node-pubsub :as node-pubsub]]})

(defn.xt node?
  "checks if a value is a node runtime"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "event.node"
                   (xt/x:get-key obj "::")))))

(defn.xt transport?
  "checks if a value is a node transport"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "event.node.transport"
                   (xt/x:get-key obj "::")))))

(defn.xt transport-create
  "wraps a transport implementation"
  {:added "4.1"}
  [transport-id impl]
  (return
   (xt/x:obj-assign
    {"::" "event.node.transport"
     :id transport-id
     :listener nil}
    (or impl {}))))

(defn.xt node-create
  "creates a transport-agnostic node runtime"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (return
   (event-common/blank-container
    "event.node"
    (xt/x:obj-assign
     {:id (or (xt/x:get-key opts "id")
              (frame/rand-id "node-" 6))
       :spaces {}
       :handlers {}
       :triggers {}
       :pending {}
       :router {:connections {}
                :subscriptions {}}
       :transports {}
       :meta (or (xt/x:get-key opts "meta") {})}
      opts))))

(defn.xt register-handler
  "registers a shared request handler"
  {:added "4.1"}
  [node action handler meta]
  (var entry {:id action
              :fn handler
              :meta (or meta {})})
  (xt/x:set-key (xt/x:get-key node "handlers")
                action
                entry)
  (return entry))

(defn.xt unregister-handler
  "unregisters a shared request handler"
  {:added "4.1"}
  [node action]
  (var handlers (xt/x:get-key node "handlers"))
  (var prev (xt/x:get-key handlers action))
  (xt/x:del-key handlers action)
  (return prev))

(defn.xt get-handler
  "gets a shared request handler"
  {:added "4.1"}
  [node action]
  (return (xt/x:get-key (xt/x:get-key node "handlers")
                        action)))

(defn.xt list-handlers
  "lists registered request handlers"
  {:added "4.1"}
  [node]
  (return (xt/x:obj-keys (xt/x:get-key node "handlers"))))

(defn.xt register-trigger
  "registers a shared stream trigger"
  {:added "4.1"}
  [node signal trigger-fn meta]
  (var entry {:id signal
              :fn trigger-fn
              :meta (or meta {})})
  (xt/x:set-key (xt/x:get-key node "triggers")
                signal
                entry)
  (return entry))

(defn.xt unregister-trigger
  "unregisters a shared stream trigger"
  {:added "4.1"}
  [node signal]
  (var triggers (xt/x:get-key node "triggers"))
  (var prev (xt/x:get-key triggers signal))
  (xt/x:del-key triggers signal)
  (return prev))

(defn.xt get-trigger
  "gets a shared stream trigger"
  {:added "4.1"}
  [node signal]
  (return (xt/x:get-key (xt/x:get-key node "triggers")
                        signal)))

(defn.xt list-triggers
  "lists registered stream triggers"
  {:added "4.1"}
  [node]
  (return (xt/x:obj-keys (xt/x:get-key node "triggers"))))

(defn.xt get-transport
  "gets an attached transport"
  {:added "4.1"}
  [node transport-id]
  (return (xt/x:get-key (xt/x:get-key node "transports")
                        transport-id)))

(defn.xt list-transports
  "lists attached transports"
  {:added "4.1"}
  [node]
  (return (xt/x:obj-keys (xt/x:get-key node "transports"))))

(defn.xt list-subscriptions
  "lists router subscriptions"
  {:added "4.1"}
  [node space signal]
  (return (router/list-subscriptions node space signal)))

(defn.xt send-transport
  "sends a frame through one transport"
  {:added "4.1"}
  [node transport-id frame]
  (var transport (-/get-transport node transport-id))
  (when (xt/x:nil? transport)
    (xt/x:err (xt/x:cat "transport not found - " transport-id)))
  (var send-fn (xt/x:get-key transport "send-fn"))
  (when (xt/x:nil? send-fn)
    (xt/x:err (xt/x:cat "transport missing send-fn - " transport-id)))
  (return (node-request/ensure-promise
           (send-fn frame))))

(defn.xt broadcast-transport-loop
  "internal helper for broadcasting across transports"
  {:added "4.1"}
  [node ids frame exclude-id index]
  (when (>= index (xt/x:len ids))
    (return (promise/x:promise-run frame)))
  (var transport-id (xt/x:get-idx ids index))
  (if (== transport-id exclude-id)
    (return (-/broadcast-transport-loop node ids frame exclude-id (+ index 1)))
    (return
     (promise/x:promise-then
      (-/send-transport node transport-id frame)
      (fn [_]
        (return (-/broadcast-transport-loop node ids frame exclude-id (+ index 1))))))))

(defn.xt broadcast-transport
  "broadcasts a frame across attached transports"
  {:added "4.1"}
  [node frame exclude-id]
  (return (-/broadcast-transport-loop node
                                      (-/list-transports node)
                                      frame
                                      exclude-id
                                      0)))

(defn.xt route-stream-loop
  "internal helper for routing streams to subscribed transports"
  {:added "4.1"}
  [node ids frame exclude-id index]
  (when (>= index (xt/x:len ids))
    (return (promise/x:promise-run frame)))
  (var transport-id (xt/x:get-idx ids index))
  (if (== transport-id exclude-id)
    (return (-/route-stream-loop node ids frame exclude-id (+ index 1)))
    (return
     (promise/x:promise-then
      (-/send-transport node transport-id frame)
      (fn [_]
        (return (-/route-stream-loop node ids frame exclude-id (+ index 1))))))))

(defn.xt route-stream
  "routes a stream to transports subscribed for the matching space and signal"
  {:added "4.1"}
  [node stream exclude-id]
  (return (-/route-stream-loop node
                               (router/target-ids node
                                                  (xt/x:get-key stream "space")
                                                  (xt/x:get-key stream "signal"))
                               stream
                               exclude-id
                               0)))

(defn.xt attach-transport
  "attaches and optionally starts a transport"
  {:added "4.1"}
  [node transport-id transport]
  (:= transport (:? (-/transport? transport)
                    transport
                    (-/transport-create transport-id transport)))
  (xt/x:set-key (xt/x:get-key node "transports")
                transport-id
                transport)
  (router/register-connection node
                              transport-id
                              {:meta (xt/x:get-key transport "meta")})
  (var start-fn (xt/x:get-key transport "start-fn"))
  (when (xt/x:nil? start-fn)
    (return (promise/x:promise-run transport)))
  (return
   (promise/x:promise-then
    (node-request/ensure-promise
     (start-fn
      (fn [event ctx]
        (:= ctx (or ctx {}))
        (when (xt/x:nil? (xt/x:get-key ctx "transport-id"))
          (xt/x:set-key ctx "transport-id" transport-id))
        (return (-/receive-frame node event ctx)))))
    (fn [listener]
      (xt/x:set-key transport "listener" listener)
      (return transport)))))

(defn.xt detach-transport
  "detaches and optionally stops a transport"
  {:added "4.1"}
  [node transport-id]
  (var transports (xt/x:get-key node "transports"))
  (var transport (xt/x:get-key transports transport-id))
  (when (xt/x:nil? transport)
    (return (promise/x:promise-run nil)))
  (xt/x:del-key transports transport-id)
  (router/unregister-connection node transport-id)
  (var stop-fn (xt/x:get-key transport "stop-fn"))
  (when (xt/x:nil? stop-fn)
    (return (promise/x:promise-run transport)))
  (return
   (promise/x:promise-then
    (node-request/ensure-promise
     (stop-fn (xt/x:get-key transport "listener")))
    (fn [_]
      (return transport)))))

(defn.xt request-target
  "resolves the outbound request target transport"
  {:added "4.1"}
  [node meta]
  (var target (xt/x:get-key meta "transport-id"))
  (when (xt/x:not-nil? target)
    (return target))
  (return (xt/x:get-idx (-/list-transports node) 0)))

(defn.xt respond-ok
  "constructs and optionally sends a successful response"
  {:added "4.1"}
  [node request data meta ctx]
  (var response (frame/response-ok-frame
                 (xt/x:get-key request "id")
                 (xt/x:get-key request "space")
                 data
                 meta))
  (var transport-id (xt/x:get-key ctx "transport-id"))
  (if (xt/x:nil? transport-id)
    (return (promise/x:promise-run response))
    (return
     (promise/x:promise-then
      (-/send-transport node transport-id response)
      (fn [_]
        (return response))))))

(defn.xt respond-error
  "constructs and optionally sends an errored response"
  {:added "4.1"}
  [node request error meta ctx]
  (var response (frame/response-error-frame
                 (xt/x:get-key request "id")
                 (xt/x:get-key request "space")
                 error
                 meta))
  (var transport-id (xt/x:get-key ctx "transport-id"))
  (if (xt/x:nil? transport-id)
    (return (promise/x:promise-run response))
    (return
     (promise/x:promise-then
      (-/send-transport node transport-id response)
      (fn [_]
        (return response))))))

(defn.xt receive-request
  "receives and handles an inbound request"
  {:added "4.1"}
  [node request ctx]
  (:= ctx (or ctx {}))
  (try
    (return
     (promise/x:promise-catch
      (promise/x:promise-then
       (node-request/invoke-handler node request)
       (fn [data]
         (return (-/respond-ok node request data nil ctx))))
      (fn [err]
        (return (-/respond-error node request err nil ctx)))))
    (catch err
      (return (-/respond-error node request err nil ctx)))))

(defn.xt receive-response
  "receives an inbound response"
  {:added "4.1"}
  [node response]
  (node-request/settle-pending node response)
  (return (promise/x:promise-run response)))

(defn.xt request
  "issues a request locally or over an attached transport"
  {:added "4.1"}
  [node space action args meta]
  (:= meta (or meta {}))
  (var request-frame (frame/request-frame space action args meta))
  (var target (-/request-target node meta))
  (if (xt/x:nil? target)
    (return
     (node-request/response-body
      (-/receive-request node request-frame nil)))
    (try
      (return
       (promise/x:promise
        (fn [resolve reject]
          (node-request/add-pending node
                                    request-frame
                                    resolve
                                    reject
                                    {:transport-id target})
          (promise/x:promise-catch
           (promise/x:promise-then
            (-/send-transport node target request-frame)
            (fn [_]
              (return request-frame)))
           (fn [err]
             (node-request/remove-pending node
                                          (xt/x:get-key request-frame "id"))
             (reject err))))))
      (catch err
        (return
         (promise/x:promise
           (fn [resolve reject]
             (reject err))))))))

(defn.xt subscribe
  "constructs and optionally sends a subscribe control frame"
  {:added "4.1"}
  [node space signal subscription-id meta]
  (:= meta (or meta {}))
  (var event (router/subscribe-frame space signal subscription-id meta))
  (var target (-/request-target node meta))
  (if (xt/x:nil? target)
    (return (promise/x:promise-run event))
    (return
     (promise/x:promise-then
      (-/send-transport node target event)
      (fn [_]
        (return event))))))

(defn.xt unsubscribe
  "constructs and optionally sends an unsubscribe control frame"
  {:added "4.1"}
  [node space signal subscription-id meta]
  (:= meta (or meta {}))
  (var event (router/unsubscribe-frame space signal subscription-id meta))
  (var target (-/request-target node meta))
  (if (xt/x:nil? target)
    (return (promise/x:promise-run event))
    (return
     (promise/x:promise-then
      (-/send-transport node target event)
      (fn [_]
        (return event))))))

(defn.xt publish
  "publishes a stream frame through node core and subscribed transports"
  {:added "4.1"}
  [node space signal data meta]
  (:= meta (or meta {}))
  (var stream (frame/stream-frame space
                                  signal
                                  data
                                  meta
                                  (xt/x:get-key meta "cause")))
  (return
   (-/receive-publish node
                      stream
                      {:transport-id (xt/x:get-key meta "transport-id")})))

(defn.xt receive-publish
  "receives an inbound stream frame"
  {:added "4.1"}
  [node stream ctx]
  (:= ctx (or ctx {}))
  (return
   (promise/x:promise-then
    (node-pubsub/receive-publish node stream)
    (fn [_]
      (return (-/route-stream node
                              stream
                              (xt/x:get-key ctx "transport-id")))))))

(defn.xt receive-frame
  "demultiplexes node frames"
  {:added "4.1"}
  [node event ctx]
  (var kind (xt/x:get-key event "kind"))
  (cond (== kind frame/KIND_REQUEST)
        (return (-/receive-request node event ctx))

        (== kind frame/KIND_RESPONSE)
        (return (-/receive-response node event))

        (== kind frame/KIND_STREAM)
        (return (-/receive-publish node event ctx))

        (== kind router/KIND_SUBSCRIBE)
        (return (router/receive-subscribe node event ctx))

        (== kind router/KIND_UNSUBSCRIBE)
        (return (router/receive-unsubscribe node event ctx))

        :else
        (return (promise/x:promise-run event))))
