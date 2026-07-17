(ns xt.substrate
  (:require [hara.lang :as l :refer [defspec.xt]]))
(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate.base-util :as base-util]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-common]
             [xt.substrate.page-core :as page]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-space :as node-space]
             [xt.substrate.base-request :as node-request]
             [xt.substrate.base-pubsub :as node-pubsub]
             [xt.substrate.base-util-handlers :as util-handlers]]})

(def.xt ^{:arglists '([node space-id opts])} create-space node-space/create-space)
(def.xt ^{:arglists '([node space-id])} get-space node-space/get-space)
(def.xt ^{:arglists '([node])} list-spaces node-space/list-spaces)
(def.xt ^{:arglists '([node space-id])} get-space-state node-space/get-space-state)
(def.xt ^{:arglists '([node space-id state])} set-space-state node-space/set-space-state)
(def.xt ^{:arglists '([node space-id f])} update-space-state node-space/update-space-state)
(def.xt ^{:arglists '([node space-id])} page-space-get page/space-get-page)
(def.xt ^{:arglists '([node space-id])} page-space-ensure page/space-ensure-page)
(def.xt ^{:arglists '([node space-id runtime])} page-space-set page/space-set-page)
(def.xt ^{:arglists '([node space-id group-id])} page-group-get page/group-get)
(def.xt ^{:arglists '([node space-id group-id])} page-group-ensure page/group-ensure)
(def.xt ^{:arglists '([node space-id group-id model-id])} page-model-ensure page/model-ensure)
(def.xt ^{:arglists '([node space-id group-id models])} page-group-add-attach page/group-add-attach)
(def.xt ^{:arglists '([node space-id group-id models])} page-group-add page/group-add)
(def.xt ^{:arglists '([node space-id group-id])} page-group-remove page/group-remove)
(def.xt ^{:arglists '([node space-id group-id model-id])} page-model-remove page/model-remove)
(def.xt ^{:arglists '([node space-id group-id event])} page-group-update page/group-update)
(def.xt ^{:arglists '([node space-id group-id model-id event])} page-model-update page/model-update)
(def.xt ^{:arglists '([node space-id group-id model-id current event])} page-model-set-input page/model-set-input)
(def.xt ^{:arglists '([node space-id group-id signal event])} page-group-trigger page/group-trigger)
(def.xt ^{:arglists '([node space-id group-id model-id signal event])} page-model-trigger page/model-trigger)
(def.xt ^{:arglists '([node space-id signal event])} page-space-trigger-all page/space-trigger-all)
(def.xt ^{:arglists '([node space-id])} page-raw-callback-add page/raw-callback-add)
(def.xt ^{:arglists '([node space-id])} page-raw-callback-remove page/raw-callback-remove)
(def.xt ^{:arglists '([node])} page-proxy-install page-proxy/install)
(def.xt ^{:arglists '([node space-id opts])} page-proxy-list page-proxy/group-list-proxy)
(def.xt ^{:arglists '([node space-id group-id opts])} page-proxy-open page-proxy/group-open-proxy)
(def.xt ^{:arglists '([node space-id group-id opts])} page-proxy-close page-proxy/group-close-proxy)
(def.xt ^{:arglists '([node space-id group-id model-id args save-output opts])} page-proxy-call page-proxy/model-proxy-call)
(def.xt ^{:arglists '([node space-id group-id opts])} page-proxy-sync page-proxy/group-sync-proxy)

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

(defspec.xt node-configure
  [:fn [EventNode
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   EventNode])

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

(defspec.xt list-subscriptions
  [:fn [EventNode
        [:xt/maybe :xt/str]
        [:xt/maybe :xt/str]]
   [:or router/RouterSubscriptions
    router/RouterSpaceSubscriptions
    [:xt/array :xt/str]]])

(defspec.xt broadcast-transport
  [:fn [EventNode
        frame/NodeFrame
        [:xt/maybe :xt/str]]
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
  "removes a shared service from the node"
  {:added "4.1"}
  [node service-id]
  (var service (-/get-service node service-id))
  (xt/x:del-key (xt/x:get-key node "services")
                service-id)
  (return service))

(defn.xt transport-get
  "gets an attached transport"
  {:added "4.1"}
  [node transport-id]
  (return (base-util/transport-get node transport-id)))

(defn.xt transport-list
  "lists active transport ids"
  {:added "4.1"}
  [node]
  (return (base-util/transport-list node)))

(defn.xt transport-send
  "sends frames through a transport"
  {:added "4.1"}
  [node transport-id frame]
  (return (base-util/transport-send node transport-id frame)))

(defn.xt list-subscriptions
  "lists router subscriptions"
  {:added "4.1"}
  [node space signal]
  (return (router/list-subscriptions node space signal)))

(defn.xt publish
  "publishes a stream frame through node core and subscribed transports"
  {:added "4.1"}
  [node space signal data meta]
  (return (base-util/publish node space signal data meta)))

(defn.xt list-triggers
  "lists registered stream triggers"
  {:added "4.1"}
  [node]
  (return (base-util/list-triggers node)))

(defn.xt register-trigger
  "registers a shared stream trigger"
  {:added "4.1"}
  [node signal trigger-fn meta]
  (return (base-util/register-trigger node signal trigger-fn meta)))

(defn.xt get-trigger
  "gets a shared stream trigger"
  {:added "4.1"}
  [node signal]
  (return (base-util/get-trigger node signal)))

(defn.xt unregister-handler
  "unregisters a shared request handler"
  {:added "4.1"}
  [node action]
  (return (base-util/unregister-handler node action)))

(defn.xt unregister-trigger
  "unregisters a shared stream trigger"
  {:added "4.1"}
  [node signal]
  (return (base-util/unregister-trigger node signal)))

(defn.xt get-handler
  "gets a shared request handler"
  {:added "4.1"}
  [node action]
  (return (base-util/get-handler node action)))

(defn.xt list-handlers
  "lists registered request handlers"
  {:added "4.1"}
  [node]
  (return (base-util/list-handlers node)))

(defn.xt register-handler
  "registers a shared request handler"
  {:added "4.1"}
  [node action handler meta]
  (return (base-util/register-handler node action handler meta)))

(defn.xt request
  "issues a request locally or over an attached transport"
  {:added "4.1"}
  [node space action args meta]
  (return (base-util/request node space action args meta)))

(defn.xt broadcast-transport
  "broadcasts a frame across attached transports"
  {:added "4.1"}
  [node frame exclude-id]
  (return (base-util/transport-broadcast-loop node
                                      (-/transport-list node)
                                      frame
                                      exclude-id
                                      0)))

(defn.xt route-stream
  "routes a stream to transports subscribed for the matching space and signal"
  {:added "4.1"}
  [node stream exclude-id]
  (return (base-util/stream-route-loop node
                                       (router/target-ids node
                                                          (xt/x:get-key stream "space")
                                                          (xt/x:get-key stream "signal"))
                                       stream
                                       exclude-id
                                       0)))

(defn.xt receive-request
  "receives and handles an inbound request"
  {:added "4.1"}
  [node request ctx]
  (:= ctx (or ctx {}))
  (base-util/request-context-merge request ctx)
  (try
    (return
     (promise/x:promise-catch
      (promise/x:promise-then
       (node-request/invoke-handler node request)
       (fn [data]
         (return (base-util/response-ok node request data nil ctx))))
      (fn [err]
        (return (base-util/response-error node request err nil ctx)))))
    (catch err
        (return (base-util/response-error node request err nil ctx)))))

(defn.xt receive-response
  "receives an inbound response"
  {:added "4.1"}
  [node response]
  (node-request/settle-pending node response)
  (return (promise/x:promise-run response)))

(defn.xt subscribe
  "constructs and optionally sends a subscribe control frame"
  {:added "4.1"}
  [node space signal subscription-id meta]
  (:= meta (or meta {}))
  (var event (router/subscribe-frame space signal subscription-id meta))
  (var target (base-util/transport-request-target node meta))
  (if (xt/x:nil? target)
    (return (promise/x:promise-run event))
    (return
     (promise/x:promise-then
      (-/transport-send node target event)
      (fn [_]
        (return event))))))

(defn.xt unsubscribe
  "constructs and optionally sends an unsubscribe control frame"
  {:added "4.1"}
  [node space signal subscription-id meta]
  (:= meta (or meta {}))
  (var event (router/unsubscribe-frame space signal subscription-id meta))
  (var target (base-util/transport-request-target node meta))
  (if (xt/x:nil? target)
    (return (promise/x:promise-run event))
    (return
     (promise/x:promise-then
      (-/transport-send node target event)
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

(defn.xt node-configure
  "applies declarative spaces, handlers, and triggers to a node"
  {:added "4.1"}
  [node opts]
  (:= opts (or opts {}))
  (util-handlers/install-util-handlers node)
  (xt/for:object [[space-id config] (or (xt/x:get-key opts "spaces") {})]
    (node-space/create-space node
                             space-id
                             (base-util/config-normalize-space space-id config)))
  (xt/for:object [[action config] (or (xt/x:get-key opts "handlers") {})]
    (var entry (base-util/config-normalize-handler action config))
    (-/register-handler node
                        action
                        (xt/x:get-key entry "fn")
                        (xt/x:get-key entry "meta")))
  (xt/for:object [[signal config] (or (xt/x:get-key opts "triggers") {})]
    (var entry (base-util/config-normalize-trigger signal config))
    (-/register-trigger node
                        signal
                        (xt/x:get-key entry "fn")
                        (xt/x:get-key entry "meta")))
  (return node))

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
         (base-util/node-base-opts opts))))
  (-/node-configure node opts)
  (return node))
