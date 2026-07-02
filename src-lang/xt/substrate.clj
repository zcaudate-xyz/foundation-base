(ns xt.substrate
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-common]
             [xt.substrate.page-core :as page]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-space :as node-space]
             [xt.substrate.base-request :as node-request]
             [xt.substrate.base-pubsub :as node-pubsub]]})

(def.xt create-space node-space/create-space)
(def.xt get-space node-space/get-space)
(def.xt list-spaces node-space/list-spaces)
(def.xt get-space-state node-space/get-space-state)
(def.xt set-space-state node-space/set-space-state)
(def.xt update-space-state node-space/update-space-state)
(def.xt get-space-page page/get-space-page)
(def.xt ensure-space-page page/ensure-space-page)
(def.xt set-space-page page/set-space-page)
(def.xt page-group-get page/group-get)
(def.xt page-group-ensure page/group-ensure)
(def.xt page-model-ensure page/model-ensure)
(def.xt page-add-group-attach page/add-group-attach)
(def.xt page-add-group page/add-group)
(def.xt page-remove-group page/remove-group)
(def.xt page-remove-model page/remove-model)
(def.xt page-group-update page/group-update)
(def.xt page-model-update page/model-update)
(def.xt page-model-set-input page/model-set-input)
(def.xt page-trigger-group page/trigger-group)
(def.xt page-trigger-model page/trigger-model)
(def.xt page-trigger-all page/trigger-all)
(def.xt add-raw-page-callback page/add-raw-callback)
(def.xt remove-raw-page-callback page/remove-raw-callback)

(defspec.xt NodeTriggerHandler
  [:fn [node-space/NodeSpace frame/NodeFrame :xt/any] :xt/any])

(defspec.xt NodeHandlerEntry
  [:xt/record
   ["id" :xt/str]
   ["fn" node-request/RequestHandler]
   ["meta" [:xt/maybe [:xt/dict :xt/str :xt/any]]]])

(defspec.xt NodeTriggerEntry
  [:xt/record
   ["id" :xt/str]
   ["fn" NodeTriggerHandler]
   ["meta" [:xt/maybe [:xt/dict :xt/str :xt/any]]]])

(defspec.xt NodeTransportListener
  [:fn [frame/NodeFrame
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt NodeTransportSendFn
  [:fn [frame/NodeFrame] :xt/any])

(defspec.xt NodeTransportStartFn
  [:fn [NodeTransportListener] :xt/any])

(defspec.xt NodeTransportStopFn
  [:fn [:xt/any] :xt/any])

(defspec.xt NodeTransport
  [:xt/record
   ["::" :xt/str]
   ["id" :xt/str]
   ["listener" [:xt/maybe :xt/any]]
   ["meta" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["send_fn" [:xt/maybe NodeTransportSendFn]]
   ["start_fn" [:xt/maybe NodeTransportStartFn]]
   ["stop_fn" [:xt/maybe NodeTransportStopFn]]])

(defspec.xt EventNode
  [:xt/record
   ["::" :xt/str]
   ["id" :xt/str]
   ["listeners" event-common/EventListenerMap]
   ["spaces" [:xt/dict :xt/str node-space/NodeSpace]]
   ["services" [:xt/dict :xt/str :xt/any]]
   ["handlers" [:xt/dict :xt/str NodeHandlerEntry]]
   ["triggers" [:xt/dict :xt/str NodeTriggerEntry]]
   ["pending" [:xt/dict :xt/str node-request/PendingEntry]]
   ["router" router/RouterState]
   ["transports" [:xt/dict :xt/str NodeTransport]]
   ["meta" [:xt/maybe [:xt/dict :xt/str :xt/any]]]])

(defspec.xt node?
  [:fn [:xt/any] :xt/bool])

(defspec.xt transport?
  [:fn [:xt/any] :xt/bool])

(defspec.xt transport-create
  [:fn [:xt/str
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeTransport])

(defspec.xt config-space-opts
  [:fn [:xt/str
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       [:xt/maybe [:xt/dict :xt/str :xt/any]]])

(defspec.xt config-handler-entry
  [:fn [:xt/str
        [:or node-request/RequestHandler
             [:xt/dict :xt/str :xt/any]]]
       NodeHandlerEntry])

(defspec.xt config-trigger-entry
  [:fn [:xt/str
        [:or NodeTriggerHandler
             [:xt/dict :xt/str :xt/any]]]
       NodeTriggerEntry])

(defspec.xt configure-node
  [:fn [EventNode
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       EventNode])

(defspec.xt node-base-opts
  [:fn [[:xt/maybe [:xt/dict :xt/str :xt/any]]]
       [:xt/dict :xt/str :xt/any]])

(defspec.xt node-create
  [:fn [[:xt/maybe [:xt/dict :xt/str :xt/any]]] EventNode])

(defspec.xt register-handler
  [:fn [EventNode
        :xt/str
        node-request/RequestHandler
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeHandlerEntry])

(defspec.xt get-services
  [:fn [EventNode] [:xt/dict :xt/str :xt/any]])

(defspec.xt get-service
  [:fn [EventNode :xt/str] :xt/any])

(defspec.xt set-service
  [:fn [EventNode :xt/str :xt/any] :xt/any])

(defspec.xt unregister-handler
  [:fn [EventNode :xt/str] [:xt/maybe NodeHandlerEntry]])

(defspec.xt get-handler
  [:fn [EventNode :xt/str] [:xt/maybe NodeHandlerEntry]])

(defspec.xt list-handlers
  [:fn [EventNode] [:xt/array :xt/str]])

(defspec.xt register-trigger
  [:fn [EventNode
        :xt/str
        NodeTriggerHandler
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeTriggerEntry])

(defspec.xt unregister-trigger
  [:fn [EventNode :xt/str] [:xt/maybe NodeTriggerEntry]])

(defspec.xt get-trigger
  [:fn [EventNode :xt/str] [:xt/maybe NodeTriggerEntry]])

(defspec.xt list-triggers
  [:fn [EventNode] [:xt/array :xt/str]])

(defspec.xt get-transport
  [:fn [EventNode :xt/str] [:xt/maybe NodeTransport]])

(defspec.xt list-transports
  [:fn [EventNode] [:xt/array :xt/str]])

(defspec.xt list-subscriptions
  [:fn [EventNode
        [:xt/maybe :xt/str]
        [:xt/maybe :xt/str]]
       [:or router/RouterSubscriptions
            router/RouterSpaceSubscriptions
            [:xt/array :xt/str]]])

(defspec.xt send-transport
  [:fn [EventNode :xt/str frame/NodeFrame] :xt/promise])

(defspec.xt broadcast-transport-loop
  [:fn [EventNode
        [:xt/array :xt/str]
        frame/NodeFrame
        [:xt/maybe :xt/str]
        :xt/int]
       :xt/promise])

(defspec.xt broadcast-transport
  [:fn [EventNode
        frame/NodeFrame
        [:xt/maybe :xt/str]]
       :xt/promise])

(defspec.xt route-stream-loop
  [:fn [EventNode
        [:xt/array :xt/str]
        frame/NodeFrame
        [:xt/maybe :xt/str]
        :xt/int]
       :xt/promise])

(defspec.xt route-stream
  [:fn [EventNode
        frame/NodeFrame
        [:xt/maybe :xt/str]]
       :xt/promise])

(defspec.xt attach-transport
  [:fn [EventNode
        :xt/str
        [:or NodeTransport
             [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt detach-transport
  [:fn [EventNode :xt/str] :xt/promise])

(defspec.xt request-target
  [:fn [EventNode
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       [:xt/maybe :xt/str]])

(defspec.xt await-pending
  [:fn [[:xt/dict :xt/str :xt/any]] :xt/promise])

(defspec.xt request-context
  [:fn [frame/NodeFrame
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   frame/NodeFrame])

(defspec.xt respond-ok
  [:fn [EventNode
        frame/NodeFrame
        :xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt respond-error
  [:fn [EventNode
        frame/NodeFrame
        :xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt receive-request
  [:fn [EventNode
        frame/NodeFrame
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt receive-response
  [:fn [EventNode frame/NodeFrame] :xt/promise])

(defspec.xt request
  [:fn [EventNode
        [:xt/maybe :xt/str]
        :xt/str
        [:xt/maybe [:xt/array :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt subscribe
  [:fn [EventNode
        [:xt/maybe :xt/str]
        :xt/str
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt unsubscribe
  [:fn [EventNode
        [:xt/maybe :xt/str]
        :xt/str
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt publish
  [:fn [EventNode
        [:xt/maybe :xt/str]
        :xt/str
        :xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt receive-publish
  [:fn [EventNode
        frame/NodeFrame
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])

(defspec.xt receive-frame
  [:fn [EventNode
        frame/NodeFrame
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       :xt/promise])


;; NODE CORE
(defn.xt node?
  "checks if a value is a node runtime"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "substrate"
                   (xt/x:get-key obj "::")))))

(defn.xt transport?
  "checks if a value is a node transport"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "substrate.transport"
                   (xt/x:get-key obj "::")))))

(defn.xt transport-create
  "wraps a transport implementation"
  {:added "4.1"}
  [transport-id impl]
  (return
   (xt/x:obj-assign
    {"::" "substrate.transport"
     :id transport-id
     :listener nil}
    (or impl {}))))

(defn.xt get-services
  "gets registered node services"
  {:added "4.1"}
  [node]
  (return (or (xt/x:get-key node "services")
              {})))

(defn.xt get-service
  "gets a registered node service"
  {:added "4.1"}
  [node service-id]
  (return (xt/x:get-key (-/get-services node)
                        service-id)))

(defn.xt set-service
  "sets a shared service on the node"
  {:added "4.1"}
  [node service-id service]
  (xt/x:set-key (xt/x:get-key node "services")
                service-id
                service)
  (return service))

(defn.xt remove-service
  "sets a shared service on the node"
  {:added "4.1"}
  [node service-id]
  (xt/x:del-key (xt/x:get-key node "services")
                service-id)
  (return service))

;; NODE REGISTRY
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
  (return (xtd/arr-sort (xt/x:obj-keys (xt/x:get-key node "handlers"))
                        (fn [x] (return x))
                        xt/x:str-lt)))

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
  (return (xtd/arr-sort (xt/x:obj-keys (xt/x:get-key node "triggers"))
                        (fn [x] (return x))
                        xt/x:str-lt)))

;; TRANSPORT LOOKUP AND DELIVERY
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
  (return (xtd/arr-sort (xt/x:obj-keys (xt/x:get-key node "transports"))
                        (fn [x] (return x))
                        xt/x:str-lt)))

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
  (var send-fn (xt/x:get-key transport "send_fn"))
  (when (xt/x:nil? send-fn)
    (xt/x:err (xt/x:cat "transport missing send_fn - " transport-id)))
  (return (node-request/ensure-promise
           (send-fn frame))))

(defn.xt broadcast-transport-loop
  "internal helper for broadcasting across transports"
  {:added "4.1"}
  [node ids frame exclude-id index]
  (when (>= index (xt/x:len ids))
    (return (promise/x:promise-run frame)))
  (var transport-id (xt/x:get-idx ids (xt/x:offset index)))
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
  (var transport-id (xt/x:get-idx ids (xt/x:offset index)))
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

;; REQUEST FLOW
(defn.xt request-target
  "resolves the outbound request target transport"
  {:added "4.1"}
  [node meta]
  (var target (xt/x:get-key meta "transport_id"))
  (when (xt/x:not-nil? target)
    (return target))
  (var transports (-/list-transports node))
  (when (== 0 (xt/x:len transports))
    (return nil))
  (return (xt/x:get-idx transports (xt/x:offset 0))))

(defn.xt await-pending
  "waits for a pending request state to settle"
  {:added "4.1"}
  [state]
  (var status (xt/x:get-key state "status"))
  (cond (== status "resolved")
        (return (promise/x:promise-run
                 (xt/x:get-key state "value")))

        (== status "rejected")
        (return
         (promise/x:promise
          (fn []
            (xt/x:throw (xt/x:get-key state "error")))))

        :else
        (return
         (promise/x:promise-then
          (xt/x:async-run
           (fn []
             (return nil)))
          (fn [_]
            (return (-/await-pending state)))))))

(defn.xt request-context
  "merges transport context into a request frame before handler invocation"
  {:added "4.1"}
  [request ctx]
  (:= ctx (or ctx {}))
  (var meta (xt/x:get-key request "meta"))
  (when (xt/x:nil? meta)
    (:= meta {})
    (xt/x:set-key request "meta" meta))
  (when (xt/x:not-nil? (xt/x:get-key ctx "transport_id"))
    (xt/x:set-key meta
                 "transport_id"
                 (xt/x:get-key ctx "transport_id")))
  (return request))

(defn.xt respond-ok
  "constructs and optionally sends a successful response"
  {:added "4.1"}
  [node request data meta ctx]
  (var response (frame/response-ok-frame
                 (xt/x:get-key request "id")
                 (xt/x:get-key request "space")
                 data
                 meta))
  (var transport-id (xt/x:get-key ctx "transport_id"))
  (when (xt/x:nil? transport-id)
    (var request-meta (xt/x:get-key request "meta"))
    (when (xt/x:not-nil? request-meta)
      (:= transport-id (xt/x:get-key request-meta "transport_id"))))
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
  (var transport-id (xt/x:get-key ctx "transport_id"))
  (when (xt/x:nil? transport-id)
    (var request-meta (xt/x:get-key request "meta"))
    (when (xt/x:not-nil? request-meta)
      (:= transport-id (xt/x:get-key request-meta "transport_id"))))
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
  (-/request-context request ctx)
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
     (node-request/invoke-handler node request-frame))
    (try
      (var pending-state {"status" "pending"
                          "value" nil
                          "error" nil})
      (node-request/add-pending node
                                request-frame
                                (fn [value]
                                  (xt/x:set-key pending-state "status" "resolved")
                                  (xt/x:set-key pending-state "value" value)
                                  (return value))
                                (fn [err]
                                  (xt/x:set-key pending-state "status" "rejected")
                                  (xt/x:set-key pending-state "error" err)
                                  (return err))
                                {:transport_id target})
      (return
       (promise/x:promise-then
        (promise/x:promise-catch
         (promise/x:promise-then
          (-/send-transport node target request-frame)
          (fn [_]
            (return nil)))
         (fn [err]
           (node-request/remove-pending node
                                        (xt/x:get-key request-frame "id"))
           (xt/x:throw err)))
        (fn [_]
          (return (-/await-pending pending-state)))))
      (catch err
        (return
         (promise/x:promise
          (fn []
            (xt/x:throw err))))))))

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
                              (xt/x:get-key ctx "transport_id")))))))

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
                      {:transport_id (xt/x:get-key meta "transport_id")})))

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

;; TRANSPORT ATTACHMENT
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
  (var start-fn (xt/x:get-key transport "start_fn"))
  (when (xt/x:nil? start-fn)
    (return (promise/x:promise-run transport)))
  (return
   (promise/x:promise-then
    (node-request/ensure-promise
     (start-fn
      (fn [event ctx]
        (:= ctx (or ctx {}))
        (when (xt/x:nil? (xt/x:get-key ctx "transport_id"))
          (xt/x:set-key ctx "transport_id" transport-id))
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
  (var stop-fn (xt/x:get-key transport "stop_fn"))
  (when (xt/x:nil? stop-fn)
    (return (promise/x:promise-run transport)))
  (return
   (promise/x:promise-then
    (node-request/ensure-promise
     (stop-fn (xt/x:get-key transport "listener")))
    (fn [_]
      (return transport)))))

;; CONFIG AND BOOTSTRAP
(defn.xt config-space-opts
  "normalises declarative space config into create-space opts"
  {:added "4.1"}
  [space-id config]
  (cond (xt/x:nil? config)
       (return nil)

       (and (xt/x:is-object? config)
            (or (xt/x:has-key? config "id")
                (xt/x:has-key? config "state")
                (xt/x:has-key? config "meta")))
       (do
         (when (and (xt/x:has-key? config "id")
                    (not (== (xt/x:get-key config "id")
                             space-id)))
           (xt/x:err (xt/x:cat "space id mismatch - " space-id)))
         (return {:state (xt/x:get-key config "state")
                  :meta (or (xt/x:get-key config "meta") {})}))

       :else
       (xt/x:err (xt/x:cat "invalid space config - " space-id))))

(defn.xt config-handler-entry
  "normalises declarative handler config into register-handler input"
  {:added "4.1"}
  [action config]
  (cond (xt/x:is-function? config)
       (return {:fn config
                :meta {}})

       (and (xt/x:is-object? config)
            (xt/x:is-function? (xt/x:get-key config "fn")))
       (do
         (when (and (xt/x:has-key? config "id")
                    (not (== (xt/x:get-key config "id")
                             action)))
           (xt/x:err (xt/x:cat "handler id mismatch - " action)))
         (return {:fn (xt/x:get-key config "fn")
                  :meta (or (xt/x:get-key config "meta") {})}))

       :else
       (xt/x:err (xt/x:cat "invalid handler config - " action))))

(defn.xt config-trigger-entry
  "normalises declarative trigger config into register-trigger input"
  {:added "4.1"}
  [signal config]
  (cond (xt/x:is-function? config)
       (return {:fn config
                :meta {}})

       (and (xt/x:is-object? config)
            (xt/x:is-function? (xt/x:get-key config "fn")))
       (do
         (when (and (xt/x:has-key? config "id")
                    (not (== (xt/x:get-key config "id")
                             signal)))
           (xt/x:err (xt/x:cat "trigger id mismatch - " signal)))
         (return {:fn (xt/x:get-key config "fn")
                  :meta (or (xt/x:get-key config "meta") {})}))

       :else
       (xt/x:err (xt/x:cat "invalid trigger config - " signal))))

(defn.xt configure-node
  "applies declarative spaces, handlers, and triggers to a node"
  {:added "4.1"}
  [node opts]
  (:= opts (or opts {}))
  (xt/for:object [[space-id config] (or (xt/x:get-key opts "spaces") {})]
   (node-space/create-space node
                            space-id
                            (-/config-space-opts space-id config)))
  (xt/for:object [[action config] (or (xt/x:get-key opts "handlers") {})]
   (var entry (-/config-handler-entry action config))
   (-/register-handler node
                       action
                       (xt/x:get-key entry "fn")
                       (xt/x:get-key entry "meta")))
  (xt/for:object [[signal config] (or (xt/x:get-key opts "triggers") {})]
   (var entry (-/config-trigger-entry signal config))
   (-/register-trigger node
                       signal
                       (xt/x:get-key entry "fn")
                       (xt/x:get-key entry "meta")))
  (return node))

(defn.xt node-base-opts
  "removes declarative config keys before constructing node state"
  {:added "4.1"}
  [opts]
  (var base (xt/x:obj-clone (or opts {})))
  (xt/x:del-key base "spaces")
  (xt/x:del-key base "handlers")
  (xt/x:del-key base "triggers")
  (return base))

(defn.xt node-create
  "creates a transport-agnostic node runtime, optionally from declarative config"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (var node
   (event-common/blank-container
    "substrate"
    (xt/x:obj-assign
     {:id (or (xt/x:get-key opts "id")
              (frame/rand-id "node-" 6))
      :spaces {}
      :services {}
      :handlers {}
      :triggers {}
      :pending {}
      :router {:connections {}
               :subscriptions {}}
      :transports {}
      :meta (or (xt/x:get-key opts "meta") {})}
     (-/node-base-opts opts))))
  (-/configure-node node opts)
  (return node))
