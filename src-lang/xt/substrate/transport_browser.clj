(ns xt.substrate.transport-browser
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.substrate :as main]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defspec.xt NodeTransportConnection
  [:xt/record
   ["node" main/EventNode]
   ["transport_id" :xt/str]
   ["transport" main/NodeTransport]
   ["target" [:xt/maybe :xt/any]]
   ["ready" [:xt/maybe :xt/any]]
   ["disconnect_fn" [:xt/maybe [:fn [] :xt/promise]]]])

(defspec.xt event-data
  [:fn [:xt/any] :xt/any])

(defspec.xt messageport-endpoint
  [:fn [:xt/any] main/NodeTransport])

(defspec.xt sharedworker-endpoint
  [:fn [:xt/any] main/NodeTransport])

(defspec.xt worker-endpoint
  [:fn [:xt/any] main/NodeTransport])

(defspec.xt self-endpoint
  [:fn [:xt/any] main/NodeTransport])

(defspec.xt connect-port
  [:fn [main/EventNode
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt connect-sharedworker
  [:fn [main/EventNode
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt connect-worker
  [:fn [main/EventNode
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt boot-self
  [:fn [main/EventNode
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   :xt/promise])

(defspec.xt disconnect
  [:fn [NodeTransportConnection] :xt/promise])

(defspec.xt blob-url
  [:fn [:xt/str] :xt/str])

(defspec.xt webworker-source
  [:fn [:xt/str] :xt/any])

(defspec.xt sharedworker-source
  [:fn [:xt/str [:xt/maybe [:xt/dict :xt/str :xt/any]]] :xt/any])

(defspec.xt sharedworker-url-source
  [:fn [:xt/str] :xt/any])

(defspec.xt node-worker-source
  [:fn [:xt/str [:xt/maybe [:xt/dict :xt/str :xt/any]]] :xt/any])

(defn.xt ready-event?
  "checks if an inbound event should resolve connection readiness"
  {:added "4.1"}
  [event opts]
  (var ready-pred (. opts ["ready_pred"]))
  (cond (xt/x:is-function? ready-pred)
        (return (ready-pred event))

        :else
        (do (var ready-signal (:? (xt/x:has-key? opts "ready_signal")
                                  (. opts ["ready_signal"])
                                  "ready"))
            (if (xt/x:nil? ready-signal)
              (return false)
              (return (and (xt/x:is-object? event)
                           (== (. event ["signal"])
                               ready-signal)))))))

(defn.xt await-ready
  "waits until a connection state records its ready event"
  {:added "4.1"}
  [state]
  (var ready (. state ["ready"]))
  (if (xt/x:not-nil? ready)
    (return (promise/x:promise-run ready))
    (return
     (promise/x:promise-then
      (promise/x:with-delay 10
        (fn []
          (return nil)))
      (fn [_]
        (return (-/await-ready state)))))))

(defn.xt connection-record
  "creates a connection record from an attached transport"
  {:added "4.1"}
  [node transport-id ready]
  (var transport (main/transport-get node transport-id))
  (return
   {"node" node
    "transport_id" transport-id
    "transport" transport
    "target" (:? (xt/x:nil? transport)
                 nil
                 (. transport ["listener"]))
    "ready" ready
    "disconnect_fn" (fn []
                      (return
                       (main/detach-transport node transport-id)))}))

(defn.xt wrap-ready-endpoint
  "suppresses the ready event from node routing and captures it in state"
  {:added "4.1"}
  [endpoint state opts]
  (var wait-ready (:? (xt/x:has-key? opts "wait_ready")
                      (. opts ["wait_ready"])
                      true))
  (if (not wait-ready)
    (return endpoint)
    (do
      (var start-fn (. endpoint ["start_fn"]))
      (if (xt/x:nil? start-fn)
        (return endpoint)
        (return
         (xt/x:obj-assign
          (xt/x:obj-assign {} endpoint)
          {"start_fn"
           (fn [listener]
             (return
              (start-fn
               (fn [event ctx]
                 (if (-/ready-event? event opts)
                   (do
                     (xt/x:set-key state "ready" event)
                     (return event))
                   (return (listener event ctx)))))))}))))))

(defn.xt source-endpoint
  "creates a transport endpoint around a create_fn source"
  {:added "4.1"}
  [source kind]
  (var current-target nil)
  (var send-fn
       (fn [frame]
         (when (xt/x:nil? current-target)
           (xt/x:err "source endpoint not started"))
         (return (. current-target (postMessage frame)))))
  (var start-fn
       (fn [listener]
         (:= current-target
             ((. source ["create_fn"])
              listener))
         (return current-target)))
  (var stop-fn
       (fn [_]
         (when (and (xt/x:not-nil? current-target)
                    (xt/x:is-function? (. current-target ["close"])))
           (. current-target (close)))
         (when (and (xt/x:not-nil? current-target)
                    (xt/x:is-function? (. current-target ["terminate"])))
           (. current-target (terminate)))
         (:= current-target nil)
         (return true)))
  (return
   {"meta" {"kind" kind}
    "send_fn" send-fn
    "start_fn" start-fn
    "stop_fn" stop-fn}))

(defn.xt connect-endpoint
  "attaches an endpoint and resolves when its ready event arrives"
  {:added "4.1"}
  [node transport-id endpoint opts]
  (var config (or opts {}))
  (var state {"ready" nil})
  (var wait-ready (:? (xt/x:has-key? config "wait_ready")
                      (. config ["wait_ready"])
                      true))
  (return
   (promise/x:promise-then
    (main/attach-transport
     node
     transport-id
     (-/wrap-ready-endpoint endpoint state config))
    (fn [_]
      (if wait-ready
        (return
         (promise/x:promise-then
          (-/await-ready state)
          (fn [ready]
            (return (-/connection-record node transport-id ready)))))
        (return (-/connection-record node transport-id nil)))))))

(defn.xt event-data
  "normalizes a browser worker message event into its payload"
  {:added "4.1"}
  [event]
  (return (:? (and (xt/x:is-object? event)
                   (xt/x:has-key? event "data"))
              (. event ["data"])
              event)))

(defn.xt messageport-endpoint
  "adapts a MessagePort-like endpoint to the node transport contract"
  {:added "4.1"}
  [port]
  (var current-callback nil)
  (var send-fn
       (fn [frame]
         (return (. port (postMessage frame)))))
  (var start-fn
       (fn [listener]
         (:= current-callback
             (fn [event]
               (return (listener (-/event-data event) nil))))
         (when (xt/x:is-function? (. port ["start"]))
           (. port (start)))
         (. port (addEventListener
                  "message"
                  current-callback
                  false))
         (return port)))
  (var stop-fn
       (fn [_]
         (when (and (xt/x:not-nil? current-callback)
                    (xt/x:is-function? (. port ["removeEventListener"])))
           (. port (removeEventListener
                    "message"
                    current-callback
                    false)))
         (when (xt/x:is-function? (. port ["close"]))
           (. port (close)))
         (:= current-callback nil)
         (return true)))
  (return
   {"meta" {"kind" "messageport"}
    "send_fn" send-fn
    "start_fn" start-fn
    "stop_fn" stop-fn}))

(defn.xt sharedworker-endpoint
  "adapts a SharedWorker or SharedWorker port to the node transport contract"
  {:added "4.1"}
  [shared-or-port]
  (var port (:? (xt/x:has-key? shared-or-port "port")
                (. shared-or-port ["port"])
                shared-or-port))
  (return (-/messageport-endpoint port)))

(defn.xt worker-endpoint
  "adapts a host-side Worker or create-fn source to the node transport contract"
  {:added "4.1"}
  [worker-source]
  (var current-worker nil)
  (var current-callback nil)
  (var send-fn
       (fn [frame]
         (var worker (or current-worker
                         (:? (xt/x:has-key? worker-source "create_fn")
                             nil
                             worker-source)))
         (when (xt/x:nil? worker)
           (xt/x:err "worker endpoint not started"))
         (var post-request (. worker ["postRequest"]))
         (if (xt/x:is-function? post-request)
           (return (post-request frame))
           (return (. worker (postMessage frame))))))
  (var start-fn
       (fn [listener]
         (if (xt/x:has-key? worker-source "create_fn")
           (do (:= current-worker
                   ((. worker-source ["create_fn"])
                    listener))
               (return current-worker))
           (do (:= current-worker worker-source)
               (:= current-callback
                   (fn [event]
                     (return (listener (-/event-data event) nil))))
               (. current-worker (addEventListener
                                  "message"
                                  current-callback
                                  false))
               (return current-worker)))))
  (var stop-fn
       (fn [_]
         (when (and (xt/x:not-nil? current-worker)
                    (xt/x:not-nil? current-callback)
                    (xt/x:is-function? (. current-worker ["removeEventListener"])))
           (. current-worker (removeEventListener
                              "message"
                              current-callback
                              false)))
         (when (and (xt/x:not-nil? current-worker)
                    (xt/x:is-function? (. current-worker ["terminate"])))
           (. current-worker (terminate)))
         (:= current-worker nil)
         (:= current-callback nil)
         (return true)))
  (return
   {"meta" {"kind" "webworker"}
    "send_fn" send-fn
    "start_fn" start-fn
    "stop_fn" stop-fn}))

(defn.xt self-endpoint
  "adapts worker self to the node transport contract"
  {:added "4.1"}
  [worker-self]
  (var current-callback nil)
  (var send-fn
       (fn [frame]
         (. worker-self (postMessage frame))
         (return (promise/x:promise-run true))))
  (var start-fn
       (fn [listener]
         (:= current-callback
             (fn [event]
               (return (listener (-/event-data event) nil))))
         (. worker-self (addEventListener
                         "message"
                         current-callback
                         false))
         (return worker-self)))
  (var stop-fn
       (fn [_]
         (when (and (xt/x:not-nil? current-callback)
                    (xt/x:is-function? (. worker-self ["removeEventListener"])))
           (. worker-self (removeEventListener
                           "message"
                           current-callback
                           false)))
         (:= current-callback nil)
         (return true)))
  (return
   {"meta" {"kind" "webworker.self"}
    "send_fn" send-fn
    "start_fn" start-fn
    "stop_fn" stop-fn}))

(defn.xt connect-port
  "attaches a MessagePort-like endpoint and waits for readiness"
  {:added "4.1"}
  [node opts]
  (var config (or opts {}))
  (var port (or (. config ["port"])
               (. config ["source"])))
  (var transport-id (or (. config ["transport_id"])
                       "port"))
  (when (xt/x:nil? port)
    (xt/x:err "connect-port requires `port` or `source`"))
  (return
   (-/connect-endpoint
    node
    transport-id
    (-/messageport-endpoint port)
    config)))

(defn.xt connect-sharedworker
  "attaches a SharedWorker endpoint and waits for readiness"
  {:added "4.1"}
  [node opts]
  (var config (or opts {}))
  (var source (or (. config ["source"])
                 (. config ["sharedworker"])))
  (var transport-id (or (. config ["transport_id"])
                       "worker"))
  (when (xt/x:nil? source)
    (xt/x:err "connect-sharedworker requires `source` or `sharedworker`"))
  (return
   (-/connect-endpoint
    node
    transport-id
    (:? (xt/x:has-key? source "create_fn")
       (-/source-endpoint source "sharedworker")
       (-/sharedworker-endpoint source))
    config)))

(defn.xt connect-worker
  "attaches a Worker endpoint and waits for readiness"
  {:added "4.1"}
  [node opts]
  (var config (or opts {}))
  (var source (or (. config ["source"])
                 (. config ["worker"])))
  (var transport-id (or (. config ["transport_id"])
                       "worker"))
  (when (xt/x:nil? source)
    (xt/x:err "connect-worker requires `source` or `worker`"))
  (return
   (-/connect-endpoint
    node
    transport-id
    (:? (xt/x:has-key? source "create_fn")
       (-/source-endpoint source "webworker")
       (-/worker-endpoint source))
    config)))

(defn.xt boot-self
  "attaches worker self/port and optionally emits a ready payload"
  {:added "4.1"}
  [node opts]
  (var config (or opts {}))
  (var target (. config ["target"]))
  (var transport-id (or (. config ["transport_id"])
                       "host"))
  (var ready (. config ["ready"]))
  (when (xt/x:nil? target)
    (xt/x:err "boot-self requires `target`"))
  (return
   (promise/x:promise-then
    (main/attach-transport
     node
     transport-id
     (-/self-endpoint target))
    (fn [_]
     (if (xt/x:nil? ready)
       (return (-/connection-record node transport-id nil))
       (return
        (promise/x:promise-then
         ((. (main/transport-get node transport-id) ["send_fn"])
          ready)
         (fn [_]
           (return (-/connection-record node transport-id ready))))))))))

(defn.xt disconnect
  "disconnects a browser transport connection"
  {:added "4.1"}
  [connection]
  (return
   (main/detach-transport
    (. connection ["node"])
    (. connection ["transport_id"]))))

(defn.xt blob-url
  "creates a blob URL from a worker script"
  {:added "4.1"}
  [script]
  (var blob (new Blob [script]
                      {:type "text/javascript"}))
  (return (. (!:G URL) (createObjectURL blob))))

(defn.xt webworker-source
  "creates a transport source map backed by a browser WebWorker.
   `opts` is passed as the second argument to the Worker constructor,
   allowing `{:type \"module\"}` to be used for ES-module worker scripts."
  {:added "4.1"}
  [script opts]
  (var worker-opts (or opts {}))
  (return
   {"create_fn"
    (fn [listener]
      (var url (-/blob-url script))
      (try
        (var worker (new Worker url worker-opts))
        (. worker (addEventListener
                   "message"
                   (fn [e]
                     (return (listener (. e ["data"]))))
                   false))
        (. (!:G URL) (revokeObjectURL url))
        (return worker)
        (catch err
          (. (!:G URL) (revokeObjectURL url))
          (throw err))))}))

(defn.xt sharedworker-source
  "creates a transport source map backed by a browser SharedWorker.
   `opts` is passed as the second argument to the SharedWorker constructor,
   allowing `{:type \"module\"}` to be used for ES-module worker scripts."
  {:added "4.1"}
  [script opts]
  (var worker-opts (or opts {}))
  (return
   {"create_fn"
    (fn [listener]
      (var url (-/blob-url script))
      (try
        (var shared (new SharedWorker url worker-opts))
        (var port (. shared ["port"]))
        (. port (start))
        (. port (addEventListener
                 "message"
                 (fn [e]
                   (return (listener (. e ["data"]))))
                 false))
        (. (!:G URL) (revokeObjectURL url))
        (return port)
        (catch err
          (. (!:G URL) (revokeObjectURL url))
          (throw err))))}))

(defn.xt sharedworker-url-source
  "creates a transport source map backed by a browser SharedWorker,
   reusing an existing URL so multiple tabs connect to the same worker"
  {:added "4.1"}
  [url]
  (return
   {"create_fn"
    (fn [listener]
      (var shared (new SharedWorker url))
      (var port (. shared ["port"]))
      (. port (start))
      (. port (addEventListener
               "message"
               (fn [e]
                 (return (listener (. e ["data"]))))
               false))
      (return port))}))

(defn.xt node-worker-source
  "creates a transport source map backed by a Node.js worker_threads Worker"
  {:added "4.1"}
  [script opts]
  (var config (or opts {}))
  (var eval-flag (. config ["eval"]))
  (var eval-mode (:? (xt/x:nil? eval-flag) true eval-flag))
  (var Worker-value (require "worker_threads"))
  (var #{Worker} Worker-value)
  (return
   {"create_fn"
    (fn [listener]
      (var worker (new Worker script
                            (:? eval-mode
                                {:eval true
                                 :type "module"}
                                {})))
      (. worker (on "message"
                    (fn [data]
                      (return (listener data)))))
      (return worker))}))
