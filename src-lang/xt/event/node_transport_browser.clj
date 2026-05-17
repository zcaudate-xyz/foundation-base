(ns xt.event.node-transport-browser
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.event.node-main :as main]
             [xt.lang.spec-base :as xt]]})

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

(defn.xt event-data
  "normalizes a browser worker message event into its payload"
  {:added "4.1"}
  [event]
  (return (:? (and (xt/x:is-object? event)
                   (xt/x:has-key? event "data"))
              (xt/x:get-key event "data")
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
         (when (xt/x:is-function? (xt/x:get-key port "start"))
           (. port (start)))
         (. port (addEventListener
                  "message"
                  current-callback
                  false))
         (return port)))
  (var stop-fn
       (fn [_]
         (when (and (xt/x:not-nil? current-callback)
                    (xt/x:is-function? (xt/x:get-key port "removeEventListener")))
           (. port (removeEventListener
                    "message"
                    current-callback
                    false)))
         (when (xt/x:is-function? (xt/x:get-key port "close"))
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
                (xt/x:get-key shared-or-port "port")
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
         (var post-request (xt/x:get-key worker "postRequest"))
         (if (xt/x:is-function? post-request)
           (return (post-request frame))
           (return (. worker (postMessage frame))))))
  (var start-fn
       (fn [listener]
         (if (xt/x:has-key? worker-source "create_fn")
           (do (:= current-worker
                   ((xt/x:get-key worker-source "create_fn")
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
                    (xt/x:is-function? (xt/x:get-key current-worker "removeEventListener")))
           (. current-worker (removeEventListener
                              "message"
                              current-callback
                              false)))
         (when (and (xt/x:not-nil? current-worker)
                    (xt/x:is-function? (xt/x:get-key current-worker "terminate")))
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
         (return (. worker-self (postMessage frame)))))
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
                    (xt/x:is-function? (xt/x:get-key worker-self "removeEventListener")))
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
